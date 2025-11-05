package run.prizm.core.auth.security.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import run.prizm.core.auth.repository.AdminRepository;
import run.prizm.core.auth.constant.ErrorCode;
import run.prizm.core.auth.constant.HttpConstants;
import run.prizm.core.auth.constant.UserType;
import run.prizm.core.auth.exception.AuthException;

@Component
@RequiredArgsConstructor
public class CurrentAdminResolver implements HandlerMethodArgumentResolver {

    private final AdminRepository adminRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentAdmin.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        validateUserType(webRequest);
        String idStr = extractHeaderValue(webRequest, HttpConstants.HEADER_ADMIN_ID.getValue(), ErrorCode.MISSING_ADMIN_INFO);

        try {
            Long id = Long.parseLong(idStr);
            return adminRepository.getActiveByIdOrThrow(id);
        } catch (NumberFormatException exception) {
            throw new AuthException(ErrorCode.INVALID_ADMIN_ID);
        }
    }

    private void validateUserType(NativeWebRequest webRequest) {
        String userType = webRequest.getHeader(HttpConstants.HEADER_USER_TYPE.getValue());
        if (!UserType.ADMIN.getValue()
                           .equals(userType)) {
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String extractHeaderValue(NativeWebRequest webRequest, String headerName, ErrorCode errorCode) {
        String value = webRequest.getHeader(headerName);
        if (value == null || value.isEmpty()) {
            throw new AuthException(errorCode);
        }
        return value;
    }
}