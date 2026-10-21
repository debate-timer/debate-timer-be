package com.debatetimer.service.admin;

import com.debatetimer.domain.customize.CustomizeTableOverview;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.dto.admin.AdminTableDetailResponse;
import com.debatetimer.dto.admin.AdminTablePageResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 어드민이 모든 회원의 테이블을 소유자 확인 없이 읽기 전용으로 조회한다.
 */
@Service
@RequiredArgsConstructor
public class AdminTableService {

    public static final int MAX_PAGE_SIZE = 100;

    private final CustomizeTableDomainRepository customizeTableDomainRepository;

    public AdminTablePageResponse getTables(int page, int size) {
        validatePage(page, size);
        return AdminTablePageResponse.from(
                customizeTableDomainRepository.findOverviewsOrderByCreatedAtDesc(page, size));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    public AdminTableDetailResponse getTable(long tableId) {
        CustomizeTableOverview overview = customizeTableDomainRepository.getOverviewById(tableId);
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getCustomizeTimeBoxes(tableId);
        return new AdminTableDetailResponse(overview, timeBoxes);
    }
}
