package com.swp.backend.controller.rescueTeam;

import com.swp.backend.annotation.RequiresRole;
import com.swp.backend.service.RescueTeamService;
import com.swp.backend.dto.chat.request.SendMessageRequest;
import com.swp.backend.dto.chat.response.MessageResponse;
import com.swp.backend.dto.common.ResponseObject;
import com.swp.backend.dto.rescueTeam.request.UpdateTaskRequest;
import com.swp.backend.dto.rescueTeam.response.TaskDetailResponse;
import com.swp.backend.dto.rescueTeam.response.TeamAssignmentResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/rescueteam")
@RequiresRole("rescue team")
public class MissionController {

    @Autowired
    private RescueTeamService rescueTeamService;

    @GetMapping("/tasks")
    public ResponseEntity<ResponseObject> getMyTasks(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) UUID testAccountId,
            HttpSession session
    ) {
        UUID teamId = (testAccountId != null) ? testAccountId : (UUID) session.getAttribute("STAFF_ID");

        // Kiểm tra cuối cùng nếu cả 2 đều null
        if (teamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject(401, "Lỗi: Vui lòng truyền testAccountId trên Swagger hoặc đăng nhập", null)
            );
        }
        Page<TeamAssignmentResponse> tasks = rescueTeamService.getTaskByFilter(teamId, filter, page);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Trả về tasks cho đội cứu hộ", tasks)
        );
//        try {
//            Page<TeamAssignmentResponse> tasks = rescueTeamService.getTaskByFilter(teamId, filter, page);
//
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(200, "Trả về tasks cho đội cứu hộ", tasks)
//            );
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404, e.getMessage(), null)
//            );
//        }

    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ResponseObject> getTaskById(
            @RequestParam(required = false) UUID testAccountId,
            @PathVariable UUID id,
            HttpSession session
    ) {
        UUID teamId = (testAccountId != null) ? testAccountId : (UUID) session.getAttribute("STAFF_ID");
        if (teamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject(401, "Lỗi: Vui lòng truyền testAccountId trên Swagger hoặc đăng nhập", null)
            );
        }
        TaskDetailResponse detailResponse = rescueTeamService.getAssignmentDetail(id, teamId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Danh sách yêu cầu tải thành công", detailResponse)
        );

//        try {
//            TaskDetailResponse detailResponse = rescueTeamService.getAssignmentDetail(id, teamId);
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(200, "Danh sách yêu cầu tải thành công", detailResponse)
//            );
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404, e.getMessage(), null)
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, e.getMessage(), null)
//            );
//        }

    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<ResponseObject> updateTaskStatus(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest updateRequest
    ) {
        String result = rescueTeamService.updateAssignment(id, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(201, "Tự động chuyển về trang task", result)
        );

//        try {
//            String result = rescueTeamService.updateAssignment(id, updateRequest);
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(201, "Tự động chuyển về trang task", result)
//            );
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(200, e.getMessage(), null)
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, e.getMessage(), null)
//            );
//        }

    }

    @GetMapping("/chat/{requestId}")
    public ResponseEntity<ResponseObject> getAllMessages(@PathVariable UUID requestId) {
        List<MessageResponse> result = rescueTeamService.getAllMessagesByRequest(requestId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));

//        try {
//            List<MessageResponse> result = rescueTeamService.getAllMessagesByRequest(requestId);
//            return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404, e.getMessage(), null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Không thể tải lịch sử chat", e.getMessage()));
//        }
    }

    @PostMapping("/chat/{requestId}")
    public ResponseEntity<ResponseObject> sendMessage(
            @PathVariable UUID requestId,
            @RequestParam(required = false) UUID testAccountId,
            @RequestBody SendMessageRequest dto,
            HttpSession session
    ) {
        UUID rescueTeamId = (testAccountId != null) ? testAccountId : (UUID) session.getAttribute("STAFF_ID");
        if (rescueTeamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject(401, "Vui lòng đăng nhập hoặc truyền testAccountId", null));
        }

        MessageResponse result = rescueTeamService.sendMessage(
                requestId,
                rescueTeamId,
                dto.content(),
                dto.sendAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(201, "Gửi tin nhắn thành công", result));

//        try {
//            MessageResponse result = rescueTeamService.sendMessage(
//                    requestId,
//                    rescueTeamId,
//                    dto.content(),
//                    dto.sendAt()
//            );
//            return ResponseEntity.status(HttpStatus.CREATED).body(
//                    new ResponseObject(201, "Gửi tin nhắn thành công", result));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Không thể gửi tin nhắn", e.getMessage()));
//        }
    }
}
