package com.iviet.ivshs.entities;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.entities.base.BaseEntity;
import com.iviet.ivshs.shared.enumeration.Platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_device", indexes = {
        @Index(name = "idx_fcm_token", columnList = "fcm_token", unique = true),
        @Index(name = "idx_device_identifier", columnList = "device_identifier", unique = true),
        @Index(name = "idx_client_id", columnList = "client_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDevice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @Column(name = "device_identifier", nullable = false, length = 255)
    private String deviceIdentifier;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "platform", length = 50)
    private Platform platform;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;
}
