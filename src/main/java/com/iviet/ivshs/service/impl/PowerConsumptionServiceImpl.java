package com.iviet.ivshs.service.impl;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreatePowerConsumptionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.PowerConsumptionDto;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDto;
import com.iviet.ivshs.entities.PowerConsumptionLan;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.PowerConsumptionService;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PowerConsumptionServiceImpl implements PowerConsumptionService {

	private final PowerConsumptionDao powerConsumptionDao;
	private final LanguageDao languageDao;
	private final RoomDao roomDao;
	private final DeviceControlDao deviceControlDao;

	@Override
	@Transactional(readOnly = true)
	public PaginatedResponse<PowerConsumptionDto> getListByRoom(Long roomId, int page, int size) {
		if (roomId == null) {
			throw new BadRequestException("Room ID is required");
		}

		List<PowerConsumptionDto> data = powerConsumptionDao.findAllByRoomId(roomId, page, size, LocaleContextHolder.getLocale().getLanguage());
		Long totalElements = powerConsumptionDao.countByRoomId(roomId);

		return new PaginatedResponse<>(data, page, size, totalElements);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResponse<PowerConsumption> getListEntityByRoom(Long roomId, int page, int size) {
		if (roomId == null) throw new BadRequestException("Room ID is required");
		
		List<PowerConsumption> data = powerConsumptionDao.findAllByRoomId(roomId, page, size);
		Long totalElements = powerConsumptionDao.countByRoomId(roomId);

		return new PaginatedResponse<>(data, page, size, totalElements);
	}

	@Override
	@Transactional(readOnly = true)
	public PowerConsumptionDto getById(Long powerSensorId) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}

		String langCode = LocaleContextHolder.getLocale().getLanguage();
		PowerConsumptionDto dto = powerConsumptionDao.findById(powerSensorId, langCode).orElseThrow(
			() -> new NotFoundException("Power sensor not found with ID: " + powerSensorId)
		);

		return dto;
	}

	@Override
	@Transactional(readOnly = true) 
	public PowerConsumption getEntityById(Long powerSensorId) {
		if (powerSensorId == null) throw new BadRequestException("Power sensor ID is required");
		return powerConsumptionDao.findById(powerSensorId).orElseThrow(() -> new NotFoundException("Power sensor not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public PowerConsumptionDto getByNaturalId(String naturalId) {
		if (naturalId.isBlank()) throw new BadRequestException("Natural ID is required");

		String langCode = LocalContextUtil.getCurrentLangCode();
		PowerConsumptionDto dto = powerConsumptionDao.findByNaturalId(naturalId, langCode)
			.orElseThrow(() -> new NotFoundException("Power sensor not found with natural ID: " + naturalId));

		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public PowerConsumption getEntityByNaturalId(String naturalId) {
		if (naturalId.isBlank()) throw new BadRequestException("Natural ID is required");

		return powerConsumptionDao.findByNaturalId(naturalId)
			.orElseThrow(() -> new NotFoundException("Power sensor not found with natural ID: " + naturalId));
	}

	@Override
	@Transactional
	public PowerConsumptionDto create(CreatePowerConsumptionDto dto) {
		if (dto == null) {
			throw new BadRequestException("Power consumption data is required");
		}
		if (dto.roomId() == null) {
			throw new BadRequestException("Room ID is required");
		}
		if (dto.deviceControlId() == null) {
			throw new BadRequestException("Device Control ID is required");
		}

		var room = roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found"));

		var deviceControl = deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found, cannot create power sensor"));
		String langCode = dto.langCode() == null ?
			LocaleContextHolder.getLocale().getLanguage() : dto.langCode();
		if (!languageDao.existsByCode(langCode)) {
			throw new BadRequestException("Language with code " + langCode + " not found");
		}

		var powerConsumption = dto.toEntity();
		powerConsumption.setRoom(room);
		powerConsumption.setDeviceControl(deviceControl);

		var sensorLan = new PowerConsumptionLan();
		sensorLan.setLangCode(langCode);
		sensorLan.setName(dto.name());
		sensorLan.setDescription(dto.description());
		sensorLan.setOwner(powerConsumption);

		powerConsumption.getTranslations().add(sensorLan);

		powerConsumptionDao.save(powerConsumption);

		return powerConsumptionDao.findById(powerConsumption.getId(), langCode).orElseThrow(
			() -> new NotFoundException("Power sensor not found with ID: " + powerConsumption.getId())
		);
	}

	@Override
	@Transactional
	public PowerConsumptionDto update(Long powerSensorId, UpdatePowerConsumptionDto dto) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}
		if (dto == null) {
			throw new BadRequestException("Update data is required");
		}

		var powerConsumption = powerConsumptionDao.findById(powerSensorId).orElseThrow(() -> new NotFoundException("Power sensor not found"));

		String langCode = dto.langCode() == null ?
			LocaleContextHolder.getLocale().getLanguage() : dto.langCode();
		if (!languageDao.existsByCode(langCode)) {
			throw new BadRequestException("Language with code " + langCode + " not found");
		}

		var sensorLan = powerConsumption.getTranslations().stream()
				.filter(lan -> lan.getLangCode().equals(langCode))
				.findFirst()
				.orElse(null);

		if (sensorLan == null) {
			sensorLan = new PowerConsumptionLan();
			sensorLan.setLangCode(langCode);
			sensorLan.setOwner(powerConsumption);
			powerConsumption.getTranslations().add(sensorLan);
		}

		if (dto.name() != null) {
			sensorLan.setName(dto.name());
		}

		if (dto.description() != null) {
			sensorLan.setDescription(dto.description());
		}

		if (dto.isActive() != null) {
			powerConsumption.setIsActive(dto.isActive());
		}

		if (dto.naturalId() != null) {
			powerConsumption.setNaturalId(dto.naturalId());
		}

		if (dto.deviceControlId() != null) {
			var deviceControl = deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found"));
			powerConsumption.setDeviceControl(deviceControl);
		}

		powerConsumptionDao.update(powerConsumption);

		return powerConsumptionDao.findById(powerSensorId,LocaleContextHolder.getLocale().getLanguage()).orElseThrow(
				() -> new NotFoundException("Power sensor not found with ID: " + powerSensorId)
			);
	}

	@Override
	@Transactional
	public void delete(Long powerSensorId) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}

		PowerConsumption powerConsumption = powerConsumptionDao.findById(powerSensorId).orElseThrow(() -> new NotFoundException("Power sensor not found"));
		

		powerConsumptionDao.delete(powerConsumption);
	}
}
