package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "air_condition",
	indexes = {
		@Index(name = "idx_air_condition_room_id", columnList = "room_id", unique = false),
		@Index(name = "idx_air_condition_natural_id", columnList = "natural_id", unique = true)
	}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AirCondition extends BaseIoTDevice<AirConditionLan>{
	public static final int MIN_TEMP = 16;
	public static final int MAX_TEMP = 32;
	public static final int MIN_FAN_SPEED = 0;
	public static final int MAX_FAN_SPEED = 5;

	public static final HashSet<ActuatorMode> SUPPORTED_MODES = new HashSet<>(Set.of(
		ActuatorMode.COOL,
		ActuatorMode.HEAT,
		ActuatorMode.DRY,
		ActuatorMode.FAN,
		ActuatorMode.AUTO
	));

	public static final HashSet<ActuatorSwing> SUPPORTED_SWINGS = new HashSet<>(Set.of(
		ActuatorSwing.ON,
		ActuatorSwing.OFF
	));

	@Column(name = "temperature")
	private Integer temperature;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "mode", length = 256)
	private ActuatorMode mode;

	@Column(name = "fan_speed")
	private Integer fanSpeed;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "swing", length = 256)
	private ActuatorSwing swing;

	@Override
	public void addTranslation(AirConditionLan translation) {
		translation.setOwner(this);
		this.getTranslations().add(translation);
	}

	public void setMode(ActuatorMode mode) {
		if (mode != null && !SUPPORTED_MODES.contains(mode)) {
			throw new IllegalArgumentException("Unsupported mode: " + mode + ". Supported modes are: " + SUPPORTED_MODES);
		}
		this.mode = mode;
	}

	public void setSwing(ActuatorSwing swing) {
		if (swing != null && !SUPPORTED_SWINGS.contains(swing)) {
			throw new IllegalArgumentException("Unsupported swing state: " + swing + ". Supported swing states are: " + SUPPORTED_SWINGS);
		}
		this.swing = swing;
	}

	public void setFanSpeed(Integer fanSpeed) {
		if (fanSpeed != null && (fanSpeed < MIN_FAN_SPEED || fanSpeed > MAX_FAN_SPEED)) {
			throw new IllegalArgumentException("Fan speed must be between " + MIN_FAN_SPEED + " and " + MAX_FAN_SPEED);
		}
		this.fanSpeed = fanSpeed;
	}

	public void setTemperature(Integer temperature) {
		if (temperature != null && (temperature < MIN_TEMP || temperature > MAX_TEMP)) {
			throw new IllegalArgumentException("Temperature must be between " + MIN_TEMP + " and " + MAX_TEMP);
		}
		this.temperature = temperature;
	}
}
