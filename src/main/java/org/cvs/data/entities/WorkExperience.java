package org.cvs.data.entities;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Defines structure and relationship(s) for the <code>work_experience</code> table.
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
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public class WorkExperience extends AbstractRetirableEntity {

	@NotBlank(message = "Organisation should not be blank")
	private String organisation;

	@NotBlank(message = "Country should not be blank")
	private String country;

	@NotBlank(message = "Position should not be blank")
	private String position;

	@Past(message = "Start date should be in the past")
	@NotNull(message = "Start date should not be blank")
	private LocalDate startDate;

	@Past(message = "End date should be in the past")
	@NotNull(message = "End date should not be blank")
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Candidate candidate;

	/**
	 * 
	 * Convenient constructor for {@link WorkExperience}.
	 * 
	 * @param	organisation	the organisation where the referee is based	
	 * @param	country			the country that depicts the home base for the referee
	 * @param	position		current position or job title for the referee
	 *
	 */	
	public WorkExperience(String organisation, String country, String position) {
		final int FALSE = 0;

		this.organisation = organisation;
		this.country = country;
		this.position = position;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
