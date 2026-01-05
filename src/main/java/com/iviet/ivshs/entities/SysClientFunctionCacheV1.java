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

/**
 * Mục đích:
 * - Tối ưu tốc độ kiểm tra quyền (Authorization Check) - Không cần JOIN
 * - Đảm bảo tính nguyên tử (Atomic) - Biết rõ quyền từ Group nào
 * - Cập nhật chính xác - Gỡ Group không làm mất quyền từ Group khác
 * 
 * Composite Primary Key: (client_id, function_code, group_id)
 * 
 * Ví dụ:
 * Client A thuộc Group "Kế toán" và Group "Quản lý"
 * Cả 2 Group đều có quyền "VIEW_REPORT"
 * 
 * Cache sẽ lưu 2 dòng:
 * - (client_id=A, function_code=VIEW_REPORT, group_id=Kế toán)
 * - (client_id=A, function_code=VIEW_REPORT, group_id=Quản lý)
 * 
 * Khi gỡ Client A khỏi Group "Kế toán", chỉ xóa dòng đầu tiên.
 * Client A vẫn có quyền VIEW_REPORT từ Group "Quản lý".
 */
@Entity
@Table(name = "sys_client_function_cache_v1",
    indexes = {
        @Index(name = "idx_cache_client_function", columnList = "client_id, function_code"),
        @Index(name = "idx_cache_client", columnList = "client_id"),
        @Index(name = "idx_cache_group", columnList = "group_id"),
        @Index(name = "idx_cache_function", columnList = "function_code")
    }
)
@IdClass(SysClientFunctionCacheV1.CacheId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysClientFunctionCacheV1 implements Serializable {

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
    private ClientV1 client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private SysGroupV1 group;

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
