package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class AirCondition extends BaseIoTActuator<AirConditionLan>{
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

	@Enumerated(EnumType.STRING)
	@Column(name = "mode")
	private ActuatorMode mode;

	@Column(name = "fan_speed")
	private Integer fanSpeed;

	@Enumerated(EnumType.STRING)
	@Column(name = "swing")
	private ActuatorSwing swing;

	@Override
	public void addTranslation(AirConditionLan translation) {
		translation.setOwner(this);
		this.getTranslations().add(translation);
	}
}
