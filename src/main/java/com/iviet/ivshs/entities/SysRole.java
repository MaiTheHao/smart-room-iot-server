package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_role",
    indexes = {
        @Index(name = "idx_sys_role_group_function", columnList = "group_id, function_id", unique = true),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysRole extends BaseAuditEntity {
	
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private SysGroup group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "function_id", nullable = false)
	private SysFunction function;
}
