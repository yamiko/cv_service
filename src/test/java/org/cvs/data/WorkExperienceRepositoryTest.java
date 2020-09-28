package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.cvs.data.entities.WorkExperience;
import org.cvs.data.repositories.WorkExperienceRepository;
import org.cvs.utils.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for {@link WorkExperienceRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class WorkExperienceRepositoryTest {

	@Autowired
	WorkExperienceRepository repository;

	@Autowired
	private Validator validator;

	WorkExperience workExperience, invalidWorkExperience, fetchedWorkExperience;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid workExperience
		workExperience = new WorkExperience("Post Office", "UK", "Head of IT");
		
		workExperience.setStartDate(LocalDate.of(2012, Month.JUNE, 15));
		workExperience.setEndDate(LocalDate.of(2014, Month.APRIL, 11));

		workExperience.setRetired(Lookup.NOT_RETIRED);
		workExperience.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid workExperience
		invalidWorkExperience = new WorkExperience("", "", "");

		invalidWorkExperience.setStartDate(LocalDate.of(2080, Month.JUNE, 15));
		invalidWorkExperience.setEndDate(LocalDate.of(2080, Month.APRIL, 11));

		invalidWorkExperience.setRetired(Lookup.NOT_RETIRED);
		invalidWorkExperience.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAmendExperience() {
		workExperience = repository.save(workExperience);

		assertAll("Properties", 
				() -> assertTrue(workExperience.getId() > 0),
		        () -> assertTrue(workExperience.getOrganisation().equals("Post Office")), 
		        () -> assertTrue(workExperience.getCountry().equals("UK")), 
		        () -> assertTrue(workExperience.getPosition().equals("Head of IT")), 
		        () -> assertTrue(workExperience.getStartDate().isEqual(LocalDate.of(2012, Month.JUNE, 15))), 
		        () -> assertTrue(workExperience.getEndDate().isEqual(LocalDate.of(2014, Month.APRIL, 11))), 
		        () -> assertNotNull(workExperience.getCreatedDate()), 
		        () -> assertNotNull(workExperience.getCreatedBy()), 
		        () -> assertNotNull(workExperience.getLastModifiedBy()), 
		        () -> assertNotNull(workExperience.getModifiedDate()), 
		        () -> {
			        fetchedWorkExperience = repository.findById(workExperience.getId()).orElse(null);
			        assertNotNull(fetchedWorkExperience);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testRetireWorkExperience() {
		workExperience = repository.save(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());
		workExperience.setRetired(Lookup.RETIRED);
		workExperience = repository.save(workExperience);	
		assertTrue(workExperience.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		workExperience = repository.save(workExperience);

		assertAll("Properties", 
				() -> assertTrue(workExperience.getCreatedBy().equals("Peter")),
				() -> assertTrue(workExperience.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<WorkExperience>> violations = validator.validate(invalidWorkExperience);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Organisation should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Country should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Position should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Start date should be in the past")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("End date should be in the past")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
