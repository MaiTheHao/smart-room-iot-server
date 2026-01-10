package com.iviet.ivshs.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_client_function_cache",
    indexes = {
        @Index(name = "idx_cache_client_function", columnList = "client_id, function_code"),
        @Index(name = "idx_cache_client", columnList = "client_id"),
        @Index(name = "idx_cache_group", columnList = "group_id"),
        @Index(name = "idx_cache_function", columnList = "function_code")
    }
)
@IdClass(SysClientFunctionCache.CacheId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysClientFunctionCache implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Id
    @Column(name = "function_code", nullable = false, length = 100)
    private String functionCode;

    @Id
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private SysGroup group;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CacheId implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private Long clientId;
        private String functionCode;
        private Long groupId;
    }
}
