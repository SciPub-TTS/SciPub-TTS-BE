package com.brotherhood.scipubtts.search.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Component
public class OpenAlexMapReader {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
    private static final Pattern MULTI_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public Map<String, Object> getMap(Map<String, Object> source, String key) {
        if (source == null) {
            return new LinkedHashMap<>();
        }

        return copyStringKeyMap(source.get(key));
    }

    public List<Map<String, Object>> getMapList(Map<String, Object> source, String key) {
        if (source == null) {
            return List.of();
        }

        return getMapListFromObject(source.get(key));
    }

    public List<Map<String, Object>> getMapListFromObject(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object item : rawList) {
            Map<String, Object> itemMap = copyStringKeyMap(item);
            if (!itemMap.isEmpty()) {
                result.add(itemMap);
            }
        }

        return result;
    }

    public List<Object> getObjectList(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        return new ArrayList<>(rawList);
    }

    public String getString(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return "";
        }

        return String.valueOf(source.get(key));
    }

    public Integer getInteger(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        return toInt(source.get(key), 0);
    }

    public Boolean getBoolean(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        Object value = source.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.valueOf(String.valueOf(value));
    }

    public int getInt(Map<String, Object> source, String key, int defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        return toInt(source.get(key), defaultValue);
    }

    public long getLong(Map<String, Object> source, String key, long defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        Object value = source.get(key);
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public double getDouble(Map<String, Object> source, String key, double defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        Object value = source.get(key);
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public String getNextCursor(Map<String, Object> response) {
        Map<String, Object> meta = getMap(response, "meta");
        String nextCursor = getString(meta, "next_cursor").trim();
        return StringUtils.hasText(nextCursor) ? nextCursor : null;
    }

    public Boolean deriveHasOrcid(List<Map<String, Object>> authorships) {
        for (Map<String, Object> authorship : authorships) {
            Map<String, Object> author = getMap(authorship, "author");
            if (!getString(author, "orcid").trim().isBlank()) {
                return true;
            }
        }

        return false;
    }

    public String derivePdfUrl(Map<String, Object> work) {
        Map<String, Object> bestOaLocation = getMap(work, "best_oa_location");
        Map<String, Object> openAccess = getMap(work, "open_access");

        String pdfUrl = getString(bestOaLocation, "pdf_url").trim();
        if (!pdfUrl.isBlank()) {
            return pdfUrl;
        }

        String openAccessUrl = getString(openAccess, "oa_url").trim();
        if (!openAccessUrl.isBlank()) {
            return openAccessUrl;
        }

        String landingPageUrl = getString(bestOaLocation, "landing_page_url").trim();
        if (!landingPageUrl.isBlank()) {
            return landingPageUrl;
        }

        return null;
    }

    public String deriveAbstractText(Map<String, Object> abstractInvertedIndex) {
        if (abstractInvertedIndex == null || abstractInvertedIndex.isEmpty()) {
            return null;
        }

        TreeMap<Integer, String> orderedTokens = new TreeMap<>();

        for (Map.Entry<String, Object> entry : abstractInvertedIndex.entrySet()) {
            List<Object> positions = getObjectList(entry.getValue());

            for (Object positionValue : positions) {
                int position = toInt(positionValue, -1);
                if (position >= 0) {
                    orderedTokens.putIfAbsent(position, entry.getKey());
                }
            }
        }

        if (orderedTokens.isEmpty()) {
            return null;
        }

        return sanitizeDisplayText(String.join(" ", orderedTokens.values()));
    }

    public String sanitizeDisplayText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String unescapedValue = HtmlUtils.htmlUnescape(value);
        String withoutHtmlTags = HTML_TAG_PATTERN.matcher(unescapedValue).replaceAll(" ");
        return MULTI_WHITESPACE_PATTERN.matcher(withoutHtmlTags).replaceAll(" ").trim();
    }

    private Map<String, Object> copyStringKeyMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String key) {
                result.put(key, entry.getValue());
            }
        }

        return result;
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
