/*
 *    This file is part of Linshare.
 *
 *   Linshare is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   Linshare is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public
 *   License along with Foobar.  If not, see
 *                                    <http://www.gnu.org/licenses/>.
 *
 *   (c) 2008 Groupe Linagora - http://linagora.org
 *
*/
package org.linagora.linShare.auth;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linagora.linShare.auth.exceptions.BadDomainException;
import org.linagora.linShare.core.domain.entities.Domain;
import org.linagora.linShare.core.domain.entities.User;
import org.linagora.linShare.core.domain.entities.UserType;
import org.linagora.linShare.core.exception.BusinessException;
import org.linagora.linShare.core.service.DomainService;
import org.linagora.linShare.core.service.LDAPQueryService;
import org.linagora.linShare.core.service.UserService;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;

public class DomainAuthProviderDao extends AbstractUserDetailsAuthenticationProvider {
	
	private LDAPQueryService ldapQueryService;
	private UserService userService;
	private DomainService domainService;
	
    private final static Log logger = LogFactory.getLog(DomainAuthProviderDao.class);
	
	public void setLdapQueryService(LDAPQueryService ldapQueryService) {
		this.ldapQueryService = ldapQueryService;
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
	}

	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		
		String login = username;
		String password = (String)authentication.getCredentials();
		String domain = null;
		if (authentication.getDetails() != null && authentication.getDetails() instanceof String) {
			domain = (String)authentication.getDetails();
		}
		User user = null;
		User foundUser = null;		
		
		// domain was specified
		if (domain != null) {
			try {
				Domain domainObject = domainService.retrieveDomain(domain);
				foundUser = ldapQueryService.auth(login, password, domainObject);
			} catch (NameNotFoundException e) {
				foundUser = userService.findUserInDB(username);
				if (foundUser != null && !foundUser.getUserType().equals(UserType.INTERNAL) && domain.equals(foundUser.getDomainId())) {
					throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
			          "Bad credentials"), domain);
				} else {
					throw new BadDomainException(e.getMessage(), domain);
				}
			} catch (Exception e) {
				throw new AuthenticationServiceException("Could not authenticate user: "+login, e);
			}
			
			if(foundUser == null) {
			      throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
			          "Bad credentials"), domain);
			} 
		}
		
		/* 
		 * if the domain is not specified (invisible domains), we try to
		 * find the user in the DB to know its domain.
		 * if we can't find it (first login), we search all the domains.
		 * in the case of invisible domains, the user has to provide
		 * its email address and not LDAP uid to log in. 
		 */
		if (domain == null && username.indexOf("@") != -1) {
			try {
				foundUser = userService.findUserInDB(username);
				if (foundUser == null) {
					List<Domain> domains = domainService.findAllDomains();
					for (Domain loopedDomain : domains) {
						try {
							foundUser = ldapQueryService.auth(login, password, loopedDomain);
							if (foundUser != null) {
								domain = loopedDomain.getIdentifier();
								logger.debug("User found in domain "+domain);
								break;
							}
						} catch (NameNotFoundException e) {
							// just not found in this domain
						} catch (IOException e) {
							//TLS negociation problem
							logger.error(e);
						} catch (NamingException e) {
							logger.error(e);
						}
					}
				} else {
					domain = foundUser.getDomain().getIdentifier();
					try {
						foundUser = ldapQueryService.auth(login, password, foundUser.getDomain());
					} catch (NameNotFoundException e) {
						throw new BadDomainException("Could not retrieve user : "+login+" in domain : "+domain, e);
					} catch (IOException e) {
						throw new AuthenticationServiceException("Could not retrieve user : "+login+" in domain : "+domain, e);
					} catch (NamingException e) {
						throw new BadCredentialsException("Could not retrieve user : "+login+" in domain : "+domain, e);
					}
				}
			} catch (BusinessException e) {
				throw new AuthenticationServiceException("Could not retrieve user : "+login+" in domain : "+domain, e);
			}
		}
		
		// invisible domain and user not found (uid login or found in no domain)
		if (foundUser == null || domain == null) {
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
	          "Bad credentials, no domain specified and user found in no domain"), domain);
		}

		// invisible domain and user found or visible domain and user found
		try {
			user = userService.findAndCreateUser(foundUser.getMail(), domain);
			
			// if we already have a guest with the same mail, and then, a domain with
			// this mail is added in linshare, when the domain user connects he should not
			// retrieve the guest account. if the two user are in the same domain, we can't
			// do anything I think...
			if (!domain.equals(user.getDomainId())) {
				throw new BadDomainException("User "+user.getLogin()+" was found but not in the domain referenced in DB (DB: "+user.getDomainId()+", found: "+domain);
			}
			
		} catch (BusinessException e) {
			logger.error(e);
			throw new AuthenticationServiceException("Could not create user account: "+foundUser.getMail(), e);
		}

        List<GrantedAuthority> grantedAuthorities = RoleProvider.getRoles(user);

		return new org.springframework.security.userdetails.User(user.getLogin(), "", true, true, true, true,
		                grantedAuthorities.toArray(new GrantedAuthority[0]));
	}

}