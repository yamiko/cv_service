package org.cvs.data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Defines structure and relationship(s) for the <code>skill</code> table.
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
public class Skill extends AbstractRetirableEntity {

	@NotBlank(message = "Description should not be blank")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Candidate candidate;

	public Skill(String description) {
		final int FALSE = 0;

		this.description = description;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
