package com.iviet.ivshs.service.language;

import org.springframework.lang.NonNull;

public interface I18nMessageService {
	String getMessage(@NonNull String key, Object... args);
}
