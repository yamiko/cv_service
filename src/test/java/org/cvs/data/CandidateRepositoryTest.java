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
public class CandidateRepositoryTest {

	@Autowired
	CandidateRepository repository;

	@Autowired
	private Validator validator;

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
		candidate = repository.save(candidate);

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
			        fetchedCandidate = repository.findById(candidate.getId()).orElse(null);
			        assertNotNull(fetchedCandidate);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendCandidate() {
		candidate = repository.save(candidate);
		log.info("Added candidate with ID: " + candidate.getId());
		candidate.setRetired(Lookup.RETIRED);
		candidate = repository.save(candidate);	
		assertTrue(candidate.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		candidate = repository.save(candidate);

		assertAll("Properties", 
				() -> assertTrue(candidate.getCreatedBy().equals("Peter")),
				() -> assertTrue(candidate.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<Candidate>> violations = validator.validate(invalidCandidate);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("First name should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Last name should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Address line 1 should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Country should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Gender should be M for Male or F for Female")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Email should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Date of birth should be in the past")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
