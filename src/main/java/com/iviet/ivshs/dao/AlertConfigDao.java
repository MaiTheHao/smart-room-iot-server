package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlertConfigDao extends BaseAuditEntityDao<AlertConfig> {

    public AlertConfigDao() {
        super(AlertConfig.class);
    }

    /** Tìm config theo namespace + sourceId (dùng để lấy configs của một Rule). */
    public List<AlertConfig> findAllByNamespaceAndSourceId(AlertNamespace namespace, String sourceId) {
        String jpql = "SELECT ac FROM AlertConfig ac WHERE ac.namespace = :namespace AND ac.sourceId = :sourceId";
        return entityManager.createQuery(jpql, AlertConfig.class)
                .setParameter("namespace", namespace)
                .setParameter("sourceId", sourceId)
                .getResultList();
    }

    /** Tìm config cụ thể theo composite key đa hình. */
    public Optional<AlertConfig> findByPolymorphicKey(AlertNamespace namespace, String alertCode, String sourceId) {
        String jpql = """
            SELECT ac FROM AlertConfig ac
            WHERE ac.namespace = :namespace
              AND ac.alertCode = :alertCode
              AND ac.sourceId = :sourceId
            """;
        return entityManager.createQuery(jpql, AlertConfig.class)
                .setParameter("namespace", namespace)
                .setParameter("alertCode", alertCode)
                .setParameter("sourceId", sourceId)
                .getResultStream().findFirst();
    }

    /** Xóa tất cả configs theo namespace + sourceId (dùng khi xóa Rule). */
    public int deleteAllByNamespaceAndSourceId(AlertNamespace namespace, String sourceId) {
        String jpql = "DELETE FROM AlertConfig ac WHERE ac.namespace = :namespace AND ac.sourceId = :sourceId";
        return entityManager.createQuery(jpql)
                .setParameter("namespace", namespace)
                .setParameter("sourceId", sourceId)
                .executeUpdate();
    }
}
