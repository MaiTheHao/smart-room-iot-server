package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysClientFunctionCacheDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysClientFunctionCache;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.entities.SysRole;
import com.iviet.ivshs.service.ClientFunctionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class ClientFunctionServiceImpl implements ClientFunctionService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private SysClientFunctionCacheDao cacheDao;

	@Autowired
	private ClientDao clientDao;

	@Autowired
	private SysGroupDao groupDao;

	@Override
	public int rebuildCacheForClient(Long clientId) {
		cacheDao.deleteByClient(clientId);

		Client client = clientDao.findById(clientId)
			.orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
		
		int createdCount = 0;
		for (SysGroup group : client.getGroups()) {
			for (SysRole role : group.getRoles()) {
				if (Boolean.TRUE.equals(role.getIsActive())) {
					SysClientFunctionCache cache = new SysClientFunctionCache();
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
		SysGroup group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int totalRebuilt = 0;
		for (Client client : group.getClients()) {
			int count = rebuildCacheForClient(client.getId());
			totalRebuilt += count;
		}
		
		return totalRebuilt;
	}

	@Override
	public int rebuildAllCache() {
		entityManager.createNativeQuery("DELETE FROM sys_client_function_cache").executeUpdate();
		
		var cb = entityManager.getCriteriaBuilder();
		var query = cb.createQuery(Client.class);
		query.from(Client.class);
		List<Client> allClients = entityManager.createQuery(query).getResultList();
		
		int totalCreated = 0;
		
		for (Client client : allClients) {
			for (SysGroup group : client.getGroups()) {
				for (SysRole role : group.getRoles()) {
					if (Boolean.TRUE.equals(role.getIsActive())) {
						SysClientFunctionCache cache = new SysClientFunctionCache();
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
		SysGroup group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int addedCount = 0;
		for (SysRole role : group.getRoles()) {
			if (Boolean.TRUE.equals(role.getIsActive())) {
				if (!cacheDao.exists(clientId, role.getFunction().getFunctionCode(), groupId)) {
					SysClientFunctionCache cache = new SysClientFunctionCache();
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
		SysGroup group = groupDao.findById(groupId)
			.orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
		
		int addedCount = 0;
		for (Client client : group.getClients()) {
			if (!cacheDao.exists(client.getId(), functionCode, groupId)) {
				SysClientFunctionCache cache = new SysClientFunctionCache();
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
		Client client = clientDao.findById(clientId)
			.orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
		
		Set<String> actualPermissions = client.getGroups().stream()
			.flatMap(group -> group.getRoles().stream())
			.filter(role -> Boolean.TRUE.equals(role.getIsActive()))
			.map(role -> role.getFunction().getFunctionCode())
			.collect(Collectors.toSet());
		
		return cachedPermissions.equals(actualPermissions);
	}
}
