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

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Max Schwaab
 *
 */
public class FilterCollectionTest {

	private Set<Entry<Object, Object>> set;
	private Set<Entry<Object, Object>> expected;

	@Before
	public void init() {
		final Properties properties = new Properties();
		properties.setProperty("real.key01", "some value");
		properties.setProperty("real.key02", "some value");
		properties.setProperty("real.key03", "some value");

		this.expected = properties.entrySet();

		properties.setProperty("test.key01", "some value");
		properties.setProperty("test.key02", "some value");
		properties.setProperty("test.key03", "some value");
		properties.setProperty("test.yek01", "some value");
		properties.setProperty("test.yek02", "some value");
		properties.setProperty("test.yek03", "some value");
		properties.setProperty("real.yek01", "some value");
		properties.setProperty("real.yek02", "some value");
		properties.setProperty("real.yek03", "some value");

		this.set = properties.entrySet();
	}

	@Test
	public void filter() {
		final FilterCollection filterCollection = new FilterCollection(this.set, "real", "yek");

		filterCollection.filter();

		Assert.assertEquals(this.expected, this.set);
	}

}
