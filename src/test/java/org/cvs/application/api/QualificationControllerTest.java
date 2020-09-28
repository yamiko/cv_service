package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.QualificationService;
import org.cvs.application.services.QualificationTypeService;
import org.cvs.application.services.CandidateService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.Qualification;
import org.cvs.data.entities.QualificationType;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.entities.Qualification;
import org.cvs.data.repositories.QualificationRepository;
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
 * Integration tests for {@link QualificationRepository}.
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
public class QualificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	QualificationService qualificationService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	QualificationTypeService qualificationTypeService;

	Candidate candidate, candidate2;

	QualificationType qualificationType;

	Qualification qualification, qualification2, invalidQualification, fetchedQualification;

	@BeforeEach
	void init() {
		// Create valid qualifications
		qualification = new Qualification("PhD in Computer Science", "University of Essex", "UK",
		        LocalDate.of(2016, Month.JUNE, 15));
		qualification.setRetired(Lookup.NOT_RETIRED);
		qualification.setVoided(Lookup.NOT_VOIDED);

		qualification2 = new Qualification("PhD in Chemical Engineering", "University of Manitoba", "Canada",
		        LocalDate.of(2020, Month.JUNE, 15));
		qualification2.setRetired(Lookup.NOT_RETIRED);
		qualification2.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid qualification
		invalidQualification = new Qualification("", "", "", LocalDate.of(2080, Month.JUNE, 15));

		invalidQualification.setRetired(Lookup.NOT_RETIRED);
		invalidQualification.setVoided(Lookup.NOT_VOIDED);

		// Create a valid qualification type
		qualificationType = new QualificationType("Doctorate");

		qualificationType.setRetired(Lookup.NOT_RETIRED);
		qualificationType.setVoided(Lookup.NOT_VOIDED);

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
	public void testGetQualifications() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualificationService.addQualification(qualification);

		qualification2.setCandidate(candidate);
		qualification2.setQualificationType(qualificationType);
		qualificationService.addQualification(qualification2);

		MvcResult result = mockMvc.perform(get("/qualifications").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].name").value("PhD in Chemical Engineering")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetCandidateQualifications() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		candidate2 = candidateService.addCandidate(candidate2);

		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualificationService.addQualification(qualification);

		qualification2.setCandidate(candidate2);
		qualification2.setQualificationType(qualificationType);
		qualificationService.addQualification(qualification2);

		MvcResult result = mockMvc
		        .perform(get("/qualifications/candidate/{candidateId}", candidate2.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[0].candidate.id").value(candidate2.getId())).andReturn();

		assertNotNull(result);
	}	
		
	@Test
	@WithMockUser
	public void testAddQualification() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);

		MvcResult result = mockMvc
		        .perform(post("/qualifications").content(asJsonString(qualification))
		                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
		                .with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("PhD in Computer Science")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long qualificationId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		qualification = qualificationService.getActiveQualification(qualificationId);

		assertAll("Properties", () -> assertTrue(qualification.getId() > 0),
		        () -> assertTrue(qualification.getId() > 0),
		        () -> assertTrue(qualification.getName().equals("PhD in Computer Science")),
		        () -> assertTrue(qualification.getInstitution().equals("University of Essex")),
		        () -> assertTrue(qualification.getDateObtained().isEqual(LocalDate.of(2016, Month.JUNE, 15))),
		        () -> assertNotNull(qualification.getCreatedDate()), () -> assertNotNull(qualification.getCreatedBy()),
		        () -> assertNotNull(qualification.getLastModifiedBy()),
		        () -> assertNotNull(qualification.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() throws Exception {
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setQualificationType(qualificationType);

		mockMvc.perform(post("/qualifications").content(asJsonString(qualification))
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testAddWithoutQualificationTypeReference() throws Exception{
		candidate = candidateService.addCandidate(candidate);

		qualification.setCandidate(candidate);

		mockMvc.perform(post("/qualifications").content(asJsonString(qualification))
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testFindNonExistentQualification() throws Exception {
		Long qualificationId = -1L;

		mockMvc.perform(get("/qualifications/active/{qualificationId}", qualificationId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidQualification() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);
		log.info("Added qualification with ID: " + qualification.getId());

		Long qualificationId = qualification.getId();

		mockMvc.perform(delete("/qualifications/{qualificationId}", qualificationId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			qualification = qualificationService.getActiveQualification(qualificationId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireQualification() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);
		log.info("Added qualification with ID: " + qualification.getId());

		Long qualificationId = qualification.getId();

		mockMvc.perform(post("/qualifications/retire/{qualificationId}", qualificationId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			qualification = qualificationService.getActiveQualification(qualificationId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		qualificationType = qualificationTypeService.addQualificationType(qualificationType);

		qualification.setCandidate(candidate);
		qualification.setQualificationType(qualificationType);
		qualification = qualificationService.addQualification(qualification);
		log.info("Added qualification with ID: " + qualification.getId());

		MvcResult result = mockMvc
		        .perform(post("/qualifications").content(asJsonString(qualification))
		                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
		                .with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("PhD in Computer Science")).andReturn();

		String response = result.getResponse().getContentAsString();

		Long qualificationId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		qualification = qualificationService.getActiveQualification(qualificationId);

		assertAll("Properties", () -> assertTrue(qualification.getCreatedBy().equals("Peter")),
		        () -> assertTrue(qualification.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/qualifications").content(asJsonString(invalidQualification))
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
