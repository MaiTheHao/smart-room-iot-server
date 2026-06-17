package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.entities.base.BaseIoTDevice;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fan", indexes = {
        @Index(name = "idx_fan_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_fan_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fan extends BaseIoTDevice<FanLan> {

    public static final Integer MIN_SPEED = 0;
    public static final Integer MAX_SPEED = 5;

    public static final HashSet<ActuatorMode> SUPPORTED_MODES = new HashSet<>(Set.of(ActuatorMode.NATURAL, ActuatorMode.SLEEP, ActuatorMode.NORMAL));

    public static final HashSet<ActuatorSwing> SUPPORTED_SWINGS = new HashSet<>(Set.of(ActuatorSwing.ON, ActuatorSwing.OFF));

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "duration")
    private Integer duration;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "mode", length = 256)
    private ActuatorMode mode;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "swing", length = 256)
    private ActuatorSwing swing;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "light", length = 256)
    private ActuatorPower light;

    public void setSpeed(Integer speed) {
        if (speed != null && (speed < MIN_SPEED || speed > MAX_SPEED)) {
            throw new IllegalArgumentException("Speed must be between " + MIN_SPEED + " and " + MAX_SPEED);
        }
        this.speed = speed;
    }

    public void setMode(ActuatorMode mode) {
        if (mode != null && !SUPPORTED_MODES.contains(mode)) {
            throw new IllegalArgumentException("Unsupported mode: " + mode + ". Supported modes are: " + SUPPORTED_MODES);
        }
        this.mode = mode;
    }

    public void setSwing(ActuatorSwing swing) {
        if (swing != null && !SUPPORTED_SWINGS.contains(swing)) {
            throw new IllegalArgumentException("Unsupported swing: " + swing + ". Supported swings are: " + SUPPORTED_SWINGS);
        }
        this.swing = swing;
    }

    @Override
    public void addTranslation(FanLan translation) {
        translation.setOwner(this);
        this.getTranslations()
                .add(translation);
    }
}
