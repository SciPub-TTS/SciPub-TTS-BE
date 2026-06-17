package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SearchWorksQueryRequest",
        description = "Search parameters for works. Year range and year exact are mutually exclusive. Citation range and citation exact are also mutually exclusive."
)
public class SearchWorksQueryRequest {
    @Schema(nullable = true, example = "AI")
    private String query;

    @Schema(
            nullable = true,
            allowableValues = {"range", "exact"},
            description = "Select one year mode. Use 'range' with yearFrom/yearTo, or 'exact' with yearExact. Do not combine both."
    )
    private String yearMode;

    @Schema(
            nullable = true,
            example = "2018",
            description = "Start year for range mode. Leave empty when using yearExact."
    )
    private Integer yearFrom;

    @Schema(
            nullable = true,
            example = "2026",
            description = "End year for range mode. Leave empty when using yearExact."
    )
    private Integer yearTo;

    @Schema(
            nullable = true,
            example = "2006",
            description = "Exact year. Leave empty when using yearFrom/yearTo."
    )
    private Integer yearExact;

    @Schema(nullable = true, example = "[\"article\"]", description = "Work types, for example article, preprint or book-chapter.")
    private List<String> type;

    @Schema(nullable = true, example = "true", description = "Filter only open access works.")
    private Boolean openAccess;

    @Schema(nullable = true, example = "[\"2202\"]", description = "OpenAlex subfield ids. Use /api/search/filters/subField/options to load valid values.")
    private List<String> subField;

    @Schema(nullable = true, example = "[\"A5024995037\"]", description = "OpenAlex author ids. Use /api/search/filters/author/options to load valid values.")
    private List<String> author;

    @Schema(nullable = true, example = "[\"I201448701\"]", description = "OpenAlex institution ids. Use /api/search/filters/institution/options to load valid values.")
    private List<String> institution;

    @Schema(nullable = true, example = "true", description = "Filter works that have a PDF or full text content.")
    private Boolean pdf;

    @Schema(nullable = true, example = "[\"VN\"]", description = "Institution country codes, for example VN, US, GB.")
    private List<String> country;

    @Schema(
            nullable = true,
            allowableValues = {"range", "exact"},
            description = "Select one citation mode. Use 'range' with citationMin/citationMax, or 'exact' with citationExact. Do not combine both."
    )
    private String citationMode;

    @Schema(
            nullable = true,
            example = "10",
            description = "Minimum citation count for range mode. Leave empty when using citationExact."
    )
    private Integer citationMin;

    @Schema(
            nullable = true,
            example = "100",
            description = "Maximum citation count for range mode. Leave empty when using citationExact."
    )
    private Integer citationMax;

    @Schema(
            nullable = true,
            example = "50",
            description = "Exact citation count. Leave empty when using citationMin/citationMax."
    )
    private Integer citationExact;

    @Schema(nullable = true, example = "[\"S64187185\"]", description = "OpenAlex source ids. Use /api/search/filters/source/options to load valid values.")
    private List<String> source;

    @Schema(nullable = true, example = "[\"G1204744554\"]", description = "OpenAlex award ids. Use /api/search/filters/award/options to load valid values.")
    private List<String> award;

    @Schema(nullable = true, allowableValues = {"is", "is not"}, description = "Filter by ORCID indexing state.")
    private String indexedByOrcid;

    @Schema(nullable = true, allowableValues = {"none", "keyword", "topic", "both"}, description = "Trending filter mode.")
    private String trendingMode;

    @Schema(nullable = true, allowableValues = {"relevance", "citation", "published"}, description = "Primary sort field.")
    private String sortBy;

    @Schema(nullable = true, allowableValues = {"asc", "desc"}, description = "Sort direction.")
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
