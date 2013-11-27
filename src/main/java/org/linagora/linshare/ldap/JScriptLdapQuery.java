/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2013 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2013. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.ldap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.naming.CommunicationException;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.LdapName;

import org.linagora.linshare.core.domain.entities.DomainPattern;
import org.linagora.linshare.core.domain.entities.Internal;
import org.linagora.linshare.core.domain.entities.LDAPConnection;
import org.linagora.linshare.core.domain.entities.LdapAttribute;
import org.linagora.linshare.core.domain.entities.User;
import org.linid.dm.authorization.lql.JScriptEvaluator;
import org.linid.dm.authorization.lql.LqlRequestCtx;
import org.linid.dm.authorization.lql.dnlist.IDnList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.BindAuthenticator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.unboundid.ldap.sdk.Filter;

public class JScriptLdapQuery {

	/** Attributes **/

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(JScriptLdapQuery.class);

	// Java to JavaScript Translator object
	private JScriptEvaluator evaluator;

	private String baseDn;

	private DomainPattern domainPattern;

	private LqlRequestCtx lqlctx;

	private IDnList dnList;

	private BeanInfo beanInfo;

	/**
	 * @param jScriptEvaluator
	 * @param ldapJndiService
	 * @param ldapConnection
	 * @param baseDn
	 * @param domainPattern
	 */
	public JScriptLdapQuery(LqlRequestCtx ctx, String baseDn, DomainPattern domainPattern, IDnList dnList) throws NamingException, IOException {
		super();
		this.lqlctx = ctx;
		this.evaluator = JScriptEvaluator.getInstance(ctx.getLdapCtx(), dnList);
		this.baseDn = baseDn;
		this.domainPattern = domainPattern;

		try {
			this.beanInfo = Introspector.getBeanInfo(Internal.class);
		} catch (IntrospectionException e) {
			logger.error("Introspection of Internal user class impossible.");
			logger.debug("message : " + e.getMessage());
		}

	}

	/** Methods **/

	public List<String> evaluate(String lqlExpression) throws NamingException {
		try {
			Date date_before = new Date();
			JScriptEvaluator evaluator = JScriptEvaluator.getInstance(lqlctx.getLdapCtx(), dnList);
			List<String> evalToStringList = evaluator.evalToStringList(lqlExpression, lqlctx.getVariables());
			if (logger.isDebugEnabled()) {
				Date date_after = new Date();
				logger.debug("diff : " + String.valueOf(date_after.getTime() - date_before.getTime()));
			}
			return evalToStringList;
		} catch (IOException e) {
			try {
				lqlctx.renewLdapCtx();
			} catch (NamingException e1) {
				return null;
			}
			return evaluate(lqlExpression);
		}
	}

	private void logLqlQuery(String command, String pattern) {
		if (logger.isDebugEnabled()) {
			logger.debug("lql command " + command);
			logger.debug("pattern: " + pattern);
			String cmd = command.replaceAll("\"[ ]*[+][ ]*pattern[ ]*[+][ ]*\"", pattern);
			cmd = cmd.replaceAll("\"[ ]*[+][ ]*mail[ ]*[+][ ]*\"", pattern);
			logger.debug("ldap filter : " + cmd);
		}
	}

	private void logLqlQuery(String command, String mail, String first_name, String last_name) {
		if (logger.isDebugEnabled()) {
			logger.debug("lql command " + command);
			logger.debug("first_name: " + first_name);
			logger.debug("last_name: " + last_name);
			String cmd = command.replaceAll("\"[ ]*[+][ ]*last_name[ ]*[+][ ]*\"", last_name);
			cmd = cmd.replaceAll("\"[ ]*[+][ ]*first_name[ ]*[+][ ]*\"", first_name);
			if (mail != null) {
				cmd = cmd.replaceAll("\"[ ]*[+][ ]*mail[ ]*[+][ ]*\"", mail);
			}
			logger.debug("ldap filter : " + cmd);
		}
	}

	private void logLqlQuery(String command, String first_name, String last_name) {
		logLqlQuery(command, null, first_name, last_name);
	}

