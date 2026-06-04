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

        Object value = source.get(key);
        if (!(value instanceof Map<?, ?> rawMap)) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String keyName) {
                result.put(keyName, entry.getValue());
            }
        }

        return result;
    }

    public List<Map<String, Object>> getMapList(Map<String, Object> source, String key) {
        if (source == null) {
            return List.of();
        }

        return getMapListFromObject(source.get(key));
    }

    public List<Map<String, Object>> getMapListFromObject(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (!(value instanceof List<?> rawList)) {
            return result;
        }

        for (Object item : rawList) {
            if (item instanceof Map<?, ?> rawMap) {
                Map<String, Object> itemMap = new LinkedHashMap<>();

                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String keyName) {
                        itemMap.put(keyName, entry.getValue());
                    }
                }

                result.add(itemMap);
            }
        }

        return result;
    }

    public List<Object> getObjectList(Object value) {
        List<Object> result = new ArrayList<>();

        if (!(value instanceof List<?> rawList)) {
            return result;
        }

        result.addAll(rawList);
        return result;
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

    public Boolean deriveHasOrcid(List<Map<String, Object>> authorships) {
        for (Map<String, Object> authorship : authorships) {
            Map<String, Object> author = getMap(authorship, "author");
            String orcid = getString(author, "orcid").trim();
            if (!orcid.isBlank()) {
                return true;
            }
        }

        return false;
    }

    public String derivePdfUrl(Map<String, Object> work) {
        Map<String, Object> bestOaLocation = getMap(work, "best_oa_location");
        Map<String, Object> openAccess = getMap(work, "open_access");

        String bestOaPdfUrl = getString(bestOaLocation, "pdf_url").trim();
        if (!bestOaPdfUrl.isBlank()) {
            return bestOaPdfUrl;
        }

        String openAccessUrl = getString(openAccess, "oa_url").trim();
        if (!openAccessUrl.isBlank()) {
            return openAccessUrl;
        }

        String bestOaLandingPageUrl = getString(bestOaLocation, "landing_page_url").trim();
        if (!bestOaLandingPageUrl.isBlank()) {
            return bestOaLandingPageUrl;
        }

        return null;
    }

    public String deriveAbstractText(Map<String, Object> abstractInvertedIndex) {
        if (abstractInvertedIndex == null || abstractInvertedIndex.isEmpty()) {
            return null;
        }

        TreeMap<Integer, String> orderedTokens = new TreeMap<>();
        for (Map.Entry<String, Object> entry : abstractInvertedIndex.entrySet()) {
            String token = entry.getKey();
            List<Object> positions = getObjectList(entry.getValue());

            for (Object positionValue : positions) {
                int position = toInt(positionValue, -1);
                if (position >= 0) {
                    orderedTokens.putIfAbsent(position, token);
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

        String unescaped = HtmlUtils.htmlUnescape(value);
        String withoutHtmlTags = HTML_TAG_PATTERN.matcher(unescaped).replaceAll(" ");
        return MULTI_WHITESPACE_PATTERN.matcher(withoutHtmlTags).replaceAll(" ").trim();
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
