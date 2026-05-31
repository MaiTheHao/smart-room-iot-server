package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fan", indexes = {
		@Index(name = "idx_fan_room_id", columnList = "room_id", unique = false),
		@Index(name = "idx_fan_natural_id", columnList = "natural_id", unique = true)
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "specific_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Fan extends BaseIoTDevice<FanLan> {

	public static final Integer MIN_SPEED = 0;
	public static final Integer MAX_SPEED = 5;

	@Column(name = "speed")
	private Integer speed;

	public void setSpeed(Integer speed) {
		if (speed != null && (speed < MIN_SPEED || speed > MAX_SPEED)) {
			throw new IllegalArgumentException("Speed must be between " + MIN_SPEED + " and " + MAX_SPEED);
		}
		this.speed = speed;
	}

}
