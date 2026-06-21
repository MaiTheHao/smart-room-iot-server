package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.AlertRecipient;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlertRecipientDao extends BaseAuditEntityDao<AlertRecipient> {

    public AlertRecipientDao() {
        super(AlertRecipient.class);
    }

    /**
     * Tìm alert event đang mở (ACTIVE/ACKNOWLEDGED) gần nhất của một AlertConfig.
     * Dùng để kiểm tra cooldown trước khi tạo alert mới.
     */
    public Optional<AlertRecipient> findLatestOpenByConfigId(Long configId) {
        String jpql = """
            SELECT ar FROM AlertRecipient ar
            WHERE ar.alertConfig.id = :configId
              AND ar.status IN (:statuses)
            ORDER BY ar.triggeredAt DESC
            """;
        return entityManager.createQuery(jpql, AlertRecipient.class)
                .setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .setMaxResults(1)
                .getResultStream().findFirst();
    }

    /**
     * Tìm tất cả alert đang mở của một AlertConfig.
     * Dùng cho auto-resolve.
     */
    public List<AlertRecipient> findAllOpenByConfigId(Long configId) {
        String jpql = """
            SELECT ar FROM AlertRecipient ar
            WHERE ar.alertConfig.id = :configId
              AND ar.status IN (:statuses)
            """;
        return entityManager.createQuery(jpql, AlertRecipient.class)
                .setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .getResultList();
    }

    // ===== RBAC QUERIES: Dynamic Group-based =====

    /**
     * Lấy tất cả alert events mà user thuộc ít nhất một group nhận tin.
     * Luồng mới: alert_recipient → alert_recipient_group → client_group → client_id.
     */
    public List<AlertRecipient> findAllByClientGroups(
            Long clientId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT ar FROM AlertRecipient ar
            JOIN AlertRecipientGroup arg ON arg.alert.id = ar.id
            JOIN arg.group g
            JOIN g.clients c
            WHERE c.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertRecipient.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByClientGroups(Long clientId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(DISTINCT ar) FROM AlertRecipient ar
            JOIN AlertRecipientGroup arg ON arg.alert.id = ar.id
            JOIN arg.group g
            JOIN g.clients c
            WHERE c.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    /** Lấy tất cả alert events (Admin view — không giới hạn). */
    public List<AlertRecipient> findAll(AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("SELECT ar FROM AlertRecipient ar WHERE 1=1");
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertRecipient.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAll(AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ar) FROM AlertRecipient ar WHERE 1=1");
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    /**
     * Lấy các alert events theo source & namespace mà user thuộc ít nhất một group nhận tin.
     */
    public List<AlertRecipient> findAllBySourceAndClientGroups(
            Long clientId, AlertNamespace namespace, String sourceId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT ar FROM AlertRecipient ar
            JOIN AlertRecipientGroup arg ON arg.alert.id = ar.id
            JOIN arg.group g
            JOIN g.clients c
            WHERE c.id = :clientId
              AND ar.alertConfig.namespace = :namespace
              AND ar.alertConfig.sourceId = :sourceId
            """);
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertRecipient.class)
                .setParameter("clientId", clientId)
                .setParameter("namespace", namespace)
                .setParameter("sourceId", sourceId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countBySourceAndClientGroups(
            Long clientId, AlertNamespace namespace, String sourceId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(DISTINCT ar) FROM AlertRecipient ar
            JOIN AlertRecipientGroup arg ON arg.alert.id = ar.id
            JOIN arg.group g
            JOIN g.clients c
            WHERE c.id = :clientId
              AND ar.alertConfig.namespace = :namespace
              AND ar.alertConfig.sourceId = :sourceId
            """);
        if (status   != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("clientId", clientId)
                .setParameter("namespace", namespace)
                .setParameter("sourceId", sourceId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    /**
     * Kiểm tra user có quyền xem/xử lý alert này không.
     * Dùng trong checkAccess(): kiểm tra group của user có trong alert_recipient_group không.
     */
    public boolean isClientInAlertGroups(Long alertId, Long clientId) {
        String jpql = """
            SELECT COUNT(ar) FROM AlertRecipient ar
            JOIN AlertRecipientGroup arg ON arg.alert.id = ar.id
            JOIN arg.group g
            JOIN g.clients c
            WHERE ar.id = :alertId AND c.id = :clientId
            """;
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("alertId", alertId)
                .setParameter("clientId", clientId)
                .getSingleResult();
        return count > 0;
    }
}
