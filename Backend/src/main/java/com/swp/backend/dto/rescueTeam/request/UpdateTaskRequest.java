package com.swp.backend.dto.rescueTeam.request;

public record UpdateTaskRequest (
        String status,
        String report
) {}
