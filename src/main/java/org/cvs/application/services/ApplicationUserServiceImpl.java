package org.cvs.application.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.exceptions.InconsistentDataException;
import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.ApplicationUserRepository;
import org.cvs.utils.Lookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationUserServiceImpl implements ApplicationUserService, UserDetailsService {

	@Autowired
	private ApplicationUserRepository userRepository;

	@Autowired
	private Validator validator;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public ApplicationUser addUser(ApplicationUser user) {
		ApplicationUser greenUser = new ApplicationUser();

		// Extract all fields to safely add to DB
		greenUser.setUsername(user.getUsername());
		greenUser.setPassword(user.getPassword());
		greenUser.setFullName(user.getFullName());

		// Validate using Bean constraints
		Set<ConstraintViolation<ApplicationUser>> violations = validator.validate(greenUser);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<ApplicationUser> constraintViolation : violations) {
				sb.append(" -> " + constraintViolation.getMessage());
			}

			throw new ConstraintViolationException("Validation error: " + sb.toString(), violations);
		}

		ApplicationUser userByName = userRepository.findAllByUsername(greenUser.getUsername()).stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED).findFirst().orElse(null);

		if (userByName != null) {
			throw new InconsistentDataException("User name already in use - " + greenUser.getUsername());
		}

		greenUser.setPassword(passwordEncoder.encode(greenUser.getPassword()));
		greenUser.setVoided(Lookup.NOT_VOIDED);
		greenUser.setRetired(Lookup.NOT_RETIRED);

		ApplicationUser newUser = userRepository.save(greenUser);
		return newUser;
	}

	@Override
	public List<ApplicationUser> getUsers() {
		List<ApplicationUser> users = userRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED)
		        .collect(Collectors.toList());
		return users;
	}

	@Override
	public List<ApplicationUser> getUsers(Long portfolioId) {
		List<ApplicationUser> users = userRepository.findAll().stream()
		        .filter(p -> p.getVoided() != Lookup.VOIDED && p.getRetired() != Lookup.RETIRED
		                && (p.getPortfolio().stream().filter(q -> q.getId().intValue() == portfolioId).findFirst()
		                        .orElse(new Portfolio()).getId().longValue() == portfolioId))
		        .collect(Collectors.toList());
		return users;
	}

	@Override
	public ApplicationUser getByUsername(String username) throws EntryNotFoundException {

		Optional<ApplicationUser> user = userRepository
		        .findByUsernameAndActiveFlags(username, Lookup.NOT_VOIDED, Lookup.NOT_RETIRED).stream().findFirst();
		return user.orElseThrow(() -> new EntryNotFoundException("Invalid operation for [USER]." + username));
	}

	@Override
	public ApplicationUser getAlwaysByUsername(String username) {

		ApplicationUser user = null;
		try {
			user = getByUsername(username);
		} catch (EntryNotFoundException ex) {
			user = new ApplicationUser();
			user.setId(Lookup.DEFAULT_USER_ID);
		}
		return user;
	}

	@Override
	public ApplicationUser getActiveUser(Long userId) throws EntryNotActiveException, EntryNotFoundException {
		ApplicationUser user = userRepository.findById(userId).orElse(null);
		if (user != null && user.getVoided() != Lookup.VOIDED && user.getRetired() != Lookup.RETIRED) {
			return user;
		} else {
			if (user == null || user.getVoided() == Lookup.VOIDED) {
				throw new EntryNotFoundException("Invalid operation for [USER]." + userId);
			} else {
				throw new EntryNotActiveException("Invalid operation for [USER]." + userId);
			}
		}
	}

	@Override
	public void deleteUser(Long userId) throws EntryNotFoundException {
		ApplicationUser user = userRepository.findById(userId).orElse(null);
		if (user != null && user.getVoided() != Lookup.VOIDED) {
			user.setVoided(Lookup.VOIDED);
			user.setVoidedReason("System operation - voided");
			userRepository.save(user);
			log.info("Deleted user with ID: " + userId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [USER]." + userId);
		}
	}

	@Override
	public void retireUser(Long userId) throws EntryNotFoundException {
		ApplicationUser user = userRepository.findById(userId).orElse(null);
		if (user != null && user.getRetired() != Lookup.RETIRED) {
			user.setRetired(Lookup.RETIRED);
			user.setRetiredReason("System operation - retired");
			userRepository.save(user);
			log.info("Retired user with ID: " + userId);
		} else {
			throw new EntryNotFoundException("Invalid operation for [USER]." + userId);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ApplicationUser user = userRepository.findAllByUsername(username).stream()
		        .filter(u -> u.getVoided() != Lookup.VOIDED && u.getRetired() != Lookup.RETIRED).findFirst()
		        .orElse(null);

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}

		return new User(user.getUsername(), user.getPassword(), Collections.emptyList());
	}
}
