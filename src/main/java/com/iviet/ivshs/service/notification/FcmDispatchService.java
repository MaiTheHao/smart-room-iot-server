package com.iviet.ivshs.service.notification;

import com.google.firebase.messaging.*;
import com.iviet.ivshs.dao.ClientDeviceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FcmDispatchService {

    private static final Logger log = LoggerFactory.getLogger(FcmDispatchService.class);

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private ClientDeviceDao clientDeviceDao;

    /**
     * Gửi Mixed Notification cho 1 thiết bị
     */
    @Async
    public void sendToSingleDevice(String targetToken, String title, String body, Map<String, String> data) {
        Message.Builder messageBuilder = Message.builder().setToken(targetToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        Message message = messageBuilder.build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("Successfully sent message! Message ID: {}", messageId);
        } catch (FirebaseMessagingException e) {
            String errorCode = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "";
            checkAndDeleteDeadToken(errorCode, targetToken);
            log.error("Error sending FCM unicast message: {}", e.getMessage());
        }
    }

    /**
     * Gửi Mixed Notification cho nhiều thiết bị (Multicast)
     */
    @Async
    public void sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) return;

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder().addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        MulticastMessage message = messageBuilder.build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Multicast result - Success: {}, Failure: {}", response.getSuccessCount(),
                    response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<String> deadTokens = new ArrayList<>();
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String failedToken = tokens.get(i);
                        FirebaseMessagingException fme = responses.get(i).getException();

                        if (fme != null) {
                            String errorCode = fme.getMessagingErrorCode() != null ? fme.getMessagingErrorCode().name()
                                    : "";
                            if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                                deadTokens.add(failedToken);
                            }
                        }
                    }
                }

                if (!deadTokens.isEmpty()) {
                    log.info("Found {} dead tokens. Deleting in batch...", deadTokens.size());
                    try {
                        clientDeviceDao.deleteByFcmTokenIn(deadTokens);
                    } catch (Exception ex) {
                        log.error("Error batch deleting FCM tokens: {}", ex.getMessage());
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Error calling FCM Multicast API: {}", e.getMessage());
        }
    }

    private void checkAndDeleteDeadToken(String errorCode, String token) {
        if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
            log.info("Token is dead or application uninstalled. Deleting token: {}", token);
            try {
                clientDeviceDao.deleteByFcmToken(token);
            } catch (Exception ex) {
                log.error("Error deleting FCM token: {}", ex.getMessage());
            }
        }
    }
}