	/**
	 * 
	 * @param pattern
	 *            : could be first name, last name, or mail fragment.
	 * @return
	 * @throws NamingException
	 */
	public List<User> complete(String pattern) throws NamingException {

		// Getting lql expression for completion
		String command = domainPattern.getAutoCompleteCommandOnAllAttributes();
		pattern = addExpansionCharacters(pattern);

		// Setting lql query parameters
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("pattern", pattern);
		if (logger.isDebugEnabled())
			logLqlQuery(command, pattern);

		// searching ldap directory with pattern
		List<String> dnResultList = this.evaluate(command);

		// Building user list from dn (getting needed attributes)
		return dnListToUsersList(dnResultList, true);
	}

	/**
	 * 
	 * @param pattern
	 *            : could be first name, surname, or mail fragment.
	 * @return
	 * @throws NamingException
	 */
	public List<User> complete(String first_name, String last_name) throws NamingException {

		// Getting lql expression for completion
		String command = domainPattern.getAutoCompleteCommandOnFirstAndLastName();
		first_name = addExpansionCharacters(first_name);
		last_name = addExpansionCharacters(last_name);

		// Setting lql query parameters
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("first_name", first_name);
		vars.put("last_name", last_name);
		logLqlQuery(command, first_name, last_name);

		// searching ldap directory with pattern
		List<String> dnResultList = this.evaluate(command);

		return dnListToUsersList(dnResultList, true);
	}

	/**
	 * 
	 * @param dnResultList
	 *            : // list of dn without baseDn used by the previous search.
	 * @param completionMode
	 *            : completion mode return a user object with only mail,
	 *            firstname, and lastname set. Otherwise all defined attributes
	 *            will be search and set (mail, firstname, lastname, uid, ...)
	 * @return List of user
	 */
	private List<User> dnListToUsersList(List<String> dnResultList, boolean completionMode) {
		ControlContext controlContext = initControlContext(completionMode);

		// converting resulting dn to User object
		List<User> users = new ArrayList<User>();
		for (String dn : dnResultList) {
			logger.debug("current dn: " + dn);
			Date date_before = new Date();
			User user = null;
			try {
				user = dnToUser(dn, controlContext.getLdapDbAttributes(), controlContext.getSearchControls());
			} catch (NamingException e) {
				logger.error(e.getMessage());
				logger.debug(e.toString());
			}
			Date date_after = new Date();
			logger.debug("fin dnToUser : " + String.valueOf(date_after.getTime() - date_before.getTime()) + " milliseconds.");
			if (user != null) {
				users.add(user);
			}
		}
		return users;
	}

	private User dnToUser(String dn, boolean completionMode) throws NamingException {
		ControlContext controlContext = initControlContext(completionMode);
		return dnToUser(dn, controlContext.getLdapDbAttributes(), controlContext.getSearchControls());
	}

	private ControlContext initControlContext(boolean completionMode) {
		// Initialization of bean introspection
		if (beanInfo == null) {
			logger.error("Introspection of Internal user class impossible. Bean inspector is not initialised.");
			return null;
		}

		// Get only ldap attributes needed for completion
		Map<String, LdapAttribute> ldapDbAttributes;
		if (completionMode) {
			// Get only ldap attributes needed for completion
			ldapDbAttributes = getLdapDbAttributeForCompletion();
		} else {
			// Get only ldap attributes
			ldapDbAttributes = getLdapDbAttribute();
		}

		// String list of ldap attributes
		Collection<String> ldapAttrList = getLdapAttrList(ldapDbAttributes);

		// ldapContext ldapCtx, String base, String filter, int scope)
		SearchControls scs = new SearchControls();
		scs.setSearchScope(SearchControls.SUBTREE_SCOPE);

		// Attributes to retrieve from ldap.
		logger.debug("ldap attributes to retrieve : " + ldapAttrList.toString());
		scs.setReturningAttributes(ldapAttrList.toArray(new String[ldapAttrList.size()]));
		return new ControlContext(ldapDbAttributes, scs);
	}

