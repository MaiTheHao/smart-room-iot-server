package com.iviet.ivshs.entities;

import java.util.Date;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.iviet.ivshs.enumeration.ClientTypeV1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_v1",
    indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientV1 extends BaseAuditEntityV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "client_type", length = 50)
    @Enumerated(EnumType.STRING)
    private ClientTypeV1 clientType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 100)
    private String macAddress;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login_at")
    private Date lastLoginAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DeviceControlV1> deviceControls;
}