/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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
package org.linagora.linshare.core.domain.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.linagora.linshare.core.domain.constants.DomainType;
import org.linagora.linshare.core.domain.vo.AbstractDomainVo;
import org.linagora.linshare.webservice.dto.DomainDto;

public abstract class AbstractDomain {
	/**
	 * Database persistence identifier
	 */
	private long persistenceId;

	protected String identifier;

	protected String label;

	protected String defaultLocale;

	protected Role defaultRole;

	protected String description;

	protected boolean template;

	protected boolean enable;

	protected MessagesConfiguration messagesConfiguration;

	protected LdapUserProvider userProvider;

	protected DomainPolicy policy;

	protected Set<DomainAccessRule> domainAccessRules;

	protected Set<Functionality> functionalities;

	protected Set<User> userList;

	protected AbstractDomain parentDomain;

	protected Set<AbstractDomain> subdomain;

	protected List<ShareExpiryRule> shareExpiryRules;

	protected Long usedSpace;

	protected Long authShowOrder;

	protected AbstractDomain() {
		this.identifier = null;
	}

	protected AbstractDomain(String identifier, String label) {
		this.identifier = identifier;
		this.label = label;
		this.description = null;
		this.functionalities = new HashSet<Functionality>();
		this.userList = new HashSet<User>();
		this.domainAccessRules = new HashSet<DomainAccessRule>();
		this.parentDomain = null;
		this.subdomain = new HashSet<AbstractDomain>();
		this.defaultRole = Role.SIMPLE;
		this.defaultLocale = "en";
		this.enable = true;
		this.template = false;
		this.usedSpace = new Long(0);
		this.shareExpiryRules = new ArrayList<ShareExpiryRule>();
		this.messagesConfiguration = new MessagesConfiguration();
		this.policy = null;
		this.authShowOrder = new Long(1);
	}

	public AbstractDomain(AbstractDomainVo d) {
		this.identifier = d.getIdentifier();
		this.label = d.getLabel();
		this.description = d.getDomainDescription();
		this.functionalities = new HashSet<Functionality>();
		this.messagesConfiguration = new MessagesConfiguration();
		this.userList = new HashSet<User>();
		this.domainAccessRules = new HashSet<DomainAccessRule>();
		this.parentDomain = null;
		this.subdomain = new HashSet<AbstractDomain>();
		this.defaultRole = d.getDefaultRole();
		this.defaultLocale = d.getDefaultLocale();
		this.enable = d.isEnable();
		this.template = d.isTemplate();
		this.usedSpace = d.getUsedSpace();
		this.shareExpiryRules = new ArrayList<ShareExpiryRule>();
		this.policy = null;
		this.authShowOrder = new Long(1);
	}

	public AbstractDomain(DomainDto domainDto, AbstractDomain parent) {
		this.identifier = domainDto.getIdentifier();
		this.label = domainDto.getLabel();
		this.description = domainDto.getDescription();
		this.functionalities = new HashSet<Functionality>();
		this.messagesConfiguration = new MessagesConfiguration();
		this.userList = new HashSet<User>();
		this.domainAccessRules = new HashSet<DomainAccessRule>();
		this.parentDomain = parent;
		this.enable = true;
		this.template = false;
		this.usedSpace = new Long(0);		
		this.subdomain = new HashSet<AbstractDomain>();
		this.defaultRole = Role.valueOf(domainDto.getUserRole());
		this.defaultLocale = domainDto.getLocale();
		this.authShowOrder = new Long(1);
	}

	public void updateDomainWith(AbstractDomain d) {
		this.label = d.getLabel();
		this.description = d.getDescription();
		this.defaultRole = d.getDefaultRole();
		this.defaultLocale = d.getDefaultLocale();
		this.enable = d.isEnable();
		this.template = d.isTemplate();
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public Role getDefaultRole() {
		return defaultRole;
	}

	public String getDescription() {
		return description;
	}

	public Set<Functionality> getFunctionalities() {
		return functionalities;
	}

	public String getIdentifier() {
		return identifier;
	}

	public long getPersistenceId() {
		return persistenceId;
	}

	public Set<User> getUserList() {
		return userList;
	}

	public boolean isEnable() {
		return enable;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public void setDefaultRole(Role defaultRole) {
		this.defaultRole = defaultRole;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setFunctionalities(Set<Functionality> functionalities) {
		this.functionalities = functionalities;
	}

	public void setPersistenceId(long persistenceId) {
		this.persistenceId = persistenceId;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public void setUserList(Set<User> userList) {
		this.userList = userList;
	}

	@Override
	public String toString() {
		return "[Domain with id: " + identifier + "]";
	}

	public AbstractDomain getParentDomain() {
		return parentDomain;
	}

	public void setParentDomain(AbstractDomain parentDomain) {
		this.parentDomain = parentDomain;
	}

	public Set<AbstractDomain> getSubdomain() {
		return subdomain;
	}

	public void addSubdomain(AbstractDomain subdomain) {
		this.subdomain.add(subdomain);
	}

	public void addFunctionality(Functionality functionality) {
		this.functionalities.add(functionality);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public LdapUserProvider getUserProvider() {
		return userProvider;
	}

	public void setUserProvider(LdapUserProvider userProvider) {
		this.userProvider = userProvider;
	}

	public DomainPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(DomainPolicy policy) {
		this.policy = policy;
	}

	public Set<DomainAccessRule> getDomainAccessRules() {
		return domainAccessRules;
	}

	public void setDomainAccessRules(Set<DomainAccessRule> domainAccessRules) {
		this.domainAccessRules = domainAccessRules;
	}

	public MessagesConfiguration getMessagesConfiguration() {
		return messagesConfiguration;
	}

	public void setMessagesConfiguration(
			MessagesConfiguration messagesConfiguration) {
		this.messagesConfiguration = messagesConfiguration;
	}

	public List<ShareExpiryRule> getShareExpiryRules() {
		return shareExpiryRules;
	}

	public void setShareExpiryRules(List<ShareExpiryRule> shareExpiryRules) {
		this.shareExpiryRules = shareExpiryRules;
	}

	public Long getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(Long usedSpace) {
		this.usedSpace = usedSpace;
	}

	public abstract DomainType getDomainType();

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		AbstractDomain d = (AbstractDomain) arg0;
		return this.getIdentifier().equals(d.getIdentifier());
	}

	@Override
	public int hashCode() {
		return this.getIdentifier().hashCode();
	}

	public Long getAuthShowOrder() {
		return authShowOrder;
	}

	public void setAuthShowOrder(Long authShowOrder) {
		this.authShowOrder = authShowOrder;
	}
}
