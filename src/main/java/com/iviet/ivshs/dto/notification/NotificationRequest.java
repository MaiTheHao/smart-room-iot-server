package com.iviet.ivshs.dto.notification;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.shared.enumeration.NotificationChannel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class NotificationRequest {

    @NonNull
    private final Set<Client> recipients;

    @NonNull
    private final List<NotificationChannel> channels;

    @NonNull
    private final String title;

    @NonNull
    private final String body;

    private final Map<String, String> data;
}
