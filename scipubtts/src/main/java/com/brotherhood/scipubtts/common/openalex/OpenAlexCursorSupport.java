package com.brotherhood.scipubtts.common.openalex;

import org.springframework.util.StringUtils;

import java.util.function.Function;

public final class OpenAlexCursorSupport {

  public static final String INITIAL_CURSOR = "*";
  public static final int MAX_PER_PAGE = 200;

  private OpenAlexCursorSupport() {
  }

  public static boolean hasNextCursor(String nextCursor) {
    return StringUtils.hasText(nextCursor);
  }

  /**
   * Resolves the OpenAlex cursor for a 1-based page number by walking cursor pages.
   * Returns null when the requested page is beyond available data.
   */
  public static String resolveCursorForPage(
          int page,
          Function<String, String> fetchAndGetNextCursor
  ) {
    if (page <= 1) {
      return INITIAL_CURSOR;
    }

    String cursor = INITIAL_CURSOR;
    for (int currentPage = 1; currentPage < page; currentPage++) {
      String nextCursor = fetchAndGetNextCursor.apply(cursor);
      if (!hasNextCursor(nextCursor)) {
        return null;
      }
      cursor = nextCursor;
    }

    return cursor;
  }
}
