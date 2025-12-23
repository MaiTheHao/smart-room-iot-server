package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.LanguageServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/languages")
public class LanguageControllerV1 {

    private final LanguageServiceV1 languageService;

    @GetMapping
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<LanguageDtoV1>>> getLanguages(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(languageService.getList(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseV1<LanguageDtoV1>> getLanguageById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(languageService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseV1<LanguageDtoV1>> getLanguageByCode(
            @PathVariable(name = "code") String code) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(languageService.getByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<LanguageDtoV1>> createLanguage(
            @RequestBody @Valid CreateLanguageDtoV1 request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(languageService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseV1<LanguageDtoV1>> updateLanguage(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateLanguageDtoV1 request) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(languageService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseV1<Void>> deleteLanguage(
            @PathVariable(name = "id") Long id) {
        
        languageService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}