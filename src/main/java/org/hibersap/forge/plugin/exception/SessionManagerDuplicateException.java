/*
 * Copyright (C) 2012 akquinet AG
 *
 * This file is part of the Forge Hibersap Plugin.
 *
 * The Forge Hibersap Plugin is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Forge Hibersap Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the Forge Hibersap Plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibersap.forge.plugin.exception;

/**
 * Indicates that a session manager name is already in use in the Hibersap context
 * 
 * @author Max Schwaab
 *
 */
public class SessionManagerDuplicateException extends Exception {

	private static final long serialVersionUID = 4484599590902696907L;
	
	public SessionManagerDuplicateException(final String sessionManagerName) {
		super("Session manager name \"" + sessionManagerName + "\" is aready in use");
	}

}
