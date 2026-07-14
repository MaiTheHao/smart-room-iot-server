package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alert_config", uniqueConstraints = {
    @UniqueConstraint(
        name = "idx_alert_config_polymorphic",
        columnNames = {"namespace", "alert_code", "source_id"}
    )
})
public class AlertConfig extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "namespace", nullable = false, length = 50)
    private AlertNamespace namespace;

    @Column(name = "alert_code", nullable = false, length = 100)
    private String alertCode;

    @Column(name = "source_id", nullable = false, length = 256)
    private String sourceId;

    @Column(name = "alert_name", nullable = false, length = 256)
    private String alertName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "channels")
    private JsonNode channels;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    @Column(name = "cooldown_minutes", nullable = false)
    @Builder.Default
    private Integer cooldownMinutes = 0;
}
