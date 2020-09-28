package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.cvs.data.entities.Qualification;
import org.cvs.data.repositories.QualificationRepository;
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
 * Integration tests for {@link QualificationRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class QualificationRepositoryTest {

	@Autowired
	QualificationRepository repository;

	@Autowired
	private Validator validator;

	Qualification qualification, qualification2, invalidQualification, fetchedQualification;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create valid qualifications
		qualification = new Qualification("PhD in Computer Science", "University of Essex", "UK", LocalDate.of(2016, Month.JUNE, 15));
		qualification.setRetired(Lookup.NOT_RETIRED);
		qualification.setVoided(Lookup.NOT_VOIDED);

		qualification2 = new Qualification("PhD in Chemistry", "University of Kent", "UK", LocalDate.of(2016, Month.JUNE, 15));
		qualification2.setRetired(Lookup.NOT_RETIRED);
		qualification2.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid qualification
		invalidQualification = new Qualification("", "", "", LocalDate.of(2080, Month.JUNE, 15));

		invalidQualification.setRetired(Lookup.NOT_RETIRED);
		invalidQualification.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testGetQualifications() {
		qualification = repository.save(qualification);
		qualification2 = repository.save(qualification2);
		List <Qualification> qualifications = repository.findAll(); 

		assertTrue(qualifications.size() == 2);
	}

	
	@Test
	@WithMockUser
	public void testAddQualification() {
		qualification = repository.save(qualification);

		assertAll("Properties", 
				() -> assertTrue(qualification.getId() > 0),
		        () -> assertTrue(qualification.getName().equals("PhD in Computer Science")), 
		        () -> assertTrue(qualification.getInstitution().equals("University of Essex")), 
		        () -> assertTrue(qualification.getDateObtained().isEqual(LocalDate.of(2016, Month.JUNE, 15))), 
		        () -> assertNotNull(qualification.getCreatedDate()), 
		        () -> assertNotNull(qualification.getCreatedBy()), 
		        () -> assertNotNull(qualification.getLastModifiedBy()), 
		        () -> assertNotNull(qualification.getModifiedDate()), 
		        () -> {
			        fetchedQualification = repository.findById(qualification.getId()).orElse(null);
			        assertNotNull(fetchedQualification);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendQualification() {
		qualification = repository.save(qualification);
		log.info("Added qualification with ID: " + qualification.getId());
		qualification.setRetired(Lookup.RETIRED);
		qualification = repository.save(qualification);	
		assertTrue(qualification.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		qualification = repository.save(qualification);

		assertAll("Properties", 
				() -> assertTrue(qualification.getCreatedBy().equals("Peter")),
				() -> assertTrue(qualification.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<Qualification>> violations = validator.validate(invalidQualification);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Name should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Institution should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Country should not be blank")).findAny().orElse(null)),
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Date obtained should be in the past")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
