package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.AlertInstanceGroup;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertInstanceDao extends BaseAuditEntityDao<AlertInstance> {

    public AlertInstanceDao() {
        super(AlertInstance.class);
    }

    /**
     * Tìm AlertInstance theo ID cùng với việc nạp trước (Fetch Join) AlertConfig và danh sách channels.
     */
    public Optional<AlertInstance> findByIdWithConfigAndChannels(Long id) {
        if (id == null) return Optional.empty();
        String jpql = """
                SELECT ar FROM AlertInstance ar
                JOIN FETCH ar.alertConfig ac
                WHERE ar.id = :id
                """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    /**
     * Tìm alert event đang mở (ACTIVE/ACKNOWLEDGED) gần nhất của một AlertConfig. Dùng để kiểm tra cooldown trước khi
     * tạo alert mới.
     */
    public Optional<AlertInstance> findLatestOpenByConfigId(Long configId) {
        String jpql = """
                SELECT ar FROM AlertInstance ar
                WHERE ar.alertConfig.id = :configId
                  AND ar.status IN (:statuses)
                ORDER BY ar.triggeredAt DESC
                """;
        return entityManager.createQuery(jpql, AlertInstance.class).setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED)).setMaxResults(1)
                .getResultStream().findFirst();
    }

    /**
     * Tìm tất cả alert đang mở của một AlertConfig. Dùng cho auto-resolve.
     */
    public List<AlertInstance> findAllOpenByConfigId(Long configId) {
        String jpql = """
                SELECT ar FROM AlertInstance ar
                WHERE ar.alertConfig.id = :configId
                  AND ar.status IN (:statuses)
                """;
        return entityManager.createQuery(jpql, AlertInstance.class).setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED)).getResultList();
    }

    // ===== RBAC QUERIES: Dynamic Group-based =====

    /**
     * Lấy tất cả alert events mà user thuộc ít nhất một group nhận tin. Luồng mới: alert_recipient →
     * alert_recipient_group → client_group → client_id.
     */
    public List<AlertInstance> findAllByClientGroups(Long clientId, AlertStatus status, Severity severity,
            AlertNamespace namespace, Instant from, Instant to, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT ar FROM AlertInstance ar
                JOIN FETCH ar.alertConfig
                LEFT JOIN FETCH ar.acknowledgedBy
                LEFT JOIN FETCH ar.resolvedBy
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        if (namespace != null) jpql.append(" AND ar.alertConfig.namespace = :namespace");
        if (from != null) jpql.append(" AND ar.triggeredAt >= :from");
        if (to != null) jpql.append(" AND ar.triggeredAt <= :to");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class).setParameter("clientId", clientId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        if (namespace != null) q.setParameter("namespace", namespace);
        if (from != null) q.setParameter("from", from);
        if (to != null) q.setParameter("to", to);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByClientGroups(Long clientId, AlertStatus status, Severity severity,
            AlertNamespace namespace, Instant from, Instant to) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(DISTINCT ar) FROM AlertInstance ar
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        if (namespace != null) jpql.append(" AND ar.alertConfig.namespace = :namespace");
        if (from != null) jpql.append(" AND ar.triggeredAt >= :from");
        if (to != null) jpql.append(" AND ar.triggeredAt <= :to");

        var q = entityManager.createQuery(jpql.toString(), Long.class).setParameter("clientId", clientId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        if (namespace != null) q.setParameter("namespace", namespace);
        if (from != null) q.setParameter("from", from);
        if (to != null) q.setParameter("to", to);
        return q.getSingleResult();
    }

    /** Lấy tất cả alert events (Admin view — không giới hạn). */
    public List<AlertInstance> findAll(AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("SELECT ar FROM AlertInstance ar JOIN FETCH ar.alertConfig LEFT JOIN FETCH ar.acknowledgedBy LEFT JOIN FETCH ar.resolvedBy WHERE 1=1");
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAll(AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ar) FROM AlertInstance ar WHERE 1=1");
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    /**
     * Lấy các alert events theo source & namespace mà user thuộc ít nhất một group nhận tin.
     */
    public List<AlertInstance> findAllBySourceAndClientGroups(Long clientId, AlertNamespace namespace, String sourceId,
            AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT ar FROM AlertInstance ar
                JOIN FETCH ar.alertConfig
                LEFT JOIN FETCH ar.acknowledgedBy
                LEFT JOIN FETCH ar.resolvedBy
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                  AND ar.alertConfig.namespace = :namespace
                  AND ar.alertConfig.sourceId = :sourceId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class).setParameter("clientId", clientId)
                .setParameter("namespace", namespace).setParameter("sourceId", sourceId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countBySourceAndClientGroups(Long clientId, AlertNamespace namespace, String sourceId,
            AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(DISTINCT ar) FROM AlertInstance ar
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                  AND ar.alertConfig.namespace = :namespace
                  AND ar.alertConfig.sourceId = :sourceId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class).setParameter("clientId", clientId)
                .setParameter("namespace", namespace).setParameter("sourceId", sourceId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    public List<AlertInstance> findAllByConfigAndClientGroups(Long clientId, Long configId, AlertStatus status,
            Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT ar FROM AlertInstance ar
                JOIN FETCH ar.alertConfig
                LEFT JOIN FETCH ar.acknowledgedBy
                LEFT JOIN FETCH ar.resolvedBy
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                  AND ar.alertConfig.id = :configId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");
        jpql.append(" ORDER BY ar.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class).setParameter("clientId", clientId)
                .setParameter("configId", configId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByConfigAndClientGroups(Long clientId, Long configId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(DISTINCT ar) FROM AlertInstance ar
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE c.id = :clientId
                  AND ar.alertConfig.id = :configId
                """);
        if (status != null) jpql.append(" AND ar.status = :status");
        if (severity != null) jpql.append(" AND ar.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class).setParameter("clientId", clientId)
                .setParameter("configId", configId);
        if (status != null) q.setParameter("status", status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    /**
     * Kiểm tra user có quyền xem/xử lý alert này không. Dùng trong checkAccess(): kiểm tra group của user có trong
     * alert_recipient_group không.
     */
    public boolean isClientInAlertGroups(Long alertId, Long clientId) {
        String jpql = """
                SELECT COUNT(ar) FROM AlertInstance ar
                JOIN AlertInstanceGroup arg ON arg.alert.id = ar.id
                JOIN arg.group g
                JOIN g.clients c
                WHERE ar.id = :alertId AND c.id = :clientId
                """;
        Long count = entityManager.createQuery(jpql, Long.class).setParameter("alertId", alertId)
                .setParameter("clientId", clientId).getSingleResult();
        return count > 0;
    }

    /** Lưu liên kết giữa AlertInstance và SysGroup. */
    public void saveRecipientGroupAssociation(AlertInstance alert, SysGroup group) {
        AlertInstanceGroup arg = AlertInstanceGroup.builder().alert(alert).group(group).build();
        entityManager.persist(arg);
    }

    public List<SysGroup> findGroupsByAlertId(Long alertId) {
        String jpql = """
                SELECT g FROM AlertInstanceGroup arg
                JOIN arg.group g
                WHERE arg.alert.id = :alertId
                """;
        return entityManager.createQuery(jpql, SysGroup.class).setParameter("alertId", alertId).getResultList();
    }
}
