package org.cvs.data.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Defines structure and relationship(s) for the <code>portfolio</code> table.
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
public class Portfolio extends AbstractRetirableEntity {

	@NotBlank(message = "Entry should not be blank")
	private String name;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "portfolio")
	private Set<ApplicationUser> applicationUser = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "portfolio")
	private Set<Candidate> candidate = new HashSet<>();

	public Portfolio(String name) {
		final int FALSE = 0;

		this.name = name;
		this.setRetired(FALSE);
		this.setVoided(FALSE);
	}

}
