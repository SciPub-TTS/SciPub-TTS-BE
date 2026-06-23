package com.brotherhood.scipubtts.common.resolver;

import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Bộ giải trình tham số (Argument Resolver) cấu hình ngầm cho Spring MVC.
 * <p>
 * Lớp này có nhiệm vụ chặn các request đi vào Controller có chứa annotation {@link CurrentUserUUID},
 * tự động bốc tách thông tin từ SecurityContext và trả về UUID sạch cho hàm xử lý.
 * </p>
 */
@Component
public class CurrentUserUUIDArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Kiểm tra xem tham số đầu vào của Controller có thỏa mãn điều kiện để kích hoạt bộ resolver này không.
     * * @param parameter thông số của hàm Controller đang được kiểm tra
     * @return {@code true} nếu tham số được đánh dấu bằng @CurrentUserUUID VÀ có kiểu dữ liệu là UUID
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserUUID.class) && parameter.getParameterType().equals(UUID.class);
    }

    /**
     * Logic xử lý bóc tách Security Context để lấy UUID gán vào tham số Controller.
     */
    @Override
    public @Nullable Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CurrentUserUUID annotation = parameter.getParameterAnnotation(CurrentUserUUID.class);
        boolean isRequired = (annotation != null) && annotation.required();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            if (isRequired) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        if (userPrincipal.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userPrincipal.getId();
    }

}
