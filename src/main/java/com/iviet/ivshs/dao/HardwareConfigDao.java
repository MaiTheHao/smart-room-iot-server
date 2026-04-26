package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.enumeration.DeviceControlType;

import jakarta.persistence.TypedQuery;

@Repository
public class HardwareConfigDao extends BaseEntityDao<HardwareConfig> {
    
    public HardwareConfigDao() {
        super(HardwareConfig.class);
    }

    public List<HardwareConfig> findByClientId(Long clientId, int page, int size) {
        String idQuery = "SELECT dc.id FROM DeviceControl dc " +
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
        
        String fetchQueryStr = "SELECT DISTINCT dc FROM DeviceControl dc " +
                           "LEFT JOIN FETCH dc.room " +
                           "LEFT JOIN FETCH dc.client " +
                           "LEFT JOIN FETCH dc.light " +
                           "LEFT JOIN FETCH dc.temperature " +
                           "LEFT JOIN FETCH dc.powerConsumption " +
                           "WHERE dc.id IN :ids " +
                           "ORDER BY dc.createdAt DESC";
        
        TypedQuery<HardwareConfig> fetchQuery = entityManager.createQuery(fetchQueryStr, HardwareConfig.class);
        fetchQuery.setParameter("ids", ids);
        return fetchQuery.getResultList();
    }

    public List<HardwareConfig> findByRoomId(Long roomId, int page, int size) {
        String jpql = "SELECT dc FROM DeviceControl dc " +
                    "LEFT JOIN FETCH dc.room " +             
                    "LEFT JOIN FETCH dc.client " +           
                    "LEFT JOIN FETCH dc.light " +            
                    "LEFT JOIN FETCH dc.temperature " +      
                    "LEFT JOIN FETCH dc.powerConsumption " + 
                    "WHERE dc.room.id = :roomId " +
                    "ORDER BY dc.createdAt DESC";
        
        return executePaginatedQuery(jpql, "roomId", roomId, page, size);
    }

    public List<HardwareConfig> findByDeviceControlType(DeviceControlType controlType) {
        String jpql = "SELECT dc FROM DeviceControl dc WHERE dc.controlType = :controlType ORDER BY dc.createdAt DESC";
        TypedQuery<HardwareConfig> query = entityManager.createQuery(jpql, HardwareConfig.class);
        query.setParameter("controlType", controlType);
        return query.getResultList();
    }

    public List<HardwareConfig> findByBleMacAddress(String bleMacAddress) {
        String jpql = "SELECT dc FROM DeviceControl dc WHERE dc.bleMacAddress = :bleMacAddress ORDER BY dc.createdAt DESC";
        TypedQuery<HardwareConfig> query = entityManager.createQuery(jpql, HardwareConfig.class);
        query.setParameter("bleMacAddress", bleMacAddress);
        return query.getResultList();
    }

    public HardwareConfig findByGpioPin(Integer gpioPin) {
        String jpql = "SELECT dc FROM DeviceControl dc WHERE dc.gpioPin = :gpioPin";
        TypedQuery<HardwareConfig> query = entityManager.createQuery(jpql, HardwareConfig.class);
        query.setParameter("gpioPin", gpioPin);
        List<HardwareConfig> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByClientId(Long clientId) {
        return count(
            root -> entityManager.getCriteriaBuilder().equal(root.get("client").get("id"), clientId)
        ) > 0;
    }

    public boolean existsByRoomId(Long roomId) {
        return count(
            root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId)
        ) > 0;
    }

    public Long countByClientId(Long clientId) {
        return count(
            root -> entityManager.getCriteriaBuilder().equal(root.get("client").get("id"), clientId)
        );
    }

    public Long countByRoomId(Long roomId) {
        return count(
            root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId)
        );
    }

    public HardwareConfig findByClientIdAndGpioPin(Long clientId, Integer gpioPin) {
        String jpql = "SELECT dc FROM DeviceControl dc WHERE dc.client.id = :clientId AND dc.gpioPin = :gpioPin";
        TypedQuery<HardwareConfig> query = entityManager.createQuery(jpql, HardwareConfig.class);
        query.setParameter("clientId", clientId);
        query.setParameter("gpioPin", gpioPin);
        List<HardwareConfig> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    private List<HardwareConfig> executePaginatedQuery(String jpql, String paramName, 
                                                        Object paramValue, int page, int size) {
        TypedQuery<HardwareConfig> query = entityManager.createQuery(jpql, HardwareConfig.class);
        query.setParameter(paramName, paramValue);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}
