package com.iviet.ivshs.service.fan;

import java.util.List;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.fan.CreateFanDto;
import com.iviet.ivshs.dto.fan.FanDto;
import com.iviet.ivshs.dto.fan.UpdateFanDto;

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

  Long countByRoomId(Long roomId);

}
