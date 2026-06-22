package com.brotherhood.scipubtts.dashboard.constant;

import lombok.Getter;

@Getter
public enum OpenAlexEntity {
  WORKS("works"),
  TOPICS("topics"),
  KEYWORDS("keywords");

  private final String path;

  OpenAlexEntity(String path) {
    this.path = path;
  }

}