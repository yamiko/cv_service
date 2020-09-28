package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.WorkExperienceService;
import org.cvs.application.services.CandidateService;
import org.cvs.data.entities.WorkExperience;
import org.cvs.data.entities.Candidate;
import org.cvs.data.repositories.WorkExperienceRepository;
import org.cvs.utils.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.Month;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

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
@AutoConfigureMockMvc
public class WorkExperienceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	WorkExperienceService workExperienceService;

	@Autowired
	CandidateService candidateService;

	Candidate candidate, candidate2;

	WorkExperience workExperience, workExperience2, invalidWorkExperience, fetchedWorkExperience;

	@BeforeEach
	void init() {
		// Create valid experiences
		workExperience = new WorkExperience("Post Office", "UK", "Head of IT");
		workExperience.setStartDate(LocalDate.of(2012, Month.JUNE, 15));
		workExperience.setEndDate(LocalDate.of(2014, Month.APRIL, 11));
		workExperience.setRetired(Lookup.NOT_RETIRED);
		workExperience.setVoided(Lookup.NOT_VOIDED);

		workExperience2 = new WorkExperience("Post Office2", "UK", "Head of Management");
		workExperience2.setStartDate(LocalDate.of(2014, Month.JUNE, 15));
		workExperience2.setEndDate(LocalDate.of(2018, Month.APRIL, 11));
		workExperience2.setRetired(Lookup.NOT_RETIRED);
		workExperience2.setVoided(Lookup.NOT_VOIDED);

		//Create an invalid workExperience
		invalidWorkExperience = new WorkExperience("", "", "");
		invalidWorkExperience.setStartDate(LocalDate.of(2080, Month.JUNE, 15));
		invalidWorkExperience.setEndDate(LocalDate.of(2080, Month.APRIL, 11));
		invalidWorkExperience.setRetired(Lookup.NOT_RETIRED);
		invalidWorkExperience.setVoided(Lookup.NOT_VOIDED);


		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
		
		candidate2 = new Candidate("John2", "", "Smith2");
		candidate2.setAddressLine1("Address 1");
		candidate2.setCountry("UK");
		candidate2.setGender("M");
		candidate2.setEmail("email2@email.com");
		candidate2.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate2.setRetired(Lookup.NOT_RETIRED);
		candidate2.setVoided(Lookup.NOT_VOIDED);		
	}

	@Test
	@WithMockUser
	public void testGetWorkExperiences() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		workExperience.setCandidate(candidate);
		workExperienceService.addWorkExperience(workExperience);

		workExperience2.setCandidate(candidate);
		workExperienceService.addWorkExperience(workExperience2);

		MvcResult result = mockMvc.perform(get("/experiences").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].position").value("Head of Management")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetCandidateWorkExperiences() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		candidate2 = candidateService.addCandidate(candidate2);

		workExperience.setCandidate(candidate);
		workExperienceService.addWorkExperience(workExperience);

		workExperience2.setCandidate(candidate2);
		workExperienceService.addWorkExperience(workExperience2);

		MvcResult result = mockMvc.perform(get("/experiences/candidate/{candidateId}", candidate2.getId()).with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[0].candidate.id").value(candidate2.getId())).andReturn();

		assertNotNull(result);
	}
	
	@Test
	@WithMockUser
	public void testAddWorkExperience() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		workExperience.setCandidate(candidate);

		MvcResult result = mockMvc
		        .perform(post("/experiences").content(asJsonString(workExperience)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.position").value("Head of IT")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long workExperienceId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);

		assertAll("Properties", () -> assertTrue(workExperience.getId() > 0), () -> assertTrue(workExperience.getId() > 0),
				() -> assertTrue(workExperience.getId() > 0),
		        () -> assertTrue(workExperience.getOrganisation().equals("Post Office")), 
		        () -> assertTrue(workExperience.getCountry().equals("UK")), 
		        () -> assertTrue(workExperience.getPosition().equals("Head of IT")), 
		        () -> assertTrue(workExperience.getStartDate().isEqual(LocalDate.of(2012, Month.JUNE, 15))), 
		        () -> assertTrue(workExperience.getEndDate().isEqual(LocalDate.of(2014, Month.APRIL, 11))), 
		        () -> assertNotNull(workExperience.getCreatedDate()), 
		        () -> assertNotNull(workExperience.getCreatedBy()), 
		        () -> assertNotNull(workExperience.getLastModifiedBy()), 
		        () -> assertNotNull(workExperience.getModifiedDate())); 
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateWorkExperience() throws Exception {
		mockMvc.perform(post("/experiences").content(asJsonString(workExperience)).contentType(MediaType.APPLICATION_JSON)
		        .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader())).andExpect(status().isNotFound())
		        .andReturn();
	}

	@Test
	@WithMockUser
	public void testFindNonExistentWorkExperience() throws Exception {
		Long workExperienceId = -1L;

		mockMvc.perform(get("/experiences/active/{workExperienceId}", workExperienceId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidWorkExperience() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());

		Long workExperienceId = workExperience.getId();

		mockMvc.perform(delete("/experiences/{workExperienceId}", workExperienceId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireWorkExperience() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());

		Long workExperienceId = workExperience.getId();

		mockMvc.perform(post("/experiences/retire/{workExperienceId}", workExperienceId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		workExperience.setCandidate(candidate);
		workExperience = workExperienceService.addWorkExperience(workExperience);
		log.info("Added workExperience with ID: " + workExperience.getId());

		MvcResult result = mockMvc
		        .perform(post("/experiences").content(asJsonString(workExperience)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.position").value("Head of IT")).andReturn();

		String response = result.getResponse().getContentAsString();

		Long workExperienceId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		workExperience = workExperienceService.getActiveWorkExperience(workExperienceId);

		assertAll("Properties", () -> assertTrue(workExperience.getCreatedBy().equals("Peter")),
		        () -> assertTrue(workExperience.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/experiences").content(asJsonString(invalidWorkExperience))
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotAcceptable()).andReturn();
	}

	public static String asJsonString(final Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
