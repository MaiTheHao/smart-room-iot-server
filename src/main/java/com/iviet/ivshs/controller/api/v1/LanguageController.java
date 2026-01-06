package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/languages")
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<LanguageDto>>> getLanguages(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(languageService.getList(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LanguageDto>> getLanguageById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(languageService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<LanguageDto>> getLanguageByCode(
            @PathVariable(name = "code") String code) {
        
        return ResponseEntity.ok(ApiResponse.ok(languageService.getByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LanguageDto>> createLanguage(
            @RequestBody @Valid CreateLanguageDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(languageService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LanguageDto>> updateLanguage(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateLanguageDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(languageService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLanguage(
            @PathVariable(name = "id") Long id) {
        
        languageService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}