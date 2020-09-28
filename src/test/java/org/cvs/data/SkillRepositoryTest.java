package org.cvs.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.cvs.data.entities.Skill;
import org.cvs.data.repositories.SkillRepository;
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
 * Integration tests for {@link SkillRepository}. 
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
public class SkillRepositoryTest {

	@Autowired
	SkillRepository repository;

	@Autowired
	private Validator validator;

	Skill skill, invalidSkill, fetchedSkill;

	int FALSE = 0;
	
	@BeforeEach
    void init() {
		// Create a valid skill
		skill = new Skill("Java 8 - Advanced");
		
		skill.setRetired(Lookup.NOT_RETIRED);
		skill.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid skill
		invalidSkill = new Skill("");

		invalidSkill.setRetired(Lookup.NOT_RETIRED);
		invalidSkill.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testAddSkill() {
		skill = repository.save(skill);

		assertAll("Properties", 
				() -> assertTrue(skill.getId() > 0),
		        () -> assertTrue(skill.getDescription().equals("Java 8 - Advanced")), 
		        () -> assertNotNull(skill.getCreatedDate()), 
		        () -> assertNotNull(skill.getCreatedBy()), 
		        () -> assertNotNull(skill.getLastModifiedBy()), 
		        () -> assertNotNull(skill.getModifiedDate()), 
		        () -> {
			        fetchedSkill = repository.findById(skill.getId()).orElse(null);
			        assertNotNull(fetchedSkill);
		        }
			);
	}

	@Test
	@WithMockUser
	public void testAmendSkill() {
		skill = repository.save(skill);
		log.info("Added skill with ID: " + skill.getId());
		skill.setRetired(Lookup.RETIRED);
		skill = repository.save(skill);	
		assertTrue(skill.getRetired() == Lookup.RETIRED);	
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() {
		skill = repository.save(skill);

		assertAll("Properties", 
				() -> assertTrue(skill.getCreatedBy().equals("Peter")),
				() -> assertTrue(skill.getLastModifiedBy().equals("Peter"))
			);
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() {
		// Validate using Bean constraints
		Set<ConstraintViolation<Skill>> violations = validator.validate(invalidSkill);

		assertAll("Properties", 
				() -> assertNotNull(violations.stream().filter(v -> v.getMessage().contentEquals("Description should not be blank")).findAny().orElse(null)),
				() -> assertFalse(violations.isEmpty())
			);
	}


}
