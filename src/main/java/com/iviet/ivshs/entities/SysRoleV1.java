package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
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
@Table(name = "sys_role_v1",
    indexes = {
        @Index(name = "idx_sys_role_group_function", columnList = "group_id, function_id", unique = true),
        @Index(name = "idx_sys_role_is_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysRoleV1 extends BaseTranslatableEntity<SysRoleLanV1> {
	
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private SysGroupV1 group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "function_id", nullable = false)
	private SysFunctionV1 function;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
}
