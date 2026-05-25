export interface SysFunctionDto {
    id: number;
    functionCode: string;
    name: string;
    description?: string;
}

export interface CreateSysFunctionDto {
    functionCode: string;
    name: string;
    description?: string;
    langCode?: string;
}

export interface UpdateSysFunctionDto {
    name?: string;
    description?: string;
    langCode?: string;
}

export interface SysFunctionWithGroupStatusDto {
    id: number;
    functionCode: string;
    name: string;
    description?: string;
    isAssignedToGroup: boolean;
    roleId?: number;
}

export interface SysGroupDto {
    id: number;
    groupCode: string;
    name: string;
    description?: string;
}

export interface CreateSysGroupDto {
    groupCode: string;
    name: string;
    description?: string;
    langCode?: string;
}

export interface UpdateSysGroupDto {
    name?: string;
    description?: string;
    langCode?: string;
}

export interface SysGroupWithClientStatusDto {
    id: number;
    groupCode: string;
    name: string;
    description?: string;
    isAssignedToClient: boolean;
}

export interface BatchOperationResultDto {
    successCount: number;
    failedCount: number;
    skippedCount: number;
    message: string;
}

