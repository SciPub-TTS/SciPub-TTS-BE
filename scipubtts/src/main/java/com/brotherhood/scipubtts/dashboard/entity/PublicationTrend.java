package com.brotherhood.scipubtts.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "publication_trends")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationTrend {

  @Id
  @Column(name = "publication_year")
  private Integer year;

  @Column(name = "publication_count", nullable = false)
  private Long publications;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public PublicationTrend(Integer year, Long publications) {
    this.year = year;
    this.publications = publications;
  }
}