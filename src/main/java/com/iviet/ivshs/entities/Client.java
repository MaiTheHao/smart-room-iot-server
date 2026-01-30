package com.iviet.ivshs.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.iviet.ivshs.enumeration.ClientType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client",
    indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_client_type_client_ip_address", columnList = "client_type, ip_address", unique = true),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseAuditEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "client_type", length = 50)
    @Enumerated(EnumType.STRING)
    private ClientType clientType;

    @Column(name = "ip_address", length = 255)
    private String ipAddress;

    @Column(name = "mac_address", length = 100)
    private String macAddress;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login_at")
    private Date lastLoginAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DeviceControl> deviceControls = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "client_group",
        joinColumns = @JoinColumn(name = "client_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "group_id", nullable = false),
        indexes = {
            @Index(name = "idx_client_group", columnList = "client_id, group_id", unique = true)
        }
    )
    private Set<SysGroup> groups = new HashSet<>();
}