package org.cvs.application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.cvs.data.entities.ApplicationUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * The {@link AuthenticationFilter} helps to authenticate the user via
 * <code>/users/login</code>.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager authenticationManager;

	/**
	 * Constructor for this {@link AuthenticationFilter} that also filters
	 * <code>/users/login</code> URL from authentication to reserve it for login
	 * operations.
	 *
	 * @param authenticationManager the {@link AuthenticationManager} for this
	 *                              application
	 * 
	 */
	public AuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;

		setFilterProcessesUrl("/users/login");
	}

	/**
	 * Attempts to authenticate the user who has logged in via an HTTP post request.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * 
	 * @return an {@link Authentication} object with the authentication results
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
	        throws AuthenticationException {
		try {
			ApplicationUser creds = new ObjectMapper().readValue(request.getInputStream(), ApplicationUser.class);

			return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(creds.getUsername(),
			        creds.getPassword(), new ArrayList<>()));
		} catch (IOException e) {
			throw new RuntimeException("Could not read request" + e);
		}
	}

	/**
	 * Generates and adds an authorisation token to the response header on
	 * successful authentication.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * 
	 * @return
	 */
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	        FilterChain filterChain, Authentication authentication) {
		String token = Jwts.builder().setSubject(((User) authentication.getPrincipal()).getUsername())
		        .setExpiration(new Date(System.currentTimeMillis() + 864_000_000))
		        .signWith(SignatureAlgorithm.HS512, "SecretKeyToGenJWTs".getBytes()).compact();
		response.addHeader("Authorization", "Bearer " + token);
	}
}
