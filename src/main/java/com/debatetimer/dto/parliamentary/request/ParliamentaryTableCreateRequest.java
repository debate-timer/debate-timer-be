package com.debatetimer.dto.parliamentary.request;

public record ParliamentaryTableCreateRequest(
        TableInfoCreateRequest info,
        TimeBoxCreateRequests table
) {

}
