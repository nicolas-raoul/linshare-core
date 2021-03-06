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
package org.linagora.linshare.view.tapestry.components;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Import(library = {"jquery/jquery-1.7.2.js", "jquery/jquery.ui.core.js", "jquery/jquery.ui.datepicker.js", "jquery/jquery.ui.datepicker-fr.js", "DatePicker.js"},stylesheet = {"jquery/jquery-ui-1.8.21.custom.css", "DatePicker.css"})

public class DatePicker {
	private static final Logger logger = LoggerFactory.getLogger(DatePicker.class);
	
	@Parameter(required=false)
	@Property
	private Date minDate;
	
	@Parameter(required=false)
	@Property
	private Date maxDate;
	
	@Parameter(required=false)
	@Property
	private Date defaultDate;
	
	@Property
	private int dateMinD;
	@Property
	private int dateMinM;
	@Property
	private int dateMinY;
	@Property
	private int dateMaxD;
	@Property
	private int dateMaxM;
	@Property
	private int dateMaxY;
	@Property
	private int dateDefD;
	@Property
	private int dateDefM;
	@Property
	private int dateDefY;
	
	@Inject
	private PersistentLocale persistentLocale;
	
	@Inject
	private Messages messages;

	private Date datePicked;

	public Date getDatePicked() {
		return datePicked;
	}

	public void setDatePicked(Date datePicked) {
		this.datePicked = datePicked;
	}
	
	@SetupRender
	public void init() {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale(messages.get("components.datePicker.regional")));
		
		dateMinD = 0;
		dateMinM = 0;
		dateMinY = 0;
		dateMaxD = 0;
		dateMaxM = 0;
		dateMaxY = 0;
		dateDefD = 0;
		dateDefM = 0;
		dateDefY = 0;
		
		if (minDate != null) {
			Calendar calMin = dateFormat.getCalendar();
			calMin.setTime(minDate);
			dateMinD = calMin.get(Calendar.DAY_OF_MONTH);
			dateMinM = calMin.get(Calendar.MONTH);
			dateMinY = calMin.get(Calendar.YEAR);
		}
		if (maxDate != null) {
			Calendar calMax = dateFormat.getCalendar();
			calMax.setTime(maxDate);
			dateMaxD = calMax.get(Calendar.DAY_OF_MONTH);
			dateMaxM = calMax.get(Calendar.MONTH);
			dateMaxY = calMax.get(Calendar.YEAR);
		}
		if (defaultDate != null) {
			Calendar calDef = dateFormat.getCalendar();
			calDef.setTime(defaultDate);
			dateDefD = calDef.get(Calendar.DAY_OF_MONTH);
			dateDefM = calDef.get(Calendar.MONTH);
			dateDefY = calDef.get(Calendar.YEAR);
		}
	}
}
