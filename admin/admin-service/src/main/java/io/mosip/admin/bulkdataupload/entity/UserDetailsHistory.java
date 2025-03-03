package io.mosip.admin.bulkdataupload.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(UserDetailHistoryPk.class)
@Table(name = "user_detail_h", schema = "master")
public class UserDetailsHistory extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -8541941111557590379L;

	@Id
	@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id", nullable = false, length = 36)),
			@AttributeOverride(name = "effDTimes", column = @Column(name = "eff_dtimes", nullable = false)) })
	private String id;
	private LocalDateTime effDTimes;

	@Column(name = "lang_code", nullable = false, length = 3)
	private String langCode;

	@Column(name = "uin", nullable = true, length = 28)
	private String uin;

	@Column(name = "name", nullable = true, length = 64)
	private String name;

	@Column(name = "email", nullable = true, length = 64)
	private String email;

	@Column(name = "mobile", nullable = true, length = 16)
	private String mobile;

	@Column(name = "status_code", nullable = true, length = 36)
	private String statusCode;

	@Column(name = "last_login_dtimes", nullable = true)
	private LocalDateTime lastLoginDateTime;

	@Column(name = "last_login_method", nullable = true, length = 64)
	private String lastLoginMethod;
	
	@Column(name = "regcntr_id", length = 10)
	private String regCenterId;

}
