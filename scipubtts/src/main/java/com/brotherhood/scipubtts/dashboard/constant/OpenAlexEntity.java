package com.brotherhood.scipubtts.dashboard.constant;

public enum OpenAlexEntity {
  WORKS("works"),
  TOPICS("topics"),
  KEYWORDS("keywords");

  private final String path;

  OpenAlexEntity(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}