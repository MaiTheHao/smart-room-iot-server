package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.SysClientFunctionCacheDao;
import com.iviet.ivshs.enumeration.SysFunctionEnum;
import com.iviet.ivshs.exception.domain.ForbiddenException;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.util.FunctionCodeHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private SysClientFunctionCacheDao cacheDao;

	@Override
	@Transactional(readOnly = true)
	public boolean hasPermission(Long clientId, String functionCode) {
		if (functionCode == null || functionCode.isBlank()) return false;
		return cacheDao.hasPermission(clientId, functionCode);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPermissions(Long clientId, List<String> functionCodes) {
		if (functionCodes == null || functionCodes.isEmpty()) return false;
		for (String functionCode : functionCodes) {
			if (!hasPermission(clientId, functionCode)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public Set<String> getPermissions(Long clientId) {
		List<String> functionCodes = cacheDao.getFunctionCodesByClient(clientId);
		return new HashSet<>(functionCodes);
	}

	@Override
	@Transactional(readOnly = true)
	public long countPermissions(Long clientId) {
		return cacheDao.countDistinctFunctionsByClient(clientId);
	}

	@Override
	@Transactional(readOnly = true)
	public void checkAccessToFloor(Long clientId, String floorCode) {
		Set<String> permissions = getPermissions(clientId);
		if (permissions.contains(SysFunctionEnum.F_ACCESS_FLOOR_ALL.getCode())) return;
		String specificFunc = FunctionCodeHelper.buildFloorAccessCode(floorCode);
		if (!permissions.contains(specificFunc)) {
			throw new ForbiddenException("Access to floor " + floorCode + " is denied");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public void checkAccessToRoom(Long clientId, String roomCode) {
		Set<String> permissions = getPermissions(clientId);
		if (permissions.contains(SysFunctionEnum.F_ACCESS_ROOM_ALL.getCode())) return;
		String specificFunc = FunctionCodeHelper.buildRoomAccessCode(roomCode);
		if (!permissions.contains(specificFunc)) {
			throw new ForbiddenException("Access to room " + roomCode + " is denied");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Set<String> getAccessFloorCodes(Long clientId) {
		Set<String> permissions = getPermissions(clientId);
		Set<String> floorCodes = new HashSet<>();

		if (permissions.contains(SysFunctionEnum.F_ACCESS_FLOOR_ALL.getCode())) {
			floorCodes.add(PermissionService.ACCESS_ALL);
			return floorCodes;
		}

		for (String funcCode : permissions) {
			if (FunctionCodeHelper.isFloorAccessCode(funcCode)) {
				String floorCode = FunctionCodeHelper.extractFloorCode(funcCode);
				if (floorCode != null) {
					floorCodes.add(floorCode);
				}
			}
		}
		return floorCodes;
	}

	@Override
	@Transactional(readOnly = true)
	public Set<String> getAccessRoomCodes(Long clientId) {
		Set<String> permissions = getPermissions(clientId);
		Set<String> roomCodes = new HashSet<>();

		if (permissions.contains(SysFunctionEnum.F_ACCESS_ROOM_ALL.getCode())) {
			roomCodes.add(PermissionService.ACCESS_ALL);
			return roomCodes;
		}

		for (String funcCode : permissions) {
			if (FunctionCodeHelper.isRoomAccessCode(funcCode)) {
				String roomCode = FunctionCodeHelper.extractRoomCode(funcCode);
				if (roomCode != null) {
					roomCodes.add(roomCode);
				}
			}
		}
		return roomCodes;
	}
}
