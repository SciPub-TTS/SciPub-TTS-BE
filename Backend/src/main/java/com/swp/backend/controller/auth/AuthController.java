package com.swp.backend.controller.auth;

import com.swp.backend.service.AuthService;
import com.swp.backend.dto.auth.request.LoginRequest;
import com.swp.backend.dto.auth.response.LoginResponse;
import com.swp.backend.dto.common.ResponseObject;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest loginRequest, HttpSession session) {

        //HttpServletRequest: Đại diện cho toàn bộ một yêu cầu HTTP. Nó chứa mọi thứ: Header, Cookies, IP người gửi, Body, Parameters... và cả Session bên trong nó.
        //HttpSession: Chỉ đại diện cho phiên làm việc của một người dùng cụ thể. Nó là một "ngăn chứa đồ" riêng biệt trên server dành cho user đó.
        //Nguyên tắc vàng: Tầng Service chỉ nên xử lý Logic (check pass, check phone). KHông cần phải biết về "HTTP", "Request" hay "Session".

        LoginResponse account = authService.authenticateUser(loginRequest);

        session.setAttribute("STAFF_ID", account.accountId());
        session.setAttribute("STAFF_ROLE", account.role());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Đăng nhập thành công", account)
        );
//        try {
//            LoginResponse account = authService.authenticateUser(loginRequest);
//
//            session.setAttribute("STAFF_ID", account.accountId());
//            session.setAttribute("STAFF_ROLE", account.role());
//
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject(200, "Đăng nhập thành công", account)
//            );
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    new ResponseObject(401, "Số điện thoại hoặc mật khẩu không đúng", null)
//            );
//        } catch (Exception e) {
//            log.error("Lỗi hệ thống khi đăng nhập cho số {}: ", loginRequest.phone(), e);
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Lỗi hệ thống", null)
//            );
//        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Đã đăng xuất thành công", null)
        );
    }
}
