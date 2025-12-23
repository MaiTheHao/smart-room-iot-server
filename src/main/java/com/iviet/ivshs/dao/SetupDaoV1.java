package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.dto.SetupResponseV1;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.exception.InternalServerErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class SetupDaoV1 {

	private static final int BATCH_SIZE = 50;

	@PersistenceContext
	protected EntityManager entityManager;

	@Transactional(rollbackFor = Exception.class)
	public SetupResponseV1 setup(SetupRequestV1 req, Long clientId) {
		int processedDevices = 0;
		String currentDeviceName = null;
		List<SetupResponseV1.CreatedDevice> createdDevices = new ArrayList<>();

		log.debug("[SETUP_START] clientId={}, roomId={}, totalDevices={}", 
			clientId, req.getRoomId(), req.getDevices() != null ? req.getDevices().size() : 0);

		try {
			ClientV1 client = entityManager.getReference(ClientV1.class, clientId);
			RoomV1 room = entityManager.getReference(RoomV1.class, req.getRoomId());
			log.debug("[SETUP_REFS_LOADED] Client and Room references loaded");

			if (req.getDevices() != null && !req.getDevices().isEmpty()) {
				for (int i = 0; i < req.getDevices().size(); i++) {
					SetupRequestV1.DeviceConfig device = req.getDevices().get(i);
					currentDeviceName = device.getName();
					log.debug("[DEVICE_PROCESSING] index={}, name={}, category={}, controlType={}", 
						i, currentDeviceName, device.getCategory(), device.getControlType());

					createdDevices.add(processDevice(device, client, room, i));
					processedDevices++;
					log.debug("[DEVICE_CREATED] name={}, id={}", currentDeviceName, createdDevices.get(i).getDeviceControlId());

					if (processedDevices % BATCH_SIZE == 0) {
						log.debug("[BATCH_FLUSH] Flushing batch at index={}", processedDevices);
						entityManager.flush();
						entityManager.clear();
						client = entityManager.getReference(ClientV1.class, clientId);
						room = entityManager.getReference(RoomV1.class, req.getRoomId());
					}
				}
			}

			entityManager.flush();
			log.info("[SETUP_SUCCESS] clientId={}, roomId={}, createdDevices={}", 
				clientId, req.getRoomId(), processedDevices);
			return buildResponse(req, createdDevices);

		} catch (jakarta.persistence.EntityNotFoundException e) {
			log.error("[SETUP_ERR_001] Entity not found | clientId={} | roomId={} | cause={}", 
				clientId, req.getRoomId(), e.getMessage(), e);
			throw new InternalServerErrorException(String.format(
				"[SETUP_ERR_001] Entity not found | clientId=%d | roomId=%d | cause=%s",
				clientId, req.getRoomId(), e.getMessage()), e);
		} catch (jakarta.persistence.PersistenceException e) {
			log.error("[SETUP_ERR_002] DB constraint violation | clientId={} | roomId={} | device={} | processed={} | cause={}", 
				clientId, req.getRoomId(), currentDeviceName, processedDevices, e.getMessage(), e);
			throw new InternalServerErrorException(String.format(
				"[SETUP_ERR_002] DB constraint violation | clientId=%d | roomId=%d | device=%s | processed=%d | cause=%s",
				clientId, req.getRoomId(), currentDeviceName, processedDevices, e.getMessage()), e);
		} catch (Exception e) {
			log.error("[SETUP_ERR_003] Unexpected failure | clientId={} | roomId={} | device={} | processed={} | cause={}", 
				clientId, req.getRoomId(), currentDeviceName, processedDevices, e.getMessage(), e);
			throw new InternalServerErrorException(String.format(
				"[SETUP_ERR_003] Unexpected failure | clientId=%d | roomId=%d | device=%s | processed=%d | cause=%s",
				clientId, req.getRoomId(), currentDeviceName, processedDevices, e.getMessage()), e);
		}
	}

	private SetupResponseV1.CreatedDevice processDevice(
		SetupRequestV1.DeviceConfig device, ClientV1 client, RoomV1 room, int position) {
		
		log.debug("[PROCESS_DEVICE_START] name={}, position={}", device.getName(), position);
		DeviceControlV1 deviceControl = createAndPersistDeviceControl(device, room, client);
		Long targetId = createSensorEntity(device, room, deviceControl);
		log.debug("[PROCESS_DEVICE_END] name={}, deviceControlId={}, targetId={}", 
			device.getName(), deviceControl.getId(), targetId);

		return SetupResponseV1.CreatedDevice.builder()
			.deviceControlId(deviceControl.getId())
			.category(device.getCategory())
			.targetId(targetId)
			.name(device.getName())
			.controlType(device.getControlType())
			.isActive(device.isActive())
			.position(position)
			.build();
	}

	private DeviceControlV1 createAndPersistDeviceControl(
		SetupRequestV1.DeviceConfig device, RoomV1 room, ClientV1 client) {
		
		log.debug("[CREATE_DEVICE_CONTROL] name={}, gpioPin={}, controlType={}", 
			device.getName(), device.getGpioPin(), device.getControlType());
		
		DeviceControlV1 deviceControl = new DeviceControlV1();
		deviceControl.setDeviceControlType(device.getControlType());
		deviceControl.setGpioPin(device.getGpioPin());
		deviceControl.setBleMacAddress(device.getBleMac());
		deviceControl.setApiEndpoint(device.getApiEndpoint());
		deviceControl.setClient(client);
		deviceControl.setRoom(room);
		
		entityManager.persist(deviceControl);
		entityManager.flush();
		log.debug("[DEVICE_CONTROL_PERSISTED] id={}", deviceControl.getId());
		return deviceControl;
	}

	private Long createSensorEntity(SetupRequestV1.DeviceConfig device, RoomV1 room, DeviceControlV1 deviceControl) {
		log.debug("[CREATE_SENSOR] category={}, name={}", device.getCategory(), device.getName());
		return switch (device.getCategory()) {
			case LIGHT -> createLight(device, room, deviceControl);
			case TEMPERATURE -> createTemperature(device, room, deviceControl);
			case POWER_CONSUMPTION -> createPowerConsumption(device, room, deviceControl);
		};
	}

	private Long createLight(SetupRequestV1.DeviceConfig device, RoomV1 room, DeviceControlV1 deviceControl) {
		log.debug("[CREATE_LIGHT] name={}", device.getName());
		LightV1 light = new LightV1();
		light.setIsActive(device.isActive());
		light.setRoom(room);
		light.setDeviceControl(deviceControl);
		return persistAndGetId(light);
	}

	private Long createTemperature(SetupRequestV1.DeviceConfig device, RoomV1 room, DeviceControlV1 deviceControl) {
		log.debug("[CREATE_TEMPERATURE] name={}", device.getName());
		TemperatureV1 temperature = new TemperatureV1();
		temperature.setIsActive(device.isActive());
		temperature.setNaturalId(device.getName());
		temperature.setRoom(room);
		temperature.setDeviceControl(deviceControl);
		return persistAndGetId(temperature);
	}

	private Long createPowerConsumption(SetupRequestV1.DeviceConfig device, RoomV1 room, DeviceControlV1 deviceControl) {
		log.debug("[CREATE_POWER_CONSUMPTION] name={}", device.getName());
		PowerConsumptionV1 powerConsumption = new PowerConsumptionV1();
		powerConsumption.setIsActive(device.isActive());
		powerConsumption.setNaturalId(device.getName());
		powerConsumption.setRoom(room);
		powerConsumption.setDeviceControl(deviceControl);
		return persistAndGetId(powerConsumption);
	}

	private <T> Long persistAndGetId(T entity) {
		log.debug("[PERSIST_ENTITY] type={}", entity.getClass().getSimpleName());
		entityManager.persist(entity);
		entityManager.flush();
		Long id = (Long) entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		log.debug("[ENTITY_PERSISTED] type={}, id={}", entity.getClass().getSimpleName(), id);
		return id;
	}

	private SetupResponseV1 buildResponse(SetupRequestV1 req, List<SetupResponseV1.CreatedDevice> createdDevices) {
		log.debug("[BUILD_RESPONSE] roomId={}, deviceCount={}", req.getRoomId(), createdDevices.size());
		return SetupResponseV1.builder()
			.roomId(req.getRoomId())
			.createdDevices(createdDevices)
			.build();
	}
}
