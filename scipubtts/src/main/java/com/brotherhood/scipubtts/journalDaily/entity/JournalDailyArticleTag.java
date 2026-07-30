package com.brotherhood.scipubtts.journalDaily.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "guardian_article_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_guardian_article_tag",
                columnNames = {"article_id", "tag_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalDailyArticleTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "article_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_guardian_tag_article")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JournalDailyArticle article;

    @Column(name = "tag_name", nullable = false, length = 255)
    private String tagName;
}