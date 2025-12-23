package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateLanguageDtoV1;
import com.iviet.ivshs.dto.LanguageDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateLanguageDtoV1;

public interface LanguageServiceV1 {
    PaginatedResponseV1<LanguageDtoV1> getList(int page, int size);
    LanguageDtoV1 getById(Long langId);
    LanguageDtoV1 getByCode(String code);
    LanguageDtoV1 create(CreateLanguageDtoV1 language);
    LanguageDtoV1 update(Long langId, UpdateLanguageDtoV1 language);
    void delete(Long langId);
}
