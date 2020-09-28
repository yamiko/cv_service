package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class ReferenceRepositoryTest {

	@Autowired
	ReferenceRepository repository;

	@Autowired
	private Validator validator;

	Reference reference, invalidReference, fetchedReference;

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
	}

	@Test
	@WithMockUser
	public void testAddReference() {
		reference = repository.save(reference);

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
			        fetchedReference = repository.findById(reference.getId()).orElse(null);
			        assertNotNull(fetchedReference);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendReference() {
		reference = repository.save(reference);
		log.info("Added reference with ID: " + reference.getId());
		reference.setRetired(Lookup.RETIRED);
		reference = repository.save(reference);	
		assertTrue(reference.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		reference = repository.save(reference);

		assertAll("Properties", 
				() -> assertTrue(reference.getCreatedBy().equals("Peter")),
				() -> assertTrue(reference.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<Reference>> violations = validator.validate(invalidReference);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Name should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Job title should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Institution should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Contact number should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Address line 1 should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Country should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Invalid email")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
