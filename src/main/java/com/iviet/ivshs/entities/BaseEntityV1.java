package com.iviet.ivshs.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntityV1 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntityV1)) return false;
        BaseEntityV1 that = (BaseEntityV1) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}