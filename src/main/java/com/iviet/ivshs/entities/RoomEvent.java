package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseEntity;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "room_event",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_room_event_code",
          columnNames = {"code"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEvent extends BaseEntity {

  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(name = "code", nullable = false, length = 100)
  private RoomEventCode code;

  @Column(name = "description", length = 255)
  private String description;
}
