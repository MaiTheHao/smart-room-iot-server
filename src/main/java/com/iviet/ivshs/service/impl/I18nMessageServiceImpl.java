package com.iviet.ivshs.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.service.I18nMessageService;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class I18nMessageServiceImpl implements I18nMessageService{

	private final MessageSource messageSource;

	@Override
	public String getMessage(String key, Object... args) {
		String message = messageSource.getMessage(
				key, 
				args, 
				key,
				LocalContextUtil.getCurrentLocale()
		);
		return message != null ? message : key;
	}
	
}
