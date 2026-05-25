export interface HealthDeviceDto {
    naturalId: string;
    category: string;
    isActive: boolean;
}

export interface HealthDataDto {
    devices: HealthDeviceDto[];
    roomCode?: string;
}

export interface HealthCheckResponseDto {
    status: number;
    message: string;
    data?: HealthDataDto;
    timestamp: string;
}

