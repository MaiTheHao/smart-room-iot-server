package com.iviet.ivshs.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        boolean sameClass = getClass() != null && that.getClass() != null && Objects.equals(getClass(), that.getClass());
        boolean sameId = getId() != null && that.getId() != null && Objects.equals(getId(), that.getId());
        return sameClass && sameId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getId());
    }
}