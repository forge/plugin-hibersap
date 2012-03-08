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

package org.hibersap.forge.plugin.sap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.Table;

/**
 * Searches for functions in a SAP system.
 * 
 * @author Max Schwaab
 *
 */
@Bapi(value = "RFC_READ_TABLE")
public class SAPFunctionModuleSearch {

	@SuppressWarnings("unused")
	@Import
	@Parameter(value = "QUERY_TABLE")
	private final String tableName = "TFDIR";
	
	@SuppressWarnings("unused")
	@Import
	@Parameter(value = "ROWCOUNT")
	private final int rowCount;
		
	@SuppressWarnings("unused")
	@Table
	@Parameter(value = "OPTIONS")
	private final List<Option> functionNamePattern;
	
	@SuppressWarnings("unused")
	@Table
	@Parameter(value = "FIELDS")
	private final List<Field> fields = Collections.singletonList(new Field());
	
	@Table
	@Parameter(value = "DATA")
	private List<FunctionModule> functionModules;

	/**
	 * Constructor - creates an instance of FunctionModuleSearch
	 * 
	 * @param functionNamePattern - The function name pattern. May contain wildcards (* or ?).
	 * @param maxResults - Sets the maximum number of results. Set 0 for no limitation.
	 */
	public SAPFunctionModuleSearch(final String functionNamePattern, final int maxResults) {
		final String sapPattern = functionNamePattern.replaceAll("\\*", "%").replaceAll("\\?", "_");
				
		this.functionNamePattern = Collections.singletonList(new Option(sapPattern));
		this.rowCount = maxResults;
	}

	/**
	 * Returns a list of function names
	 * 
	 * @return - The name list
	 */
	public List<String> getFunctionNames() {
		final List<String> functionName = new ArrayList<String>();
		for(FunctionModule element : this.functionModules) {
			functionName.add(element.name);
		}
		
		return functionName;
	}
	
	/*
	 * A class mapping the option data type of SAP
	 */
	@BapiStructure
	private static class Option {
		
		@SuppressWarnings("unused")
		@Parameter(value = "TEXT")
		private final String optionsQueryString;
		
		@SuppressWarnings("unused")
		private Option() {
			this.optionsQueryString = "";
		}
		
		public Option(final String functionNamePattern) {
			this.optionsQueryString = String.format("FMODE EQ 'R' AND FUNCNAME LIKE '%s'", functionNamePattern);
		}
		
	}


	/*
	 * A class mapping the field data type of SAP
	 */
	@BapiStructure
	private static class Field {
		
		@SuppressWarnings("unused")
		@Parameter(value = "FIELDNAME")
		private final String name = "FUNCNAME";
		
		private Field() {}
		
	}
	
	/*
	 * A class mapping the function module data type of SAP
	 */
	@BapiStructure
	private static class FunctionModule {
		
		@Parameter(value = "WA")
		private final String name;
		
		private FunctionModule() {
			this.name = "";
		}

	}
	
}
