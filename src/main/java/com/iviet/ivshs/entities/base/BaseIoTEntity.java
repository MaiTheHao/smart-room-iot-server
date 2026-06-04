package com.iviet.ivshs.entities.base;

import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTEntity<T extends BaseTranslation<? extends BaseTranslatableEntity<T>>>
        extends BaseTranslatableEntity<T> {

    @Column(name = "natural_id", length = 256, unique = true, nullable = false)
    private String naturalId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "specific_type", length = 256, insertable = false, updatable = false)
    private String specificType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hardware_config_id", unique = true)
    private HardwareConfig hardwareConfig;

    @Override
    public void touch() {
        super.touch();
        if (this.room != null) {
            this.room.touch();
        }
    }
}
