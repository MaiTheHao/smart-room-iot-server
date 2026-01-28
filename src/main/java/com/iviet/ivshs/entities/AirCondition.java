package com.iviet.ivshs.entities;

import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;

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
public class AirCondition extends BaseIoTDevice<AirConditionLan> {

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@Column(name = "power")
	private AcPower power;

	@Column(name = "temperature")
	private Integer temperature;

	@Enumerated(EnumType.STRING)
	@Column(name = "mode")
	private AcMode mode;

	@Column(name = "fan_speed")
	private Integer fanSpeed; // 0: auto, 1-5: speed levels

	@Enumerated(EnumType.STRING)
	@Column(name = "swing")
	private AcSwing swing;
}
