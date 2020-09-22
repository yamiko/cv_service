package org.cvs.application.security;

import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * This {@link AuthorizationFilter} validates token passed through the
 * application's {@link AuthenticationFilter}.
 * 
 * @author Yamiko J. Msosa
 * @version 1.0
 *
 */
public class AuthorizationFilter extends BasicAuthenticationFilter {
	/**
	 * Constructor for this class.
	 *
	 * @param authenticationManager the {@link AuthenticationManager} for this
	 *                              application
	 * 
	 */
	public AuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	/**
	 * Filters included and excluded URLs
	 *
	 * @param request     the HttpServletRequest object
	 * @param response    the HttpServletResponse object
	 * @param filterChain the chain of filters for URLs in this application
	 * 
	 * @return
	 */
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws IOException, ServletException {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer")) {
			filterChain.doFilter(request, response);
			return;
		}
		UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		filterChain.doFilter(request, response);
	}

	/**
	 * Returns a valid or invalid authentication token based on passed
	 * authentication
	 *
	 * @param request the HttpServletRequest object
	 * 
	 * @return a validated or invalidated authentication token
	 */
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null) {
			String user = Jwts.parser().setSigningKey("SecretKeyToGenJWTs".getBytes())
			        .parseClaimsJws(token.replace("Bearer", "")).getBody().getSubject();

			if (user != null) {
				return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
			}

			return null;
		}
		return null;
	}
}
