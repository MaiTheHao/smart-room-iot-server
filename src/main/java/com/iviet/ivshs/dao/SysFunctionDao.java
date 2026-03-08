package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.SysFunctionDto;
import com.iviet.ivshs.dto.SysFunctionWithGroupStatusDto;
import com.iviet.ivshs.entities.SysFunction;

@Repository
public class SysFunctionDao extends BaseTranslatableEntityDao<SysFunction> {

  public SysFunctionDao() {
    super(SysFunction.class);
  }

  public Optional<SysFunction> findByCode(String functionCode) {
    return findOne(root -> entityManager.getCriteriaBuilder()
      .equal(root.get("functionCode"), functionCode));
  }

  public Optional<SysFunctionDto> findByCode(String functionCode, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      WHERE f.functionCode = :functionCode
      """.formatted(dtoClassPath);

    List<SysFunctionDto> results = entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("functionCode", functionCode)
      .setParameter("langCode", langCode)
      .setMaxResults(1)
      .getResultList();

    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public boolean existsByCode(String functionCode) {
    return exists(root -> entityManager.getCriteriaBuilder()
      .equal(root.get("functionCode"), functionCode));
  }

  public Optional<SysFunctionDto> findById(Long functionId, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      WHERE f.id = :functionId
      """.formatted(dtoClassPath);

    List<SysFunctionDto> results = entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("functionId", functionId)
      .setParameter("langCode", langCode)
      .setMaxResults(1)
      .getResultList();

    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public List<SysFunctionDto> findAll(String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("langCode", langCode)
      .getResultList();
  }

  public List<SysFunctionDto> findAll(int page, int size, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("langCode", langCode)
      .setFirstResult(page * size)
      .setMaxResults(size)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByGroupId(Long groupId, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      WHERE r.group.id = :groupId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("groupId", groupId)
      .setParameter("langCode", langCode)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByGroupId(Long groupId, String langCode, int page, int size) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      WHERE r.group.id = :groupId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("groupId", groupId)
      .setParameter("langCode", langCode)
      .setFirstResult(page * size)
      .setMaxResults(size)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByGroupCode(String groupCode, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      JOIN r.group g
      WHERE g.groupCode = :groupCode
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("groupCode", groupCode)
      .setParameter("langCode", langCode)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByGroupCode(String groupCode, String langCode, int page, int size) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      JOIN r.group g
      WHERE g.groupCode = :groupCode
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("groupCode", groupCode)
      .setParameter("langCode", langCode)
      .setFirstResult(page * size)
      .setMaxResults(size)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByClientId(Long clientId, String langCode) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT DISTINCT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      JOIN r.group g
      JOIN g.clients c
      WHERE c.id = :clientId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("clientId", clientId)
      .setParameter("langCode", langCode)
      .getResultList();
  }

  public List<SysFunctionDto> findAllByClientId(Long clientId, String langCode, int page, int size) {
    String dtoClassPath = SysFunctionDto.class.getName();

    String jpql = """
      SELECT DISTINCT new %s(f.id, f.functionCode, flan.name, flan.description)
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      JOIN f.roles r
      JOIN r.group g
      JOIN g.clients c
      WHERE c.id = :clientId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionDto.class)
      .setParameter("clientId", clientId)
      .setParameter("langCode", langCode)
      .setFirstResult(page * size)
      .setMaxResults(size)
      .getResultList();
  }

  public List<SysFunctionWithGroupStatusDto> findAllWithGroupStatus(Long groupId, String langCode) {
    String dtoClassPath = SysFunctionWithGroupStatusDto.class.getName();

    String jpql = """
      SELECT new %s(
        f.id,
        f.functionCode,
        flan.name,
        flan.description,
        CASE WHEN r.id IS NOT NULL THEN true ELSE false END,
        r.id
      )
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      LEFT JOIN f.roles r ON r.group.id = :groupId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionWithGroupStatusDto.class)
      .setParameter("groupId", groupId)
      .setParameter("langCode", langCode)
      .getResultList();
  }

  public List<SysFunctionWithGroupStatusDto> findAllWithGroupStatus(
      Long groupId, String langCode, int page, int size) {
    String dtoClassPath = SysFunctionWithGroupStatusDto.class.getName();

    String jpql = """
      SELECT new %s(
        f.id,
        f.functionCode,
        flan.name,
        flan.description,
        CASE WHEN r.id IS NOT NULL THEN true ELSE false END,
        r.id
      )
      FROM SysFunction f
      LEFT JOIN f.translations flan ON flan.langCode = :langCode
      LEFT JOIN f.roles r ON r.group.id = :groupId
      ORDER BY f.functionCode ASC
      """.formatted(dtoClassPath);

    return entityManager.createQuery(jpql, SysFunctionWithGroupStatusDto.class)
      .setParameter("groupId", groupId)
      .setParameter("langCode", langCode)
      .setFirstResult(page * size)
      .setMaxResults(size)
      .getResultList();
  }

  public long countAll() {
    String jpql = "SELECT COUNT(f) FROM SysFunction f";
    return entityManager.createQuery(jpql, Long.class)
      .getSingleResult();
  }

  public long countByGroupId(Long groupId) {
    String jpql = """
      SELECT COUNT(DISTINCT f.id)
      FROM SysFunction f
      JOIN f.roles r
      WHERE r.group.id = :groupId
      """;

    return entityManager.createQuery(jpql, Long.class)
      .setParameter("groupId", groupId)
      .getSingleResult();
  }

  public long countByGroupCode(String groupCode) {
    String jpql = """
      SELECT COUNT(DISTINCT f.id)
      FROM SysFunction f
      JOIN f.roles r
      JOIN r.group g
      WHERE g.groupCode = :groupCode
      """;

    return entityManager.createQuery(jpql, Long.class)
      .setParameter("groupCode", groupCode)
      .getSingleResult();
  }

  public long countByClientId(Long clientId) {
    String jpql = """
      SELECT COUNT(DISTINCT f.id)
      FROM SysFunction f
      JOIN f.roles r
      JOIN r.group g
      JOIN g.clients c
      WHERE c.id = :clientId
      """;

    return entityManager.createQuery(jpql, Long.class)
      .setParameter("clientId", clientId)
      .getSingleResult();
  }
}
