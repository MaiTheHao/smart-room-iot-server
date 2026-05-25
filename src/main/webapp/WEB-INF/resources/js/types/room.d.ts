export interface RoomDto {
    id: number;
    name: string;
    code: string;
    floorId: number;
    description?: string;
    version?: number;
}

export interface CreateRoomDto {
    name: string;
    code: string;
    floorId: number;
    description?: string;
    langCode?: string;
}

export interface UpdateRoomDto {
    name?: string;
    floorId?: number;
    description?: string;
    langCode?: string;
}

