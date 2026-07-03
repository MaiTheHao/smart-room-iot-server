package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.shared.enumeration.ClientType;

@Repository
public class ClientDao extends BaseAuditEntityDao<Client> {

    private static final List<ClientType> GATEWAY_TYPES = List.of(
        ClientType.HARDWARE_GATEWAY,
        ClientType.HARDWARE_GATEWAY_ESP32
    );

    public ClientDao() {
        super(Client.class);
    }

    // ======= Find by Username =======
    public Optional<Client> findByUsername(String username) {
        return findOne(root -> this.getCB().equal(root.get("username"), username));
    }

    public Optional<Client> findUserByUsername(String username) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("username"), username),
                this.getCB().equal(root.get("clientType"), ClientType.USER)));
    }

    public Optional<Client> findGatewayByUsername(String username) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("username"), username),
                root.get("clientType").in(GATEWAY_TYPES)));
    }

    public boolean existsByUsername(String username) {
        return exists(root -> this.getCB().equal(root.get("username"), username));
    }

    // ======= Find by IP Address =======
    public Optional<Client> findByIpAddress(String ipAddress) {
        return findOne(root -> this.getCB().equal(root.get("ipAddress"), ipAddress));
    }

    public Optional<Client> findUserByIpAddress(String ipAddress) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("ipAddress"), ipAddress),
                this.getCB().equal(root.get("clientType"), ClientType.USER)));
    }

    public Optional<Client> findGatewayByIpAddress(String ipAddress) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("ipAddress"), ipAddress),
                root.get("clientType").in(GATEWAY_TYPES)));
    }

    // ======= Find by ID =======
    public Optional<Client> findById(Long id) {
        return findOne(root -> this.getCB().equal(root.get("id"), id));
    }

    public Optional<Client> findUserById(Long id) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("id"), id),
                this.getCB().equal(root.get("clientType"), ClientType.USER)));
    }

    public Optional<Client> findGatewayById(Long id) {
        return findOne(root -> this.getCB().and(
                this.getCB().equal(root.get("id"), id),
                root.get("clientType").in(GATEWAY_TYPES)));
    }

    // ======= Find Gateways by Room ID =======
    public List<Client> findGatewaysByRoomId(Long roomId, int page, int size) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                "JOIN c.hardwareConfigs dc " +
                "WHERE dc.room.id = :roomId " +
                "AND c.clientType IN :clientTypes " +
                "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomId", roomId)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Client> findGatewaysByRoomId(Long roomId) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                "JOIN c.hardwareConfigs dc " +
                "WHERE dc.room.id = :roomId " +
                "AND c.clientType IN :clientTypes " +
                "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomId", roomId)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .getResultList();
    }

    public long countGatewaysByRoomId(Long roomId) {
        String jpql = "SELECT COUNT(DISTINCT c) FROM Client c " +
                "JOIN c.hardwareConfigs dc " +
                "WHERE dc.room.id = :roomId " +
                "AND c.clientType IN :clientTypes";

        return entityManager.createQuery(jpql, Long.class)
                .setParameter("roomId", roomId)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .getSingleResult();
    }

    // ======= Find Gateways by Room Code =======
    public List<Client> findGatewaysByRoomCode(String roomCode, int page, int size) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                "JOIN c.hardwareConfigs dc " +
                "WHERE dc.room.code = :roomCode " +
                "AND c.clientType IN :clientTypes " +
                "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomCode", roomCode)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Client> findGatewaysByRoomCode(String roomCode) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                "JOIN c.hardwareConfigs dc " +
                "WHERE dc.room.code = :roomCode " +
                "AND c.clientType IN :clientTypes " +
                "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomCode", roomCode)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .getResultList();
    }

    // ======= Find All Gateways =======
    public List<Client> findAllGateways() {
        String jpql = "SELECT c FROM Client c " +
                "WHERE c.clientType IN :clientTypes " +
                "ORDER BY c.createdAt DESC";
        return entityManager.createQuery(jpql, Client.class)
                .setParameter("clientTypes", GATEWAY_TYPES)
                .getResultList();
    }

    public java.util.Set<Client> findAllWithDevicesByIdIn(java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Set.of();
        }
        String jpql = "SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.clientDevices WHERE c.id IN :ids";
        return new java.util.HashSet<>(entityManager.createQuery(jpql, Client.class)
                .setParameter("ids", ids)
                .getResultList());
    }
}
