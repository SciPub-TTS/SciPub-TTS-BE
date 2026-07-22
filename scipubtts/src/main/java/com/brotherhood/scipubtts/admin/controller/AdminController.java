package com.brotherhood.scipubtts.admin.controller;

import com.brotherhood.scipubtts.admin.dto.AdminApiCallLogPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiCallConsumerResponse;
import com.brotherhood.scipubtts.admin.dto.AdminApiUsageDailyResponse;
import com.brotherhood.scipubtts.admin.dto.AdminDashboardStatisticsResponse;
import com.brotherhood.scipubtts.admin.dto.AdminOpenAlexFieldSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserDetailResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserBanSummaryResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserPageResponse;
import com.brotherhood.scipubtts.admin.dto.AdminUserSearchHistoryPageResponse;
import com.brotherhood.scipubtts.admin.service.AdminService;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.system.dto.CronConfigResponse;
import com.brotherhood.scipubtts.system.dto.UpdateCronConfigRequest;
import com.brotherhood.scipubtts.system.service.ScheduleService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ScheduleService  scheduleService;

    @GetMapping("/users")
    public ResponseEntity<ResponseObject> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "RECENT") String sort
    ) {
        AdminUserPageResponse data = adminService.getAllUsers(page, size, sort);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Users fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseObject> getUserDetail(
            @PathVariable UUID userId
    ) {
        AdminUserDetailResponse data = adminService.getUserDetail(userId);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "User detail fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/users/{userId}/search-history")
    public ResponseEntity<ResponseObject> getUserSearchHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminUserSearchHistoryPageResponse data = adminService.getUserSearchHistory(userId, page, size);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "User search history fetched successfully",
                        data
                )
        );
    }

    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ResponseObject> banUser(
            @Parameter(hidden = true) @CurrentUserUUID UUID adminId,
            @PathVariable UUID userId
    ) {
        AdminUserResponse data = adminService.banUser(adminId, userId);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Account banned successfully",
                        data
                )
        );
    }

    @PatchMapping("/users/{userId}/unban")
    public ResponseEntity<ResponseObject> unbanUser(
            @Parameter(hidden = true) @CurrentUserUUID UUID adminId,
            @PathVariable UUID userId
    ) {
        AdminUserResponse data = adminService.unbanUser(adminId, userId);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Account unbanned successfully",
                        data
                )
        );
    }

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<ResponseObject> getDashboardStatistics() {
        AdminDashboardStatisticsResponse data = adminService.getDashboardStatistics();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Dashboard statistics fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/dashboard/openalex-field-summary")
    public ResponseEntity<ResponseObject> getOpenAlexFieldSummary() {
        AdminOpenAlexFieldSummaryResponse data = adminService.getOpenAlexFieldSummary();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "OpenAlex field summary fetched successfully",
                        data
                )
        );
    }

    @PostMapping("/dashboard/openalex-field-summary/sync")
    public ResponseEntity<ResponseObject> syncOpenAlexFieldSummary() {
        AdminOpenAlexFieldSummaryResponse data = adminService.syncOpenAlexFieldSummary();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "OpenAlex field summary synced successfully",
                        data
                )
        );
    }

    @GetMapping("/users/ban-summary")
    public ResponseEntity<ResponseObject> getUserBanSummary() {
        AdminUserBanSummaryResponse data = adminService.getUserBanSummary();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "User ban summary fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/dashboard/api-calls/top-users")
    public ResponseEntity<ResponseObject> getTopApiConsumersThisMonth() {
        List<AdminApiCallConsumerResponse> data = adminService.getTopApiConsumersThisMonth();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Top API consumers fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/dashboard/api-calls/usage-over-time")
    public ResponseEntity<ResponseObject> getApiUsageLast7Days() {
        List<AdminApiUsageDailyResponse> data = adminService.getApiUsageLast7Days();

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "API usage over time fetched successfully",
                        data
                )
        );
    }

    @GetMapping("/dashboard/api-calls/logs")
    public ResponseEntity<ResponseObject> getApiCallLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) String callerType,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String endpoint
    ) {
        AdminApiCallLogPageResponse data = adminService.getApiCallLogs(
                page,
                size,
                from,
                to,
                callerType,
                userId,
                jobType,
                status,
                endpoint
        );

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "API call logs fetched successfully",
                        data
                )
        );
    }

    @GetMapping("config/sync-cron")
    public ResponseEntity<ResponseObject> getConfigFeedSyncCron() {
        List<CronConfigResponse> data = scheduleService.getAllSchedules();
        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Cron configs fetched successfully",
                        data
                )
        );
    }

    @PatchMapping("config/sync-cron/{configKey}")
    public ResponseEntity<ResponseObject> updateConfigSyncCron(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateCronConfigRequest request
    ) {
        CronConfigResponse data = scheduleService.updateCronConfig(configKey, request);

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Cron config updated successfully",
                        data
                )
        );
    }

}
