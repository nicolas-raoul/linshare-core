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
package org.linagora.linshare.view.tapestry.pages.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.vo.DisplayableLogEntryVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.FunctionalityFacade;
import org.linagora.linshare.core.facade.LogEntryFacade;
import org.linagora.linshare.core.facade.UserAutoCompleteFacade;
import org.linagora.linshare.core.facade.UserFacade;
import org.linagora.linshare.core.utils.FileUtils;
import org.linagora.linshare.core.utils.StringJoiner;
import org.linagora.linshare.view.tapestry.beans.LogCriteriaBean;
import org.linagora.linshare.view.tapestry.beans.ShareSessionObjects;
import org.linagora.linshare.view.tapestry.enums.CriterionMatchMode;
import org.slf4j.Logger;

import com.google.common.collect.Lists;


/**
 * display the timeline of all action of the current user
 * @author ncharles
 *
 */
public class Index {

    @SessionState
    @Property
    private ShareSessionObjects shareSessionObjects;


	/* ***********************************************************
	 *                Injected services 
	 ************************************************************ */  

	@Inject
	private Messages messages;

	@Inject
	private LogEntryFacade logEntryFacade;
	
	@Inject
	private UserAutoCompleteFacade userAutoCompleteFacade;
	
	@Inject
	private UserFacade userFacade;
	
	@Environmental
	private FormSupport formSupport;
	
	@Component
	private Form formReport;
 
    @Inject
    private Logger logger;

	/* ***********************************************************
	 *                         Properties
	 ************************************************************ */    

	@Property
	private String actorListMails;

	@Property
	private String targetListMails;


	@Property(write=false) //used in tml
	private ValueEncoder<LogAction> logActionEncoder;

	@Property(write=false) //used in tml
	private SelectModel logActionModel;
	
	/**
	 * the list of traces matching the request
	 */
	@Persist
	@Property
	private List<DisplayableLogEntryVo> logEntries;
	
	@Property //used in the tml for the grid
	private DisplayableLogEntryVo logEntry;
	
	@Persist 
	@Property(write=false) //used in tml
	private boolean displayGrid;
	
	@Property
	private LogCriteriaBean criteria;
	
	@SessionState
	@Property
	private UserVo userVo;

	@Property
	private int autocompleteMin;
	
	@Inject
	private FunctionalityFacade functionalityFacade;
	
	
	/* ***********************************************************
	 *                       Phase processing
	 ************************************************************ */
	
	@SetupRender
	public void init() throws BusinessException {
		autocompleteMin = functionalityFacade.completionThreshold(userVo.getDomainIdentifier());
	}
	
	/* ***********************************************************
	 *                   Event handlers&processing
	 ************************************************************ */	
	
	public void onActivate() {
		logActionEncoder = new EnumValueEncoder<LogAction>(LogAction.class);
		logActionModel = new EnumSelectModel(LogAction.class, messages);

		if (null == criteria) {
			criteria = new LogCriteriaBean();
		} else {
			// actor is FIXED
			actorListMails = userVo.getMail();

			if ((criteria.getTargetMails() != null)
					&& (criteria.getTargetMails().size() > 0)) {
				targetListMails = StringJoiner.join(criteria.getTargetMails(), ",");
			}
		}
	}
	
	
	public Object onActionFromReset() {
		criteria = new LogCriteriaBean();
		logEntries = null;
		displayGrid = false;
		return null;
	}
	
	public void onValidateFormFromFormReport() {
		if(null!=criteria.getBeforeDate() 
				&& null !=criteria.getAfterDate() 
				&& criteria.getAfterDate().before(criteria.getBeforeDate())){
			formReport.recordError(messages.get("pages.administration.audit.error.invalidDate"));
		}
	}
	
	public Object onSuccessFromFormReport()  {
		/*
		 * XXX: fugly fix
		 */
		criteria.setActorMails(Lists.newArrayList(userVo.getMail()));
		criteria.setActorDomain(userVo.getMail());
		criteria.setActorFirstname(null);
		criteria.setActorLastname(null);

		if ((targetListMails != null) && (targetListMails.length() > 0)) {
			criteria.setTargetMails(StringJoiner.split(targetListMails, ","));
		}
		logEntries = logEntryFacade.findByCriteria(criteria, userVo);
		displayGrid = true;

		return null;
	}
	
	/**
	 * AutoCompletion for name field.
	 * @param value the value entered by the user
	 * @return list the list of string matched by value.
	 */
	public List<String> onProvideCompletionsFromTargetMails(String value){
		return autoCompleteMail(value);
	}

	/* ***********************************************************
	 *                          Helpers
	 ************************************************************ */
	
	private List<String> autoCompleteMail(String value) {
		List<String> res = new ArrayList<String>();
		try {
			res = userAutoCompleteFacade.autoCompleteMail(userVo, value);
		} catch (BusinessException e) {
			logger.error("Can not autocomplete mails" + e.getMessage());
		}
		return res;
	}
	

	public Date getBeforeDate() {
		return null == criteria.getBeforeDate() ? null : criteria.getBeforeDate().getTime();
	}
	
	public void setBeforeDate(final Date d) {
		Calendar c = null;
		if(null!= d){
			c = new GregorianCalendar();
			c.setTime(d);
			//Initialize to the starting day at 00h
			c.set(GregorianCalendar.HOUR, 0);
			c.set(GregorianCalendar.MINUTE, 0);
			c.set(GregorianCalendar.SECOND, 0);
		} 
		criteria.setBeforeDate(c);
	}
	
	public Date getAfterDate() {
		return null == criteria.getAfterDate() ? null : criteria.getAfterDate().getTime();
	}
	
	public void setAfterDate(final Date d) {
		Calendar c = null;
		if(null!= d){
			c = new GregorianCalendar();
			c.setTime(d);
			//Initialize to the next day at 00h
			c.set(GregorianCalendar.HOUR, 23);
			c.set(GregorianCalendar.MINUTE, 59);
			c.set(GregorianCalendar.SECOND, 59);
		}
		criteria.setAfterDate(c);
	}


	public String getFilesize() {
		if (logEntry.getFileSize() == null)
			return "";
		return FileUtils.getFriendlySize(logEntry.getFileSize(), messages);
	}
	
	public String getActionDate() {
		SimpleDateFormat formatter = new SimpleDateFormat(messages.get("global.pattern.timestamp"));
		return formatter.format(logEntry.getActionDate().getTime());
	}

	public CriterionMatchMode getFileNameMatchModeStart() { return CriterionMatchMode.START; }
	public CriterionMatchMode getFileNameMatchModeAnywhere() { return CriterionMatchMode.ANYWHERE; }

    
    Object onException(Throwable cause) {
    	shareSessionObjects.addError(messages.get("global.exception.message"));
    	logger.error(cause.getMessage());
    	cause.printStackTrace();
    	return this;
    }
}
