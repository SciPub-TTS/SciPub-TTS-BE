package com.brotherhood.scipubtts.auth.service.impl;

import com.brotherhood.scipubtts.auth.service.RefreshCookieService;
import com.brotherhood.scipubtts.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class RefreshCookieServiceImpl implements RefreshCookieService {

    private final AuthProperties authProperties;

    public RefreshCookieServiceImpl(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public void addRefreshCookie(HttpServletResponse response, String rawRefreshToken, boolean rememberMe, Duration ttl) {

        //Dòng này quyết định xem Cookie này sẽ tồn tại trong trình duyệt bao lâu dựa trên tính năng "Remember Me" (Ghi nhớ đăng nhập):
        //      Nếu rememberMe = true: Thời gian sống của cookie (maxAge) sẽ bằng chính thời gian sống của Token (ttl.toSeconds(), ví dụ: 7 ngày hoặc 30 ngày tính bằng giây). Khi tắt trình duyệt đi mở lại, user vẫn không bị đăng xuất.
        //      Nếu rememberMe = false (Không tích chọn): maxAge sẽ nhận giá trị -1. Trong đặc tả của Cookie, -1 nghĩa là tạo ra một Session Cookie (Cookie phiên). Cookie này sẽ tự động bị xóa ngay lập tức khi người dùng tắt trình duyệt (đóng tab/đóng app).
        long maxAge = rememberMe ? ttl.toSeconds() : -1;

        ResponseCookie cookie = ResponseCookie
                .from(authProperties.getRefreshCookieName(), rawRefreshToken)
                .httpOnly(true)
                .secure(authProperties.isRefreshCookieSecure())
                .sameSite(authProperties.getRefreshCookieSameSite())
                .path(authProperties.getRefreshCookiePath())
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void clearRefreshCookie(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie
            .from(authProperties.getRefreshCookieName(), "")
            .httpOnly(true)
            .secure(authProperties.isRefreshCookieSecure())
            .sameSite(authProperties.getRefreshCookieSameSite())
            .path(authProperties.getRefreshCookiePath())
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                //Một request gửi lên có thể mang theo rất nhiều cookie khác nhau
                //Dòng này dùng để lọc ra duy nhất cái cookie nào có tên (cookie.getName()) trùng khớp chính xác với cấu hình tên của Refresh Cookie
                .filter(cookie -> authProperties.getRefreshCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                //Dòng này sử dụng thư viện tiện ích StringUtils của Spring. Nó kiểm tra xem chuỗi Token vừa lấy ra có thực sự chứa ký tự nào không.
                .filter(StringUtils::hasText)
                //Trả về kết quả hợp lệ đầu tiên tìm thấy dưới dạng Optional<String>. Nếu đi qua hết các bộ lọc phía trên mà không tìm thấy cookie nào khớp, hàm findFirst() cũng sẽ tự động trả về Optional.empty().
                .findFirst();
    }
}
