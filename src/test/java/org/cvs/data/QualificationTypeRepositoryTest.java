package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.cvs.data.entities.QualificationType;
import org.cvs.data.repositories.QualificationTypeRepository;
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
 * Integration tests for {@link QualificationTypeRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class QualificationTypeRepositoryTest {

	@Autowired
	QualificationTypeRepository repository;

	@Autowired
	private Validator validator;

	QualificationType qualificationType, invalidQualificationType, fetchedQualificationType;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid qualificationType
		qualificationType = new QualificationType("Doctorate");
		
		qualificationType.setRetired(Lookup.NOT_RETIRED);
		qualificationType.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid qualificationType
		invalidQualificationType = new QualificationType("");

		invalidQualificationType.setRetired(Lookup.NOT_RETIRED);
		invalidQualificationType.setVoided(Lookup.NOT_VOIDED);

	}

	@Test
	@WithMockUser
	public void testAddQualificationType() {
		qualificationType = repository.save(qualificationType);

		assertAll("Properties", 
				() -> assertTrue(qualificationType.getId() > 0),
		        () -> assertTrue(qualificationType.getName().equals("Doctorate")), 
		        () -> assertNotNull(qualificationType.getCreatedDate()), 
		        () -> assertNotNull(qualificationType.getCreatedBy()), 
		        () -> assertNotNull(qualificationType.getLastModifiedBy()), 
		        () -> assertNotNull(qualificationType.getModifiedDate()), 
		        () -> {
			        fetchedQualificationType = repository.findById(qualificationType.getId()).orElse(null);
			        assertNotNull(fetchedQualificationType);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendQualificationType() {
		qualificationType = repository.save(qualificationType);
		log.info("Added qualificationType with ID: " + qualificationType.getId());
		qualificationType.setRetired(Lookup.RETIRED);
		qualificationType = repository.save(qualificationType);	
		assertTrue(qualificationType.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		qualificationType = repository.save(qualificationType);

		assertAll("Properties", 
				() -> assertTrue(qualificationType.getCreatedBy().equals("Peter")),
				() -> assertTrue(qualificationType.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<QualificationType>> violations = validator.validate(invalidQualificationType);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Name should not be blank")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
