package com.brotherhood.scipubtts.config;

import com.brotherhood.scipubtts.auth.security.CustomUserDetailsService;
import com.brotherhood.scipubtts.auth.security.jwt.JwtAuthenticationFilter;
import com.brotherhood.scipubtts.auth.security.oauth2.CustomAuthenticationFailureHandler;
import com.brotherhood.scipubtts.auth.security.oauth2.CustomOAuth2UserService;
import com.brotherhood.scipubtts.auth.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.brotherhood.scipubtts.auth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.brotherhood.scipubtts.common.exception.RestAccessDeniedHandler;
import com.brotherhood.scipubtts.common.exception.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            DaoAuthenticationProvider daoAuthenticationProvider) {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)

                // =================================================================================
// 1. CẤU HÌNH QUẢN LÝ SESSION (SESSION MANAGEMENT POLICY)
// =================================================================================
/*
 *  TẠI SAO KHÔNG DÙNG 'SessionCreationPolicy.IF_REQUIRED' (Mặc định)?
 * - IF_REQUIRED có nghĩa là: "Backend sẽ không chủ động tạo Session, trừ khi có tính năng nào
 * đó bắt buộc phải cần (và OAuth2 Login mặc định của Spring chính là tính năng ép tạo Session)".
 * - HẬU QUẢ (Gây ra lỗi lệch UUID trước đó): Khi người dùng Đăng nhập bằng Google thành công,
 * Spring Security tự động sinh ra một HTTP Session ở Backend và trả về Cookie `JSESSIONID` cho Client.
 * Khi người dùng bấm Logout ở Frontend, Client thường chỉ xóa Access Token ở phía mình chứ KHÔNG
 * gọi API hủy Session ở Backend. Do đó, Session cũ vẫn sống nguyên trên Server.
 * - XUNG ĐỘT: Khi người dùng đăng nhập lại bằng Email/Password, trình duyệt theo thói quen tự động
 * đính kèm cái Cookie `JSESSIONID` cũ lên. Bộ lọc của Spring Security nhìn thấy Cookie này hợp lệ
 * liền "hớn hở" lôi thông tin User Google cũ trong Session ra xài, đè hoàn toàn lên luồng xử lý
 * Token/Credentials mới bạn vừa nhập. Dẫn đến việc lấy sai thông tin và lệch UUID.
 *
 *  TẠI SAO CHỌN 'SessionCreationPolicy.STATELESS'?
 * - Ra lệnh cho Spring Security HOÀN TOÀN KHÔNG tạo, không lưu và không sử dụng HTTP Session ở Backend.
 * Mọi Request gửi lên độc lập 100% và bắt buộc phải được xác thực thông qua Token (JWT/Refresh Token).
 * Điều này bẻ gãy hoàn toàn cơ chế tự động nhận dạng bằng Cookie `JSESSIONID`, giải quyết triệt để lỗi lệch UUID.
 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authenticationProvider(daoAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/error", "/favicon.ico",
                                "/register",
                                "/api/auth/register",
                                "/api/auth/register/google/**",
                                "/api/auth/oauth2/google",
                                "/api/auth/oauth2/exchange",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/verify-email",
                                "/api/auth/forgot-password/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/search/summary",
                                "/api/search/filters/**",
                                "/api/search/works",
                                "/api/search/entities",
                                "/api/search/trending-topics",
                                "/api/search/trending-keywords",
                                "/api/search/hot-topics",
                                "/api/search/hot-keywords",
                                "/api/papers/**",
                                "/api/authors/**",
                                "/api/topics/**",
                                "/api/statistic/**",
                                "/api/data/**",
                                "/api/report/**",
                                "/api/home/landing/summary",
                                "/api/health"
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/insights/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .oauth2Login(
/*
 * 🍪 TẠI SAO PHẢI CẤU HÌNH 'authorizationRequestRepository()'?
 * - Luồng Authorization Code Flow của OAuth2 mặc định bắt buộc phải lưu trữ tạm thời 2 thứ:
 * Chuỗi `state` ngẫu nhiên (dùng để chống tấn công giả mạo CSRF) và `redirect_uri` ban đầu,
 * trước khi hệ thống chuyển hướng (redirect) người dùng sang trang đăng nhập của Google.
 * - Vì phía trên chúng ta đã cấu hình hệ thống là STATELESS (cấm dùng Session ở Backend), nên
 * Spring không còn chỗ nào trên Server để cất giữ cái `state` tạm thời này nữa. Nếu để mặc định,
 * khi từ Google quay lại, Spring sẽ báo lỗi ngay ("Authorization Request Not Found" hoặc "State Mismatch").
 *
 * 💡 GIẢI PHÁP:
 * - Đoạn cấu hình dưới đây chỉ định một Repository tùy chỉnh (thường cài đặt bằng lớp
 * HttpCookieOAuth2AuthorizationRequestRepository).
 * - Thay vì lưu chuỗi `state` và thông tin tạm ở Session của Server, ta tiến hành MÃ HÓA bảo mật
 * rồi CẤT TẠM VÀO COOKIE trên chính trình duyệt của người dùng.
 * - Khi người dùng đăng nhập xong bên Google và được dẫn quay trở lại App, Spring sẽ lôi cái
 * Cookie tạm này ra, giải mã, đối chiếu chuỗi `state` xem có khớp không. Nếu khớp và hợp lệ,
 * Spring sẽ xóa ngay Cookie tạm này đi và đi tiếp luồng xử lý sinh JWT/Refresh Token.
 *
 * KẾT QUẢ: Giúp luồng đăng nhập Google chạy mượt mà, an toàn tuyệt đối mà Backend vẫn giữ được
 * trạng thái STATELESS 100%, không tốn một chút tài nguyên bộ nhớ nào để nuôi Session.
 */
                        oauth2 -> oauth2
                        .authorizationEndpoint(auth ->
                                auth.authorizationRequestRepository(
                                        authorizationRequestRepository
                                )
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                );


        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}