package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.MotionDetectorDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.MotionDetector;
import com.iviet.ivshs.entities.MotionDetectorLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotionDetectorSetupStrategy extends AbstractDeviceSetupStrategy {

    private final MotionDetectorDao motionDetectorDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.MOTION_DETECTOR;
    }

    @Override
    public void persist(
            SetupRequest.BodyData.DeviceConfig device,
            Room room,
            HardwareConfig hardwareConfig) {
        MotionDetector motionDetector = new MotionDetector();
        setupBaseIoTProperties(motionDetector, device, room, hardwareConfig);
        entityManager.persist(motionDetector);
        entityManager.flush();
        attachTranslations(motionDetector, device.getTranslations(), MotionDetectorLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            motionDetectorDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
