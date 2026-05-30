package com.brotherhood.scipubtts.search.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SearchHistorySaveRequest {
    private String query;
    private UUID userId;
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- DTO + Lombok @Getter/@Setter.
File nay lam gi:
- Payload save search history (query, userId).
Flow chay:
- FE gui query -> Controller gan userId -> Service save.
*/

