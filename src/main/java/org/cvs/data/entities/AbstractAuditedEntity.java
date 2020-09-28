package org.cvs.data.entities;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Abstract class that defines audit fields for database tables.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = -1L;

	@CreatedDate
	private LocalDateTime createdDate;

	@CreatedBy
	private String createdBy;

	@LastModifiedDate
	private LocalDateTime modifiedDate;

	@LastModifiedBy
	private String lastModifiedBy;
}
