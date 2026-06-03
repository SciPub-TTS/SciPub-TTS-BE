package com.brotherhood.scipubtts.common.annotation;

import java.lang.annotation.*;


/**
 * Custom Annotation dùng tại tầng Controller để tự động inject (tiêm) UUID của User đang đăng nhập.
 * <p>
 * Giúp thay thế việc gọi trực tiếp @AuthenticationPrincipal UserPrincipal principal và xử lý logic thủ công.
 * </p>
 */
@Target(ElementType.PARAMETER) // Chỉ áp dụng trên tham số của phương thức (Controller method parameter)
@Retention(RetentionPolicy.RUNTIME)// Duy trì trong suốt quá trình ứng dụng chạy (Runtime) để Reflection đọc được
@Documented // Xuất hiện trong tài liệu Javadoc của dự án
public @interface CurrentUserUUID {

    /**
     * Xác định xem endpoint này có bắt buộc phải đăng nhập hay không.
     * <ul>
     * <li>{@code true} (Mặc định): Nếu user chưa đăng nhập, hệ thống tự động ném lỗi 401 (Unauthorized).</li>
     * <li>{@code false}: Nếu user chưa đăng nhập, biến UUID nhận giá trị {@code null} (Dùng cho các endpoint lai/công khai).</li>
     * </ul>
     */
    boolean required() default true;
}