package com.brotherhood.scipubtts.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SearchWorksQueryRequest"
)
public class SearchWorksQueryRequest {
    @Schema(nullable = true, example = "AI")
    private String query;

    @Schema(nullable = true, allowableValues = {"range", "exact"})
    private String yearMode;

    @Schema(nullable = true, example = "2018")
    private Integer yearFrom;

    @Schema(nullable = true, example = "2026")
    private Integer yearTo;

    @Schema(nullable = true, example = "2006")
    private Integer yearExact;

    @Schema(nullable = true, example = "[\"article\"]")
    private List<String> type;

    @Schema(nullable = true, example = "true")
    private Boolean openAccess;

    @Schema(nullable = true, example = "[\"2202\"]")
    private List<String> subField;

    @Schema(nullable = true, example = "[\"A5024995037\"]")
    private List<String> author;

    @Schema(nullable = true, example = "[\"I201448701\"]")
    private List<String> institution;

    @Schema(nullable = true, example = "true")
    private Boolean pdf;

    @Schema(nullable = true, example = "[\"VN\"]")
    private List<String> country;

    @Schema(nullable = true, allowableValues = {"range", "exact"})
    private String citationMode;

    @Schema(nullable = true, example = "10")
    private Integer citationMin;

    @Schema(nullable = true, example = "100")
    private Integer citationMax;

    @Schema(nullable = true, example = "50")
    private Integer citationExact;

    @Schema(nullable = true, example = "[\"S64187185\"]")
    private List<String> source;

    @Schema(nullable = true, example = "[\"G1204744554\"]")
    private List<String> award;

    @Schema(nullable = true, allowableValues = {"is", "is not"})
    private String indexedByOrcid;

    @Schema(nullable = true, allowableValues = {"none", "keyword", "topic", "both"})
    private String trendingMode;

    @Schema(nullable = true, allowableValues = {"relevance", "citation", "published"})
    private String sortBy;

    @Schema(nullable = true, allowableValues = {"asc", "desc"})
    private String sortDirection;

    @Schema(nullable = true, example = "1", defaultValue = "1")
    private Integer page;

    @Schema(nullable = true, example = "20", defaultValue = "20")
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

    public String getTrendingMode() {
        return trendingMode;
    }

    public void setTrendingMode(String trendingMode) {
        this.trendingMode = trendingMode;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
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
