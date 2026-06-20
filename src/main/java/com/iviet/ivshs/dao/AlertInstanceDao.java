package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlertInstanceDao extends BaseAuditEntityDao<AlertInstance> {

    public AlertInstanceDao() {
        super(AlertInstance.class);
    }

    /**
     * Tìm alert đang mở (ACTIVE hoặc ACKNOWLEDGED) gần nhất của một Config.
     * Dùng để kiểm tra logic cooldown trước khi tạo alert mới.
     */
    public Optional<AlertInstance> findLatestOpenByConfigId(Long configId) {
        String jpql = """
            SELECT ai FROM AlertInstance ai
            WHERE ai.alertConfig.id = :configId
              AND ai.status IN (:statuses)
            ORDER BY ai.triggeredAt DESC
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    /**
      * Tìm tất cả alert đang mở của một Config (kèm recipients đã FETCH sẵn).
      * Dùng cho logic auto-resolve khi điều kiện không còn thỏa mãn.
      */
    public List<AlertInstance> findAllOpenByConfigId(Long configId) {
        String jpql = """
            SELECT DISTINCT ai FROM AlertInstance ai
            LEFT JOIN FETCH ai.recipients
            WHERE ai.alertConfig.id = :configId
              AND ai.status IN (:statuses)
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .getResultList();
    }

    /**
     * Tìm một AlertInstance kèm recipients được FETCH sẵn (tránh N+1).
     * Dùng trước khi gọi NotificationService hoặc kiểm tra quyền G_USER.
     */
    public Optional<AlertInstance> findByIdWithRecipients(Long id) {
        String jpql = """
            SELECT ai FROM AlertInstance ai
            LEFT JOIN FETCH ai.recipients
            WHERE ai.id = :id
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    // ========== G_ADMIN: Xem toàn bộ alerts ==========

    public List<AlertInstance> findAll(AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder(
            "SELECT ai FROM AlertInstance ai WHERE 1=1"
        );
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAll(AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ai) FROM AlertInstance ai WHERE 1=1");
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    // ========== G_MAINTENANCE: Xem alerts của group ==========
    // Dùng JSON_CONTAINS của MySQL 8 để kiểm tra groupCode trong recipient_groups

    public List<AlertInstance> findAllByGroupCode(
            String groupCode, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.alertConfig raa
            WHERE JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByGroupCode(String groupCode, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.alertConfig raa
            WHERE JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    // ========== G_USER: "My Alerts" — chỉ xem alert mà mình là recipient ==========

    public List<AlertInstance> findAllByRecipientClientId(
            Long clientId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByRecipientClientId(Long clientId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    // ========== Query by ruleId with RBAC ==========

    public List<AlertInstance> findAllByRuleId(Long ruleId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder(
            "SELECT ai FROM AlertInstance ai WHERE ai.alertConfig.rule.id = :ruleId"
        );
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("ruleId", ruleId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAllByRuleId(Long ruleId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ai) FROM AlertInstance ai WHERE ai.alertConfig.rule.id = :ruleId");
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("ruleId", ruleId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    public List<AlertInstance> findAllByRuleIdAndGroupCode(
            Long ruleId, String groupCode, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.alertConfig raa
            WHERE raa.rule.id = :ruleId AND JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("ruleId", ruleId)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByRuleIdAndGroupCode(Long ruleId, String groupCode, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.alertConfig raa
            WHERE raa.rule.id = :ruleId AND JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("ruleId", ruleId)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    public List<AlertInstance> findAllByRuleIdAndRecipientClientId(
            Long ruleId, Long clientId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE ai.alertConfig.rule.id = :ruleId AND r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("ruleId", ruleId)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByRuleIdAndRecipientClientId(Long ruleId, Long clientId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE ai.alertConfig.rule.id = :ruleId AND r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("ruleId", ruleId)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }
}
