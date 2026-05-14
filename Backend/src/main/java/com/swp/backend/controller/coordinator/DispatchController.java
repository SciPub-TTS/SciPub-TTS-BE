package com.swp.backend.controller.coordinator;

import com.swp.backend.service.DispatchService;
import com.swp.backend.dto.chat.request.SendMessageRequest;
import com.swp.backend.dto.chat.response.MessageResponse;
import com.swp.backend.dto.common.ResponseObject;

import com.swp.backend.dto.coordinator.request.UpdateRequest;
import com.swp.backend.dto.coordinator.response.NearbyTeamResponse;
import com.swp.backend.dto.coordinator.response.RequestDetailResponse;
import com.swp.backend.dto.coordinator.response.RequestListResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/coordinator")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    @GetMapping("/requests")
    public ResponseEntity<ResponseObject> getRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RequestListResponse> result = dispatchService.getRequests(status, page, size);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));

//        try {
//            Page<RequestListResponse> result = dispatchService.getRequests(status, page, size);
//            return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null));
//        }
    }


    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ResponseObject> getRequestDetail(@PathVariable UUID requestId) {
        RequestDetailResponse result = dispatchService.getRequestDetail(requestId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        try {
//            RequestDetailResponse result = dispatchService.getRequestDetail(requestId);
//            return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404, e.getMessage(), null));
//        }
    }

    @GetMapping("/requests/{requestId}/nearby-teams")
    public ResponseEntity<ResponseObject> getNearbyTeams(
            @PathVariable UUID requestId,
            @RequestParam String vehicleType
    ) {
        List<NearbyTeamResponse> result = dispatchService.getNearbyTeams(requestId, vehicleType);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        try {
//            List<NearbyTeamResponse> result = dispatchService.getNearbyTeams(requestId, vehicleType);
//            return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404, e.getMessage(), null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Không thể lấy đội cứu hộ gần nhất", e.getMessage()));
//        }
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ResponseObject> acceptRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) UUID testAccountId,
            @RequestBody UpdateRequest dto,
            HttpSession session
    ) {
        UUID coordinatorId = testAccountId != null
                ? testAccountId
                : (UUID) session.getAttribute("STAFF_ID");

        if (coordinatorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject(401, "Vui lòng đăng nhập hoặc truyền testAccountId", null));
        }
        RequestDetailResponse detail = dispatchService.updateRequest(requestId, coordinatorId, dto);
        return ResponseEntity.ok(new ResponseObject(200, "Chấp nhận yêu cầu thành công", detail));
//        try {
//            RequestDetailResponse detail = dispatchService.updateRequest(requestId, coordinatorId, dto);
//            return ResponseEntity.ok(new ResponseObject(200, "Chấp nhận yêu cầu thành công", detail));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Không thể cập nhật yêu cầu", e.getMessage()));
//        }
    }

    @GetMapping("/chat/{requestId}")
    public ResponseEntity<ResponseObject> getAllMessages(@PathVariable UUID requestId) {
        List<MessageResponse> result = dispatchService.takeAllMessageOfRequest(requestId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        try {
//            List<MessageResponse> result = dispatchService.takeAllMessageOfRequest(requestId);
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
        UUID coordinatorId = testAccountId != null
                ? testAccountId
                : (UUID) session.getAttribute("STAFF_ID");

        if (coordinatorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject(401, "Vui lòng đăng nhập hoặc truyền testAccountId", null));
        }
        MessageResponse result = dispatchService.sendMessage(
                requestId,
                coordinatorId,
                dto.content(),
                dto.sendAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(201, "Gửi tin nhắn thành công", result));
//        try {
//            MessageResponse result = dispatchService.sendMessage(
//                    requestId,
//                    coordinatorId,
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
