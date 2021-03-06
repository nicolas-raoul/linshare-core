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
package org.linagora.opends;

import org.opends.server.api.Backend;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.DN;
import org.opends.server.util.StaticUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to manage directory state (start/stop/status/...)
 * @author Sebastien Bahloul <sbahloul@linagora.com>
 */
public class LdapServer {

	/** The local logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(LdapServer.class);
	
	private static String DIRECTORY_REAL_ROOT = "dc=linpki,dc=org";
	
	public void start() throws Exception {
		LOGGER.info("Starting embedded OpenDS directory");
		EmbeddedOpenDS.startServer();
		EmbeddedOpenDS.initializeTestBackend(false, DIRECTORY_REAL_ROOT );
		Backend backend = DirectoryServer.getBackend(DN.decode(DIRECTORY_REAL_ROOT));
		backend.addEntry(StaticUtils.createEntry(DN.decode(DIRECTORY_REAL_ROOT)), null);
		if(EmbeddedOpenDS.class.getResource("test.ldif") == null || EmbeddedOpenDS.class.getResource("test.ldif").toURI().getPath() == null) {
			LOGGER.error("Unable to load LDIF sample content !");
		} else {
			EmbeddedOpenDS.importLdif(EmbeddedOpenDS.class.getResource("test.ldif").toURI().getPath());
			LOGGER.info("LDIF sample content loaded successfully");
		}
		LOGGER.info("Embedded OpenDS directory started");
	}
	
	public void stop() {
		EmbeddedOpenDS.shutdownServer("Normal stop process");
	}

}
