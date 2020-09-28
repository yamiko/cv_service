package org.cvs.data.entities;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Defines structure and relationship(s) for the <code>qualification_type</code>
 * table.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class QualificationType extends AbstractRetirableEntity {

	@NotBlank(message = "Name should not be blank")
	private String name;

	public QualificationType(String name) {
		final int FALSE = 0;

		this.name = name;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
