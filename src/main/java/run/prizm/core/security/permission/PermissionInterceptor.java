package run.prizm.core.security.permission;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import run.prizm.core.space.channel.permission.ChannelPermission;
import run.prizm.core.space.channel.permission.ChannelPermissionCalculator;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final WorkspaceUserRepository workspaceUserRepository;
    private final ChannelPermissionCalculator channelPermissionCalculator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireWorkspaceRole workspaceRoleAnnotation = handlerMethod.getMethodAnnotation(RequireWorkspaceRole.class);
        RequireChannelPermission channelPermissionAnnotation = handlerMethod.getMethodAnnotation(RequireChannelPermission.class);

        if (workspaceRoleAnnotation == null && channelPermissionAnnotation == null) {
            return true;
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (workspaceRoleAnnotation != null) {
            return checkWorkspaceRole(userId, pathVariables, workspaceRoleAnnotation, response);
        }

        return checkChannelPermission(userId, pathVariables, channelPermissionAnnotation, response);

    }

    private boolean checkWorkspaceRole(Long userId, Map<String, String> pathVariables,
                                       RequireWorkspaceRole annotation, HttpServletResponse response) {
        String workspaceIdStr = pathVariables.get("workspaceId");
        if (workspaceIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        Long workspaceId = Long.parseLong(workspaceIdStr);
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElse(null);

        if (workspaceUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        for (WorkspaceUserRole requiredRole : annotation.value()) {
            if (workspaceUser.getRole() == requiredRole) {
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private boolean checkChannelPermission(Long userId, Map<String, String> pathVariables,
                                           RequireChannelPermission annotation, HttpServletResponse response) {
        String workspaceIdStr = pathVariables.get("workspaceId");
        String channelIdStr = pathVariables.get("channelId");

        if (workspaceIdStr == null || channelIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        Long workspaceId = Long.parseLong(workspaceIdStr);
        Long channelId = Long.parseLong(channelIdStr);

        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElse(null);

        if (workspaceUser == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        Map<Long, ChannelPermission> permissions = channelPermissionCalculator.calculatePermissions(workspaceUser);
        ChannelPermission userPermission = permissions.getOrDefault(channelId, ChannelPermission.NONE);

        if (userPermission.getLevel() >= annotation.value()
                                                   .getLevel()) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}