package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseIoTDevice;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: Temporary workaround to allow specific_type write for Light. Remove when model strategy is clean.
@Entity
@Table(name = "light", indexes = {
        @Index(name = "idx_light_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_light_natural_id", columnList = "natural_id", unique = true)
})
@AttributeOverride(name = "specificType", column = @Column(name = "specific_type", length = 256, insertable = true, updatable = false))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Light extends BaseIoTDevice<LightLan> {
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 100;

    @Column(name = "level")
    private Integer level;

    public void setLevel(Integer level) {
        if (level != null && (level < MIN_LEVEL || level > MAX_LEVEL)) {
            throw new IllegalArgumentException("Level must be between " + MIN_LEVEL + " and " + MAX_LEVEL);
        }
        this.level = level;
    }

    @Override
    public Object extractBusinessData() {
        return null;
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.LIGHT;
    }
}