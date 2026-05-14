package com.swp.backend.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CloudinaryUtils {

    /**
     * Tách Public ID từ URL Cloudinary
     * URL mẫu: <a href="https://res.cloudinary.com/diag3tget/image/upload/v1770796864/rescue_requests/uquxkzzyxwomaohcqp8s.jpg">...</a>
     * Kết quả: rescue_requests/uquxkzzyxwomaohcqp8s
     */
    public static String extractPublicId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 1. Tìm vị trí của chuỗi "rescue_requests/"
            int startIndex = url.lastIndexOf("rescue_requests/");
            if (startIndex == -1) {
                log.warn("Không tìm thấy thư mục rescue_requests trong URL: {}", url);
                return null;
            }

            // 2. Tìm vị trí dấu chấm cuối cùng (bắt đầu của phần mở rộng .jpg, .png...)
            int endIndex = url.lastIndexOf(".");

            // 3. Cắt chuỗi lấy từ startIndex đến endIndex
            if (endIndex > startIndex) {
                return url.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tách public_id từ URL {}: {}", url, e.getMessage());
        }

        return null;
    }
}