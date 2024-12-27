package com.debatetimer.fixture;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtoGenerator {

    public TableInfoCreateRequest generateTableInfoCreateRequest(String tableName) {
        return new TableInfoCreateRequest(tableName, "PARLIAMENTARY", "주제");
    }

    public TimeBoxCreateRequests generateTimeBoxCreateRequests() {
        List<TimeBoxCreateRequest> timeBoxCreateRequests = new ArrayList<>(
                Arrays.asList(
                        generateTimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 180),
                        generateTimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 180)
                )
        );
        return new TimeBoxCreateRequests(timeBoxCreateRequests);
    }

    public TimeBoxCreateRequest generateTimeBoxCreateRequest(Stance stance, BoxType boxType, int time) {
        return new TimeBoxCreateRequest(stance.name(), boxType.name(), time, 1);
    }

    public ParliamentaryTableCreateRequest generateParliamentaryTableCreateRequest(String tableName) {
        TableInfoCreateRequest tableInfoCreateRequest = generateTableInfoCreateRequest(tableName);
        TimeBoxCreateRequests timeBoxCreateRequests = generateTimeBoxCreateRequests();
        return new ParliamentaryTableCreateRequest(tableInfoCreateRequest, timeBoxCreateRequests);
    }
}
