package com.iviet.ivshs.entities.id;

import java.io.Serializable;
import java.util.Objects;

public class AlertInstanceGroupId implements Serializable {
    private Long alert;
    private Long group;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertInstanceGroupId that)) return false;
        return Objects.equals(alert, that.alert) && Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alert, group);
    }
}
