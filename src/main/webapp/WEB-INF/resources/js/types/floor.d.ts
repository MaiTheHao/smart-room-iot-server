export interface FloorDto {
    id: number;
    name: string;
    code: string;
    level: number;
    description?: string;
    version?: number;
}

export interface CreateFloorDto {
    name: string;
    code: string;
    level: number;
    description?: string;
    langCode?: string;
}

export interface UpdateFloorDto {
    name?: string;
    level?: number;
    description?: string;
    langCode?: string;
}

