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
package org.linagora.linshare.view.tapestry.pages.administration.domains;

import java.util.List;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.linagora.linshare.core.domain.vo.AbstractDomainVo;
import org.linagora.linshare.core.domain.vo.DomainPatternVo;
import org.linagora.linshare.core.domain.vo.LDAPConnectionVo;
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

    @Persist
    @Property
    private List<DomainPatternVo> domainPatterns;

    @Persist
    @Property
    private List<LDAPConnectionVo> ldapConnections;

    @Property
    private AbstractDomainVo domain;

    @Property
    private DomainPatternVo domainPattern;

    @Property
    private LDAPConnectionVo ldapConnection;

    @Persist
    @Property
    private AbstractDomainVo selectedDomain;

    @Property
    @Persist
    private boolean superadmin;

    @Property
    @Persist(value="flash")
    private String domainToDelete;

    @Property
    @Persist(value="flash")
    private String patternToDelete;

    @Property
    @Persist(value="flash")
    private String connectionToDelete;

    @SetupRender
    public void init() throws BusinessException {
        domains = domainFacade.findAllTopDomain();
        domainPatterns = domainFacade.findAllUserDomainPatterns();
        ldapConnections = domainFacade.findAllLDAPConnections();
    }

    @OnEvent(value="domainDeleteEvent")
    public void deleteDomain() throws BusinessException {
        domainFacade.deleteDomain(domainToDelete, loginUser);
        domains = domainFacade.findAllTopDomain();
    }

    @OnEvent(value="patternDeleteEvent")
    public void deletePattern() throws BusinessException {
        domainFacade.deletePattern(patternToDelete, loginUser);
        domains = domainFacade.findAllTopDomain();
    }

    @OnEvent(value="connectionDeleteEvent")
    public void deleteConnection() throws BusinessException {
        domainFacade.deleteConnection(connectionToDelete, loginUser);
        ldapConnections = domainFacade.findAllLDAPConnections();
    }

    public boolean getConnectionIsDeletable() throws BusinessException {
        return domainFacade.connectionIsDeletable(ldapConnection.getIdentifier(), loginUser);
    }

    public boolean getPatternIsDeletable() throws BusinessException {
        return domainFacade.patternIsDeletable(domainPattern.getIdentifier(), loginUser);
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

    public void onActionFromDeletePattern(String pattern) {
        this.patternToDelete = pattern;
    }

    public void onActionFromDeleteConnection(String connection) {
        this.connectionToDelete = connection;
    }

    Object onException(Throwable cause) {
        shareSessionObjects.addError(messages.get("global.exception.message"));
        logger.error(cause.getMessage());
        cause.printStackTrace();
        return this;
    }


}