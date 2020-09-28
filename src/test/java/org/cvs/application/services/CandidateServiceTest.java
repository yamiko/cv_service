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
import org.cvs.data.repositories.CandidateRepository;
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
 * Integration tests for {@link CandidateRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class CandidateServiceTest {

	@Autowired
	CandidateService service;

	Candidate candidate, invalidCandidate, fetchedCandidate;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");
		
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));

		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid candidate
		invalidCandidate = new Candidate("", "", "");

		invalidCandidate.setGender("K");
		invalidCandidate.setDateOfBirth(LocalDate.of(2080, Month.JUNE, 15));

		invalidCandidate.setRetired(Lookup.NOT_RETIRED);
		invalidCandidate.setVoided(Lookup.NOT_VOIDED);

	}

	@Test
	@WithMockUser
	public void testAddCandidate() {
		candidate = service.addCandidate(candidate);

		assertAll("Properties", 
				() -> assertTrue(candidate.getId() > 0),
		        () -> assertTrue(candidate.getFirstName().equals("John")), 
		        () -> assertTrue(candidate.getLastName().equals("Smith")), 
		        () -> assertTrue(candidate.getCountry().equals("UK")), 
		        () -> assertTrue(candidate.getAddressLine1().equals("Address 1")), 
		        () -> assertTrue(candidate.getGender().equals("M")), 
		        () -> assertTrue(candidate.getEmail().equals("email@email.com")), 
		        () -> assertTrue(candidate.getDateOfBirth().isEqual(LocalDate.of(1987, Month.JUNE, 15))), 
		        () -> assertNotNull(candidate.getCreatedDate()), 
		        () -> assertNotNull(candidate.getCreatedBy()), 
		        () -> assertNotNull(candidate.getLastModifiedBy()), 
		        () -> assertNotNull(candidate.getModifiedDate()), 
		        () -> {
			        fetchedCandidate = service.getActiveCandidate(candidate.getId());
			        assertNotNull(fetchedCandidate);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testFindNonExistentCandidate() {	
		Long candidateId = -1L;
						
		assertThrows(EntryNotFoundException.class, () -> {
			candidate = service.getActiveCandidate(candidateId);
		});
	}
	
	@Test
	@WithMockUser
	public void testVoidCandidate() {
		candidate = service.addCandidate(candidate);
		log.info("Added candidate with ID: " + candidate.getId());
		
		Long candidateId = candidate.getId();
				
		service.deleteCandidate(candidateId);
		
		assertThrows(EntryNotFoundException.class, () -> {
			candidate = service.getActiveCandidate(candidateId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireCandidate() {
		candidate = service.addCandidate(candidate);
		log.info("Added candidate with ID: " + candidate.getId());
		
		Long candidateId = candidate.getId();
				
		service.retireCandidate(candidateId);
		
		assertThrows(EntryNotActiveException.class, () -> {
			candidate = service.getActiveCandidate(candidateId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = service.addCandidate(candidate);

		assertAll("Properties", 
				() -> assertTrue(candidate.getCreatedBy().equals("Peter")),
				() -> assertTrue(candidate.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {		
		assertThrows(ConstraintViolationException.class, () -> {
			service.addCandidate(invalidCandidate);
		});
	}


}
