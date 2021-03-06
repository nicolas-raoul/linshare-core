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

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.linagora.linshare.core.domain.constants.LinShareTestConstants;
import org.linagora.linshare.core.domain.constants.Policies;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.Policy;
import org.linagora.linshare.core.domain.entities.StringValueFunctionality;
import org.linagora.linshare.core.domain.entities.UnitValueFunctionality;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.exception.TechnicalErrorCode;
import org.linagora.linshare.core.exception.TechnicalException;
import org.linagora.linshare.core.repository.AbstractDomainRepository;
import org.linagora.linshare.core.repository.DomainPolicyRepository;
import org.linagora.linshare.core.repository.FunctionalityRepository;
import org.linagora.linshare.core.service.FunctionalityOldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

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
public class FunctionalityServiceImplTest extends AbstractJUnit4SpringContextTests{
	
	
	private static Logger logger = LoggerFactory.getLogger(FunctionalityServiceImplTest.class);
	
	private LoadingServiceTestDatas datas;
	
	private int TOTAL_COUNT_FUNC=LoadingServiceTestDatas.TOTAL_COUNT_FUNC;
	
	@Autowired
	private FunctionalityOldService functionalityService;
	
	@Autowired
	private FunctionalityRepository functionalityRepository;
	
	@Autowired
	private AbstractDomainRepository abstractDomainRepository;
	
	@Autowired
	private DomainPolicyRepository domainPolicyRepository;
	
