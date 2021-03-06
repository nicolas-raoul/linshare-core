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

import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.UserFacade;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.admin.UserRestService;
import org.linagora.linshare.webservice.dto.UserDto;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/rest/admin/users", description = "User administration service.")
public class UserRestServiceImpl extends WebserviceBase implements
		UserRestService {

	private final UserFacade userFacade;

	public UserRestServiceImpl(final UserFacade userFacade) {
		this.userFacade = userFacade;
	}

	@Path("/search/{pattern}")
	@ApiOperation(value = "Provide user autocompletion.", response = UserDto.class, responseContainer = "Set")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public Set<UserDto> completionUser(
			@ApiParam(value = "Pattern to complete.", required = true) @PathParam("pattern") String pattern)
			throws BusinessException {
		userFacade.checkAuthentication();
		return userFacade.completionUser(pattern);
	}

	@Path("/search/internals/{pattern}")
	@ApiOperation(value = "Search among internal users.", response = UserDto.class, responseContainer = "Set")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public Set<UserDto> getInternals(
			@ApiParam(value = "Internal users to search for.", required = true) @PathParam("pattern") String pattern)
			throws BusinessException {
		userFacade.checkAuthentication();
		return userFacade.getInternals(pattern);
	}

	@Path("/search/guests/{pattern}")
	@ApiOperation(value = "Search among guests.", response = UserDto.class, responseContainer = "Set")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public Set<UserDto> getGuests(
			@ApiParam(value = "Guests to search for.", required = true) @PathParam("pattern") String pattern)
			throws BusinessException {
		userFacade.checkAuthentication();
		return userFacade.getGuests(pattern);
	}

	@Path("/")
	@ApiOperation(value = "Update an user.")
	@PUT
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void updateUser(
			@ApiParam(value = "User to update", required = true) UserDto userDto)
			throws BusinessException {
		userFacade.checkAuthentication();
		userFacade.updateUser(userDto);
	}

	@Path("/")
	@ApiOperation(value = "Delete an user.")
	@DELETE
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void deleteUser(
			@ApiParam(value = "User to delete.", required = true) UserDto userDto)
			throws BusinessException {
		userFacade.checkAuthentication();
		userFacade.deleteUser(userDto);
	}

	@Path("/inconstitent")
	@ApiOperation(value = "Find all inconsistent users.")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void getAllInconsistent() throws BusinessException {
		userFacade.checkAuthentication();
		userFacade.getAllInconsistent();
	}

	@Path("/inconstitent")
	@ApiOperation(value = "Update an inconsistent user's domain.")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public void updateInconsistentUser(
			@ApiParam(value = "Inconsistent user to update.", required = true) UserDto userDto)
			throws BusinessException {
		userFacade.checkAuthentication();
		userFacade.updateInconsistentUser(userDto);
	}
}