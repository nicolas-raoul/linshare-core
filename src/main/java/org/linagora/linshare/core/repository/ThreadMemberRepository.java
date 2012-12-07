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
package org.linagora.linshare.core.repository;

import java.util.List;

import org.linagora.linshare.core.domain.entities.Thread;
import org.linagora.linshare.core.domain.entities.ThreadMember;
import org.linagora.linshare.core.domain.entities.User;

public interface ThreadMemberRepository extends AbstractRepository<ThreadMember> {

	/**
	 * Find ThreadMember by id
	 * 
	 * @param id
	 * @return the ThreadMember
	 */
	public ThreadMember findById(String id);

	/**
	 * Find ThreadMember corresponding to a User
	 * 
	 * @param thread
	 * @param user
	 * @return the ThreadMember
	 */
    public ThreadMember findUserThreadMember(Thread thread, User user);

	/**
	 * Find all ThreadMember corresponding to a User
	 * 
	 * @param user
	 * @return the ThreadMember
	 */
	public List<ThreadMember> findAllUserMemberships(User user);
	
	/**
	 * Find all ThreadMember corresponding to a User where he's admin
	 * 
	 * @param user
	 * @return the ThreadMember
	 */
	public List<ThreadMember> findAllUserAdminMemberships(User user);
}