-- =========================================================================
-- V7: Schema initialization for Motion Detector and Room Event System
-- =========================================================================

-- 1. Table: motion_detector
CREATE TABLE IF NOT EXISTS motion_detector (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    code VARCHAR(256) NOT NULL,
    natural_id VARCHAR(256) NOT NULL,
    current_motion BOOLEAN NULL,
    last_event_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_motion_detector_room FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    CONSTRAINT uq_motion_detector_natural_id UNIQUE (natural_id),
    CONSTRAINT uq_motion_detector_room_code UNIQUE (room_id, code)
);

CREATE INDEX idx_motion_detector_room_id ON motion_detector(room_id);
CREATE INDEX idx_motion_detector_natural_id ON motion_detector(natural_id);

-- 2. Table: motion_detector_lan (i18n translations)
CREATE TABLE IF NOT EXISTS motion_detector_lan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    lang_code VARCHAR(10) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_mdl_owner FOREIGN KEY (owner_id) REFERENCES motion_detector(id) ON DELETE CASCADE,
    CONSTRAINT uq_mdl_owner_lang UNIQUE (owner_id, lang_code)
);

CREATE INDEX idx_mdl_owner_id ON motion_detector_lan(owner_id);

-- 3. Table: motion_metrics (Time-series telemetry log)
CREATE TABLE IF NOT EXISTS motion_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_category VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    motion_detected BOOLEAN NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unix_minute BIGINT NOT NULL
);

CREATE INDEX idx_motion_metrics_target ON motion_metrics(target_category, target_id, timestamp);
CREATE INDEX idx_motion_metrics_timestamp ON motion_metrics(timestamp);
CREATE INDEX idx_motion_metrics_unix_minute ON motion_metrics(unix_minute);

-- 4. Table: room_event (Master Event Catalog)
CREATE TABLE IF NOT EXISTS room_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT uq_room_event_code UNIQUE (code)
);

-- Seed master event
INSERT INTO room_event (code, description)
VALUES ('MOTION_DETECTED', 'Motion detected event in room')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 5. Table: room_event_config (Per-Room Event Binding & Settings)
CREATE TABLE IF NOT EXISTS room_event_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    room_event_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    cooldown_seconds INT NOT NULL DEFAULT 0,
    last_triggered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,
    CONSTRAINT fk_rec_room FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_room_event FOREIGN KEY (room_event_id) REFERENCES room_event(id) ON DELETE CASCADE,
    CONSTRAINT uq_rec_room_event UNIQUE (room_id, room_event_id)
);

CREATE INDEX idx_rec_room_id ON room_event_config(room_id);
