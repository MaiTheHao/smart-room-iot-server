package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDaoV1;
import com.iviet.ivshs.dao.LanguageDaoV1;
import com.iviet.ivshs.dao.RoomDaoV1;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.FloorV1;
import com.iviet.ivshs.entities.RoomLanV1;
import com.iviet.ivshs.entities.RoomV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.RoomMapperV1;
import com.iviet.ivshs.service.RoomServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImplV1 implements RoomServiceV1 {

    private final RoomDaoV1 roomDao;
    private final FloorDaoV1 floorDao;
    private final LanguageDaoV1 languageDao;
    private final RoomMapperV1 roomMapper;

    @Override
    public PaginatedResponseV1<RoomDtoV1> getListByFloor(Long floorId, int page, int size) {
        if (floorId == null) throw new BadRequestException("Floor ID is required");
        
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<RoomDtoV1> content = roomDao.findAllByFloorId(floorId, page, size, langCode);
        
        return new PaginatedResponseV1<>(content, page, size, roomDao.countByFloorId(floorId));
    }

    @Override
    public RoomDtoV1 getById(Long roomId) {
        return roomDao.findById(roomId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + roomId));
    }

    @Override
    @Transactional
    public RoomDtoV1 create(Long floorId, CreateRoomDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.code())) throw new BadRequestException("Room data and code are required");

        FloorV1 floor = floorDao.findById(floorId)
                .orElseThrow(() -> new NotFoundException("Floor not found with ID: " + floorId));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);
        
        _checkDuplicate(dto.code().trim(), null);

        RoomV1 room = roomMapper.fromCreateDto(dto);
        room.setCode(dto.code().trim());
        room.setFloor(floor);

        RoomLanV1 roomLan = new RoomLanV1();
        roomLan.setLangCode(langCode);
        roomLan.setName(dto.name() != null ? dto.name().trim() : "");
        roomLan.setDescription(dto.description());
        roomLan.setRoom(room);

        room.getRoomLans().add(roomLan);
        roomDao.save(room);

        return roomMapper.toDto(room, roomLan);
    }

    @Override
    @Transactional
    public RoomDtoV1 update(Long roomId, UpdateRoomDtoV1 dto) {
        RoomV1 room = roomDao.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + roomId));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);

        if (StringUtils.hasText(dto.code()) && !dto.code().trim().equals(room.getCode())) {
            _checkDuplicate(dto.code().trim(), roomId);
            room.setCode(dto.code().trim());
        }

        if (dto.floorId() != null && !dto.floorId().equals(room.getFloor().getId())) {
            FloorV1 newFloor = floorDao.findById(dto.floorId())
                    .orElseThrow(() -> new NotFoundException("Target floor not found: " + dto.floorId()));
            room.setFloor(newFloor);
        }

        RoomLanV1 roomLan = room.getRoomLans().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    RoomLanV1 newLan = new RoomLanV1();
                    newLan.setLangCode(langCode);
                    newLan.setRoom(room);
                    room.getRoomLans().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) roomLan.setName(dto.name().trim());
        if (dto.description() != null) roomLan.setDescription(dto.description());

        roomDao.save(room);
        return roomMapper.toDto(room, roomLan);
    }

    @Override
    @Transactional
    public void delete(Long roomId) {
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
}