package com.iviet.ivshs.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.iviet.ivshs.dao.DeviceControlDaoV1;
import com.iviet.ivshs.dao.LanguageDaoV1;
import com.iviet.ivshs.dao.PowerConsumptionDaoV1;
import com.iviet.ivshs.dao.PowerConsumptionValueDaoV1;
import com.iviet.ivshs.dao.RoomDaoV1;
import com.iviet.ivshs.dto.AveragePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.HealthCheckRequestDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.PowerConsumptionDtoV1;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDtoV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.entities.PowerConsumptionLanV1;
import com.iviet.ivshs.entities.PowerConsumptionV1;
import com.iviet.ivshs.entities.PowerConsumptionValueV1;
import com.iviet.ivshs.entities.RoomV1;
import com.iviet.ivshs.enumeration.GatewayCommandV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.PowerConsumptionMapperV1;
import com.iviet.ivshs.mapper.PowerConsumptionValueMapperV1;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import com.iviet.ivshs.service.PowerConsumptionServiceV1;

@Service
public class PowerConsumptionServiceImplV1 implements PowerConsumptionServiceV1 {

	@Autowired
	private PowerConsumptionDaoV1 powerConsumptionDao;

	@Autowired
	private PowerConsumptionValueDaoV1 powerConsumptionValueDao;

	@Autowired
	private LanguageDaoV1 languageDao;

	@Autowired
	private RoomDaoV1 RoomDaoV1;

	@Autowired
	private DeviceControlDaoV1 deviceControlDao;

	@Autowired
	private PowerConsumptionMapperV1 powerConsumptionMapper;

	@Autowired
	private PowerConsumptionValueMapperV1 powerConsumptionValueMapper;

	@Autowired
	private HealthCheckServiceV1 healthCheckService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public PaginatedResponseV1<PowerConsumptionDtoV1> getListByRoom(Long roomId, int page, int size) {
		if (roomId == null) {
			throw new BadRequestException("Room ID is required");
		}

		List<PowerConsumptionDtoV1> data = powerConsumptionDao.findAllByRoomId(roomId, page, size,
			LocaleContextHolder.getLocale().getLanguage());
		Long totalElements = powerConsumptionDao.countByRoomId(roomId);

		return new PaginatedResponseV1<>(data, page, size, totalElements);
	}

