package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.Floor;
import com.iviet.ivshs.entities.RoomLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.RoomMapper;
import com.iviet.ivshs.service.FloorService;
import com.iviet.ivshs.service.RoomService;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomDao roomDao;
    private final FloorDao floorDao;
    private final FloorService floorService;
    private final LanguageDao languageDao;
    private final RoomMapper roomMapper;
    private final PermissionService permissionService;

    @Override
    public PaginatedResponse<RoomDto> getListByFloor(Long floorId, int page, int size) {
        floorService.getEntityById(floorId);

        String langCode = LocalContextUtil.getCurrentLangCode();
        List<RoomDto> rooms = roomDao.findAllByFloorId(floorId, page, size, langCode);
       
        Set<String> accessibleRoomCodes = permissionService.getAccessibleRoomCodes();
        if (!accessibleRoomCodes.contains(PermissionService.ACCESS_ALL)) rooms.removeIf(room -> !accessibleRoomCodes.contains(room.code()));

        long total = roomDao.countByFloorId(floorId);
        return new PaginatedResponse<>(rooms, page, size, total);
    }

    @Override
    public PaginatedResponse<RoomDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<RoomDto> rooms = roomDao.findAll(page, size, langCode);

        Set<String> accessibleRoomCodes = permissionService.getAccessibleRoomCodes();
        if (!accessibleRoomCodes.contains(PermissionService.ACCESS_ALL)) rooms.removeIf(room -> !accessibleRoomCodes.contains(room.code()));

        return new PaginatedResponse<>(
                rooms,
                page, size, roomDao.count()
        );
    }

    @Override
    public List<RoomDto> getAllByFloor(Long floorId) {
        floorService.getEntityById(floorId);

        String langCode = LocalContextUtil.getCurrentLangCode();
        List<RoomDto> rooms = roomDao.findAllByFloorId(floorId, langCode);

        Set<String> accessibleRoomCodes = permissionService.getAccessibleRoomCodes();
        if (!accessibleRoomCodes.contains(PermissionService.ACCESS_ALL)) rooms.removeIf(room -> !accessibleRoomCodes.contains(room.code()));

        return rooms;
    } 

    @Override
    public List<RoomDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<RoomDto> rooms = roomDao.findAll(langCode);

        Set<String> accessibleRoomCodes = permissionService.getAccessibleRoomCodes();
        if (!accessibleRoomCodes.contains(PermissionService.ACCESS_ALL)) rooms.removeIf(room -> !accessibleRoomCodes.contains(room.code()));

        return rooms;
    }

    @Override
    public RoomDto getById(Long roomId) {
        RoomDto roomDto = roomDao.findById(roomId, LocalContextUtil.getCurrentLangCode()).orElseThrow(() -> new NotFoundException("Room not found with ID: " + roomId));
        permissionService.requireAccessRoom(roomDto.code());
        return roomDto;
    }

    @Override
    @Transactional
    public RoomDto create(Long floorId, CreateRoomDto dto) {
        Floor floor = floorService.getEntityById(floorId);

        permissionService.requireManageRoom();

        if (dto == null || !StringUtils.hasText(dto.code())) {
            throw new BadRequestException("Room data and code are required");
        }

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        _checkDuplicate(dto.code().trim(), null);

        Room room = roomMapper.fromCreateDto(dto);
        room.setCode(dto.code().trim());
        room.setFloor(floor);

        RoomLan roomLan = new RoomLan();
        roomLan.setLangCode(langCode);
        roomLan.setName(dto.name() != null ? dto.name().trim() : "");
        roomLan.setDescription(dto.description());
        roomLan.setOwner(room);

        room.getTranslations().add(roomLan);
        roomDao.save(room);

        return roomMapper.toDto(room, roomLan);
    }

    @Override
    @Transactional
    public RoomDto update(Long roomId, UpdateRoomDto dto) {
        permissionService.requireManageRoom();

        Room room = roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with ID: " + roomId));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        if (StringUtils.hasText(dto.code()) && !dto.code().trim().equals(room.getCode())) {
            _checkDuplicate(dto.code().trim(), roomId);
            room.setCode(dto.code().trim());
        }

        if (dto.floorId() != null && !dto.floorId().equals(room.getFloor().getId())) {
            Floor newFloor = floorDao.findById(dto.floorId())
                    .orElseThrow(() -> new NotFoundException("Target floor not found: " + dto.floorId()));
            room.setFloor(newFloor);
        }

        RoomLan roomLan = room.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    RoomLan newLan = new RoomLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(room);
                    room.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) {
            roomLan.setName(dto.name().trim());
        }
        if (dto.description() != null) {
            roomLan.setDescription(dto.description());
        }

        roomDao.save(room);
        return roomMapper.toDto(room, roomLan);
    }

    @Override
    @Transactional
    public void delete(Long roomId) {
        permissionService.requireManageRoom();

        if (!roomDao.existsById(roomId)) throw new NotFoundException("Room not found");
        
        roomDao.deleteById(roomId);
    }

    private void _checkDuplicate(String code, Long currentId) {
        roomDao.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Room code already exists: " + code);
            }
        });
    }

    @Override
    public Room getEntityById(Long roomId) {
        Room room = roomDao.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found with ID: " + roomId));
        permissionService.requireAccessRoom(room.getCode());
        return room;
    }

    @Override
    public RoomDto getByCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) throw new BadRequestException("Room code is required");
        
        RoomDto roomDto = roomDao.findByCode(roomCode, LocalContextUtil.getCurrentLangCode()).orElseThrow(() -> new NotFoundException("Room not found with code: " + roomCode));
        permissionService.requireAccessRoom(roomDto.code());
        return roomDto;
    }

    @Override
    public Room getEntityByCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) throw new BadRequestException("Room code is required");
        
        Room room = roomDao.findByCode(roomCode).orElseThrow(() -> new NotFoundException("Room not found with code: " + roomCode));
        permissionService.requireAccessRoom(room.getCode());
        return room;
    }
}