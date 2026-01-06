package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_group_v1",
    indexes = {
        @Index(name = "idx_sys_group_code", columnList = "group_code", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysGroupV1 extends BaseTranslatableEntity<SysGroupLanV1> {

	private static final long serialVersionUID = 1L;

	@Column(name = "group_code", nullable = false, length = 100, unique = true)
	private String groupCode;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<SysRoleV1> roles = new HashSet<>();

	@ManyToMany(mappedBy = "groups")
	private Set<ClientV1> clients = new HashSet<>();
}
