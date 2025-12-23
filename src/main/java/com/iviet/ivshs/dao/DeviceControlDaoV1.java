package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.enumeration.DeviceControlTypeV1;

import jakarta.persistence.TypedQuery;

@Repository
public class DeviceControlDaoV1 extends BaseDao<DeviceControlV1, Long> {
    
    public DeviceControlDaoV1() {
        super(DeviceControlV1.class);
    }

    public List<DeviceControlV1> findByClientId(Long clientId, int page, int size) {
        String idQuery = "SELECT dc.id FROM DeviceControlV1 dc " +
                        "WHERE dc.client.id = :clientId " +
                        "ORDER BY dc.createdAt DESC";
        
        TypedQuery<Long> query = entityManager.createQuery(idQuery, Long.class);
        query.setParameter("clientId", clientId);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<Long> ids = query.getResultList();
        
        if (ids.isEmpty()) {
            return List.of();
        }
        
        String fetchQueryStr = "SELECT DISTINCT dc FROM DeviceControlV1 dc " +
                           "LEFT JOIN FETCH dc.room " +
                           "LEFT JOIN FETCH dc.client " +
                           "LEFT JOIN FETCH dc.light " +
                           "LEFT JOIN FETCH dc.temperature " +
                           "LEFT JOIN FETCH dc.powerConsumption " +
                           "WHERE dc.id IN :ids " +
                           "ORDER BY dc.createdAt DESC";
        
        TypedQuery<DeviceControlV1> fetchQuery = entityManager.createQuery(fetchQueryStr, DeviceControlV1.class);
        fetchQuery.setParameter("ids", ids);
        return fetchQuery.getResultList();
    }

    public List<DeviceControlV1> findByRoomId(Long roomId, int page, int size) {
        String jpql = "SELECT dc FROM DeviceControlV1 dc " +
                    "LEFT JOIN FETCH dc.room " +             
                    "LEFT JOIN FETCH dc.client " +           
                    "LEFT JOIN FETCH dc.light " +            
                    "LEFT JOIN FETCH dc.temperature " +      
                    "LEFT JOIN FETCH dc.powerConsumption " + 
                    "WHERE dc.room.id = :roomId " +
                    "ORDER BY dc.createdAt DESC";
        
        return executePaginatedQuery(jpql, "roomId", roomId, page, size);
    }

    public List<DeviceControlV1> findByDeviceControlType(DeviceControlTypeV1 deviceControlType) {
        String jpql = "SELECT dc FROM DeviceControlV1 dc WHERE dc.deviceControlType = :deviceControlType ORDER BY dc.createdAt DESC";
        TypedQuery<DeviceControlV1> query = entityManager.createQuery(jpql, DeviceControlV1.class);
        query.setParameter("deviceControlType", deviceControlType);
        return query.getResultList();
    }

    public List<DeviceControlV1> findByBleMacAddress(String bleMacAddress) {
        String jpql = "SELECT dc FROM DeviceControlV1 dc WHERE dc.bleMacAddress = :bleMacAddress ORDER BY dc.createdAt DESC";
        TypedQuery<DeviceControlV1> query = entityManager.createQuery(jpql, DeviceControlV1.class);
        query.setParameter("bleMacAddress", bleMacAddress);
        return query.getResultList();
    }

    public DeviceControlV1 findByGpioPin(Integer gpioPin) {
        String jpql = "SELECT dc FROM DeviceControlV1 dc WHERE dc.gpioPin = :gpioPin";
        TypedQuery<DeviceControlV1> query = entityManager.createQuery(jpql, DeviceControlV1.class);
        query.setParameter("gpioPin", gpioPin);
        List<DeviceControlV1> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByClientId(Long clientId) {
        return countByField("client.id", clientId) > 0;
    }

    public boolean existsByRoomId(Long roomId) {
        return countByField("room.id", roomId) > 0;
    }

    public Long countByClientId(Long clientId) {
        return countByField("client.id", clientId);
    }

    public Long countByRoomId(Long roomId) {
        return countByField("room.id", roomId);
    }

    public DeviceControlV1 findByClientIdAndGpioPin(Long clientId, Integer gpioPin) {
        String jpql = "SELECT dc FROM DeviceControlV1 dc WHERE dc.client.id = :clientId AND dc.gpioPin = :gpioPin";
        TypedQuery<DeviceControlV1> query = entityManager.createQuery(jpql, DeviceControlV1.class);
        query.setParameter("clientId", clientId);
        query.setParameter("gpioPin", gpioPin);
        List<DeviceControlV1> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    private List<DeviceControlV1> executePaginatedQuery(String jpql, String paramName, 
                                                        Object paramValue, int page, int size) {
        TypedQuery<DeviceControlV1> query = entityManager.createQuery(jpql, DeviceControlV1.class);
        query.setParameter(paramName, paramValue);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}
