package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.id.AlertInstanceGroupId;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join table: alert_recipient ↔ sys_group. Xác định nhóm nào có quyền xem và xử lý sự cố alert này. Thay thế hoàn toàn
 * bảng alert_recipient cũ (join client_id).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alert_instance_group")
@IdClass(AlertInstanceGroupId.class)
public class AlertInstanceGroup {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertInstance alert;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SysGroup group;
}
