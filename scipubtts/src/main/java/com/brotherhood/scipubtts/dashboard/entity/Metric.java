package com.brotherhood.scipubtts.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_metric_snapshot",
                        columnNames = {
                                "title",
                                "start_time",
                                "end_time"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Metric {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private double value;

  @Column(name = "change_value", nullable = false)
  private double change;

  @Column(name = "start_time", nullable = false)
  private LocalDate startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDate endTime;
}