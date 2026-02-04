package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.SysFunctionDto;
import com.iviet.ivshs.dto.SysGroupDto;
import com.iviet.ivshs.dto.SysGroupWithClientStatusDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysGroup;

@Repository
public class SysGroupDao extends BaseTranslatableEntityDao<SysGroup> {

	private final String GROUP_DTO = SysGroupDto.class.getName();
	private final String FUNC_DTO = SysFunctionDto.class.getName();
	private final String CLIENT_DTO = ClientDto.class.getName();

	public SysGroupDao() {
		super(SysGroup.class);
	}

	// =========================================================================
	// 1. NHÓM QUYỀN (SYS_GROUP)
	// =========================================================================

	public Optional<SysGroup> findEntityByCode(String groupCode) {
		return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("groupCode"), groupCode));
	}

	public boolean existsByCode(String groupCode) {
		return exists(root -> entityManager.getCriteriaBuilder().equal(root.get("groupCode"), groupCode));
	}

	public Optional<SysGroupDto> findByCode(String groupCode, String langCode) {
		String jpql = "SELECT new %s(g.id, g.groupCode, glan.name, glan.description) FROM SysGroup g " +
					  "LEFT JOIN g.translations glan ON glan.langCode = :langCode WHERE g.groupCode = :groupCode";
		return entityManager.createQuery(String.format(jpql, GROUP_DTO), SysGroupDto.class)
				.setParameter("groupCode", groupCode).setParameter("langCode", langCode)
				.getResultList().stream().findFirst();
	}

	public Optional<SysGroupDto> findById(Long groupId, String langCode) {
		String jpql = "SELECT new %s(g.id, g.groupCode, glan.name, glan.description) FROM SysGroup g " +
					  "LEFT JOIN g.translations glan ON glan.langCode = :langCode WHERE g.id = :groupId";
		return entityManager.createQuery(String.format(jpql, GROUP_DTO), SysGroupDto.class)
				.setParameter("groupId", groupId).setParameter("langCode", langCode)
				.getResultList().stream().findFirst();
	}

	public List<SysGroupDto> findAll(String langCode) {
		return findAll(0, Integer.MAX_VALUE, langCode);
	}

	public List<SysGroupDto> findAll(int page, int size, String langCode) {
		String jpql = "SELECT new %s(g.id, g.groupCode, glan.name, glan.description) FROM SysGroup g " +
					  "LEFT JOIN g.translations glan ON glan.langCode = :langCode ORDER BY g.groupCode ASC";
		return entityManager.createQuery(String.format(jpql, GROUP_DTO), SysGroupDto.class)
				.setParameter("langCode", langCode).setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	// =========================================================================
	// 2. CHỨC NĂNG (FUNCTIONS) CỦA NHÓM
	// =========================================================================

	public List<SysFunctionDto> findFunctionsByGroupId(Long groupId, String langCode) {
		return findFunctionsByGroupId(groupId, langCode, 0, Integer.MAX_VALUE);
	}

	public List<SysFunctionDto> findFunctionsByGroupId(Long groupId, String langCode, int page, int size) {
		String jpql = "SELECT new %s(f.id, f.functionCode, flan.name, flan.description) FROM SysGroup g " +
					  "JOIN g.roles r JOIN r.function f " +
					  "LEFT JOIN f.translations flan ON flan.langCode = :langCode " +
					  "WHERE g.id = :groupId ORDER BY f.functionCode ASC";
		return entityManager.createQuery(String.format(jpql, FUNC_DTO), SysFunctionDto.class)
				.setParameter("groupId", groupId).setParameter("langCode", langCode)
				.setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	// =========================================================================
	// 3. NGƯỜI DÙNG (CLIENTS) CỦA NHÓM
	// =========================================================================

	public List<ClientDto> findClientsByGroupId(Long groupId) {
		return findClientsByGroupId(groupId, 0, Integer.MAX_VALUE);
	}

	public List<ClientDto> findClientsByGroupId(Long groupId, int page, int size) {
		String jpql = "SELECT new %s(c.id, c.username, c.clientType, c.ipAddress, c.macAddress, c.avatarUrl, c.lastLoginAt) " +
					  "FROM SysGroup g JOIN g.clients c WHERE g.id = :groupId ORDER BY c.username ASC";
		return entityManager.createQuery(String.format(jpql, CLIENT_DTO), ClientDto.class)
				.setParameter("groupId", groupId).setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	public List<Client> findClientEntitiesByGroupId(Long groupId) {
		return findClientEntitiesByGroupId(groupId, 0, Integer.MAX_VALUE);
	}

	public List<Client> findClientEntitiesByGroupId(Long groupId, int page, int size) {
		String jpql = "SELECT c FROM SysGroup g JOIN g.clients c WHERE g.id = :groupId ORDER BY c.username ASC";
		return entityManager.createQuery(jpql, Client.class)
				.setParameter("groupId", groupId).setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	// =========================================================================
	// 4. TRUY VẤN THEO CLIENT (REVERSE LOOKUP)
	// =========================================================================

	public List<SysGroupDto> findAllByClientId(Long clientId, String langCode) {
		return findAllByClientId(clientId, langCode, 0, Integer.MAX_VALUE);
	}

	public List<SysGroupDto> findAllByClientId(Long clientId, String langCode, int page, int size) {
		String jpql = "SELECT new %s(g.id, g.groupCode, glan.name, glan.description) FROM Client c " +
					  "JOIN c.groups g LEFT JOIN g.translations glan ON glan.langCode = :langCode " +
					  "WHERE c.id = :clientId";
		return entityManager.createQuery(String.format(jpql, GROUP_DTO), SysGroupDto.class)
				.setParameter("clientId", clientId).setParameter("langCode", langCode)
				.setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	public List<SysGroup> findEntitiesByClientId(Long clientId) {
		return findEntitiesByClientId(clientId, 0, Integer.MAX_VALUE);
	}

	public List<SysGroup> findEntitiesByClientId(Long clientId, int page, int size) {
		String jpql = "SELECT g FROM Client c JOIN c.groups g WHERE c.id = :clientId";
		return entityManager.createQuery(jpql, SysGroup.class)
				.setParameter("clientId", clientId).setFirstResult(page * size).setMaxResults(size).getResultList();
	}

	// =========================================================================
	// 5. THỐNG KÊ (COUNTING)
	// =========================================================================

	public long countAll() {
		return entityManager.createQuery("SELECT COUNT(g) FROM SysGroup g", Long.class).getSingleResult();
	}

	public long countFunctionsByGroupId(Long groupId) {
		String jpql = "SELECT COUNT(f) FROM SysGroup g JOIN g.roles r JOIN r.function f WHERE g.id = :groupId";
		return entityManager.createQuery(jpql, Long.class).setParameter("groupId", groupId).getSingleResult();
	}

	public long countClientsByGroupId(Long groupId) {
		String jpql = "SELECT COUNT(c) FROM SysGroup g JOIN g.clients c WHERE g.id = :groupId";
		return entityManager.createQuery(jpql, Long.class).setParameter("groupId", groupId).getSingleResult();
	}

	public Long countAllByClientId(Long clientId) {
		String jpql = "SELECT COUNT(g) FROM Client c JOIN c.groups g WHERE c.id = :clientId";
		return entityManager.createQuery(jpql, Long.class).setParameter("clientId", clientId).getSingleResult();
	}

	public List<com.iviet.ivshs.dto.SysGroupWithClientStatusDto> findAllWithClientStatus(Long clientId, String langCode) {
		String dtoPath = SysGroupWithClientStatusDto.class.getName();

		String jpql = """
			SELECT new %s(
				g.id,
				g.groupCode,
				glan.name,
				glan.description,
				CASE WHEN cg.id IS NOT NULL THEN true ELSE false END
			)
			FROM SysGroup g
			LEFT JOIN g.translations glan ON glan.langCode = :langCode
			LEFT JOIN g.clients cg ON cg.id = :clientId
			ORDER BY g.groupCode ASC
		""".formatted(dtoPath);
		
		return entityManager.createQuery(jpql, com.iviet.ivshs.dto.SysGroupWithClientStatusDto.class)
				.setParameter("clientId", clientId)
				.setParameter("langCode", langCode)
				.getResultList();
	}
}
