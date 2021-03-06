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

package org.linagora.linshare.webservice.admin.impl;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.ThreadFacade;
import org.linagora.linshare.webservice.admin.ThreadRestService;
import org.linagora.linshare.webservice.dto.ThreadDto;
import org.linagora.linshare.webservice.dto.ThreadMemberDto;

public class ThreadRestServiceImpl implements ThreadRestService {

	private ThreadFacade threadFacade;

	public ThreadRestServiceImpl(final ThreadFacade threadFacade) {
		super();
		this.threadFacade = threadFacade;
	}

	@Path("/")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public List<ThreadDto> getAll() throws BusinessException {
		threadFacade.checkAuthentication();
		return threadFacade.getAll();
	}

	@Path("/{uuid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public ThreadDto get(@PathParam("uuid") String uuid)
			throws BusinessException {
		threadFacade.checkAuthentication();
		return threadFacade.get(uuid);
	}

	@Path("/{uuid}/members")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public List<ThreadMemberDto> getMembers(@PathParam("uuid") String uuid)
			throws BusinessException {
		threadFacade.checkAuthentication();
		return threadFacade.getMembers(uuid);
	}

	@Path("/{uuid}/members")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void addMember(@PathParam("uuid") String uuid, ThreadMemberDto member)
			throws BusinessException {
		threadFacade.checkAuthentication();
		threadFacade.addMember(uuid, member);
	}

	@Path("/")
	@DELETE
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void delete(ThreadDto thread) throws BusinessException {
		threadFacade.checkAuthentication();
		threadFacade.delete(thread.getUuid());
	}
}
