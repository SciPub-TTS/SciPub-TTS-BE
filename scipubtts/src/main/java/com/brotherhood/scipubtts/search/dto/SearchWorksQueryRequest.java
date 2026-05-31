package com.brotherhood.scipubtts.search.dto;

import java.util.List;

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

    public static SearchWorksQueryRequest empty() {
        return new SearchWorksQueryRequest();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getYearMode() {
        return yearMode;
    }

    public void setYearMode(String yearMode) {
        this.yearMode = yearMode;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public Integer getYearExact() {
        return yearExact;
    }

    public void setYearExact(Integer yearExact) {
        this.yearExact = yearExact;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public Boolean getOpenAccess() {
        return openAccess;
    }

    public void setOpenAccess(Boolean openAccess) {
        this.openAccess = openAccess;
    }

    public List<String> getSubField() {
        return subField;
    }

    public void setSubField(List<String> subField) {
        this.subField = subField;
    }

    public List<String> getAuthor() {
        return author;
    }

    public void setAuthor(List<String> author) {
        this.author = author;
    }

    public List<String> getInstitution() {
        return institution;
    }

    public void setInstitution(List<String> institution) {
        this.institution = institution;
    }

    public Boolean getPdf() {
        return pdf;
    }

    public void setPdf(Boolean pdf) {
        this.pdf = pdf;
    }

    public List<String> getCountry() {
        return country;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public String getCitationMode() {
        return citationMode;
    }

    public void setCitationMode(String citationMode) {
        this.citationMode = citationMode;
    }

    public Integer getCitationMin() {
        return citationMin;
    }

    public void setCitationMin(Integer citationMin) {
        this.citationMin = citationMin;
    }

    public Integer getCitationMax() {
        return citationMax;
    }

    public void setCitationMax(Integer citationMax) {
        this.citationMax = citationMax;
    }

    public Integer getCitationExact() {
        return citationExact;
    }

    public void setCitationExact(Integer citationExact) {
        this.citationExact = citationExact;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public List<String> getAward() {
        return award;
    }

    public void setAward(List<String> award) {
        this.award = award;
    }

    public String getIndexedByOrcid() {
        return indexedByOrcid;
    }

    public void setIndexedByOrcid(String indexedByOrcid) {
        this.indexedByOrcid = indexedByOrcid;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }
}
