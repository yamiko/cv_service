package org.cvs.application.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.data.entities.Candidate;
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
public class WorkExperienceServiceTest {

	@Autowired
	WorkExperienceService workExperienceService;

	@Autowired
	CandidateService candidateService;


	WorkExperience workExperience, invalidWorkExperience, fetchedWorkExperience;
	
	Candidate candidate, candidate2;
	
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

		// Create valid candidates
		candidate = new Candidate("John", "", "Smith");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);

		candidate = new Candidate("John2", "", "Smith2");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email2@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddWorkExperience() {
		candidate = candidateService.addCandidate(candidate);
		
		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);

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
			        fetchedWorkExperience = workExperienceService.getActiveWorkExperience(workExperience.getId());
			        assertNotNull(fetchedWorkExperience);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() {
		assertThrows(EntryNotFoundException.class, () -> {
			workExperience = workExperienceService.addWorkExperience(workExperience);
		});
	}

	
	@Test
	@WithMockUser
	public void testFindNonExistentWorkExperience() {	
		Long workExperienceId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidWorkExperience() {
		candidate = candidateService.addCandidate(candidate);
		
		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());
		
		Long workExperienceId = workExperience.getId();
				
		workExperienceService.deleteWorkExperience(workExperienceId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireWorkExperience() {
		candidate = candidateService.addCandidate(candidate);
		
		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());
		
		Long workExperienceId = workExperience.getId();
				
		workExperienceService.retireWorkExperience(workExperienceId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = candidateService.addCandidate(candidate);
		
		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);

		assertAll("Properties", 
				() -> assertTrue(workExperience.getCreatedBy().equals("Peter")),
				() -> assertTrue(workExperience.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			workExperienceService.addWorkExperience(invalidWorkExperience);
		});
	}
}
