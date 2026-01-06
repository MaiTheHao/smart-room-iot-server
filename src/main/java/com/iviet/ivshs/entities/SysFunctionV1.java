package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_function_v1",
    indexes = {
        @Index(name = "idx_sys_function_code", columnList = "function_code", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysFunctionV1 extends BaseTranslatableEntity<SysFunctionLanV1> {

	private static final long serialVersionUID = 1L;

	@Column(name = "function_code", nullable = false, length = 256, unique = true)
	private String functionCode;

	@OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<SysRoleV1> roles = new HashSet<>();
}
