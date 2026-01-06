package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientTypeV1;

@Repository
public class ClientDao extends BaseAuditEntityDao<Client> {

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
            this.getCB().equal(root.get("clientType"), ClientTypeV1.USER)
        ));
    }

    public Optional<Client> findGatewayByUsername(String username) {
        return findOne(root -> this.getCB().and(
            this.getCB().equal(root.get("username"), username),
            this.getCB().equal(root.get("clientType"), ClientTypeV1.HARDWARE_GATEWAY)
        ));
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
            this.getCB().equal(root.get("clientType"), ClientTypeV1.USER)
        ));
    }

    public Optional<Client> findGatewayByIpAddress(String ipAddress) {
        return findOne(root -> this.getCB().and(
            this.getCB().equal(root.get("ipAddress"), ipAddress),
            this.getCB().equal(root.get("clientType"), ClientTypeV1.HARDWARE_GATEWAY)
        ));
    }

    // ======= Find by ID =======
    public Optional<Client> findById(Long id) {
        return findOne(root -> this.getCB().equal(root.get("id"), id));
    }

    public Optional<Client> findUserById(Long id) {
        return findOne(root -> this.getCB().and(
            this.getCB().equal(root.get("id"), id),
            this.getCB().equal(root.get("clientType"), ClientTypeV1.USER)
        ));
    }

    public Optional<Client> findGatewayById(Long id) {
        return findOne(root -> this.getCB().and(
            this.getCB().equal(root.get("id"), id),
            this.getCB().equal(root.get("clientType"), ClientTypeV1.HARDWARE_GATEWAY)
        ));
    }

    // ======= Find Gateways by Room ID =======
    public List<Client> findGatewaysByRoomId(Long roomId, int page, int size) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                      "JOIN c.deviceControls dc " +
                      "WHERE dc.room.id = :roomId " +
                      "AND c.clientType = :clientType " +
                      "ORDER BY c.createdAt DESC";
        
        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomId", roomId)
                .setParameter("clientType", ClientTypeV1.HARDWARE_GATEWAY)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Client> findGatewaysByRoomId(Long roomId) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                      "JOIN c.deviceControls dc " +
                      "WHERE dc.room.id = :roomId " +
                      "AND c.clientType = :clientType " +
                      "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomId", roomId)
                .setParameter("clientType", ClientTypeV1.HARDWARE_GATEWAY)
                .getResultList();
    }

    public long countGatewaysByRoomId(Long roomId) {
        String jpql = "SELECT COUNT(DISTINCT c) FROM Client c " +
                      "JOIN c.deviceControls dc " +
                      "WHERE dc.room.id = :roomId " +
                      "AND c.clientType = :clientType";

        return entityManager.createQuery(jpql, Long.class)
                .setParameter("roomId", roomId)
                .setParameter("clientType", ClientTypeV1.HARDWARE_GATEWAY)
                .getSingleResult();
    }

    // ======= Find Gateways by Room Code =======
    public List<Client> findGatewaysByRoomCode(String roomCode, int page, int size) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                      "JOIN c.deviceControls dc " +
                      "WHERE dc.room.code = :roomCode " +
                      "AND c.clientType = :clientType " +
                      "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomCode", roomCode)
                .setParameter("clientType", ClientTypeV1.HARDWARE_GATEWAY)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Client> findGatewaysByRoomCode(String roomCode) {
        String jpql = "SELECT DISTINCT c FROM Client c " +
                      "JOIN c.deviceControls dc " +
                      "WHERE dc.room.code = :roomCode " +
                      "AND c.clientType = :clientType " +
                      "ORDER BY c.createdAt DESC";

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("roomCode", roomCode)
                .setParameter("clientType", ClientTypeV1.HARDWARE_GATEWAY)
                .getResultList();
    }
}
