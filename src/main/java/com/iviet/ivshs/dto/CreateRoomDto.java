package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateRoomDto(
    @NotBlank(message = "Room name is required")
    @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters")
    String name,

    @NotBlank(message = "Room code is required")
    @Size(max = 256)
    String code,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    @NotNull(message = "Floor ID is required")
    Long floorId,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {
    public Room toEntity() {
        Room room = new Room();
        room.setCode(this.code);
        return room;
    }
}