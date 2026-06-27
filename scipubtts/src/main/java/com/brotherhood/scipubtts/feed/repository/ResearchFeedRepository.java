package com.brotherhood.scipubtts.feed.repository;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.feed.model.FeedDraft;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResearchFeedRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public int batchInsertDoNothing(Collection<FeedDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO research_feed_item (
                    id,
                    user_id,
                    work_openalex_id,
                    title_snapshot,
                    authors_snapshot,
                    source_snapshot,
                    publication_year,
                    publication_date,
                    citation_snapshot,
                    reason_json,
                    generated_at,
                    author_openalex_ids_snapshot,
                    work_type_snapshot,
                    topic_snapshot,
                    topic_openalex_id_snapshot,
                    abstract_text,
                    doi,
                    pdf_url,
                    keywords_json,
                    primary_fields_snapshot,
                    subfield_snapshot
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?
                )
                ON CONFLICT (user_id, work_openalex_id)
                DO NOTHING
                """;

        int[][] result = jdbcTemplate.batchUpdate(
                sql,
                drafts,
                500,
                (ps, draft) -> {
                    ps.setObject(1, UUID.randomUUID());
                    ps.setObject(2, draft.getUserId());
                    ps.setString(3, draft.getWorkOpenalexId());
                    ps.setString(4, draft.getTitleSnapshot());
                    ps.setString(5, draft.getAuthorsSnapshot());
                    ps.setString(6, draft.getSourceSnapshot());

                    if (draft.getPublicationYear() == null) {
                        ps.setObject(7, null);
                    } else {
                        ps.setInt(7, draft.getPublicationYear());
                    }

                    if (draft.getPublicationDate() == null) {
                        ps.setDate(8, null);
                    } else {
                        ps.setDate(8, Date.valueOf(draft.getPublicationDate()));
                    }

                    if (draft.getCitationSnapshot() == null) {
                        ps.setObject(9, null);
                    } else {
                        ps.setInt(9, draft.getCitationSnapshot());
                    }

                    String reasonJson;
                    try {
                        reasonJson = objectMapper.writeValueAsString(
                                java.util.Map.of("reasons", draft.getReasons())
                        );
                    } catch (JsonProcessingException e) {
                        throw new BusinessException(ErrorCode.JSON_SERIALIZATION_ERROR);
                    }

                    ps.setString(10, reasonJson);
                    ps.setTimestamp(11, Timestamp.from(draft.getGeneratedAt().toInstant()));

                    ps.setString(12, draft.getAuthorOpenAlexIdsSnapshot());
                    ps.setString(13, draft.getWorkTypeSnapshot());
                    ps.setString(14, draft.getTopicSnapshot());
                    ps.setString(15, draft.getTopicOpenAlexIdSnapshot());
                    ps.setString(16, draft.getAbstractText());
                    ps.setString(17, draft.getDoi());
                    ps.setString(18, draft.getPdfUrl());

                    if (StringUtils.hasText(draft.getKeywordsJson())) {
                        ps.setString(19, draft.getKeywordsJson());
                    } else {
                        ps.setNull(19, java.sql.Types.OTHER);
                    }

                    ps.setString(20, draft.getPrimaryFieldSnapshot());
                    ps.setString(21, draft.getSubfieldSnapshot());
                }
        );

        int saved = 0;
        for (int[] batch : result) {
            for (int row : batch) {
                if (row > 0) {
                    saved += row;
                }
            }
        }

        return saved;
    }
}
