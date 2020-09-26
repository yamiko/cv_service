package org.cvs.data.entities;

import javax.persistence.MappedSuperclass;

import org.cvs.utils.Lookup;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Abstract class that defines mandatory database columns for retirable tables.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractRetirableEntity extends AbstractVoidableEntity {
	private int retired = Lookup.NOT_RETIRED;
	private String retiredReason;

}
