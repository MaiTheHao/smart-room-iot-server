package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.ClientServiceV1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
@Validated
public class ClientControllerV1 {

    @Autowired
    private ClientServiceV1 clientService;

    // --- CRUD CLIENT ---

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ClientDto>>> 
            getClients(
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponse<ClientDto> paginated = 
            clientService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(paginated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientDto>> getById(
            @PathVariable(name = "id") Long id) {
        ClientDto dto = clientService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<ClientDto>>> 
            getAllByRoomId(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponse<ClientDto> paginated = 
            clientService.getAllGatewaysByRoomId(roomId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(paginated));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientDto>> create(
            @RequestBody @Valid CreateClientDto request) {
        ClientDto created = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateClientDto request) {
        ClientDto updated = clientService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        clientService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, 
                "Deleted successfully"));
    }
}
