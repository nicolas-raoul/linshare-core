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
package org.linagora.linshare.view.tapestry.pages.administration.domains.subdomains;

import java.util.List;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.linagora.linshare.core.domain.vo.AbstractDomainVo;
import org.linagora.linshare.core.domain.vo.GuestDomainVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.AbstractDomainFacade;
import org.linagora.linshare.view.tapestry.beans.ShareSessionObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Index {
    
	private static Logger logger = LoggerFactory.getLogger(Index.class);

    @SessionState
    @Property
    private ShareSessionObjects shareSessionObjects;
    
	@Inject
	private Messages messages;
	
    @Inject
    private AbstractDomainFacade domainFacade;
    
    @SessionState
    private UserVo loginUser;
	
	@Persist
	@Property
	private List<AbstractDomainVo> domains;
	
	@Property
	private AbstractDomainVo domain;
	
	@Property
	private GuestDomainVo guestDomain;
	
	@Persist
	@Property
	private AbstractDomainVo currentTopDomain;

    @Property
    @Persist(value="flash")
	private String domainToDelete;

	@SetupRender
    public void init() throws BusinessException {
    	domains = domainFacade.findAllSubDomainWithoutGuestDomain(currentTopDomain.getIdentifier());
    	guestDomain = domainFacade.findGuestDomain(currentTopDomain.getIdentifier());
	}
    
	@OnEvent(value="domainDeleteEvent")
    public void deleteDomain() throws BusinessException {
		domainFacade.deleteDomain(domainToDelete, loginUser);
		domains = domainFacade.findAllSubDomainWithoutGuestDomain(currentTopDomain.getIdentifier());
		guestDomain = domainFacade.findGuestDomain(currentTopDomain.getIdentifier());
    }
    
	public String getConnectionIdentifier() {
		return domain.getLdapIdentifier();
	}
	
	public String getPatternIdentifier() {
		return domain.getPatternIdentifier();
	}

    public void onActionFromDeleteDomain(String domain) {
        this.domainToDelete = domain;
    }
    
    public String getTopDomainName() {
    	return currentTopDomain.getLabel();
    }
    
    public boolean getCanCreateGuestDomain() throws BusinessException {
    	return domainFacade.canCreateGuestDomain(currentTopDomain.getIdentifier());
    }
    
    public boolean getGuestDomainAllowed() throws BusinessException {
    	return domainFacade.guestDomainAllowed(currentTopDomain.getIdentifier());
    }

    public void onActivate(String identifier) throws BusinessException {
		if (identifier != null) {
			currentTopDomain = domainFacade.retrieveDomain(identifier);
		}
	}

    public Object[] getContextParams()
    {
      return new String[]{currentTopDomain.getIdentifier(), domain.getIdentifier()};
    }
    
    public Object[] getGuestContextParams()
    {
    	logger.debug("guestContextParams call");
    	if(guestDomain == null) {
    		logger.debug("Guest domain is null");
    		return new String[]{currentTopDomain.getIdentifier(), null};
    	} else {
    		logger.debug("Guest domain is not null");
    		return new String[]{currentTopDomain.getIdentifier(), guestDomain.getIdentifier()};
    	}
    } 
    
    public String getGuestDomainLabel() {
    		return guestDomain.getLabel();
    }
    
    Object onException(Throwable cause) {
    	shareSessionObjects.addError(messages.get("global.exception.message"));
    	logger.error(cause.getMessage());
    	cause.printStackTrace();
    	return this;
    }
}
