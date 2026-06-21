package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.id.AlertConfigGroupId;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join table: alert_config ↔ sys_group. Xác định các nhóm nào sẽ nhận thông báo khi config này được kích hoạt.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alert_config_group")
@IdClass(AlertConfigGroupId.class)
public class AlertConfigGroup {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_config_id", nullable = false)
    private AlertConfig alertConfig;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SysGroup group;
}
