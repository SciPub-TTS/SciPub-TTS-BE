package com.brotherhood.scipubtts.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // ===== REQUEST =====
    EXISTING_ACTIVE_REQUEST(HttpStatus.CONFLICT,
            "SĐT %s đã có một yêu cầu đang được xử lý."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy yêu cầu với ID/SĐT %s."),
    INVALID_REQUEST_TYPE(HttpStatus.BAD_REQUEST,
            "Loại yêu cầu không hợp lệ."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST,
            "Status không hợp lệ: %s"),
    INVALID_STATUS_UPDATE(HttpStatus.BAD_REQUEST,
            "Coordinator chỉ được set status 'đang xử lý' hoặc 'đã huỷ'"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST,
            "Yêu cầu đang ở trạng thái %s, không thể cập nhật"),
    REQUEST_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST,
            "Yêu cầu đã hoàn thành, không thể thay đổi"),

    REQUEST_LOCATION_MISSING(HttpStatus.BAD_REQUEST,
            "Yêu cầu chưa có thông tin vị trí"),

    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy nhiệm vụ."),

    // ===== CITIZEN =====
    INVALID_PHONE(HttpStatus.BAD_REQUEST,
            "Số điện thoại không hợp lệ."),

    // ===== MESSAGE =====
    INVALID_REQUEST_ID(HttpStatus.BAD_REQUEST,
            "Thiếu requestId."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST,
            "Nội dung tin nhắn không được để trống."),
    SENDER_NOT_FOUND(HttpStatus.BAD_REQUEST,
            "Không tìm thấy người gửi."),
    INVALID_SENDER_ID(HttpStatus.BAD_REQUEST,
            "Thiếu senderId"),

    // ===== IMAGE =====
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "Tải ảnh lên thất bại."),
    CLOUDINARY_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "Xóa ảnh trên Cloudinary thất bại."),

    // ===== AUTH =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,
            "Email hoặc mật khẩu không chính xác."),
    AUTH_BANNED(HttpStatus.UNAUTHORIZED,
            "Tài khoản hiện tại đang bị khoá, vui lòng liên hệ %s"),
    EMAIL_EXISTED(HttpStatus.BAD_REQUEST,
            "Email đã tồn tại"),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,
            "Token không hợp lệ"),
    UNVERIFIED_EMAIL(HttpStatus.BAD_REQUEST,
            "Please verify email before login"),

    // ===== SYSTEM / DATABASE =====
    UNSUPPORTED_DATE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR,
            "Định dạng thời gian không được hỗ trợ: %s."),


    // ===== VEHICLE =====
    INVALID_VEHICLE_TYPE(HttpStatus.BAD_REQUEST,
            "Loại phương tiện không hợp lệ. Chỉ chấp nhận: 'xuồng', 'xe cứu hộ', 'trực thăng'"),
    VEHICLE_NOT_AVAILABLE(HttpStatus.NOT_FOUND,
            "Không có phương tiện khả dụng"),
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy phương tiện với ID: %s"),
    INVALID_VEHICLE_STATE(HttpStatus.BAD_REQUEST,
            "Trạng thái phương tiện không hợp lệ"),
    INVALID_VEHICLE_OWNER(HttpStatus.BAD_REQUEST,
            "Nhân viên không hợp lệ: chỉ cho phép 'cứu hộ'"),


    // ===== TEAM =====
    RESCUE_TEAM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy đội cứu hộ với id: %s"),
    NO_NEARBY_TEAM_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy đội cứu hộ nào phù hợp với phương tiện: %s"),
    INVALID_TEAM_ROLE(HttpStatus.FORBIDDEN,
            "Tài khoản gửi tin nhắn không phải đội cứu hộ"),

    // ===== COORDINATOR =====
    COORDINATOR_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy điều phối viên với id: %s"),
    INVALID_COORDINATOR_ROLE(HttpStatus.FORBIDDEN,
            "Tài khoản gửi tin nhắn không phải điều phối viên"),

    // ===== URGENCY =====
    INVALID_URGENCY(HttpStatus.BAD_REQUEST,
            "Mức độ khẩn cấp không hợp lệ. Chỉ chấp nhận: 'cao', 'trung bình', 'thấp'"),

    // ===== STAFF =====
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Không tìm thấy nhân viên với ID: %s"),
    STAFF_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "Số điện thoại đã tồn tại: %s"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST,
            "Role không hợp lệ. Chỉ chấp nhận: 'điều phối viên' hoặc 'cứu hộ'"),
    INVALID_STAFF_STATE(HttpStatus.BAD_REQUEST,
            "Trạng thái không hợp lệ. Chỉ chấp nhận: 'hoạt động' hoặc 'không hoạt động'"),

    // ===== DASHBOARD =====
    STAFF_DATA_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi thống kê: không có dữ liệu nhân viên"),
    VEHICLE_DATA_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi thống kê: không có dữ liệu phương tiện"),
    TEAM_PERFORMANCE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi thống kê: không có dữ liệu hiệu suất đội"),
    CITY_DATA_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi thống kê: không có dữ liệu thành phố");


    private final HttpStatus status;
    private final String message;
}
