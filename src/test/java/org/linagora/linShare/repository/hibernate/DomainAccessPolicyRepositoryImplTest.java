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
package org.linagora.linShare.repository.hibernate;

import junit.framework.Assert;

import org.junit.Test;
import org.linagora.linShare.core.domain.entities.AbstractDomain;
import org.linagora.linShare.core.domain.entities.AllowAllDomain;
import org.linagora.linShare.core.domain.entities.AllowDomain;
import org.linagora.linShare.core.domain.entities.DomainAccessPolicy;
import org.linagora.linShare.core.domain.entities.DomainAccessRule;
import org.linagora.linShare.core.domain.entities.RootDomain;
import org.linagora.linShare.core.exception.BusinessException;
import org.linagora.linShare.core.repository.AbstractDomainRepository;
import org.linagora.linShare.core.repository.DomainAccessPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"classpath:springContext-test.xml", 
		"classpath:springContext-datasource.xml",
		"classpath:springContext-repository.xml"})
public class DomainAccessPolicyRepositoryImplTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private DomainAccessPolicyRepository domainAccessPolicyRepository;

	@Test
	public void testCreateDomainAccessRule1() throws BusinessException{
		DomainAccessPolicy policy = new DomainAccessPolicy();
		logger.debug("Current policy : " + policy.toString());
		
		domainAccessPolicyRepository.create(policy);
		Assert.assertNotNull(policy.getPersistenceId());
		
		DomainAccessPolicy entityPolicy = domainAccessPolicyRepository.findById(policy.getPersistenceId());
		
		Assert.assertTrue(entityPolicy != null );
		domainAccessPolicyRepository.delete(entityPolicy);
	}
	
	@Test
	public void testCreateDomainAccessRule2() throws BusinessException{
		
		DomainAccessPolicy policy = new DomainAccessPolicy();
		logger.debug("Current policy : " + policy.toString());
		
		domainAccessPolicyRepository.create(policy);
		
		DomainAccessPolicy entityPolicy = domainAccessPolicyRepository.findById(policy.getPersistenceId());
		
		Assert.assertTrue(entityPolicy != null );
		
		domainAccessPolicyRepository.delete(entityPolicy);
	}
}