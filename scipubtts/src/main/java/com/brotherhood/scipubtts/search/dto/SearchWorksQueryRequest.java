package com.brotherhood.scipubtts.search.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchWorksQueryRequest {

    private String query;

    private String yearMode;
    private Integer yearFrom;
    private Integer yearTo;
    private Integer yearExact;

    private List<String> type;
    private Boolean openAccess;
    private List<String> subField;
    private List<String> author;
    private List<String> institution;
    private Boolean pdf;
    private List<String> country;

    private String citationMode;
    private Integer citationMin;
    private Integer citationMax;
    private Integer citationExact;

    private List<String> source;
    private List<String> award;
    private String indexedByOrcid;

    private String sort;
    private Integer page;
    private Integer perPage;
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- DTO + Lombok @Getter/@Setter.
File nay lam gi:
- Chua tat ca params FE gui len khi search works.
Flow chay:
- Spring bind query params vao DTO -> Service doc field de build filter.
*/

