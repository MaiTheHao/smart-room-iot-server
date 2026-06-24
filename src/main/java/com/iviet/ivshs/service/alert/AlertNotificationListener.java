package com.iviet.ivshs.service.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.notification.NotificationRequest;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.service.alert.event.AlertNotificationEvent;
import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNotificationListener {

    private final AlertInstanceDao alertInstanceDao;
    private final SysGroupDao sysGroupDao;
    private final ClientDao clientDao;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlertNotificationEvent(AlertNotificationEvent event) {
        AlertConfig config = event.getConfig();
        AlertInstance alert = event.getAlert();

        try {
            List<SysGroup> recipientGroups = alertInstanceDao.findGroupsByAlertId(alert.getId());
            Set<Client> recipientsWithDevices = loadDevicesForGroups(recipientGroups);
            List<NotificationChannel> channels = parseChannels(config.getChannels());
            Map<String, String> data = buildFcmData(event.getActionType().name(), alert);

            String bodyMessage = (event.getActionType() == AlertActionType.RE_TRIGGERED && event.getLogMessage() != null)
                    ? event.getLogMessage()
                    : alert.getBody();

            NotificationRequest request = NotificationRequest.builder().recipients(recipientsWithDevices)
                    .channels(channels).title(config.getAlertName()).body(bodyMessage).data(data).build();

            notificationService.sendNotification(request);
            log.info("Successfully sent async notification for alert ID: {}", alert.getId());
        } catch (Exception e) {
            log.error("Failed to send FCM notification for alert ID {}: {}", alert.getId(), e.getMessage(), e);
        }
    }

    private Set<Client> loadDevicesForGroups(List<SysGroup> groups) {
        if (groups == null || groups.isEmpty()) return Set.of();
        Set<Long> groupIds = groups.stream().map(SysGroup::getId).collect(Collectors.toUnmodifiableSet());
        List<Client> clients = sysGroupDao.findClientEntitiesByGroupIds(groupIds);
        if (clients.isEmpty()) return Set.of();
        Set<Long> clientIds = clients.stream().map(Client::getId).collect(Collectors.toUnmodifiableSet());
        return clientDao.findAllWithDevicesByIdIn(clientIds);
    }

    private Map<String, String> buildFcmData(String type, AlertInstance alert) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("entityId", String.valueOf(alert.getId()));
        data.put("severity", alert.getSeverity().name());
        data.put("deepLink", "smartroom://alert/" + alert.getId());
        data.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        return data;
    }

    private List<NotificationChannel> parseChannels(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) return List.of();
        return StreamSupport.stream(jsonNode.spliterator(), false).map(JsonNode::asText).filter(text -> !text.isBlank())
                .map(NotificationChannel::fromValue).toList();
    }
}
