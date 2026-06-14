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
}
