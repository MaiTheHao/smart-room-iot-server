package com.iviet.ivshs.entities.id;

import java.io.Serializable;
import java.util.Objects;

public class AlertConfigGroupId implements Serializable {
    private Long alertConfig;
    private Long group;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertConfigGroupId that)) return false;
        return Objects.equals(alertConfig, that.alertConfig) && Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertConfig, group);
    }
}
