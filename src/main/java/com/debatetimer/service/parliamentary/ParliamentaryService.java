package com.debatetimer.service.parliamentary;

import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.errorcode.ClientErrorCode;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import com.debatetimer.dto.parliamentary.ParliamentaryTableResponse;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParliamentaryService {

    private final ParliamentaryTableRepository tableRepository;
    private final ParliamentaryTimeBoxRepository timeBoxRepository;

    public ParliamentaryTableResponse findTable(long tableId, long memberId) {
        ParliamentaryTable table = tableRepository.getById(tableId);
        validateTableOwner(memberId, table);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        return new ParliamentaryTableResponse(table, timeBoxes);
    }

    private void validateTableOwner(long memberId, ParliamentaryTable table) {
        if (!table.isOwn(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.TABLE_OWNER_MISMATCHED);
        }
    }

    private ParliamentaryTimeBoxes findTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(table);
        return new ParliamentaryTimeBoxes(timeBoxes);
    }
}
