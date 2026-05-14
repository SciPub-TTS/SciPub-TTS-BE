package com.swp.backend.dto.coordinator.request;

public record TakeListRequest (
    int pageSize,
    int pageNumber,
    String status
){
}
