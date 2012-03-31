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

package org.hibersap.forge.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Filters a given Collection of Entry elements 
 * 
 * The key names of the entries are used for comparison
 * 
 * @author Max Schwaab
 *
 */
public class FilterCollection {
	
	/** The collection **/
	private final Collection<Entry<Object, Object>> collection;
	/** The starts-with-pattern expression **/
	private final String startsWithPattern;
	/** The exclude pattern expression **/
	private final String excludePattern;
	
	/**
	 * Constructor - Instantiates a new FilterCollection 
	 * 
	 * @param collection - the collection
	 * @param startsWithPattern - the expression the entry key should start with
	 * @param excludePattern - the expression the entry key should not contain
	 */
	public FilterCollection(final Collection<Entry<Object, Object>> collection, final String startsWithPattern, final String excludePattern) {
		this.collection = collection;
		this.startsWithPattern = startsWithPattern;
		this.excludePattern = excludePattern;
	}
	
	/**
	 * Filters the given collection
	 * 
	 * @return the filtered collection
	 */
	public Collection<Entry<Object, Object>> filter() {
		final Iterator<Entry<Object, Object>> iterator = collection.iterator();
		final startsWithAndExcludePredicate connectionTypePredicate = new startsWithAndExcludePredicate();
		
		while(iterator.hasNext()) {
			if(!connectionTypePredicate.isSatisfied(iterator.next())) {
				iterator.remove();
			}
		}
		
		return collection;
	}
	
	/**
	 * A Predicate
	 * 
	 * Satisfied if entry key starts with {@link FilterCollection#startsWithPattern} and do not contains {@link FilterCollection#excludePattern}
	 * 
	 * @author Max Schwaab
	 *
	 */
	private class startsWithAndExcludePredicate {

		/**
		 * Tests if predicate is satisfied
		 * 
		 * @param entry - the Entry object to be tested
		 * @return true or false, depending on test result
		 */
		public boolean isSatisfied(Entry<Object, Object> entry) {
			final String key = entry.getKey().toString();
			
			if(key.startsWith(startsWithPattern) && !key.contains(excludePattern)) {
				return true;
			}
			return false;
		}
		
	}

}
