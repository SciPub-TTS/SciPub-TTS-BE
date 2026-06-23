package com.brotherhood.scipubtts.dashboard.constant;

import lombok.Getter;

@Getter
public enum FormulaType {

  BALANCED("balanced"),

  TRENDING("trending"),

  EMERGING("emerging"),

  IMPACT("impact"),

  DOMINANT("dominant");

  private final String formula;

  FormulaType(String formula) {
    this.formula = formula;
  }

    public static FormulaType from(String formula) {
    for (FormulaType type : values()) {
      if (type.formula.equalsIgnoreCase(formula)) {
        return type;
      }
    }

    throw new IllegalArgumentException(
            "Unknown formula: " + formula
    );
  }
}