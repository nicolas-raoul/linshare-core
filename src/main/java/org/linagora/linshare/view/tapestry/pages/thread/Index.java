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
package org.linagora.linshare.view.tapestry.pages.thread;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;
import org.linagora.linshare.core.domain.vo.ThreadVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.FunctionalityFacade;
import org.linagora.linshare.core.facade.ThreadEntryFacade;
import org.linagora.linshare.view.tapestry.beans.ShareSessionObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Index {

	private static Logger logger = LoggerFactory.getLogger(Index.class);

	/*
	 * Tapestry properties
	 */

	@SessionState
	@Property
	private ShareSessionObjects shareSessionObjects;

	@SessionState
	@Property
	private UserVo userVo;

	@Persist
	@Property
	private List<ThreadVo> threads;

	@Persist
	@Property
	private String pattern;

    @Property
    private ThreadVo current;
 
    @Property
    private boolean latest;

	/*
	 * Injected beans
	 */

    @InjectPage
    private ThreadContent threadContent;

	@Inject
	private PersistentLocale persistentLocale;

	@Inject
	private Messages messages;

	@Inject
	private FunctionalityFacade functionalityFacade;

	@Inject
	private ThreadEntryFacade threadEntryFacade;

	public Object onActivate() {
		if (!functionalityFacade
				.isEnableThreadTab(userVo.getDomainIdentifier()))
			return org.linagora.linshare.view.tapestry.pages.Index.class;
		return null;
	}

	@SetupRender
	public void init() throws BusinessException {
		if (!latest) {
			updateThreadList();
		}
	}
	
	public void onSuccessFromThreadSearch() throws BusinessException {
		StringUtils.trim(pattern);
		updateThreadList();
	}

    public Object onActionFromShowThreadContent(String lsUuid) {
		threadContent.setMySelectedThread(Iterables.find(threads,
				ThreadVo.equalTo(lsUuid)));
		return threadContent;
    }
 
    public Object onActionFromLatests() {
    	threads = null;
    	pattern = null;
    	return null;
    }

	/*
	 * Getters
	 */

	public boolean isShowCreateButton() {
		return functionalityFacade.isEnableCreateThread(userVo
				.getDomainIdentifier());
	}

	public int getCount() {
		try {
			return threadEntryFacade.countEntries(current);
		} catch (BusinessException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

	public String getCreationDate() {
		return getFormatter().format(current.getCreationDate().getTime());
	}

	public String getModificationDate() {
		return getFormatter().format(current.getModificationDate().getTime());
	}

	/*
	 * Exception Handling
	 */

	public Object onException(Throwable cause) {
		shareSessionObjects.addError(messages.get("global.exception.message"));
		logger.error(cause.getMessage());
		cause.printStackTrace();
		return this;
	}
	
	/*
	 * Helpers
	 */
	
	private void updateThreadList() throws BusinessException {
		latest = threads == null;
		if (latest) {
			threads = threadEntryFacade.getLatestThreads(userVo, 10);
		} else {
			threads = StringUtils.isNotBlank(pattern) ?
					threadEntryFacade.searchThread(userVo, pattern) :
					threadEntryFacade.getAllMyThread(userVo);
		}
	}
	
	private Format getFormatter() {
		return new SimpleDateFormat(messages.get("global.pattern.timestamp"));
	}
}