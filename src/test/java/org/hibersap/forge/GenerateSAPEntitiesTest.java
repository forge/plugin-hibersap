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

package org.hibersap.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Max Schwaab
 *
 */
public class GenerateSAPEntitiesTest extends AbstractShellTest {

	private String pluginDirPath;
	private String filePath;
	private boolean fileExists = false;

	@org.jboss.arquillian.container.test.api.Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true, GenerateSAPEntitiesPlugin.class.getPackage());
	}

	@Before
	public void init() throws Exception {
		this.pluginDirPath = getShell().getEnvironment().getPluginDirectory().getFullyQualifiedName();
		this.filePath = this.pluginDirPath + "/org/hibersap/forge/hibersap-plugin/config/sap-connection.properties";
		final File file = new File(this.filePath);
		final Properties properties = new Properties();
		this.fileExists = file.exists();

		if (!this.fileExists) {
			file.getParentFile().mkdirs();

			final InputStream inputStream = getClass().getResourceAsStream("/META-INF/sap-connection.properties");
			properties.load(inputStream);

			final OutputStream outputStream = new FileOutputStream(this.filePath);
			properties.store(outputStream, "Forge hibersap-plugin\nSAP connection properties");
			outputStream.close();
		}
	}

	@After
	public void cleanUp() {
		if (!this.fileExists) {
			final File file = new File(this.filePath);
			deleteSAPProperties(file);
		}
	}

	@Test
	public void listProperties() throws Exception {
		getShell().execute("generate-sap-entities list-properties");
	}

	@Test
	public void setProperty() throws Exception {
		final String propertyOldValue;
		final String propertyKey = "jco.client.ashost";
		final String propertyNewValue = "some.sap.system.de";
		final Properties properties = new Properties();
		final InputStream inputStream01 = new FileInputStream(this.filePath);

		properties.load(inputStream01);
		inputStream01.close();
		propertyOldValue = properties.get(propertyKey).toString();
		getShell().execute("generate-sap-entities set-property --key " + propertyKey + " --value " + propertyNewValue);

		final InputStream inputStream02 = new FileInputStream(this.filePath);
		properties.clear();
		properties.load(inputStream02);
		inputStream02.close();

		Assert.assertEquals(propertyNewValue, properties.get(propertyKey));

		if (this.fileExists) {
			final OutputStream outputStream = new FileOutputStream(this.filePath);
			properties.setProperty(propertyKey, propertyOldValue);
			properties.store(outputStream, "Forge hibersap-plugin\nSAP connection properties");
			outputStream.close();
		}
	}

	@Test
	public void deleteProperty() throws Exception {
		final String propertyOldValue;
		final String propertyKey = "jco.client.sysnr";
		final Properties properties = new Properties();
		final InputStream inputStream01 = new FileInputStream(this.filePath);

		properties.load(inputStream01);
		inputStream01.close();
		propertyOldValue = properties.get(propertyKey).toString();
		getShell().execute("generate-sap-entities delete-property --key " + propertyKey);

		final InputStream inputStream02 = new FileInputStream(this.filePath);
		properties.clear();
		properties.load(inputStream02);
		inputStream02.close();

		Assert.assertEquals(null, properties.get(propertyKey));

		if (this.fileExists) {
			final OutputStream outputStream = new FileOutputStream(this.filePath);
			properties.setProperty(propertyKey, propertyOldValue);
			properties.store(outputStream, "Forge hibersap-plugin\nSAP connection properties");
			outputStream.close();
		}
	}

	private void deleteSAPProperties(final File file) {
		if (!file.getAbsolutePath().endsWith("plugins")) {
			file.delete();
			deleteSAPProperties(file.getParentFile());
		}
	}

}
