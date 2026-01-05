package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.dao.ClientDaoV1;
import com.iviet.ivshs.dao.SysClientFunctionCacheDaoV1;
import com.iviet.ivshs.dao.SysGroupDaoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.SysClientFunctionCacheV1;
import com.iviet.ivshs.entities.SysGroupV1;
import com.iviet.ivshs.entities.SysRoleV1;
import com.iviet.ivshs.service.ClientFunctionCacheServiceV1;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class ClientFunctionServiceImplV1 implements ClientFunctionCacheServiceV1 {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private SysClientFunctionCacheDaoV1 cacheDao;

	@Autowired
	private ClientDaoV1 clientDao;

	@Autowired
	private SysGroupDaoV1 groupDao;

	@Override
	public int rebuildCacheForClient(Long clientId) {
		cacheDao.deleteByClient(clientId);

		ClientV1 client = clientDao.findById(clientId)
			.orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
		
		int createdCount = 0;
		for (SysGroupV1 group : client.getGroups()) {
			for (SysRoleV1 role : group.getRoles()) {
				if (Boolean.TRUE.equals(role.getIsActive())) {
					SysClientFunctionCacheV1 cache = new SysClientFunctionCacheV1();
					cache.setClientId(clientId);
					cache.setFunctionCode(role.getFunction().getFunctionCode());
					cache.setGroupId(group.getId());
					
					if (!cacheDao.exists(clientId, role.getFunction().getFunctionCode(), group.getId())) {
						cacheDao.save(cache);
						createdCount++;
					}
				}
			}
		}
		
		return createdCount;
	}

	@Override
	public int rebuildCacheForGroup(Long groupId) {
		SysGroupV1 group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int totalRebuilt = 0;
		for (ClientV1 client : group.getClients()) {
			int count = rebuildCacheForClient(client.getId());
			totalRebuilt += count;
		}
		
		return totalRebuilt;
	}

	@Override
	public int rebuildAllCache() {
		entityManager.createNativeQuery("DELETE FROM sys_client_function_cache_v1").executeUpdate();
		
		var cb = entityManager.getCriteriaBuilder();
		var query = cb.createQuery(ClientV1.class);
		query.from(ClientV1.class);
		List<ClientV1> allClients = entityManager.createQuery(query).getResultList();
		
		int totalCreated = 0;
		
		for (ClientV1 client : allClients) {
			for (SysGroupV1 group : client.getGroups()) {
				for (SysRoleV1 role : group.getRoles()) {
					if (Boolean.TRUE.equals(role.getIsActive())) {
						SysClientFunctionCacheV1 cache = new SysClientFunctionCacheV1();
						cache.setClientId(client.getId());
						cache.setFunctionCode(role.getFunction().getFunctionCode());
						cache.setGroupId(group.getId());
						
						if (!cacheDao.exists(client.getId(), role.getFunction().getFunctionCode(), group.getId())) {
							cacheDao.save(cache);
							totalCreated++;
						}
					}
				}
			}
		}
		
		return totalCreated;
	}

	@Override
	public int clearCacheForClientGroup(Long clientId, Long groupId) {
		int deletedCount = cacheDao.deleteByClientAndGroup(clientId, groupId);
		return deletedCount;
	}

	@Override
	public int clearCacheForClient(Long clientId) {
		int deletedCount = cacheDao.deleteByClient(clientId);
		return deletedCount;
	}

	@Override
	public int clearCacheForGroup(Long groupId) {
		int deletedCount = cacheDao.deleteByGroup(groupId);
		return deletedCount;
	}

	@Override
	public int clearCacheForFunction(String functionCode) {
		int deletedCount = cacheDao.deleteByFunctionCode(functionCode);
		return deletedCount;
	}

	@Override
	public int addPermissionsForClientGroup(Long clientId, Long groupId) {
		SysGroupV1 group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int addedCount = 0;
		for (SysRoleV1 role : group.getRoles()) {
			if (Boolean.TRUE.equals(role.getIsActive())) {
				if (!cacheDao.exists(clientId, role.getFunction().getFunctionCode(), groupId)) {
					SysClientFunctionCacheV1 cache = new SysClientFunctionCacheV1();
					cache.setClientId(clientId);
					cache.setFunctionCode(role.getFunction().getFunctionCode());
					cache.setGroupId(groupId);
					cacheDao.save(cache);
					addedCount++;
				}
			}
		}
		
		return addedCount;
	}

	@Override
	public int addPermissionsForGroupFunction(Long groupId, String functionCode) {
		SysGroupV1 group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int addedCount = 0;
		for (ClientV1 client : group.getClients()) {
			if (!cacheDao.exists(client.getId(), functionCode, groupId)) {
				SysClientFunctionCacheV1 cache = new SysClientFunctionCacheV1();
				cache.setClientId(client.getId());
				cache.setFunctionCode(functionCode);
				cache.setGroupId(groupId);
				cacheDao.save(cache);
				addedCount++;
			}
		}
		
		return addedCount;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean validateCache(Long clientId) {
		Set<String> cachedPermissions = new HashSet<>(cacheDao.getFunctionCodesByClient(clientId));
		ClientV1 client = clientDao.findById(clientId)
			.orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
		
		Set<String> actualPermissions = client.getGroups().stream()
			.flatMap(group -> group.getRoles().stream())
			.filter(role -> Boolean.TRUE.equals(role.getIsActive()))
			.map(role -> role.getFunction().getFunctionCode())
			.collect(Collectors.toSet());
		
		return cachedPermissions.equals(actualPermissions);
	}
}
