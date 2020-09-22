package org.cvs.data.entities;

import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Abstract class that defines mandatory columns for all voidable database tables.F
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractVoidableEntity extends AbstractAuditedEntity{
	private int voided;
	private String voidedReason;
}
