package org.cvs.data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Defines structure and relationship(s) for the <code>reference</code> table.
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
public class Reference extends AbstractRetirableEntity {

	@NotBlank(message = "Entry should not be blank")
	private String name, JobTitle, institution, contactNumber, addressLine1, country;

	@NotBlank(message = "Email should not be blank")
	@Email(message = "Invalid email")
	private String email;

	private String addressLine2, addressLine3, postcode;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Candidate candidate;

	public Reference(String name, String jobTitle, String institution, String email) {
		final int FALSE = 0;

		this.name = name;
		this.JobTitle = jobTitle;
		this.institution = institution;
		this.email = email;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
