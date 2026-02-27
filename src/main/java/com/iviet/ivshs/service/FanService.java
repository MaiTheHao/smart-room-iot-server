package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;

public interface FanService {
    
    PaginatedResponse<FanDto> getList(int page, int size);

    List<FanDto> getAll();
    
    PaginatedResponse<FanDto> getListByRoomId(Long roomId, int page, int size);

    List<FanDto> getAllByRoomId(Long roomId);

    FanDto getByRoomAndNaturalId(Long roomId, String naturalId);
    
    FanDto getById(Long id);
    
    FanDto create(CreateFanDto dto);
    
    FanDto update(Long id, UpdateFanDto dto);
    
    void delete(Long id);


}
