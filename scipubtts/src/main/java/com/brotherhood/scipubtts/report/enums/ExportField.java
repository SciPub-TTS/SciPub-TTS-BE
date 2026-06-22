package com.brotherhood.scipubtts.report.enums;

import lombok.Getter;

@Getter
public enum ExportField {

    TITLE          ("Title",          false),
    AUTHORS        ("Author",         true),
    YEAR           ("Year",           false),
    CITATION_COUNT ("Citation Count", false),
    OPEN_ACCESS    ("Open Access",    false),
    FIELD          ("Field",          false),
    DOMAIN         ("Domain",         false),
    KEYWORD        ("Keyword",        true),
    SUBFIELD       ("Subfield",       false),
    TOPIC          ("Topic",          true),
    INSTITUTION    ("Institution",    true),
    COUNTRY        ("Country",        true),
    DOI            ("DOI",            false),
    ABSTRACT       ("Abstract",       false),
    TYPE           ("Type",           false);

    private final String columnLabel;
    /**
     * -- GETTER --
     * true nếu field này có thể có nhiều giá trị (Authors, Institutions, Countries, Topics, Keywords)
     */
    private final boolean multiValued;

    ExportField(String columnLabel, boolean multiValued) {
        this.columnLabel = columnLabel;
        this.multiValued = multiValued;
    }

}