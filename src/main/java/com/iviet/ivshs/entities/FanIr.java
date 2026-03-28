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
}
