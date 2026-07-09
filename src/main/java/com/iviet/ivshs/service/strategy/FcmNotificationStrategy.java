package com.iviet.ivshs.service.notification.strategy.impl;

import com.google.firebase.messaging.*;
import com.google.firebase.messaging.AndroidConfig;
import com.iviet.ivshs.dao.ClientDeviceDao;
import com.iviet.ivshs.dto.NotificationRequest;
import com.iviet.ivshs.entities.ClientDevice;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationStrategy implements NotificationStrategy {

    private static final Set<String> DEAD_TOKEN_CODES = Set.of("UNREGISTERED", "INVALID_ARGUMENT");
    private static final int FCM_MULTICAST_LIMIT = 250;

    private final FirebaseMessaging firebaseMessaging;
    private final ClientDeviceDao clientDeviceDao;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRequest request) {
        if (request == null || request.getRecipients() == null) {
            log.warn("Notification request or recipients list is null — skipping");
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                processAndDispatch(request);
            } catch (Exception e) {
                log.error("Critical error in asynchronous push notification dispatch", e);
            }
        });
    }

    private void processAndDispatch(NotificationRequest request) {
        List<String> fcmTokens = request.getRecipients().stream()
                .filter(client -> client != null && client.getClientDevices() != null)
                .flatMap(client -> client.getClientDevices().stream()).filter(device -> device != null)
                .map(ClientDevice::getFcmToken).filter(token -> token != null && !token.isBlank()).distinct().toList();

        if (fcmTokens.isEmpty()) {
            log.debug("No FCM tokens found for {} recipients — Skipping", request.getRecipients().size());
            return;
        }

        log.info("Sending push notifications to {} tokens for {} recipients", fcmTokens.size(),
                request.getRecipients().size());

        List<List<String>> batches = partition(fcmTokens, FCM_MULTICAST_LIMIT);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (List<String> batch : batches) {
                executor.submit(
                        () -> sendMulticastBatch(batch, request.getTitle(), request.getBody(), request.getData()));
            }
        }

        log.info("Completed sending push notifications for {} tokens", fcmTokens.size());
    }

    @Deprecated
    public void sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) return;

        Thread.startVirtualThread(() -> {
            List<List<String>> batches = partition(tokens, FCM_MULTICAST_LIMIT);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (List<String> batch : batches) {
                    executor.submit(() -> sendMulticastBatch(batch, title, body, data));
                }
            }
        });
    }

    @Deprecated
    public void sendToSingleDevice(String targetToken, String title, String body, Map<String, String> data) {
        if (targetToken == null || targetToken.isBlank()) return;

        Thread.startVirtualThread(() -> {
            Message.Builder messageBuilder = Message.builder().setToken(targetToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            try {
                String messageId = firebaseMessaging.send(messageBuilder.build());
                log.info("Message sent successfully! ID: {}", messageId);
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode errorCode = e.getMessagingErrorCode();
                String codeName = errorCode != null ? errorCode.name() : "";
                checkAndDeleteDeadToken(codeName, targetToken);
                log.error("Error sending FCM Unicast message: {}", e.getMessage());
            }
        });
    }

    private void sendMulticastBatch(List<String> batch, String title, String body, Map<String, String> data) {
        MulticastMessage.Builder messageBuilder = MulticastMessage.builder().addAllTokens(batch)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(messageBuilder.build());
            log.info("Multicast result - Success: {}, Failure: {}", response.getSuccessCount(),
                    response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleDeadTokens(response.getResponses(), batch);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Error calling FCM Multicast API: {}", e.getMessage());
        }
    }

    private void handleDeadTokens(List<SendResponse> responses, List<String> tokens) {
        List<String> deadTokens = IntStream.range(0, responses.size()).filter(i -> !responses.get(i).isSuccessful())
                .filter(i -> responses.get(i).getException() instanceof FirebaseMessagingException fme
                        && fme.getMessagingErrorCode() != null
                        && DEAD_TOKEN_CODES.contains(fme.getMessagingErrorCode().name()))
                .mapToObj(tokens::get).toList();

        if (!deadTokens.isEmpty()) {
            log.info("Detected {} dead tokens. Deleting in bulk from DB...", deadTokens.size());
            try {
                clientDeviceDao.deleteByFcmTokenIn(deadTokens);
            } catch (Exception ex) {
                log.error("Error deleting dead tokens in bulk: {}", ex.getMessage());
            }
        }
    }

    private void checkAndDeleteDeadToken(String errorCode, String token) {
        if (DEAD_TOKEN_CODES.contains(errorCode)) {
            log.info("Token is dead or application was uninstalled. Deleting single token: {}", token);
            try {
                clientDeviceDao.deleteByFcmToken(token);
            } catch (Exception ex) {
                log.error("Error deleting single token in DB: {}", ex.getMessage());
            }
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        return IntStream.range(0, (list.size() + size - 1) / size)
                .mapToObj(i -> list.subList(i * size, Math.min((i + 1) * size, list.size()))).toList();
    }
}
