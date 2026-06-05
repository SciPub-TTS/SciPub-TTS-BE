package com.brotherhood.scipubtts.dashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Metric {
  private String title;
  private long value;
  private double change;
}