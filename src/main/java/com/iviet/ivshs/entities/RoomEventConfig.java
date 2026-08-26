package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "room_event_config",
    indexes = {
      @Index(name = "idx_room_event_config_room_id", columnList = "room_id"),
      @Index(name = "idx_room_event_config_event_id", columnList = "room_event_id"),
      @Index(name = "idx_room_event_config_is_active", columnList = "is_active")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_room_event_config_room_event",
          columnNames = {"room_id", "room_event_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEventConfig extends BaseAuditEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_event_id", nullable = false)
  private RoomEvent roomEvent;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "cooldown_seconds", nullable = false)
  private Integer cooldownSeconds = 0;

  @Column(name = "last_triggered_at")
  private Instant lastTriggeredAt;
}
