package com.brotherhood.scipubtts.report.dto.response;

import java.util.Map;

public record TopicFormulaComparisonResponse(
        String topicId,
        String name,
        Map<String, FormulaResult> resultsByFormula // key: "trending", "emerging", "impact", "balanced"
) {
  public record FormulaResult(
          Integer rank,
          Double score,
          Double change
  ) {}
}