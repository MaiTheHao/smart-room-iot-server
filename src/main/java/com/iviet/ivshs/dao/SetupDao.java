package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.ActuatorMode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Deprecated
@Slf4j
@Repository
public class SetupDao extends BaseDao<SetupDao> {

	private static final int BATCH_SIZE = 50;

	public SetupDao() {
		super(SetupDao.class);
	}

	public int persistDeviceSetup(java.util.List<SetupRequest.BodyData.DeviceConfig> devices, Long clientId, Long roomId) {
		if (devices == null || devices.isEmpty()) {
			log.warn("[SETUP:DAO] No devices to persist: clientId={}, roomId={}", clientId, roomId);
			return 0;
		}

		log.info("[SETUP:DAO] Starting device persistence: total={}, roomId={}, clientId={}", 
			devices.size(), roomId, clientId);

		try {
			Client client = entityManager.getReference(Client.class, clientId);
			Room room = entityManager.getReference(Room.class, roomId);

			int processedDevices = 0;
			for (int i = 0; i < devices.size(); i++) {
				SetupRequest.BodyData.DeviceConfig device = devices.get(i);
				
				if (device.getCategory() == null) {
					log.warn("[SETUP:DAO:SKIP] Device category is null at index={}, name={}", 
						i, device.getName());
					continue;
				}

				try {
					if (log.isDebugEnabled()) {
						log.debug("[SETUP:DAO:DEVICE] index={}, name={}, category={}", 
							i, device.getName(), device.getCategory());
					}

					persistDevice(device, client, room);
					processedDevices++;

					if (processedDevices % BATCH_SIZE == 0) {
						log.info("[SETUP:DAO:BATCH] Flushed batch: processed={}/{}", 
							processedDevices, devices.size());
						entityManager.flush();
						entityManager.clear();
					}
					
				} catch (Exception e) {
					log.error("[SETUP:DAO:ERROR] Failed to persist device at index={}, name={}, category={}: {}", 
						i, device.getName(), device.getCategory(), e.getMessage(), e);
				}
			}

			entityManager.flush();
			log.info("[SETUP:DAO] Completed device persistence: total={}, processed={}, failed={}, roomId={}, clientId={}", 
				devices.size(), processedDevices, devices.size() - processedDevices, roomId, clientId);
			
			return processedDevices;
			
		} catch (Exception e) {
			log.error("[SETUP:DAO:CRITICAL] Critical error during batch persistence: clientId={}, roomId={}, error={}", 
				clientId, roomId, e.getMessage(), e);
			throw new RuntimeException("Failed to persist device setup: " + e.getMessage(), e);
		}
	}

	private void persistDevice(SetupRequest.BodyData.DeviceConfig device, Client client, Room room) {
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:PERSIST] name={}, category={}", device.getName(), device.getCategory());
		}
		
		HardwareConfig hardwareConfig = createAndPersistDeviceControl(device, room, client);
		buildDeviceEntity(device, room, hardwareConfig, device.getTranslations());
	}

	private HardwareConfig createAndPersistDeviceControl(
		SetupRequest.BodyData.DeviceConfig device, Room room, Client client) {
		
		HardwareConfig hardwareConfig = new HardwareConfig();
		hardwareConfig.setControlType(device.getControlType());
		hardwareConfig.setGpioPin(device.getGpioPin().getFirst());
		hardwareConfig.setBleMacAddress(device.getBleMac());
		hardwareConfig.setApiEndpoint(device.getApiEndpoint());
		hardwareConfig.setClient(client);
		hardwareConfig.setRoom(room);
		
		entityManager.persist(hardwareConfig);
		entityManager.flush();
		
		if (log.isDebugEnabled()) {
			log.debug("[SETUP:CTRL] hardwareConfig created: id={}, name={}, type={}", 
				hardwareConfig.getId(), device.getName(), device.getControlType());
		}
		
		return hardwareConfig;
	}

	private void buildDeviceEntity(SetupRequest.BodyData.DeviceConfig device, Room room, 
		HardwareConfig hardwareConfig, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		switch (device.getCategory()) {
			case LIGHT -> createLight(device, room, hardwareConfig, translations);
			case TEMPERATURE -> createTemperature(device, room, hardwareConfig, translations);
			case POWER_CONSUMPTION -> createPowerConsumption(device, room, hardwareConfig, translations);
			case AIR_CONDITION -> createAirCondition(device, room, hardwareConfig, translations);
		}
	}

	private void createLight(SetupRequest.BodyData.DeviceConfig device, Room room, 
		HardwareConfig hardwareConfig, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		Light light = new Light();
		setupBaseIoTProperties(light, device, room, hardwareConfig);
		attachTranslations(light, translations, LightLan::new);
		
		entityManager.persist(light);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:LIGHT] created: id={}", light.getId());
	}

	private void createTemperature(SetupRequest.BodyData.DeviceConfig device, Room room, 
		HardwareConfig hardwareConfig, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		Temperature temperature = new Temperature();
		setupBaseIoTProperties(temperature, device, room, hardwareConfig);
		attachTranslations(temperature, translations, TemperatureLan::new);
		
		entityManager.persist(temperature);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:TEMP] created: id={}", temperature.getId());
	}

	private void createPowerConsumption(SetupRequest.BodyData.DeviceConfig device, Room room, 
		HardwareConfig hardwareConfig, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		PowerConsumption powerConsumption = new PowerConsumption();
		setupBaseIoTProperties(powerConsumption, device, room, hardwareConfig);
		attachTranslations(powerConsumption, translations, PowerConsumptionLan::new);
		
		entityManager.persist(powerConsumption);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:POWER] created: id={}", powerConsumption.getId());
	}

	private void createAirCondition(SetupRequest.BodyData.DeviceConfig device, Room room, 
		HardwareConfig hardwareConfig, Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations) {
		AirCondition ac = new AirCondition();
		setupBaseIoTProperties(ac, device, room, hardwareConfig);
		
		ac.setTemperature(26);
		ac.setMode(ActuatorMode.COOL);
		ac.setFanSpeed(1);
		ac.setSwing(ActuatorSwing.OFF);
		
		attachTranslations(ac, translations, AirConditionLan::new);
		
		entityManager.persist(ac);
		
		if (log.isDebugEnabled()) log.debug("[SETUP:AC] created: id={}", ac.getId());
	}

	private void setupBaseIoTProperties(BaseIoTEntity<?> entity, 
		SetupRequest.BodyData.DeviceConfig device, Room room, HardwareConfig hardwareConfig) {
		entity.setIsActive(device.isActive());
		entity.setNaturalId(device.getNaturalId());
		entity.setRoom(room);
		entity.setHardwareConfig(hardwareConfig);

		if (entity instanceof BaseIoTDevice<?> actuator) {
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