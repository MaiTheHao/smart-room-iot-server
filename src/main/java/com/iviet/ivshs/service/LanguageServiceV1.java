package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateLanguageDto;
import com.iviet.ivshs.dto.LanguageDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateLanguageDto;

public interface LanguageServiceV1 {
    PaginatedResponse<LanguageDto> getList(int page, int size);
    LanguageDto getById(Long langId);
    LanguageDto getByCode(String code);
    LanguageDto create(CreateLanguageDto language);
    LanguageDto update(Long langId, UpdateLanguageDto language);
    void delete(Long langId);
}
