package com.iviet.ivshs.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlDeviceResponseV1 {
	private String status;   
	private String message;  
	private String error;    
}
