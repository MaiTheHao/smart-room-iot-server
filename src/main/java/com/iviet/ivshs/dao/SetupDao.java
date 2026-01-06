package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.entities.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public class SetupDao extends BaseDao<SetupDao> {

	private static final int BATCH_SIZE = 50;

	public SetupDao() {
		super(SetupDao.class);
	}

	public int persistDeviceSetup(java.util.List<SetupRequestV1.DeviceConfig> devices, Long clientId, Long roomId) {
		if (devices == null || devices.isEmpty()) {
			return 0;
		}

		log.info("[SETUP] Starting device setup: total={}, roomId={}, clientId={}", 
			devices.size(), roomId, clientId);

		int processedDevices = 0;
		for (int i = 0; i < devices.size(); i++) {
			SetupRequestV1.DeviceConfig device = devices.get(i);
			
			if (log.isDebugEnabled()) {
				log.debug("[SETUP:DEVICE] index={}, name={}, category={}", 
					i, device.getName(), device.getCategory());
			}

			Client client = entityManager.getReference(Client.class, clientId);
			Room room = entityManager.getReference(Room.class, roomId);
			
			persistDevice(device, client, room);
			processedDevices++;

			if (processedDevices % BATCH_SIZE == 0) {
				log.info("[SETUP:BATCH] Flushed batch: processed={}/{}", processedDevices, devices.size());
				entityManager.flush();
				entityManager.clear();
			}
		}

		entityManager.flush();
		log.info("[SETUP] Completed device setup: total={}, processed={}, roomId={}, clientId={}", 
			devices.size(), processedDevices, roomId, clientId);
		return processedDevices;
	}

	private void persistDevice(SetupRequestV1.DeviceConfig device, Client client, Room room) {
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:PERSIST] name={}, category={}", device.getName(), device.getCategory());
		}
		
		DeviceControl deviceControl = createAndPersistDeviceControl(device, room, client);
		createSensorEntity(device, room, deviceControl, device.getTranslations());
	}

	private DeviceControl createAndPersistDeviceControl(
		SetupRequestV1.DeviceConfig device, Room room, Client client) {
		
		DeviceControl deviceControl = new DeviceControl();
		deviceControl.setDeviceControlType(device.getControlType());
		deviceControl.setGpioPin(device.getGpioPin());
		deviceControl.setBleMacAddress(device.getBleMac());
		deviceControl.setApiEndpoint(device.getApiEndpoint());
		deviceControl.setClient(client);
		deviceControl.setRoom(room);
		
		entityManager.persist(deviceControl);
		entityManager.flush();
		
		if (log.isDebugEnabled()) log.debug("[SETUP:CTRL] deviceControl created: id={}, name={}, type={}", deviceControl.getId(), device.getName(), device.getControlType());
		
		return deviceControl;
	}

	private void createSensorEntity(SetupRequestV1.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequestV1.DeviceConfig.TranslationDetail> translations) {
		switch (device.getCategory()) {
			case LIGHT -> createLight(device, room, deviceControl, translations);
			case TEMPERATURE -> createTemperature(device, room, deviceControl, translations);
			case POWER_CONSUMPTION -> createPowerConsumption(device, room, deviceControl, translations);
		}
	}

	private void createLight(SetupRequestV1.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequestV1.DeviceConfig.TranslationDetail> translations) {
		Light light = new Light();
		light.setIsActive(device.isActive());
		light.setRoom(room);
		light.setDeviceControl(deviceControl);
		light.setNaturalId(device.getNaturalId());
		persistEntity(light);
		persistTranslations(light, translations);
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:LIGHT] created: id={}, naturalId={}, translations={}", 
				light.getId(), light.getNaturalId(), 
				translations != null ? translations.size() : 0);
		}
	}

	private void createTemperature(SetupRequestV1.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequestV1.DeviceConfig.TranslationDetail> translations) {
		Temperature temperature = new Temperature();
		temperature.setIsActive(device.isActive());
		temperature.setNaturalId(device.getNaturalId());
		temperature.setRoom(room);
		temperature.setDeviceControl(deviceControl);
		persistEntity(temperature);
		persistTranslations(temperature, translations);
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:TEMP] created: id={}, naturalId={}, translations={}", 
				temperature.getId(), temperature.getNaturalId(), 
				translations != null ? translations.size() : 0);
		}
	}

	private void createPowerConsumption(SetupRequestV1.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequestV1.DeviceConfig.TranslationDetail> translations) {
		PowerConsumption powerConsumption = new PowerConsumption();
		powerConsumption.setIsActive(device.isActive());
		powerConsumption.setNaturalId(device.getNaturalId());
		powerConsumption.setRoom(room);
		powerConsumption.setDeviceControl(deviceControl);
		persistEntity(powerConsumption);
		persistTranslations(powerConsumption, translations);
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:POWER] created: id={}, naturalId={}, translations={}", 
				powerConsumption.getId(), powerConsumption.getNaturalId(), 
				translations != null ? translations.size() : 0);
		}
	}

	private <T> void persistEntity(T entity) {
		entityManager.persist(entity);
	}

	private void persistTranslations(Object entity, Map<String, SetupRequestV1.DeviceConfig.TranslationDetail> translations) {
		if (translations == null || translations.isEmpty()) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:TRANS_START] entityType={}, count={}", 
				entity.getClass().getSimpleName(), translations.size());
		}

		translations.forEach((langCode, detail) -> {
			if (entity instanceof Light light) {
				createLightTranslation(light, langCode, detail);
			} else if (entity instanceof Temperature temperature) {
				createTemperatureTranslation(temperature, langCode, detail);
			} else if (entity instanceof PowerConsumption powerConsumption) {
				createPowerConsumptionTranslation(powerConsumption, langCode, detail);
			}
		});
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:TRANS_END] entityType={}", entity.getClass().getSimpleName());
		}
	}

	private void createLightTranslation(
		Light light, 
		String langCode, 
		SetupRequestV1.DeviceConfig.TranslationDetail detail) {
		
		LightLan lightLan = new LightLan();
		lightLan.setLangCode(langCode);
		lightLan.setName(detail.getName());
		lightLan.setDescription(detail.getDescription());
		lightLan.setOwner(light);
		
		light.getTranslations().add(lightLan);
		entityManager.persist(lightLan);
	}

	private void createTemperatureTranslation(
		Temperature temperature, 
		String langCode, 
		SetupRequestV1.DeviceConfig.TranslationDetail detail) {
		
		TemperatureLan temperatureLan = new TemperatureLan();
		temperatureLan.setLangCode(langCode);
		temperatureLan.setName(detail.getName());
		temperatureLan.setDescription(detail.getDescription());
		temperatureLan.setOwner(temperature);
		
		temperature.getTranslations().add(temperatureLan);
		entityManager.persist(temperatureLan);
	}

	private void createPowerConsumptionTranslation(
		PowerConsumption powerConsumption, 
		String langCode, 
		SetupRequestV1.DeviceConfig.TranslationDetail detail) {
		
		PowerConsumptionLan powerConsumptionLan = new PowerConsumptionLan();
		powerConsumptionLan.setLangCode(langCode);
		powerConsumptionLan.setName(detail.getName());
		powerConsumptionLan.setDescription(detail.getDescription());
		powerConsumptionLan.setOwner(powerConsumption);
		
		powerConsumption.getTranslations().add(powerConsumptionLan);
		entityManager.persist(powerConsumptionLan);
	}
}
