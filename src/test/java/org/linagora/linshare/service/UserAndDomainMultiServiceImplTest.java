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
package org.linagora.linshare.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.linagora.linshare.core.domain.constants.LinShareConstants;
import org.linagora.linshare.core.domain.constants.LinShareTestConstants;
import org.linagora.linshare.core.domain.entities.Role;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.AbstractDomainRepository;
import org.linagora.linshare.core.repository.DomainPolicyRepository;
import org.linagora.linshare.core.repository.FunctionalityRepository;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.UserAndDomainMultiService;
import org.linagora.linshare.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


@ContextConfiguration(locations = { 
		"classpath:springContext-datasource.xml",
		"classpath:springContext-repository.xml",
		"classpath:springContext-dao.xml",
		"classpath:springContext-service.xml",
		"classpath:springContext-business-service.xml",
		"classpath:springContext-facade.xml",
		"classpath:springContext-startopends.xml",
		"classpath:springContext-jackRabbit.xml",
		"classpath:springContext-test.xml"
		})
public class UserAndDomainMultiServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests{
	

	private static Logger logger = LoggerFactory.getLogger(UserAndDomainMultiServiceImplTest.class);

	private LoadingServiceTestDatas datas;
	
	@Autowired
	private FunctionalityRepository functionalityRepository;
	
	@Autowired
	private AbstractDomainRepository abstractDomainRepository;
	
	@Autowired
	private DomainPolicyRepository domainPolicyRepository;
	
	@Qualifier("userRepository")
	@Autowired
	private UserRepository<User> userRepository;
	
	@Autowired
	private UserAndDomainMultiService userAndDomainMultiService;	
	
	@Autowired
	private AbstractDomainService abstractDomainService;
	
	@Autowired
	private UserService userService;
	
	
	@Before
	public void setUp() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_SETUP);
		
		datas = new LoadingServiceTestDatas(functionalityRepository,abstractDomainRepository,domainPolicyRepository,userRepository,userService);
		
		datas.loadUsers();
		
		logger.debug(LinShareTestConstants.END_SETUP);
	}

	@After
	public void tearDown() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_TEARDOWN);
	
		datas.deleteUsers();
		
		logger.debug(LinShareTestConstants.END_TEARDOWN);
	}

	// TODO fix tests.
	@Ignore
	@Test
	public void testDeleteUserInSubDomain() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		UserVo root = new UserVo(userService.findOrCreateUser("root@localhost.localdomain", LinShareConstants.rootDomainIdentifier));
		User actor = datas.getUser1();
		userService.updateUserRole(actor.getLsUuid(), LoadingServiceTestDatas.topDomainName, actor.getMail(), Role.ADMIN, root);
		
		String user1Login = new String(datas.getUser1().getLogin());
		String user2Login = new String(datas.getUser2().getLogin());
		String user3Login = new String(datas.getUser3().getLogin());
		
		try {
			logger.debug("John Doe trying to delete Jane Smith (who is in a subdomain)");
			userAndDomainMultiService.deleteDomainAndUsers(actor, LoadingServiceTestDatas.subDomainName1);
		}catch (BusinessException e) {
			logger.error("userAndDomainMultiService can not delete a user in subdomain");
			e.printStackTrace();
		}
		
		User tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.topDomainName, user1Login);
		Assert.assertNotNull(tmpUser);
		
		tmpUser = userRepository.findByMail(user2Login);
		Assert.assertNull(tmpUser);
		
		tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.guestDomainName1, user3Login);
		Assert.assertNotNull(tmpUser);	
	
		logger.debug(LinShareTestConstants.END_TEST);
	}
	
	// TODO fix tests.
	@Ignore
	@Test
	public void testDeleteGuestInGuestDomain() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);

		UserVo root = new UserVo(userService.findOrCreateUser("root@localhost.localdomain", LinShareConstants.rootDomainIdentifier));
		User actor = datas.getUser1();
		userService.updateUserRole(actor.getLsUuid(), LoadingServiceTestDatas.topDomainName, actor.getMail(), Role.ADMIN, root);
		
		String user1Login = new String(datas.getUser1().getLogin());
		String user2Login = new String(datas.getUser2().getLogin());
		String user3Login = new String(datas.getUser3().getLogin());
		
		try {
			logger.debug("John Doe trying to delete Foo Bar (who is in a guest domain)");
			
			userAndDomainMultiService.deleteDomainAndUsers(actor, LoadingServiceTestDatas.guestDomainName1);
			
			
		}catch (BusinessException e) {
			logger.error("userAndDomainMultiService can not delete a user in guest domain");
			logger.error(e.toString());
		}
		
		User tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.topDomainName, user1Login);
		Assert.assertNotNull(tmpUser);
		
		tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.subDomainName1, user2Login);
		Assert.assertNotNull(tmpUser);
		
		tmpUser = userRepository.findByMail(user3Login);
		Assert.assertNull(tmpUser);
		
		logger.debug(LinShareTestConstants.END_TEST);
	}	
	
	// TODO fix tests.
	@Ignore
	@Test
	public void testDeleteUserInTopDomainWithSubDomainUser() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		User actor = userService.findOrCreateUser("root@localhost.localdomain", LinShareConstants.rootDomainIdentifier);
		
		String user1Login = new String(datas.getUser1().getLogin());
		String user2Login = new String(datas.getUser2().getLogin());
		String user3Login = new String(datas.getUser3().getLogin());

		
		try {
			logger.debug("Jane Smith trying to delete Top Domain then she had no right on it");
			
			userAndDomainMultiService.deleteDomainAndUsers(actor, LoadingServiceTestDatas.topDomainName);
			
		}catch (BusinessException e) {
			logger.error("userAndDomainMultiService can not delete a user in top domain");
			logger.error(e.toString());
		}
		
		User tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.topDomainName, user1Login);
		Assert.assertNull(tmpUser);
		
		tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.subDomainName1, user2Login);
		Assert.assertNull(tmpUser);
		
		tmpUser = userRepository.findByMailAndDomain(LoadingServiceTestDatas.guestDomainName1, user3Login);
		Assert.assertNull(tmpUser);
			
		logger.debug(LinShareTestConstants.END_TEST);
	}
	
	// TODO fix tests.
	@Ignore	
	@Test
	public void testDeleteDomain() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		User actor = userService.findOrCreateUser("root@localhost.localdomain", LinShareConstants.rootDomainIdentifier);
		
		try {
			logger.debug("Root trying to delete John Doe, Jane Smith and Foo Bar (who are in top domain, sub domain and guest domain)");
			
			userAndDomainMultiService.deleteDomainAndUsers(actor, LoadingServiceTestDatas.topDomainName);
			
		}catch (BusinessException e) {
			logger.error("userAndDomainMultiService can not delete a users in top and sub domain");
			logger.error(e.toString());
		}
		
		Assert.assertNull(abstractDomainService.retrieveDomain(LoadingServiceTestDatas.topDomainName));
		Assert.assertNull(abstractDomainService.retrieveDomain(LoadingServiceTestDatas.subDomainName1));
		Assert.assertNull(abstractDomainService.retrieveDomain(LoadingServiceTestDatas.guestDomainName1));
		
		logger.debug(LinShareTestConstants.END_TEST);
	}

}
