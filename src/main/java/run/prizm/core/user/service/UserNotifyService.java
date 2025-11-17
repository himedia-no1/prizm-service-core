package run.prizm.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.user.dto.UserNotifyListResponse;
import run.prizm.core.user.entity.UserNotify;
import run.prizm.core.user.repository.UserNotifyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotifyService {

    private final UserNotifyRepository userNotifyRepository;

    @Transactional(readOnly = true)
    public UserNotifyListResponse getNotifications(Long userId) {
        List<UserNotify> notifications = userNotifyRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        List<UserNotifyListResponse.UserNotifyItem> items = notifications.stream()
                                                                         .map(notify -> new UserNotifyListResponse.UserNotifyItem(
                                                                                 notify.getId(),
                                                                                 notify.getType(),
                                                                                 notify.getSender() != null ? notify.getSender()
                                                                                                                    .getId() : null,
                                                                                 notify.getContent(),
                                                                                 notify.getLocationId(),
                                                                                 notify.isImportant(),
                                                                                 notify.isRead(),
                                                                                 notify.getCreatedAt()
                                                                         ))
                                                                         .toList();

        return new UserNotifyListResponse(items);
    }
}