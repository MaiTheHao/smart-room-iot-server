package com.iviet.ivshs.entities.base;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.shared.enumeration.ActuatorPower;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTDevice<L extends BaseTranslation<? extends BaseTranslatableEntity<L>>>
		extends BaseIoTEntity<L> {

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "power", length = 256)
	private ActuatorPower power;
}
