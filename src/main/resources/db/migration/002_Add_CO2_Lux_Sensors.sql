-- Create CO2 Sensor table
CREATE TABLE IF NOT EXISTS co2_sensor (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255),
    location    VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Lux Sensor table
CREATE TABLE IF NOT EXISTS lux_sensor (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255),
    location    VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create CO2 Sensor LAN table (for LAN-specific fields like IP, port)
CREATE TABLE IF NOT EXISTS co2_sensor_lan (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL UNIQUE,
    ip_address  VARCHAR(255),
    port        INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Lux Sensor LAN table
CREATE TABLE IF NOT EXISTS lux_sensor_lan (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL UNIQUE,
    ip_address  VARCHAR(255),
    port        INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create CO2 Metric table
CREATE TABLE IF NOT EXISTS co2_metric (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL,
    co2         DOUBLE NOT NULL,
    timestamp   BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_co2_metric_device_id (device_id),
    INDEX idx_co2_metric_timestamp (timestamp)
);

-- Create Lux Metric table
CREATE TABLE IF NOT EXISTS lux_metric (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL,
    lux         DOUBLE NOT NULL,
    timestamp   BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lux_metric_device_id (device_id),
    INDEX idx_lux_metric_timestamp (timestamp)
);
