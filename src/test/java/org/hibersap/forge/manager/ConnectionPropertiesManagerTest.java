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

package org.hibersap.forge.manager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 
 * @author Max Schwaab
 *
 */
public class ConnectionPropertiesManagerTest {

	private ConnectionPropertiesManager manager;
	private String tempPath;

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void init() throws Exception {
		this.tempPath = this.folder.getRoot().getAbsolutePath() + "\\";

		this.manager = new ConnectionPropertiesManager(this.tempPath);
	}

	@Test
	public void readDefaultSAPProperties() {
		Assert.assertEquals("some.sap-system.com", this.manager.getSAPProperty("jco.client.ashost"));
	}

	@Test
	public void writeSAPProperties() throws Exception {
		final String propertyKey = "jco.client.ashost";
		final String propertyValue = "some.sap.de";

		setAndWriteProperty(propertyKey, propertyValue);

		final Properties properties = new Properties();
		final InputStream inputStream = new FileInputStream(this.tempPath + "sap-connection.properties");

		properties.load(inputStream);
		inputStream.close();

		Assert.assertEquals(properties.getProperty(propertyKey), propertyValue);
	}

	@Test
	public void readSAPProperties() throws Exception {
		final String propertyKey = "jco.client.user";
		final String propertyValue = "newuser";

		setAndWriteProperty(propertyKey, propertyValue);

		this.manager = new ConnectionPropertiesManager(this.tempPath);

		Assert.assertEquals(propertyValue, this.manager.getSAPProperty(propertyKey));
	}

	@Test
	public void getAllSAPProperties() {
		final Set<Entry<Object, Object>> allSAPProperties = this.manager.getAllSAPProperties();

		Assert.assertEquals(12, allSAPProperties.size());
	}

	private void setAndWriteProperty(final String propertyKey, final String propertyValue) throws Exception {
		this.manager.setSAPProperty(propertyKey, propertyValue);
		this.manager.writeSAPProperties();
	}

}
