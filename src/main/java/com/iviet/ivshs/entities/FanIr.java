package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("IR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FanIr extends Fan {

	public static final Integer MIN_SPEED = 0;
	public static final Integer MAX_SPEED = 9999;
	
	public static final HashSet<ActuatorMode> SUPPORTED_MODES = new HashSet<>(Set.of(
		ActuatorMode.NATURAL,
		ActuatorMode.SLEEP,
		ActuatorMode.NORMAL
	));

	public static final HashSet<ActuatorSwing> SUPPORTED_SWINGS = new HashSet<>(Set.of(
		ActuatorSwing.ON,
		ActuatorSwing.OFF
	));

	@Enumerated(EnumType.STRING)
	@Column(name = "mode")
	private ActuatorMode mode;

	@Enumerated(EnumType.STRING)
	@Column(name = "swing")
	private ActuatorSwing swing;

	@Enumerated(EnumType.STRING)
	@Column(name = "light")
	private ActuatorState light;

	@Column(name = "speed")
	private Integer speed;
}
