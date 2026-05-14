package com.swp.backend.controller.citizen;

import com.swp.backend.service.CitizenService;
import com.swp.backend.dto.citizen.request.LookupRequest;
import com.swp.backend.dto.citizen.request.RescueRequest;
import com.swp.backend.dto.citizen.request.UpdateRequest;
import com.swp.backend.dto.citizen.response.CitizenRescueResponse;
import com.swp.backend.dto.chat.request.SendMessageRequest;
import com.swp.backend.dto.chat.response.MessageResponse;
import com.swp.backend.dto.common.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/citizen")
public class CitizenRequestController {
    private final CitizenService citizenService;

    public CitizenRequestController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }


    @PostMapping(value = "/sendRequest", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseObject> sendRequest(@ModelAttribute RescueRequest rescueRequest) {
        // Sử dụng @ModelAttribute để nhận cả các field text và danh sách MultipartFile
        //Lỗi 400 thường dùng cho sai định dạng dữ liệu (thiếu field, sai kiểu
        // Ở đây người dùng gửi lên hoàn toàn hợp lệ, nhưng do trạng thái hệ thông nên sinh ra xung đột
        //409 Conflict là mã code chính xác nhất cho trường hợp "đã tồn tại/xung đột trạng thái"
        var savedRequest = citizenService.createRescueRequest(rescueRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(201, "Yêu cầu đã được gửi", savedRequest)
        );

//        try {
//            var savedRequest = citizenService.createRescueRequest(rescueRequest);
//            return ResponseEntity.status(HttpStatus.CREATED).body(
//                    new ResponseObject(201, "Yêu cầu đã được gửi", savedRequest)
//            );
//        } catch (RuntimeException e) {
//            if ("EXISTING_ACTIVE_REQUEST".equals(e.getMessage())) {
//
//                return ResponseEntity.status(HttpStatus.CONFLICT).body(
//                        new ResponseObject(409, "Bạn đã có một yêu cầu đang được xử lý. Vui lòng vào trang tra cứu để theo dõi.", null)
//                );
//            }
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Lỗi hệ thống: " + e.getMessage(), null)
//            );
//        }
    }


    //Chuyển thành GetMapping
    @PostMapping(value = "/lookup")
    public ResponseEntity<ResponseObject> lookup(@RequestBody LookupRequest lookupRequest) {
        CitizenRescueResponse response = citizenService.lookUpRequest(lookupRequest);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Tìm thấy thông tin yêu cầu cứu hộ", response)
        );

//        if (response == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    new ResponseObject(404,
//                            "Không tìm thấy yêu cầu đang được xử lí hay tạm hoãn của số điện thoại này",
//                            null));
//        } else {
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(200, "Tìm thấy thông tin yêu cầu cứu hộ", response)
//            );
//        }

    }

    //Exception quá chung chung
    @PutMapping(value = "/edit", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseObject> edit(@ModelAttribute UpdateRequest updateRequest) {
        CitizenRescueResponse response = citizenService.updateRescueRequest(updateRequest);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200,
                        "Thông tin đã được chỉnh sửa thành công",
                        response
                )
        );
//        try {
//            CitizenRescueResponse response = citizenService.updateRescueRequest(updateRequest);
//
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(200,
//                            "Thông tin đã được chỉnh sửa thành công",
//                            response
//                    )
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500,
//                            "Lỗi server",
//                            e.getMessage()
//                    )
//            );
//        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<ResponseObject> cancelRequest(@PathVariable UUID id) { // ← đổi Long thành UUID
        citizenService.cancelRequest(id);
        return ResponseEntity.ok(new ResponseObject(200, "Đã hủy yêu cầu", null));

//        try {
//            citizenService.cancelRequest(id);
//            return ResponseEntity.ok(new ResponseObject(200, "Đã hủy yêu cầu", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseObject(500, "Lỗi hệ thống", null));
//        }
    }

    @GetMapping("/chat/{requestId}")
    public ResponseEntity<ResponseObject> getAllMessages(@PathVariable UUID requestId) {
        List<MessageResponse> result = citizenService.getAllMessagesByRequest(requestId);
        return ResponseEntity.ok(new ResponseObject(200, "Success", result));
//        try {
//            List<MessageResponse> result = citizenService.getAllMessagesByRequest(requestId);
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
            @RequestBody SendMessageRequest dto
    ) {
        MessageResponse result = citizenService.sendMessage(
                requestId,
                dto.content(),
                dto.sendAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(201, "Gửi tin nhắn thành công", result));
//        try {
//            MessageResponse result = citizenService.sendMessage(
//                    requestId,
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