	@Before
	public void setUp() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_SETUP);
		datas = new LoadingServiceTestDatas(functionalityRepository,abstractDomainRepository,domainPolicyRepository,null, null);
		datas.loadDatas();
		logger.debug(LinShareTestConstants.END_SETUP);
	}

	@After
	public void tearDown() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_TEARDOWN);
		datas.deleteDatas();
		logger.debug(LinShareTestConstants.END_TEARDOWN);
	}
	
	

	@Test
	public void testGetAllFunctionalityForRootDomain() {
		logger.info(LinShareTestConstants.BEGIN_TEST);

		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.rootDomainName);
		Assert.assertNotNull(domain);
		
		Assert.assertEquals(TOTAL_COUNT_FUNC,domain.getFunctionalities().size());
		List<Functionality> list = functionalityService.getAllFunctionalities(domain);
		
		Assert.assertNotNull(list);
		Assert.assertEquals(TOTAL_COUNT_FUNC, list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetAllFunctionalityForTopDomain() {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.topDomainName);
		Assert.assertNotNull(domain);
		
		Assert.assertEquals(2,domain.getFunctionalities().size());
		List<Functionality> list = functionalityService.getAllFunctionalities(domain);
		
		Assert.assertNotNull(list);
		Assert.assertEquals(TOTAL_COUNT_FUNC, list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetAllFunctionalityForSubDomain() {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertNotNull(domain);
		
		Assert.assertEquals(1,domain.getFunctionalities().size());
		
		List<Functionality> list = functionalityService.getAllFunctionalities(domain);
		
		Assert.assertNotNull(list);
		Assert.assertEquals(TOTAL_COUNT_FUNC, list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetFunctionalityByIdentifiers() {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertNotNull(domain);
		
		// The first functionality should be override by the current subdomain
		Functionality fonc1 = functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.FILESIZE_MAX);
		Assert.assertNotNull(fonc1);
		Assert.assertEquals(domain.getIdentifier(), fonc1.getDomain().getIdentifier());
		
		// The second functionality should be override by the top domain
		Functionality fonc2 = functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.QUOTA_USER);
		Assert.assertNotNull(fonc2);
		Assert.assertNotSame(domain.getIdentifier(), fonc2.getDomain().getIdentifier());
		Assert.assertEquals(domain.getParentDomain().getIdentifier() , fonc2.getDomain().getIdentifier());
		
		// The third functionality should be override by the root domain
		Functionality fonc3 = functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		Assert.assertNotNull(fonc3);
		Assert.assertNotSame(domain.getIdentifier(), fonc3.getDomain().getIdentifier());
		Assert.assertNotSame(domain.getParentDomain().getIdentifier() , fonc3.getDomain().getIdentifier());
		Assert.assertEquals(domain.getParentDomain().getParentDomain().getIdentifier() , fonc3.getDomain().getIdentifier());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetAllAvailableFunctionalities() {
		
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.topDomainName2);
		Assert.assertNotNull(domain);
		
		List<Functionality> list  = functionalityService.getAllAvailableFunctionalities(domain);
		Assert.assertNotNull(list);
		logger.debug("Nb func : " + list.size());
		for (Functionality functionality : list) {
			logger.debug("func : " + functionality.toString());
		}
		
		Assert.assertEquals(datas.getAvailableFunctionalitiesForTopDomain2(), list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetAllAlterableFunctionalities() {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.topDomainName2);
		Assert.assertNotNull(domain);
		
		List<Functionality> list  = functionalityService.getAllAlterableFunctionalities(domain);
		Assert.assertNotNull(list);
		logger.debug("Nb func : " + list.size());
		Assert.assertEquals(datas.getAlterableFunctionalitiesForTopDomain2(), list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testGetAllEditableFunctionalities() {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.topDomainName2);
		Assert.assertNotNull(domain);
		
		List<Functionality> list  = functionalityService.getAllEditableFunctionalities(domain);
		Assert.assertNotNull(list);
		logger.debug("Nb func : " + list.size());
		Assert.assertEquals(datas.getEditableFunctionalitiesForTopDomain2(), list.size());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	
	
	@Test
	public void testUpdateUnitValueFunctionality1() throws BusinessException {
		// This test is designed to check a simple functionality modification.
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertNotNull(domain);
		
		UnitValueFunctionality func = (UnitValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.FILESIZE_MAX);
		
		Assert.assertEquals(50,func.getValue().intValue());
		func.setValue(25);
		functionalityService.update(domain, func);
		
		domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertEquals(1, domain.getFunctionalities().size());
		for (Functionality it : domain.getFunctionalities()) {
			Assert.assertEquals(25, ((UnitValueFunctionality)it).getValue().intValue());
		}
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	
	@Test
	public void testUpdateUnitValueFunctionality2() throws BusinessException {
		// This test is designed to check a simple update without modification.
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertNotNull(domain);
		
		UnitValueFunctionality func = (UnitValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.FILESIZE_MAX);
		
		Assert.assertEquals(50,func.getValue().intValue());
		functionalityService.update(domain, func);
		
		domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertEquals(1, domain.getFunctionalities().size());
		for (Functionality it : domain.getFunctionalities()) {
			Assert.assertEquals(50, ((UnitValueFunctionality)it).getValue().intValue());
		}
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testUpdateUnitValueFunctionality2bis() throws BusinessException {
		// This test is designed to check a simple update without modification.
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertNotNull(domain);
		
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		Assert.assertTrue(func.getValue().equals(LoadingServiceTestDatas.timeStampingUrl));
		functionalityService.update(domain, func);
		
		domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName1);
		Assert.assertEquals(1, domain.getFunctionalities().size());
		logger.debug(LinShareTestConstants.END_TEST);

	
	}
	
	
	
	@Test
	public void testUpdateUnitValueFunctionality3() throws BusinessException {
		// This test is designed to check when a functionality is set up with the same parameters as its ancestor, it should be deleted.
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		Assert.assertEquals(1, domain.getFunctionalities().size());
		
		
		List<Functionality> list  = functionalityService.getAllEditableFunctionalities(domain);
		Assert.assertNotNull(list);
		Assert.assertEquals(datas.getEditableFunctionalitiesForTopDomain2(), list.size());
		
		
		UnitValueFunctionality func = (UnitValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.QUOTA_USER);
		
		Assert.assertFalse(func.getDomain().getIdentifier().equals(domain.getParentDomain().getIdentifier()));
		Assert.assertEquals(125,func.getValue().intValue());
		func.setValue(250);
		functionalityService.update(domain, func);
		
		list  = functionalityService.getAllEditableFunctionalities(domain);
		Assert.assertNotNull(list);
		Assert.assertEquals(datas.getEditableFunctionalitiesForTopDomain2(), list.size());
		
		domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		
		Assert.assertEquals(0, domain.getFunctionalities().size());
		
		func = (UnitValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.QUOTA_USER);
		Assert.assertEquals(250,func.getValue().intValue());
		
		Assert.assertTrue(func.getDomain().getIdentifier().equals(domain.getParentDomain().getIdentifier()));
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testUpdateUnitValueFunctionality4() throws BusinessException {
		// This test is designed to check when we modify a functionality from its ancestor, it should create a new entity.
		logger.info(LinShareTestConstants.BEGIN_TEST);
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		
		
		List<Functionality> list  = functionalityService.getAllEditableFunctionalities(domain);
		Assert.assertNotNull(list);
		Assert.assertEquals(datas.getEditableFunctionalitiesForTopDomain2(), list.size());
		
		
		
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		Assert.assertFalse(func.getDomain().getIdentifier().equals(domain.getIdentifier()));
		Assert.assertTrue(func.getDomain().getIdentifier().equals(domain.getParentDomain().getParentDomain().getIdentifier()));
		Assert.assertTrue(func.getValue().equals(LoadingServiceTestDatas.timeStampingUrl));
		func.setValue("plop");
		functionalityService.update(domain, func);
		
		
		domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		
		Assert.assertEquals(2, domain.getFunctionalities().size());
		
		StringValueFunctionality func2 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		Assert.assertTrue(func2.getDomain().getIdentifier().equals(domain.getIdentifier()));
		Assert.assertTrue(func.getId() !=func2.getId());
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	
	
	@Test
	public void testUpdateActivationPolicyFunctionality1() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality
		func.setValue("plop");
		functionalityService.update(domain, func);
		
		// updating this new functionality without reloading it: should failed
		func.setValue("plop2");
		func.getActivationPolicy().setPolicy(Policies.FORBIDDEN);
		
		try {
			
			functionalityService.update(domain, func);
			Assert.fail("This message should not be seen. An BusinessException was expected.");
		} catch (TechnicalException e) {
			if(!e.getErrorCode().equals(TechnicalErrorCode.FUNCTIONALITY_ENTITY_MODIFICATION_NOT_ALLOW)) {
				Assert.fail("unkown error : " + e.getMessage());
				throw(e);
			}
		}
		
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testUpdateActivationPolicyFunctionality2() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.subDomainName2);
		Assert.assertNotNull(domain);
		
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality
		func.setValue("plop");
		functionalityService.update(domain, func);
		
		func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func.setValue("plop2");
		func.getActivationPolicy().setPolicy(Policies.FORBIDDEN);
		
		try {
			functionalityService.update(domain, func);
			Assert.fail("This message should not be seen. An technicalException was expected.");
		} catch (TechnicalException e) {
//			e.printStackTrace();
		}
		
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testUpdateRootDomainFunctionality() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		
		AbstractDomain domain = abstractDomainRepository.findById(LoadingServiceTestDatas.rootDomainName);
		Assert.assertNotNull(domain);
		
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(domain.getIdentifier(), LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		func.setValue("plop");
		func.getActivationPolicy().setPolicy(Policies.FORBIDDEN);
		functionalityService.update(domain, func);
		
		logger.debug(LinShareTestConstants.END_TEST);

	}

	
	private int countFunctionality(String domainId, String funcIdentifier) {
		// reload each time 
		AbstractDomain domain = abstractDomainRepository.findById(domainId);
		Assert.assertNotNull(domain);
		return countFunctionality(domain, funcIdentifier);
	}
	
	
	private int countFunctionality(AbstractDomain domain , String funcIdentifier) {
		int count = 0;
		
		Set<Functionality> functionalities = domain.getFunctionalities();
		for (Functionality f : functionalities) {
			if(f.getIdentifier().equals(funcIdentifier)) {
				logger.debug("countFunctionality:" + domain.getIdentifier());
				count++;
			}
			
		}
		
		Set<AbstractDomain> subdomain = domain.getSubdomain();
		for (AbstractDomain abstractDomain : subdomain) {
			count += countFunctionality(abstractDomain, funcIdentifier);
		}
		return count;
	}
	
	
	@Test
	public void testUpdateActivationPolicyFunctionality3() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		String rootDomainId = LoadingServiceTestDatas.rootDomainName;
		
		Assert.assertEquals(1, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 1
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		func.getActivationPolicy().setStatus(false);
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality for top domain
		functionalityService.update(LoadingServiceTestDatas.topDomainName, func);
		Assert.assertEquals(2, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 2
		StringValueFunctionality func2 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func2.getActivationPolicy().setPolicy(Policies.FORBIDDEN);
		// modification of a functionality which belong to the top domain. This will lead to the creation of a new functionality for sub domain
		functionalityService.update(LoadingServiceTestDatas.subDomainName1, func2);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 3
		StringValueFunctionality func3 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func3.getActivationPolicy().setPolicy(Policies.FORBIDDEN);
		// root domain functionality is set with a forbidden policy, this should lead to the suppression of all functionalities above rootDomain
		functionalityService.update(rootDomainId, func3);
		Assert.assertEquals(1, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
	
		
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	@Test
	public void testUpdateConfigurationPolicyFunctionality1() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		String rootDomainId = LoadingServiceTestDatas.rootDomainName;
		
		Assert.assertEquals(1, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 1
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		func.getConfigurationPolicy().setStatus(false);
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality for top domain
		functionalityService.update(LoadingServiceTestDatas.topDomainName, func);
		Assert.assertEquals(2, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 2
		StringValueFunctionality func2 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func2.getConfigurationPolicy().setPolicy(Policies.FORBIDDEN);
		// modification of a functionality which belong to the top domain. This will lead to the creation of a new functionality for sub domain
		functionalityService.update(LoadingServiceTestDatas.subDomainName1, func2);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 3
		StringValueFunctionality func3 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func3.getConfigurationPolicy().setPolicy(Policies.MANDATORY);
		// root domain functionality is set with a mandatory policy, this should lead to the modification of all functionalities above rootDomain
		functionalityService.update(rootDomainId, func3);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
	
		StringValueFunctionality func4 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		logger.debug("func4.getConfigurationPolicy().getPolicy() : " + func4.getConfigurationPolicy().getPolicy());
		Assert.assertTrue(func4.getConfigurationPolicy().getPolicy().equals(Policies.MANDATORY));
		
		logger.debug(LinShareTestConstants.END_TEST);

	}
	
	
	@Test
	public void testUpdateConfigurationPolicyFunctionality2() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		String rootDomainId = LoadingServiceTestDatas.rootDomainName;
		String valueFromTop="value from top";
		String valueFromSub="value from sub";
		
		Assert.assertEquals(1, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 1
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		func.getConfigurationPolicy().setStatus(false);
		func.setValue(valueFromTop);
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality for top domain
		functionalityService.update(LoadingServiceTestDatas.topDomainName, func);
		Assert.assertEquals(2, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 2
		StringValueFunctionality func2 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func2.getConfigurationPolicy().setPolicy(Policies.MANDATORY);
		func2.setValue(valueFromSub);
		// modification of a functionality which belong to the top domain. This will lead to the creation of a new functionality for sub domain
		functionalityService.update(LoadingServiceTestDatas.subDomainName1, func2);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 3
		StringValueFunctionality func3 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		Assert.assertEquals(LoadingServiceTestDatas.timeStampingUrl, func3.getValue());
		func3.getConfigurationPolicy().setPolicy(Policies.FORBIDDEN);

		// root domain functionality is set with a forbidden policy, this should lead to the modification of all functionalities above rootDomain
		functionalityService.update(rootDomainId, func3);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
	
		
		// Step 4
		StringValueFunctionality func4 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		logger.debug("func4.getConfigurationPolicy().getPolicy() : " + func4.getConfigurationPolicy().getPolicy());
		Assert.assertTrue(func4.getConfigurationPolicy().getPolicy().equals(Policies.FORBIDDEN));
		logger.debug("func4.getValue()" + func4.getValue());
		Assert.assertEquals(LoadingServiceTestDatas.timeStampingUrl, func4.getValue());
		
		logger.debug(LinShareTestConstants.END_TEST);
	}
	
	@Test
	public void testUpdateConfigurationPolicyFunctionality3() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		String rootDomainId = LoadingServiceTestDatas.rootDomainName;
		
		Assert.assertEquals(1, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 1
		StringValueFunctionality func = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		
		func.getConfigurationPolicy().setStatus(false);
		// modification of a functionality which belong to the root domain. This will lead to the creation of a new functionality for top domain
		functionalityService.update(LoadingServiceTestDatas.topDomainName, func);
		Assert.assertEquals(2, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 2
		StringValueFunctionality func2 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func2.getConfigurationPolicy().setPolicy(Policies.FORBIDDEN);
		// modification of a functionality which belong to the top domain. This will lead to the creation of a new functionality for sub domain
		functionalityService.update(LoadingServiceTestDatas.subDomainName1, func2);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
		
		// Step 3
		StringValueFunctionality func3 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		func3.getConfigurationPolicy().setPolicy(Policies.MANDATORY);
		// root domain functionality is set with a forbidden policy, this should lead to the suppression of all functionalities above rootDomain
		functionalityService.update(rootDomainId, func3);
		Assert.assertEquals(3, countFunctionality(rootDomainId, LoadingServiceTestDatas.TEST_TIME_STAMPING));
		
	
		StringValueFunctionality func4 = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.subDomainName1, LoadingServiceTestDatas.TEST_TIME_STAMPING);
		Assert.assertTrue(func4.getConfigurationPolicy().getPolicy().equals(Policies.MANDATORY));
		
		logger.debug(LinShareTestConstants.END_TEST);
	}
	
	@Test
	public void testCheckAndUpdate() throws BusinessException {
		logger.info(LinShareTestConstants.BEGIN_TEST);

		Policy activationPolicy = new Policy();
		Policy configurationPolicy = new Policy();
		
		// Try to modify functionalities with a specific configuration. Fail in Linshare 1.0.0-rc1
		try {
			// Set the functionality entity
			StringValueFunctionality funcAncestor = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.rootDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
			
			activationPolicy = new Policy();
			activationPolicy.setStatus(true);
			activationPolicy.setSystem(false);
			activationPolicy.setPolicy(Policies.ALLOWED);
			funcAncestor.setActivationPolicy(activationPolicy);
			
			configurationPolicy = new Policy();
			configurationPolicy.setStatus(false);
			configurationPolicy.setSystem(true);
			configurationPolicy.setPolicy(Policies.FORBIDDEN);
			funcAncestor.setConfigurationPolicy(configurationPolicy);
			
			functionalityService.update(LoadingServiceTestDatas.rootDomainName,funcAncestor);
			
			// Set the functionality entity
			StringValueFunctionality funcEntity = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
			
			activationPolicy = new Policy();
			activationPolicy.setStatus(false);
			activationPolicy.setSystem(false);
			activationPolicy.setPolicy(Policies.FORBIDDEN);
			funcEntity.setActivationPolicy(activationPolicy);
			
			configurationPolicy = new Policy();
			configurationPolicy.setStatus(false);
			configurationPolicy.setSystem(true);
			configurationPolicy.setPolicy(Policies.FORBIDDEN);
			funcEntity.setConfigurationPolicy(configurationPolicy);
			
			functionalityService.update(LoadingServiceTestDatas.topDomainName,funcEntity);
			
			// Set the functionality DTO
			StringValueFunctionality funcDto = (StringValueFunctionality)functionalityService.getFunctionalityByIdentifiers(LoadingServiceTestDatas.topDomainName, LoadingServiceTestDatas.TEST_TIME_STAMPING);
			
			activationPolicy = new Policy();
			activationPolicy.setStatus(true);
			activationPolicy.setSystem(false);
			activationPolicy.setPolicy(Policies.MANDATORY);
			funcDto.setActivationPolicy(activationPolicy);
			
			configurationPolicy = new Policy();
			configurationPolicy.setStatus(false);
			configurationPolicy.setSystem(true);
			configurationPolicy.setPolicy(Policies.FORBIDDEN);
			funcDto.setConfigurationPolicy(configurationPolicy);
			
			functionalityService.update(LoadingServiceTestDatas.topDomainName, funcDto);
		} catch (TechnicalException e){
			Assert.fail();
		}
		
		logger.info(LinShareTestConstants.END_TEST);
	}
}