	private User dnToUser(String dn, Map<String, LdapAttribute> ldapDbAttributes, SearchControls scs) throws NamingException {
		// returned value
		User user = new Internal();
		NamingEnumeration<SearchResult> results = lqlctx.getLdapCtx().search(dn, "(objectclass=*)", scs);
		while (results != null && results.hasMore()) {
			SearchResult entry = (SearchResult) results.next();

			// Handle the entry's response controls (if any)
			if (entry instanceof HasControls) {
				Control[] controls = ((HasControls) entry).getControls();
				if (logger.isDebugEnabled()) {
					logger.debug("entry name has controls " + controls.toString());
				}
			}

			// setting ldap attributes to user object.
			for (String dbAttrKey : ldapDbAttributes.keySet()) {
				LdapAttribute dbAttr = ldapDbAttributes.get(dbAttrKey);
				String ldapAttrName = dbAttr.getAttribute();
				logger.debug("dbAttrKey = " + dbAttrKey + ", ldap attr = " + ldapAttrName);

				Attribute ldapAttr = entry.getAttributes().get(ldapAttrName);
				boolean isNull = false;
				String value = null;
				try {
					// ldapAttr and value can be null. ldapAttr.get() can raise
					// NPE.
					value = (String) ldapAttr.get();
				} catch (NullPointerException e) {
					isNull = true;
				}

				if (value == null)
					isNull = true;

				if (isNull) {
					if (dbAttr.getSystem()) {
						logger.error("Attribute : '" + ldapAttrName + "' can not be null, it is required by the system.");
						return null;
					} else {
						logger.debug("Attribute : '" + ldapAttrName + "' is null.");
						continue;
					}
				} else {
					logger.debug("value : " + value);
					// updating user property with current attribute value
					if (!setUserAttribute(user, dbAttrKey, value)) {
						logger.error("Can not set attribute : " + dbAttrKey + " with value : " + value);
						return null;
					}
				}
			}
		}
		return user;
	}

	/**
	 * Convert database LDAP attributes map to a attribute name list.
	 * 
	 * @param ldapDbAttributes
	 *            : map of database LDAP attributes
	 * @return List of attribute names.
	 */
	private Collection<String> getLdapAttrList(Map<String, LdapAttribute> ldapDbAttributes) {
		Collection<String> ldapAttrList = Maps.transformValues(ldapDbAttributes, new Function<LdapAttribute, String>() {
			public String apply(LdapAttribute input) {
				return input.getAttribute();
			}
		}).values();
		return ldapAttrList;
	}

	/**
	 * Filtering database LDAP attributes map to get only attributes needed for
	 * completion.
	 * 
	 * @return
	 */
	private Map<String, LdapAttribute> getLdapDbAttributeForCompletion() {
		Map<String, LdapAttribute> dbAttributes = domainPattern.getAttributes();
		Predicate<LdapAttribute> completionFilter = new Predicate<LdapAttribute>() {
			public boolean apply(LdapAttribute attr) {
				if (attr.getEnable()) {
					// Is attribute needed for completion ?
					return attr.getCompletion();
				}
				return false;
			}
		};

		Map<String, LdapAttribute> filterValues = Maps.filterValues(dbAttributes, completionFilter);
		return filterValues;
	}

	/**
	 * Filtering database LDAP attributes map to get only attributes needed for
	 * build a user.
	 * 
	 * @return
	 */
	private Map<String, LdapAttribute> getLdapDbAttribute() {
		Map<String, LdapAttribute> dbAttributes = domainPattern.getAttributes();
		Predicate<LdapAttribute> completionFilter = new Predicate<LdapAttribute>() {
			public boolean apply(LdapAttribute attr) {
				return attr.getEnable();
			}
		};

		Map<String, LdapAttribute> filterValues = Maps.filterValues(dbAttributes, completionFilter);
		return filterValues;
	}

