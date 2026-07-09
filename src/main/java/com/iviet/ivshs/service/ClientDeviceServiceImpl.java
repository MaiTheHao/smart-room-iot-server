package com.iviet.ivshs.service.clientdevice;

import com.iviet.ivshs.dao.ClientDeviceDao;
import com.iviet.ivshs.dto.RegisterClientDeviceDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.ClientDevice;
import com.iviet.ivshs.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientDeviceServiceImpl implements ClientDeviceService {

    private final ClientDeviceDao clientDeviceDao;
    private final ClientService clientService;

    @Override
    @Transactional
    public void registerDevice(RegisterClientDeviceDto request) {
        Client currentClient = clientService.getFromSecurityContext();

        ClientDevice clientDevice = clientDeviceDao.findByDeviceIdentifier(request.getDeviceIdentifier())
                .orElse(null);

        boolean isNew = false;
        if (clientDevice == null) {
            clientDevice = new ClientDevice();
            isNew = true;
        }

        clientDevice.setClient(currentClient);
        clientDevice.setFcmToken(request.getFcmToken());
        clientDevice.setDeviceIdentifier(request.getDeviceIdentifier());
        if (request.getPlatform() != null) {
            clientDevice.setPlatform(request.getPlatform());
        }
        clientDevice.setLastUpdatedAt(Instant.now());

        if (isNew) {
            clientDeviceDao.save(clientDevice);
        } else {
            clientDeviceDao.update(clientDevice);
        }
        log.info("Registered/Updated device {} for client {}", request.getDeviceIdentifier(), currentClient.getId());
    }

    @Override
    @Transactional
    public void logoutDevice(Long clientId, String deviceIdentifier, com.iviet.ivshs.shared.enumeration.Platform platform) {
        clientDeviceDao.deleteByClientIdAndDeviceIdentifierAndPlatform(clientId, deviceIdentifier, platform);
        log.info("Logged out device {} ({}) for client {}", deviceIdentifier, platform, clientId);
    }
}
