package com.iviet.ivshs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity ánh xạ bảng persistent_logins cho Spring Security Remember-Me.
 * 
 * LƯU Ý: Bảng này được Spring Security quản lý thông qua JdbcTokenRepositoryImpl.
 * Entity này CHỉ DÙNG CHO MỤC ĐÍCH THAM KHẢO, không nên thao tác trực tiếp
 * để tránh xung đột với cơ chế Remember-Me của Spring Security.
 */
@Entity
@Table(name = "persistent_logins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistentLoginV1 {
	
	@Id
	@Column(name = "series", length = 64)
	private String series;

	@Column(name = "username", nullable = false, length = 64)
	private String username;

	@Column(name = "token", nullable = false, length = 64)
	private String token;

	@Column(name = "last_used", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUsed;
}
