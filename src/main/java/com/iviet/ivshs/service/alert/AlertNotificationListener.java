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
        AlertConfig alertConfig = event.getConfig();
        AlertInstance alertInstance = event.getAlert();

        try {
            List<SysGroup> recipientGroups = alertInstanceDao.findGroupsByAlertId(alertInstance.getId());
            Set<Client> recipientsWithDevices = loadDevicesForGroups(recipientGroups);
            List<NotificationChannel> channels = parseChannels(alertConfig.getChannels());
            Long alertConfigId = alertConfig.getId();
            Long alertInstanceId = alertInstance.getId();
            Map<String, String> data = buildFcmData(event.getActionType().name(), alertConfigId, alertInstanceId, alertInstance.getSeverity().name());

            String bodyMessage = (event.getActionType() == AlertActionType.RE_TRIGGERED && event.getLogMessage() != null)
                    ? event.getLogMessage()
                    : alertInstance.getBody();

            NotificationRequest request = NotificationRequest.builder().recipients(recipientsWithDevices)
                    .channels(channels).title(alertConfig.getAlertName()).body(bodyMessage).data(data).build();

            notificationService.sendNotification(request);
            log.info("Successfully sent async notification for alert ID: {}", alertInstance.getId());
        } catch (Exception e) {
            log.error("Failed to send FCM notification for alert ID {}: {}", alertInstance.getId(), e.getMessage(), e);
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

    private Map<String, String> buildFcmData(String actionType, Long alertConfigId, Long alertInstanceId, String severity) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "ALERT");
        data.put("status", actionType);
        data.put("entityId", String.valueOf(alertInstanceId));
        data.put("severity", severity);
        data.put("deepLink", String.format("smartroom://alert_detail/%d/%d", alertConfigId, alertInstanceId));
        data.put("timestamp", Instant.now().toString());
        return data;
    }

    private List<NotificationChannel> parseChannels(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) return List.of();
        return StreamSupport.stream(jsonNode.spliterator(), false).map(JsonNode::asText).filter(text -> !text.isBlank())
                .map(NotificationChannel::fromValue).toList();
    }
}
