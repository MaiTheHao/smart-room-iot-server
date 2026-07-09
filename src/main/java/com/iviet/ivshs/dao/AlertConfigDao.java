package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.dto.AlertConfigFilterDto;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertConfigGroup;
import com.iviet.ivshs.entities.SysGroup;
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

    /** Lấy danh sách groupCode liên kết với một configId. */
    public List<String> findGroupCodesByConfigId(Long configId) {
        String jpql = "SELECT acg.group.groupCode FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :configId";
        return entityManager.createQuery(jpql, String.class)
                .setParameter("configId", configId)
                .getResultList();
    }

    /** Lưu liên kết giữa AlertConfig và SysGroup. */
    public void saveGroupAssociation(AlertConfig config, SysGroup group) {
        AlertConfigGroup acg = AlertConfigGroup.builder()
                .alertConfig(config)
                .group(group)
                .build();
        entityManager.persist(acg);
    }

    /** Xóa các liên kết group của một alert config. */
    public int deleteGroupAssociations(Long configId) {
        String jpql = "DELETE FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :configId";
        return entityManager.createQuery(jpql)
                .setParameter("configId", configId)
                .executeUpdate();
    }

    /** Lấy danh sách SysGroup liên kết với một configId. */
    public List<SysGroup> findGroupsByConfigId(Long alertConfigId) {
        String jpql = "SELECT acg.group FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :id";
        return entityManager.createQuery(jpql, SysGroup.class)
                .setParameter("id", alertConfigId)
                .getResultList();
    }

    /** Lấy danh sách AlertConfigGroup fetch group liên kết với danh sách configIds. Tránh N+1 query. */
    public List<AlertConfigGroup> findAssociationsByConfigIds(List<Long> configIds) {
        if (configIds == null || configIds.isEmpty()) return List.of();
        String jpql = "SELECT acg FROM AlertConfigGroup acg JOIN FETCH acg.group WHERE acg.alertConfig.id IN :configIds";
        return entityManager.createQuery(jpql, AlertConfigGroup.class)
                .setParameter("configIds", configIds)
                .getResultList();
    }

    /** Lấy tất cả AlertConfig (có thể filter theo namespace optional). Dùng cho trang manage độc lập. */
    public List<AlertConfig> findAll(AlertNamespace namespace, int page, int size) {
        StringBuilder jpql = new StringBuilder("SELECT ac FROM AlertConfig ac WHERE 1=1");
        if (namespace != null) jpql.append(" AND ac.namespace = :namespace");
        jpql.append(" ORDER BY ac.id DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertConfig.class);
        if (namespace != null) q.setParameter("namespace", namespace);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    /** Đếm tổng AlertConfig (có thể filter theo namespace optional). */
    public long countAll(AlertNamespace namespace) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ac) FROM AlertConfig ac WHERE 1=1");
        if (namespace != null) jpql.append(" AND ac.namespace = :namespace");

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (namespace != null) q.setParameter("namespace", namespace);
        return q.getSingleResult();
    }

    public List<AlertConfig> findAllByFilter(AlertConfigFilterDto filter) {
        StringBuilder jpql = new StringBuilder("SELECT ac FROM AlertConfig ac WHERE 1=1");
        if (filter.namespace() != null) {
            jpql.append(" AND ac.namespace = :namespace");
        }
        if (filter.alertCode() != null && !filter.alertCode().isBlank()) {
            jpql.append(" AND ac.alertCode = :alertCode");
        }
        if (filter.sourceId() != null && !filter.sourceId().isBlank()) {
            jpql.append(" AND ac.sourceId = :sourceId");
        }
        jpql.append(" ORDER BY ac.id DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertConfig.class);
        if (filter.namespace() != null) {
            q.setParameter("namespace", filter.namespace());
        }
        if (filter.alertCode() != null && !filter.alertCode().isBlank()) {
            q.setParameter("alertCode", filter.alertCode());
        }
        if (filter.sourceId() != null && !filter.sourceId().isBlank()) {
            q.setParameter("sourceId", filter.sourceId());
        }
        return q.setFirstResult(filter.page() * filter.size())
                .setMaxResults(filter.size())
                .getResultList();
    }

    public long countAllByFilter(AlertConfigFilterDto filter) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ac) FROM AlertConfig ac WHERE 1=1");
        if (filter.namespace() != null) {
            jpql.append(" AND ac.namespace = :namespace");
        }
        if (filter.alertCode() != null && !filter.alertCode().isBlank()) {
            jpql.append(" AND ac.alertCode = :alertCode");
        }
        if (filter.sourceId() != null && !filter.sourceId().isBlank()) {
            jpql.append(" AND ac.sourceId = :sourceId");
        }

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (filter.namespace() != null) {
            q.setParameter("namespace", filter.namespace());
        }
        if (filter.alertCode() != null && !filter.alertCode().isBlank()) {
            q.setParameter("alertCode", filter.alertCode());
        }
        if (filter.sourceId() != null && !filter.sourceId().isBlank()) {
            q.setParameter("sourceId", filter.sourceId());
        }
        return q.getSingleResult();
    }
}
