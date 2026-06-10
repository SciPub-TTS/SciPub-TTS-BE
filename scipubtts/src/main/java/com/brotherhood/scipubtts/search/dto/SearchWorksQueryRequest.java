package com.brotherhood.scipubtts.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SearchWorksQueryRequest",
        description = "All search filters are optional. Leave unused fields empty in Swagger."
)
public class SearchWorksQueryRequest {
    @Schema(description = "Keyword matched against OpenAlex works", nullable = true, example = "AI")
    private String query;

    @Schema(description = "Year filter mode", nullable = true, allowableValues = {"range", "exact"})
    private String yearMode;

    @Schema(description = "Start year when yearMode=range", nullable = true, example = "2020")
    private Integer yearFrom;

    @Schema(description = "End year when yearMode=range", nullable = true, example = "2025")
    private Integer yearTo;

    @Schema(description = "Exact year when yearMode=exact", nullable = true, example = "2024")
    private Integer yearExact;

    @Schema(description = "Work types such as article or book-chapter", nullable = true, example = "[\"article\"]")
    private List<String> type;

    @Schema(description = "Filter only open-access works", nullable = true, example = "true")
    private Boolean openAccess;

    @Schema(description = "OpenAlex subfield ids", nullable = true, example = "[\"subfields/1302\"]")
    private List<String> subField;

    @Schema(description = "OpenAlex author ids", nullable = true, example = "[\"A507823743\"]")
    private List<String> author;

    @Schema(description = "OpenAlex institution ids", nullable = true, example = "[\"I71267560\"]")
    private List<String> institution;

    @Schema(description = "Filter works that have PDF content", nullable = true, example = "true")
    private Boolean pdf;

    @Schema(description = "Country codes such as US, VN, KR", nullable = true, example = "[\"US\"]")
    private List<String> country;

    @Schema(description = "Citation filter mode", nullable = true, allowableValues = {"range", "exact"})
    private String citationMode;

    @Schema(description = "Minimum citation count when citationMode=range", nullable = true, example = "10")
    private Integer citationMin;

    @Schema(description = "Maximum citation count when citationMode=range", nullable = true, example = "100")
    private Integer citationMax;

    @Schema(description = "Exact citation count when citationMode=exact", nullable = true, example = "50")
    private Integer citationExact;

    @Schema(description = "OpenAlex source ids", nullable = true, example = "[\"S4306400194\"]")
    private List<String> source;

    @Schema(description = "OpenAlex award ids", nullable = true, example = "[\"https://openalex.org/awards/A1969205030\"]")
    private List<String> award;

    @Schema(description = "ORCID filter", nullable = true, allowableValues = {"is", "is not"})
    private String indexedByOrcid;

    @Schema(description = "Sort expression", nullable = true, example = "cited_by_count:desc")
    private String sort;

    @Schema(description = "Page number", nullable = true, example = "1", defaultValue = "1")
    private Integer page;

    @Schema(description = "Page size", nullable = true, example = "20", defaultValue = "20")
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
