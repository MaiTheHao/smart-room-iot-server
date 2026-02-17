package com.iviet.ivshs.entities;

import com.iviet.ivshs.enumeration.ActuatorPower;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTActuator<L extends BaseTranslation<?>> extends BaseIoTEntity<L> {
  @Enumerated(EnumType.STRING)
	@Column(name = "power")
	private ActuatorPower power;
}
