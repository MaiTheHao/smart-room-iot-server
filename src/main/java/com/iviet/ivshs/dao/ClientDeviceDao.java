package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.entities.ClientDevice;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientDeviceDao extends BaseEntityDao<ClientDevice> {

    public ClientDeviceDao() {
        super(ClientDevice.class);
    }

    public Optional<ClientDevice> findByDeviceIdentifier(String deviceIdentifier) {
        return findOne(root -> this.getCB().equal(root.get("deviceIdentifier"), deviceIdentifier));
    }

    @Transactional
    public void deleteByFcmToken(String fcmToken) {
        String jpql = "DELETE FROM ClientDevice cd WHERE cd.fcmToken = :fcmToken";
        entityManager.createQuery(jpql)
                .setParameter("fcmToken", fcmToken)
                .executeUpdate();
    }

    @Transactional
    public void deleteByFcmTokenIn(List<String> fcmTokens) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }
        String jpql = "DELETE FROM ClientDevice cd WHERE cd.fcmToken IN :fcmTokens";
        entityManager.createQuery(jpql)
                .setParameter("fcmTokens", fcmTokens)
                .executeUpdate();
    }

    @Transactional
    public void deleteByClientIdAndDeviceIdentifierAndPlatform(Long clientId, String deviceIdentifier, com.iviet.ivshs.shared.enumeration.Platform platform) {
        String jpql = "DELETE FROM ClientDevice cd WHERE cd.client.id = :clientId AND cd.deviceIdentifier = :deviceIdentifier AND cd.platform = :platform";
        entityManager.createQuery(jpql)
                .setParameter("clientId", clientId)
                .setParameter("deviceIdentifier", deviceIdentifier)
                .setParameter("platform", platform)
                .executeUpdate();
    }
}
