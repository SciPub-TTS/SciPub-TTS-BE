package com.brotherhood.scipubtts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    //Các cấu hình mang tính chất bổ trợ hạ tầng (như Jackson/ObjectMapper, mã hóa dữ liệu, định dạng ngày tháng) nên luôn được tách biệt hoàn toàn khỏi các cấu hình nghiệp vụ phức tạp (như Security/Phân quyền). Việc chia nhỏ cấu hình theo đúng trách nhiệm (Single Responsibility Principle) sẽ giúp Backend của bạn cực kỳ ổn định và không bao giờ lo bị lỗi Circular Dependency
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
