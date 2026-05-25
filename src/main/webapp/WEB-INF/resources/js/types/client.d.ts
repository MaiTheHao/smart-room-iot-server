import { ClientType } from './enums';

export interface ClientDto {
    id: number;
    username: string;
    clientType: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    lastLoginAt?: string;
    gatewayPassword?: string;
}

export interface CreateClientDto {
    username: string;
    password: string;
    clientType: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    gatewayPassword?: string;
}

export interface UpdateClientDto {
    password?: string;
    clientType?: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    gatewayPassword?: string;
}

export interface LoginDto {
    username: string;
    password: string;
}

export interface JwtResponse {
    token: string;
    type: string; // Usually "Bearer"
    username: string;
    groups: string[]; // User roles/groups
}

