package com.brotherhood.scipubtts.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "topics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_topic_snapshot",
                        columnNames = {"topic_id", "start_time", "end_time"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Topic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "topic_id", nullable = false)
  private String topicId;

  @Column(nullable = false)
  private String name;

  @Column(name = "start_time", nullable = false)
  private LocalDate startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDate endTime;

  @Column(nullable = false)
  private double velocity;

  @Column(nullable = false)
  private double acceleration;

  @Column(name = "citation_decay", nullable = false)
  private double citationDecay;

  @Column(name = "newcomer_author", nullable = false)
  private double newComerAuthor;

  @Column(nullable = false)
  private double institution;

  @Column(nullable = false)
  private long works;

  @Column(nullable = false)
  private long citations;
}