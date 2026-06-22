package com.brotherhood.scipubtts.report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExportMetadata(

        LocalDate exportDate,

        String searchQuery,

        int selectedPaperCount,

        List<String> selectedFields,

        String format
) {}