	private boolean setUserAttribute(User user, String attr_key, String curValue) {
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			Method userSetter = pd.getWriteMethod();
			String method = DomainPattern.USER_METHOD_MAPPING.get(attr_key);
			if (userSetter != null && method.equals(userSetter.getName())) {
				try {
					userSetter.invoke(user, curValue);
					return true;
				} catch (Exception e) {
					logger.error("Introspection : can not call method '" + userSetter.getName() + "' on User object.");
					logger.debug("message : " + e.getMessage());
					break;
				}
			}
		}
		return false;
	}

	/**
	 * This method allow to search a user from part of his mail or/and first
	 * name or/and last name
	 * 
	 * @param mail
	 * @param first_name
	 * @param last_name
	 * @return
	 * @throws NamingException
	 */
	public List<User> searchUser(String mail, String first_name, String last_name) throws NamingException {

		// Getting lql expression for completion
		String command = domainPattern.getSearchUserCommand();
		mail = addExpansionCharacters(mail);
		first_name = addExpansionCharacters(first_name);
		last_name = addExpansionCharacters(last_name);

		// Setting lql query parameters
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("mail", mail);
		vars.put("first_name", first_name);
		vars.put("last_name", last_name);
		if (logger.isDebugEnabled())
			logLqlQuery(command, mail, first_name, last_name);

		// searching ldap directory with pattern
		List<String> dnResultList = this.evaluate(command);

		return dnListToUsersList(dnResultList, false);
	}

	/**
	 * This method allow to find a user from his mail (entire mail, not a
	 * fragment).
	 * 
	 * @param mail
	 * @return a user
	 * @throws NamingException
	 */
	public User findUser(String mail) throws NamingException {

		// Getting lql expression for completion
		String command = domainPattern.getSearchUserCommand();
		if (mail == null || mail.length() < 1) {
			return null;
		}
		String first_name = "*";
		String last_name = "*";

		// Setting lql query parameters
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("mail", mail);
		vars.put("first_name", first_name);
		vars.put("last_name", last_name);
		if (logger.isDebugEnabled())
			logLqlQuery(command, mail, first_name, last_name);

		// searching ldap directory with pattern
		List<String> dnResultList = this.evaluate(command);

		if (dnResultList.size() == 1) {
			return dnToUser(dnResultList.get(0), false);
		} else if (dnResultList.size() > 1) {
			logger.error("mail must be unique ! " + mail);
		}
		return null;
	}

	/**
	 * test if a user exists using his mail. (entire mail, not a fragment)
	 * 
	 * @param mail
	 * @return
	 * @throws NamingException
	 */
	public Boolean isUserExist(String mail) throws NamingException {

		if (mail == null || mail.length() < 1) {
			return false;
		}
		String first_name = "*";
		String last_name = "*";

		// Getting lql expression for completion
		String command = domainPattern.getSearchUserCommand();

		// Setting lql query parameters
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("mail", mail);
		vars.put("first_name", first_name);
		vars.put("last_name", last_name);
		if (logger.isDebugEnabled())
			logLqlQuery(command, mail, first_name, last_name);

		// searching LDAP directory with pattern
		List<String> dnResultList = this.evaluate(command);
		if (dnResultList != null && !dnResultList.isEmpty()) {
			if (dnResultList.size() == 1) {
				return true;
			}
			logger.error("Multiple results found for mail : " + mail);
		}
		return false;
	}

	/**
	 * Ldap Authentification method
	 * 
	 * @param login
	 * @param userPasswd
	 * @return
	 * @throws NamingException
	 */
	public User auth(LDAPConnection ldapConnection, String login, String userPasswd) throws NamingException {

		String command = domainPattern.getAuthCommand();
		Map<String, Object> vars = lqlctx.getVariables();
		vars.put("login", login);
		if (logger.isDebugEnabled())
			logLqlQuery(command, login);

		// searching ldap directory with pattern
		// InvalidSearchFilterException
		List<String> dnResultList = this.evaluate(command);

		if (dnResultList == null || dnResultList.size() < 1) {
			throw new NameNotFoundException("No user found for login: " + login);
		} else if (dnResultList.size() > 1) {
			logger.error("The authentification query had returned more than one user !!!");
			return null;
		}

		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(ldapConnection.getProviderUrl());
		ldapContextSource.setBase(baseDn);
		String userDn = dnResultList.get(0);

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDn, userPasswd);

		BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
		authenticator.setUserDnPatterns(new String[] { "{0}" });
		try {
			ldapContextSource.afterPropertiesSet();
			authenticator.authenticate(authentication);
		} catch (BadCredentialsException e) {
			logger.debug("auth failed : BadCredentialsException(" + userDn + ")");
			return null;
		} catch (Exception e) {
			logger.error("auth failed for unexpected exception: " + e.getMessage());
			return null;
		}
		return dnToUser(userDn, false);
	}

	/**
	 * This method is designed to add expansion characters to the input string.
	 * 
	 * @param string
	 *            : any string
	 * @return
	 */
	private String addExpansionCharacters(String string) {
		if (string == null || string.length() < 1) {
			string = "*";
		} else {
			string = "*" + string.trim() + "*";
		}
		return string;
	}
}
