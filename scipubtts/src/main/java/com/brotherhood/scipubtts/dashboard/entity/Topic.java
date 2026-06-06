package com.brotherhood.scipubtts.dashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Topic {
  private String topicId;
  private String name;
  private String startTime;
  private String endTime;
  private double velocity;
  private double acceleration;
  private double citation ;
  private double newComerAuthor;
  private double institution;
}