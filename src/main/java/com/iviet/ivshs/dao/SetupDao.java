package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.ActuatorMode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

// NOTE: Gửi đến dev đến sau, sau này hãy sử dụng Strategy Pattern để clean lại toàn bộ Setup flow
@Slf4j
@Repository
public class SetupDao extends BaseDao<SetupDao> {

	private static final int BATCH_SIZE = 50;

	public SetupDao() {
		super(SetupDao.class);
	}

	public int persistDeviceSetup(java.util.List<SetupRequest.BodyData.DeviceConfig> devices, Long clientId, Long roomId) {
		if (devices == null || devices.isEmpty()) {
			return 0;
		}

		log.info("[SETUP] Starting device setup: total={}, roomId={}, clientId={}", 
			devices.size(), roomId, clientId);

		Client client = entityManager.getReference(Client.class, clientId);
		Room room = entityManager.getReference(Room.class, roomId);

		int processedDevices = 0;
		for (int i = 0; i < devices.size(); i++) {
			SetupRequest.BodyData.DeviceConfig device = devices.get(i);
			if (device.getCategory() == null) {
				log.warn("[SETUP:SKIP] Device category is null, skipping device at index={}", i);
				continue;
			}

			if (log.isDebugEnabled()) {
				log.debug("[SETUP:DEVICE] index={}, name={}, category={}", 
					i, device.getName(), device.getCategory());
			}

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

	private void persistDevice(SetupRequest.BodyData.DeviceConfig device, Client client, Room room) {
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:PERSIST] name={}, category={}", device.getName(), device.getCategory());
		}
		
		DeviceControl deviceControl = createAndPersistDeviceControl(device, room, client);
		buildDeviceEntity(device, room, deviceControl, device.getTranslations());
	}

	private DeviceControl createAndPersistDeviceControl(
		SetupRequest.BodyData.DeviceConfig device, Room room, Client client) {
		
		DeviceControl deviceControl = new DeviceControl();
		deviceControl.setDeviceControlType(device.getControlType());
		deviceControl.setGpioPin(device.getGpioPin());
		deviceControl.setBleMacAddress(device.getBleMac());
		deviceControl.setApiEndpoint(device.getApiEndpoint());
		deviceControl.setClient(client);
		deviceControl.setRoom(room);
		
		entityManager.persist(deviceControl);
		entityManager.flush();
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:CTRL] deviceControl created: id={}, name={}, type={}", 
				deviceControl.getId(), device.getName(), device.getControlType());
		}
		
		return deviceControl;
	}

	private void buildDeviceEntity(SetupRequest.BodyData.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		switch (device.getCategory()) {
			case LIGHT -> createLight(device, room, deviceControl, translations);
			case TEMPERATURE -> createTemperature(device, room, deviceControl, translations);
			case POWER_CONSUMPTION -> createPowerConsumption(device, room, deviceControl, translations);
			case AIR_CONDITION -> createAirCondition(device, room, deviceControl, translations);
		}
	}

	private void createLight(SetupRequest.BodyData.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		Light light = new Light();
		setupBaseIoTProperties(light, device, room, deviceControl);
		attachTranslations(light, translations, LightLan::new);
		
		entityManager.persist(light);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:LIGHT] created: id={}", light.getId());
	}

	private void createTemperature(SetupRequest.BodyData.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		Temperature temperature = new Temperature();
		setupBaseIoTProperties(temperature, device, room, deviceControl);
		attachTranslations(temperature, translations, TemperatureLan::new);
		
		entityManager.persist(temperature);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:TEMP] created: id={}", temperature.getId());
	}

	private void createPowerConsumption(SetupRequest.BodyData.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		PowerConsumption powerConsumption = new PowerConsumption();
		setupBaseIoTProperties(powerConsumption, device, room, deviceControl);
		attachTranslations(powerConsumption, translations, PowerConsumptionLan::new);
		
		entityManager.persist(powerConsumption);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:POWER] created: id={}", powerConsumption.getId());
	}

	private void createAirCondition(SetupRequest.BodyData.DeviceConfig device, Room room, 
		DeviceControl deviceControl, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		AirCondition ac = new AirCondition();
		setupBaseIoTProperties(ac, device, room, deviceControl);
		
		ac.setTemperature(26);
		ac.setMode(ActuatorMode.COOL);
		ac.setFanSpeed(1);
		ac.setSwing(ActuatorSwing.OFF);
		
		attachTranslations(ac, translations, AirConditionLan::new);
		
		entityManager.persist(ac);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:AC] created: id={}", ac.getId());
	}

	private void setupBaseIoTProperties(BaseIoTEntity<?> entity, 
		SetupRequest.BodyData.DeviceConfig device, Room room, DeviceControl deviceControl) {
		entity.setIsActive(device.isActive());
		entity.setNaturalId(device.getNaturalId());
		entity.setRoom(room);
		entity.setDeviceControl(deviceControl);

		if (entity instanceof BaseIoTActuator<?> actuator) {
			actuator.setPower(ActuatorPower.OFF);
		}
	}

	private <T extends BaseTranslatableEntity<L>, L extends BaseTranslation<T>> void attachTranslations(
		T entity, 
		Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations,
		java.util.function.Supplier<L> translationSupplier) {
		
		if (translations == null || translations.isEmpty()) {
			return;
		}

		translations.forEach((langCode, detail) -> {
			L translation = translationSupplier.get();
			translation.setLangCode(langCode);
			translation.setName(detail.getName());
			translation.setDescription(detail.getDescription());
			
			entity.addTranslation(translation);
		});
	}
}