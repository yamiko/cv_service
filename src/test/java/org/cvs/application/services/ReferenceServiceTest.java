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
import org.cvs.data.entities.Reference;
import org.cvs.data.repositories.ReferenceRepository;
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
 * Integration tests for {@link ReferenceRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class ReferenceServiceTest {

	@Autowired
	ReferenceService referenceService;

	@Autowired
	CandidateService candidateService;


	Reference reference, invalidReference, fetchedReference;
	
	Candidate candidate;
	
	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid reference
		reference = new Reference("John Malkovich", "Professor of History Studies", "University of Kent", "test@test.com");
		
		reference.setContactNumber("0893453234");
		reference.setAddressLine1("Address 1");
		reference.setCountry("UK");

		reference.setRetired(Lookup.NOT_RETIRED);
		reference.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid reference
		invalidReference = new Reference("", "", "", "someemail");

		invalidReference.setRetired(Lookup.NOT_RETIRED);
		invalidReference.setVoided(Lookup.NOT_VOIDED);

		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");
		
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));

		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddReference() {
		candidate = candidateService.addCandidate(candidate);
		
		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);

		assertAll("Properties", 
				() -> assertTrue(reference.getId() > 0),
		        () -> assertTrue(reference.getName().equals("John Malkovich")), 
		        () -> assertTrue(reference.getInstitution().equals("University of Kent")), 
		        () -> assertTrue(reference.getJobTitle().equals("Professor of History Studies")), 
		        () -> assertTrue(reference.getEmail().equals("test@test.com")), 
		        () -> assertTrue(reference.getAddressLine1().equals("Address 1")), 
		        () -> assertTrue(reference.getCountry().equals("UK")), 
		        () -> assertNotNull(reference.getCreatedDate()), 
		        () -> assertNotNull(reference.getCreatedBy()), 
		        () -> assertNotNull(reference.getLastModifiedBy()), 
		        () -> assertNotNull(reference.getModifiedDate()), 
		        () -> {
			        fetchedReference = referenceService.getActiveReference(reference.getId());
			        assertNotNull(fetchedReference);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() {
		assertThrows(EntryNotFoundException.class, () -> {
			reference = referenceService.addReference(reference);
		});
	}

	
	@Test
	@WithMockUser
	public void testFindNonExistentReference() {	
		Long referenceId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			reference = referenceService.getActiveReference(referenceId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidReference() {
		candidate = candidateService.addCandidate(candidate);
		
		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);
		log.info("Added reference with ID: " + reference.getId());
		
		Long referenceId = reference.getId();
				
		referenceService.deleteReference(referenceId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			reference = referenceService.getActiveReference(referenceId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireReference() {
		candidate = candidateService.addCandidate(candidate);
		
		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);
		log.info("Added reference with ID: " + reference.getId());
		
		Long referenceId = reference.getId();
				
		referenceService.retireReference(referenceId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			reference = referenceService.getActiveReference(referenceId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = candidateService.addCandidate(candidate);
		
		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);

		assertAll("Properties", 
				() -> assertTrue(reference.getCreatedBy().equals("Peter")),
				() -> assertTrue(reference.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			referenceService.addReference(invalidReference);
		});
	}
}
