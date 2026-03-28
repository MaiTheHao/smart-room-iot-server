package com.iviet.ivshs.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("GPIO")
@Getter
@Setter
public class FanGpio extends Fan {
}
