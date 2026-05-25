export interface TemperatureValueDto {
    id: number;
    sensorId: number;
    tempC: number;
    timestamp: string;
}

export interface AverageTemperatureValueDto {
    timestamp: string;
    avgTempC: number;
}

export interface EnergyMetricDto {
    timestamp: string;
    voltage?: number;
    current?: number;
    power?: number;
    energy?: number;
    frequency?: number;
    powerFactor?: number;
}

