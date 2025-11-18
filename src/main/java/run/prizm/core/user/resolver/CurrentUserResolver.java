package run.prizm.core.user.resolver;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Claims claims)) {
            throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION);
        }

        Object idObj = claims.get("id");

        if (idObj == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());

        Class<?> parameterType = parameter.getParameterType();

        if (parameterType.equals(Long.class)) {
            return userId;
        }

        return userRepository.findById(userId)
                             .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}