package com.iviet.ivshs.dto;

import java.util.Map;
import java.util.Optional;

import org.springframework.lang.NonNull;

import lombok.Builder;

@Builder
public record LoginViewModel (
	Optional<String> errorMessage
) {

	@NonNull
	public Map<String, Object> toModelAttributes() {
		return Map.of(
			"errorMessage", errorMessage.orElse("")
		);
	}
}