	@Override
	@Transactional(readOnly = true)
	public PowerConsumptionDtoV1 getById(Long powerSensorId) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}

		String langCode = LocaleContextHolder.getLocale().getLanguage();
		PowerConsumptionDtoV1 dto = powerConsumptionDao.findById(powerSensorId, langCode);
		if (dto == null) {
			throw new NotFoundException("Power sensor not found");
		}

		return dto;
	}

	@Override
	@Transactional
	public PowerConsumptionDtoV1 create(CreatePowerConsumptionDtoV1 dto) {
		if (dto == null) {
			throw new BadRequestException("Power consumption data is required");
		}
		if (dto.getRoomId() == null) {
			throw new BadRequestException("Room ID is required");
		}
		if (dto.getDeviceControlId() == null) {
			throw new BadRequestException("Device Control ID is required");
		}

		RoomV1 room = RoomDaoV1.findById(dto.getRoomId()).orElseThrow(() -> new NotFoundException("Room not found"));

		DeviceControlV1 deviceControl = deviceControlDao.findById(dto.getDeviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found, cannot create power sensor"));
		String langCode = dto.getLangCode() == null ?
			LocaleContextHolder.getLocale().getLanguage() : dto.getLangCode();
		if (!languageDao.existsByCode(langCode)) {
			throw new BadRequestException("Language with code " + langCode + " not found");
		}

		PowerConsumptionV1 powerConsumption = powerConsumptionMapper.fromCreateDto(dto);
		powerConsumption.setRoom(room);
		powerConsumption.setDeviceControl(deviceControl);

		PowerConsumptionLanV1 sensorLan = new PowerConsumptionLanV1();
		sensorLan.setLangCode(langCode);
		sensorLan.setName(dto.getName());
		sensorLan.setDescription(dto.getDescription());
		sensorLan.setSensor(powerConsumption);

		powerConsumption.getSensorLans().add(sensorLan);

		powerConsumptionDao.save(powerConsumption);

		return powerConsumptionDao.findById(powerConsumption.getId(), langCode);
	}

	@Override
	@Transactional
	public PowerConsumptionDtoV1 update(Long powerSensorId, UpdatePowerConsumptionDtoV1 dto) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}
		if (dto == null) {
			throw new BadRequestException("Update data is required");
		}

		PowerConsumptionV1 powerConsumption = powerConsumptionDao.findById(powerSensorId).orElseThrow(() -> new NotFoundException("Power sensor not found"));

		String langCode = dto.getLangCode() == null ?
			LocaleContextHolder.getLocale().getLanguage() : dto.getLangCode();
		if (!languageDao.existsByCode(langCode)) {
			throw new BadRequestException("Language with code " + langCode + " not found");
		}

		PowerConsumptionLanV1 sensorLan = powerConsumption.getSensorLans().stream()
				.filter(lan -> lan.getLangCode().equals(langCode))
				.findFirst()
				.orElse(null);

		if (sensorLan == null) {
			sensorLan = new PowerConsumptionLanV1();
			sensorLan.setLangCode(langCode);
			sensorLan.setSensor(powerConsumption);
			powerConsumption.getSensorLans().add(sensorLan);
		}

		if (dto.getName() != null) {
			sensorLan.setName(dto.getName());
		}

		if (dto.getDescription() != null) {
			sensorLan.setDescription(dto.getDescription());
		}

		if (dto.getIsActive() != null) {
			powerConsumption.setIsActive(dto.getIsActive());
		}

		if (dto.getNaturalId() != null) {
			powerConsumption.setNaturalId(dto.getNaturalId());
		}

		if (dto.getDeviceControlId() != null) {
			DeviceControlV1 deviceControl = deviceControlDao.findById(dto.getDeviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found"));
			powerConsumption.setDeviceControl(deviceControl);
		}

		powerConsumptionDao.update(powerConsumption);

		return powerConsumptionDao.findById(powerSensorId,
			LocaleContextHolder.getLocale().getLanguage());
	}

	@Override
	@Transactional
	public void delete(Long powerSensorId) {
		if (powerSensorId == null) {
			throw new BadRequestException("Power sensor ID is required");
		}

		PowerConsumptionV1 powerConsumption = powerConsumptionDao.findById(powerSensorId).orElseThrow(() -> new NotFoundException("Power sensor not found"));

		powerConsumptionDao.delete(powerConsumption);
	}

	@Override
	@Transactional
	public void ingestSensorData(Long sensorId, CreatePowerConsumptionValueDtoV1 dto) {
		if (sensorId == null) {
			throw new BadRequestException("Sensor ID is required");
		}
		if (dto == null) {
			throw new BadRequestException("Power consumption data is required");
		}

		PowerConsumptionV1 sensor = powerConsumptionDao.findById(sensorId).orElseThrow(() -> new NotFoundException("Sensor not found"));

		PowerConsumptionValueV1 powerConsumptionValue = powerConsumptionValueMapper.fromCreateDto(dto);
		powerConsumptionValue.setSensor(sensor);

		powerConsumptionValueDao.save(powerConsumptionValue);
		sensor.setCurrentWatt(dto.getWatt());
		sensor.setCurrentWattHour(dto.getWattHour());
		powerConsumptionDao.update(sensor);
	}

	@Override
	@Transactional
	public void ingestSensorDataBatch(Long sensorId, List<CreatePowerConsumptionValueDtoV1> dtos) {
		if (sensorId == null) {
			throw new BadRequestException("Sensor ID is required");
		}
		if (dtos == null || dtos.isEmpty()) {
			throw new BadRequestException("Power consumption data list is required");
		}

		PowerConsumptionV1 sensor = powerConsumptionDao.findById(sensorId).orElseThrow(() -> new NotFoundException("Sensor not found"));

		List<PowerConsumptionValueV1> valuesToSave = new ArrayList<>();

		for (CreatePowerConsumptionValueDtoV1 dto : dtos) {
			PowerConsumptionValueV1 powerConsumptionValue = powerConsumptionValueMapper.fromCreateDto(dto);
			powerConsumptionValue.setSensor(sensor);
			valuesToSave.add(powerConsumptionValue);
			sensor.setCurrentWatt(dto.getWatt());
			sensor.setCurrentWattHour(dto.getWattHour());
		}

		powerConsumptionValueDao.saveAll(valuesToSave);
		powerConsumptionDao.update(sensor);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AveragePowerConsumptionValueDtoV1> getAverageValueHistoryByRoomId(Long roomId, Instant startedAt,
			Instant endedAt) {
		if (roomId == null) {
			throw new BadRequestException("Room ID is required");
		}
		if (startedAt == null || endedAt == null) {
			throw new BadRequestException("Date range is required");
		}
		if (startedAt.isAfter(endedAt)) {
			throw new BadRequestException("Start date must be before end date");
		}

		try {
			return powerConsumptionValueDao.getAverageHistoryByRoom(roomId, startedAt, endedAt);
		} catch (Exception e) {
			System.err.println("Error fetching average power consumption history by room: " + e.getMessage());
			e.printStackTrace();
			return new java.util.ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<SumPowerConsumptionValueDtoV1> getSumValueHistoryByRoomId(Long roomId, Instant startedAt,
			Instant endedAt) {
		if (roomId == null) {
			throw new BadRequestException("Room ID is required");
		}
		if (startedAt == null || endedAt == null) {
			throw new BadRequestException("Date range is required");
		}
		if (startedAt.isAfter(endedAt)) {
			throw new BadRequestException("Start date must be before end date");
		}

		try {
			return powerConsumptionValueDao.getSumHistoryByRoom(roomId, startedAt, endedAt);
		} catch (Exception e) {
			System.err.println("Error fetching sum power consumption history by room: " + e.getMessage());
			e.printStackTrace();
			return new java.util.ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<AveragePowerConsumptionValueDtoV1> getAverageValueHistoryByClientId(Long clientId, Instant startedAt,
			Instant endedAt) {
		if (clientId == null) {
			throw new BadRequestException("Client ID is required");
		}
		if (startedAt == null || endedAt == null) {
			throw new BadRequestException("Date range is required");
		}
		if (startedAt.isAfter(endedAt)) {
			throw new BadRequestException("Start date must be before end date");
		}

		try {
			return powerConsumptionValueDao.getAverageHistoryByClient(clientId, startedAt, endedAt);
		} catch (Exception e) {
			System.err.println("Error fetching average power consumption history by client: " + e.getMessage());
			e.printStackTrace();
			return new java.util.ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<SumPowerConsumptionValueDtoV1> getSumValueHistoryByClientId(Long clientId, Instant startedAt,
			Instant endedAt) {
		if (clientId == null) {
			throw new BadRequestException("Client ID is required");
		}
		if (startedAt == null || endedAt == null) {
			throw new BadRequestException("Date range is required");
		}
		if (startedAt.isAfter(endedAt)) {
			throw new BadRequestException("Start date must be before end date");
		}

		try {
			return powerConsumptionValueDao.getSumHistoryByClient(clientId, startedAt, endedAt);
		} catch (Exception e) {
			System.err.println("Error fetching sum power consumption history by client: " + e.getMessage());
			e.printStackTrace();
			return new java.util.ArrayList<>();
		}
	}

	@Override
	@Transactional
	public int cleanupDataByRange(Long sensorId, Instant startedAt, Instant endedAt) {
		if (sensorId == null) {
			throw new BadRequestException("Sensor ID is required");
		}
		if (startedAt == null || endedAt == null) {
			throw new BadRequestException("Date range is required");
		}

		return powerConsumptionValueDao.deleteByTimestampBetween(startedAt, endedAt);
	}

	@Override
	public HealthCheckResponseDtoV1 healthCheck(Long sensorId) {
		TransactionTemplate tm = new TransactionTemplate(transactionManager);
		HealthCheckRequestDtoV1 reqDto = tm.execute(status -> prepareHealthCheckRequest(sensorId));
		return healthCheckService.check(reqDto);
	}

	private HealthCheckRequestDtoV1 prepareHealthCheckRequest(Long sensorId) {
		if (sensorId == null) throw new BadRequestException("Sensor ID is required");

		PowerConsumptionV1 sensor = powerConsumptionDao.findById(sensorId).orElseThrow(() -> new NotFoundException("Sensor not found"));

		DeviceControlV1 deviceControl = sensor.getDeviceControl();
		if (deviceControl == null) throw new NotFoundException("Device Control not found for sensor");

		return HealthCheckRequestDtoV1.builder()
			.deviceControlType(deviceControl.getDeviceControlType())
			.clientId(deviceControl.getClient().getId())
			.clientIpAddress(deviceControl.getClient().getIpAddress())
			.gpioPin(deviceControl.getGpioPin())
			.command(GatewayCommandV1.HEALTH_CHECK.toString())
			.build();
	}
}
