package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_role_lan_v1", 
    indexes = {
        @Index(name = "idx_sys_role_lan_owner_lang", columnList = "owner_id, lang_code", unique = true)
    }
)
public class SysRoleLanV1 extends BaseTranslation<SysRoleV1> {
    
    private static final long serialVersionUID = 1L;
}
