package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "mode", length = 256)
	private ActuatorMode mode;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "swing", length = 256)
	private ActuatorSwing swing;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "light", length = 256)
	private ActuatorState light;

	@Column(name = "speed")
	private Integer speed;
}
