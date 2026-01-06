package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_function_lan", 
    indexes = {
        @Index(name = "idx_sys_function_lan_owner_lang", columnList = "owner_id, lang_code", unique = true)
    }
)
public class SysFunctionLan extends BaseTranslation<SysFunction> {
    
    private static final long serialVersionUID = 1L;
}
