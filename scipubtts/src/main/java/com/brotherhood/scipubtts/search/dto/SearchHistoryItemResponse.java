package com.brotherhood.scipubtts.search.dto;

public record SearchHistoryItemResponse(
        String id,
        String query,
        String savedAt
) {
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- Java record.
File nay lam gi:
- Dinh nghia 1 item history tra ve FE.
Flow chay:
- Service map projection DB sang record nay -> FE render dropdown.
*/

