package run.prizm.core.user.resolver;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import run.prizm.core.user.entity.User;
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
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Claims)) {
            throw new RuntimeException("Invalid authentication");
        }

        Claims claims = (Claims) principal;
        Object idObj = claims.get("id");

        if (idObj == null) {
            throw new RuntimeException("Invalid token");
        }

        Long userId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        
        Class<?> parameterType = parameter.getParameterType();
        
        if (parameterType.equals(Long.class)) {
            return userId;
        }
        
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }
}