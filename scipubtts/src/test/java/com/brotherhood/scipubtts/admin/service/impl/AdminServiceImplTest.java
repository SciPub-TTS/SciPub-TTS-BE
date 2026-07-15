package com.brotherhood.scipubtts.admin.service.impl;

import com.brotherhood.scipubtts.admin.dto.AdminDashboardStatisticsResponse;
import com.brotherhood.scipubtts.admin.dto.AdminOpenAlexFieldSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserBanSummaryResponse;
import com.brotherhood.scipubtts.admin.repository.AdminDashboardRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexSubfieldRepository;
import com.brotherhood.scipubtts.admin.repository.OpenAlexTopicRepository;
import com.brotherhood.scipubtts.admin.service.OpenAlexFieldTaxonomySyncService;
import com.brotherhood.scipubtts.auth.service.RefreshTokenService;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.search.repository.KeywordTrendReadRepository;
import com.brotherhood.scipubtts.search.repository.SearchHistoryRepository;
import com.brotherhood.scipubtts.search.repository.TopicTrendReadRepository;
import com.brotherhood.scipubtts.user.repository.UserProfileRepository;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceImplTest {

    private AdminDashboardRepository adminDashboardRepository;
    private KeywordTrendReadRepository keywordTrendReadRepository;
    private TopicTrendReadRepository topicTrendReadRepository;
    private OpenAlexSubfieldRepository openAlexSubfieldRepository;
    private OpenAlexTopicRepository openAlexTopicRepository;
    private OpenAlexFieldTaxonomySyncService openAlexFieldTaxonomySyncService;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminDashboardRepository = mock(AdminDashboardRepository.class);
        keywordTrendReadRepository = mock(KeywordTrendReadRepository.class);
        topicTrendReadRepository = mock(TopicTrendReadRepository.class);
        openAlexSubfieldRepository = mock(OpenAlexSubfieldRepository.class);
        openAlexTopicRepository = mock(OpenAlexTopicRepository.class);
        openAlexFieldTaxonomySyncService = mock(OpenAlexFieldTaxonomySyncService.class);

        adminService = new AdminServiceImpl(
                mock(UserRepository.class),
                mock(RefreshTokenService.class),
                adminDashboardRepository,
                mock(UserFollowRepository.class),
                mock(UserBookmarkRepository.class),
                mock(UserProfileRepository.class),
                mock(SearchHistoryRepository.class),
                keywordTrendReadRepository,
                topicTrendReadRepository,
                openAlexSubfieldRepository,
                openAlexTopicRepository,
                openAlexFieldTaxonomySyncService,
                mock(ApplicationEventPublisher.class)
        );
        ReflectionTestUtils.setField(adminService, "totalApiCredit", 100000L);
    }

    @Test
    void getDashboardStatisticsUsesLatestKeywordAndTopicTrendCounts() {
        LocalDate keywordSnapshotDate = LocalDate.of(2026, 7, 5);
        LocalDate topicSnapshotDate = LocalDate.of(2026, 7, 12);

        when(adminDashboardRepository.countUsers()).thenReturn(42L);
        when(adminDashboardRepository.countBannedUsers()).thenReturn(3L);
        when(adminDashboardRepository.countApiCallsFrom(org.mockito.ArgumentMatchers.any())).thenReturn(7L);
        when(openAlexSubfieldRepository.count()).thenReturn(12L);
        when(openAlexTopicRepository.count()).thenReturn(345L);
        when(adminDashboardRepository.findLatestSynchronization()).thenReturn(Optional.empty());
        when(keywordTrendReadRepository.findLatestSnapshotDate()).thenReturn(keywordSnapshotDate);
        when(keywordTrendReadRepository.countTrendingKeywords(keywordSnapshotDate)).thenReturn(18L);
        when(topicTrendReadRepository.findLatestSnapshotDate()).thenReturn(topicSnapshotDate);
        when(topicTrendReadRepository.countTrendingTopics(topicSnapshotDate)).thenReturn(27L);

        AdminDashboardStatisticsResponse response = adminService.getDashboardStatistics();

        assertThat(response.totalUsers().value()).isEqualTo(42L);
        assertThat(response.activeTrends().value()).isEqualTo(18L);
        assertThat(response.bannedUsers().value()).isEqualTo(3L);
        assertThat(response.apiCallsUsed().value()).isEqualTo(27L);
        assertThat(response.apiCallsToday().value()).isEqualTo(7L);
        assertThat(response.totalApiCredit().value()).isEqualTo(100000L);
        assertThat(response.totalSubfields().value()).isEqualTo(12L);
        assertThat(response.totalTopics().value()).isEqualTo(345L);
        assertThat(response.lastSynchronization().value()).isNull();
        assertThat(Arrays.stream(AdminDashboardStatisticsResponse.StatisticCard.class.getRecordComponents())
                .map(component -> component.getName())
                .toList())
                .containsExactly("value");
    }

    @Test
    void getDashboardStatisticsReturnsZeroTrendCountsWhenSnapshotsDoNotExist() {
        when(adminDashboardRepository.findLatestSynchronization()).thenReturn(Optional.empty());
        when(keywordTrendReadRepository.findLatestSnapshotDate()).thenReturn(null);
        when(topicTrendReadRepository.findLatestSnapshotDate()).thenReturn(null);

        AdminDashboardStatisticsResponse response = adminService.getDashboardStatistics();

        assertThat(response.activeTrends().value()).isZero();
        assertThat(response.apiCallsUsed().value()).isZero();
        verify(keywordTrendReadRepository, never()).countTrendingKeywords(org.mockito.ArgumentMatchers.any());
        verify(topicTrendReadRepository, never()).countTrendingTopics(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getOpenAlexFieldSummaryReturnsCountsFromDatabase() {
        when(openAlexSubfieldRepository.count()).thenReturn(12L);
        when(openAlexTopicRepository.count()).thenReturn(345L);

        AdminOpenAlexFieldSummaryResponse response = adminService.getOpenAlexFieldSummary();

        assertThat(response.totalSubfields().value()).isEqualTo(12L);
        assertThat(response.totalTopics().value()).isEqualTo(345L);
    }

    @Test
    void getOpenAlexFieldSummaryReturnsZeroWhenDatabaseIsEmpty() {
        AdminOpenAlexFieldSummaryResponse response = adminService.getOpenAlexFieldSummary();

        assertThat(response.totalSubfields().value()).isZero();
        assertThat(response.totalTopics().value()).isZero();
    }

    @Test
    void syncOpenAlexFieldSummaryRunsSyncAndReturnsCountsFromDatabase() {
        when(openAlexSubfieldRepository.count()).thenReturn(12L);
        when(openAlexTopicRepository.count()).thenReturn(345L);

        AdminOpenAlexFieldSummaryResponse response = adminService.syncOpenAlexFieldSummary();

        verify(openAlexFieldTaxonomySyncService).syncFieldTaxonomy();
        assertThat(response.totalSubfields().value()).isEqualTo(12L);
        assertThat(response.totalTopics().value()).isEqualTo(345L);
    }

    @Test
    void getUserBanSummaryReturnsValueOnlyCards() {
        when(adminDashboardRepository.countActiveUsers()).thenReturn(39L);
        when(adminDashboardRepository.countBannedUsers()).thenReturn(3L);

        AdminUserBanSummaryResponse response = adminService.getUserBanSummary();

        assertThat(response.active().value()).isEqualTo(39L);
        assertThat(response.banned().value()).isEqualTo(3L);
        assertThat(response.total().value()).isEqualTo(42L);
        assertThat(Arrays.stream(AdminUserBanSummaryResponse.class.getRecordComponents())
                .map(component -> component.getName())
                .toList())
                .containsExactly("active", "banned", "total");
    }

    @Test
    void getUserBanSummaryReturnsZeroValueCardsWhenDatabaseIsEmpty() {
        AdminUserBanSummaryResponse response = adminService.getUserBanSummary();

        assertThat(response.active().value()).isZero();
        assertThat(response.banned().value()).isZero();
        assertThat(response.total().value()).isZero();
    }
}
