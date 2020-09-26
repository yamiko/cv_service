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
 * Defines structure and relationship(s) for the <code>qualification</code> table.
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
@JsonIdentityInfo(generator=ObjectIdGenerators.UUIDGenerator.class, property="@id")
public class Qualification extends AbstractRetirableEntity {

	@NotBlank(message = "Name should not be blank")
	private String name;
	
	@NotBlank(message = "Institution should not be blank")
	private String institution;

	@NotBlank(message = "Country should not be blank")
	private String country;

	@Past(message = "Date obtained should be in the past")
	@NotNull(message = "Local date should not be blank")
	private LocalDate dateObtained;

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	private Candidate candidate = new Candidate();

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	private QualificationType qualificationType = new QualificationType();

	public Qualification(String name, String institution, String country, LocalDate dateObtained) {
		final int FALSE = 0;

		this.name = name;
		this.institution = institution;
		this.country = country;
		this.dateObtained = dateObtained;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
