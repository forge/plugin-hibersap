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

package org.hibersap.forge.plugin.util;

/**
 * Utilities for the hibersap-plugin project
 * 
 * @author Max Schwaab
 *
 */
public class Utils {
	
	/**
	 * Checks for correct path formatting if path ends with [\] or [/])
	 * 
	 * @param path - the path to check
	 * @throws IllegalArgumentException
	 */
	public static void checkPath(final String path) throws IllegalArgumentException {
		if(!path.endsWith("\\") && !path.endsWith("/")) {
			throw new IllegalArgumentException("Expected path ends with [\\] or [/], but was " + path.charAt(path.length() - 1));
		}		
	}

	/**
	 * Converts a given String to camelCase AbcDef
	 * 
	 * You can choose a character as "spacer" character, e.g. "_"
	 * Exapmles: ABC_DEF, Abc_Def and abc_def
	 * 
	 * @param input - the input String
	 * @param spacer - the character used as spacer (e.g. "_")
	 * @return the camelCased input String
	 */
	public static String toCamelCase(final String input, final char spacer) {
		if(input == null) {
			return null;
		}
		if(input.isEmpty()) {
			return "";
		}
		
		final StringBuilder stringBuilder = new StringBuilder();
		
		for(final String subString : input.split(Character.toString(spacer))) {
		    stringBuilder.append(subString.substring(0, 1).toUpperCase());
		    stringBuilder.append(subString.substring(1).toLowerCase());
		}
		
		return stringBuilder.toString();
	}
	
}
