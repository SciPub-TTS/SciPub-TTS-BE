package com.brotherhood.scipubtts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI v3 (Swagger UI) cho toàn bộ hệ thống SciPub-TTS Backend.
 * <p>
 * Lớp này chịu trách nhiệm khởi tạo giao diện Swagger, thiết lập thông tin dự án
 * và kích hoạt nút "Authorize" (ổ khóa) để hỗ trợ truyền JWT Token khi test API.
 * </p>
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Cấu hình Custom Bean OpenAPI nhằm tùy biến metadata và tích hợp cơ chế bảo mật Bearer Token.
     * * @return Đối tượng OpenAPI chứa toàn bộ đặc tả cấu hình của hệ thống
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Tên định danh cho cấu hình bảo mật, dùng làm chìa khóa liên kết trong hệ thống
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. Thiết lập thông tin cơ bản hiển thị ở đầu trang Swagger UI
                .info(new Info()
                        .title("SciPub-TTS API Documentation")
                        .version("v1.0")
                        .description("Tài liệu cấu hình và thử nghiệm API cho hệ thống SciPub-TTS"))

                // 2. Ép tất cả các Endpoint mặc định đều có thể đính kèm Token này.
                // Nếu không có dòng này, các API cá nhân sẽ không có biểu tượng ổ khóa nhỏ bên cạnh.
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 3. Định nghĩa cấu trúc thành phần (Components) chứa cơ chế bảo mật cho Swagger
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName) // Đặt tên trùng với chìa khóa định danh phía trên
                                        .type(SecurityScheme.Type.HTTP) // Định dạng bảo mật qua giao thức HTTP
                                        .scheme("bearer") // Xác định chuẩn mã hóa token là Bearer (Tự thêm chữ 'Bearer ' vào Header)
                                        .bearerFormat("JWT"))); // Khai báo cho lập trình viên biết đây là chuỗi JWT Token
    }
}
