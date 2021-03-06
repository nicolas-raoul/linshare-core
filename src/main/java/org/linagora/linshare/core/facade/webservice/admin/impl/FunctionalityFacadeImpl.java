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

package org.linagora.linshare.core.facade.webservice.admin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.linagora.linshare.core.domain.constants.Policies;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.Role;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.FunctionalityFacade;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.FunctionalityOldService;
import org.linagora.linshare.core.service.FunctionalityService;
import org.linagora.linshare.webservice.dto.FunctionalityDto;

public class FunctionalityFacadeImpl extends AdminGenericFacadeImpl implements FunctionalityFacade {

	private AbstractDomainService abstractDomainService;
	private FunctionalityService functionalityService;
	private FunctionalityOldService functionalityOldService;

	public FunctionalityFacadeImpl(final AccountService accountService,
			final AbstractDomainService abstractDomainService, final FunctionalityService functionalityService,
			FunctionalityOldService functionalityOldService) {
		super(accountService);
		this.abstractDomainService = abstractDomainService;
		this.functionalityService = functionalityService;
		this.functionalityOldService = functionalityOldService;
	}

	@Override
	public FunctionalityDto get(String domain, String identifier) throws BusinessException {
		Functionality f = functionalityService.getFunctionality(domain, identifier);
		boolean parentAllowAPUpdate = functionalityService.activationPolicyIsMutable(f, domain);
		boolean parentAllowCPUpdate = functionalityService.configurationPolicyIsMutable(f, domain);
		FunctionalityDto func = new FunctionalityDto(f, parentAllowAPUpdate, parentAllowCPUpdate);
		func.setDomain(domain);
		return func;
	}

	@Override
	public List<FunctionalityDto> getAll(String domain) throws BusinessException {
		Set<Functionality> entities = functionalityService.getAllFunctionalities(domain);

		List<FunctionalityDto> ret = new ArrayList<FunctionalityDto>();
		for (Functionality f : entities) {
			boolean parentAllowAPUpdate = functionalityService.activationPolicyIsMutable(f, domain);
			boolean parentAllowCPUpdate = functionalityService.configurationPolicyIsMutable(f, domain);
			FunctionalityDto func = new FunctionalityDto(f, parentAllowAPUpdate, parentAllowCPUpdate);
			// We force the domain id to be coherent to the argument.
			func.setDomain(domain);
			ret.add(func);
		}
		return ret;
	}

	@Override
	public void update(String domain, FunctionalityDto func) throws BusinessException {
		checkAuthentication(Role.ADMIN);
		Functionality f = functionalityService.getFunctionality(domain, func.getIdentifier());
		
		// copy of activation policy.
		String ap = func.getActivationPolicy().getPolicy().trim().toUpperCase();
		f.getActivationPolicy().setPolicy(Policies.valueOf(ap));
		f.getActivationPolicy().setStatus(func.getActivationPolicy().getStatus());

		// copy of configuration policy.
		String cp = func.getConfigurationPolicy().getPolicy().trim().toUpperCase();
		f.getConfigurationPolicy().setPolicy(Policies.valueOf(cp));
		f.getConfigurationPolicy().setStatus(func.getConfigurationPolicy().getStatus());

		// copy of parameters.
		f.updateFunctionalityValuesOnlyFromDto(func);
		functionalityOldService.update(domain, f);
	}

	@Override
	public void delete(String domain, FunctionalityDto func) throws BusinessException {
		User actor = checkAuthentication(Role.ADMIN);
		functionalityService.deleteFunctionality(actor, domain, func.getIdentifier());
	}
}