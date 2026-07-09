package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateClientDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/clients")
@Validated
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  // --- CRUD CLIENT ---

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<ClientDto>> getMe() {
    Client client = clientService.getFromSecurityContext();
    ClientDto dto = clientService.getById(client.getId());
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PaginatedResponse<ClientDto>>> getClients(@RequestParam(name = "page", defaultValue = "0")
  int page, @RequestParam(name = "size", defaultValue = "10")
  int size) {
    PaginatedResponse<ClientDto> paginated = clientService.getList(page, size);
    return ResponseEntity.ok(ApiResponse.ok(paginated));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ClientDto>> getById(@PathVariable(name = "id")
  Long id) {
    ClientDto dto = clientService.getById(id);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<ApiResponse<PaginatedResponse<ClientDto>>> getAllByRoomId(@PathVariable(name = "roomId")
  Long roomId, @RequestParam(name = "page", defaultValue = "0")
  int page, @RequestParam(name = "size", defaultValue = "10")
  int size) {
    PaginatedResponse<ClientDto> paginated = clientService.getListGatewaysByRoomId(roomId, page, size);
    return ResponseEntity.ok(ApiResponse.ok(paginated));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ClientDto>> create(@RequestBody
  @Valid
  CreateClientDto request) {
    ClientDto created = clientService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ClientDto>> update(@PathVariable(name = "id")
  Long id, @RequestBody
  @Valid
  UpdateClientDto request) {
    ClientDto updated = clientService.update(id, request);
    return ResponseEntity.ok(ApiResponse.ok(updated));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<ClientDto>> patch(@PathVariable(name = "id")
  Long id, @RequestBody
  UpdateClientDto request) {
    ClientDto updated = clientService.patchUpdate(id, request);
    return ResponseEntity.ok(ApiResponse.ok(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id")
  Long id) {
    clientService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
  }

  @DeleteMapping("/{id}/device-controls")
  public ResponseEntity<ApiResponse<Void>> deleteAllDeviceControls(@PathVariable(name = "id")
  Long id) {
    clientService.deleteAllHardwareConfig(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "All hardware configurations deleted successfully"));
  }

  @DeleteMapping("/{id}/hardware-configs")
  public ResponseEntity<ApiResponse<Void>> deleteAllHardwareConfigs(@PathVariable(name = "id")
  Long id) {
    clientService.deleteAllHardwareConfig(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "All hardware configurations deleted successfully"));
  }
}
