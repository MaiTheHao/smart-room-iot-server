package com.iviet.ivshs.service.language;

import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.language.CreateLanguageDto;
import com.iviet.ivshs.dto.language.LanguageDto;
import com.iviet.ivshs.dto.language.UpdateLanguageDto;

public interface LanguageService {
    PaginatedResponse<LanguageDto> getList(int page, int size);

    LanguageDto getById(Long langId);

    LanguageDto getByCode(String code);

    LanguageDto create(CreateLanguageDto language);

    LanguageDto update(Long langId, UpdateLanguageDto language);

    void delete(Long langId);
}
