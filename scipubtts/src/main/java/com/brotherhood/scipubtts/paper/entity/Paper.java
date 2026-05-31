package com.brotherhood.scipubtts.paper.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "papers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paper {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(unique = true, nullable = false)
  private String openalexId;

  @Column(unique = true)
  private String doi;

  @Column(nullable = false)
  private String title;

  private Integer publicationYear;

  private LocalDate publicationDate;

  private Integer citedByCount;

  @Column(columnDefinition = "jsonb")
  private String rawPayload;
}
