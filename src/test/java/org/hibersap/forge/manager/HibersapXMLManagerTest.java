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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibersap.configuration.xml.HibersapConfig;
import org.hibersap.configuration.xml.HibersapJaxbXmlParser;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.forge.manager.HibersapXMLManager;
import org.hibersap.forge.sap.SAPFunctionModuleSearch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 
 * @author Max Schwaab
 *
 */
public class HibersapXMLManagerTest {

	private String hibersapXMLStorePath;
	private HibersapXMLManager manager;
	
	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();
	
	@Before
	public void init() throws Exception {
		hibersapXMLStorePath = folder.getRoot().getAbsolutePath() + System.getProperty("file.separator");
		manager = new HibersapXMLManager(hibersapXMLStorePath);
	}

	@Test
	public void addSessionManager() throws Exception {
		final String sessionManagerName = "SM099";
		final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig(sessionManagerName);
		
		manager.addSessionManager(sessionManagerConfig);
		
		assertEquals(sessionManagerConfig, manager.getHibersapConfig().getSessionManager(sessionManagerName));
	}
	
	@Test
	public void sessionManagerNameExists() throws Exception {
		final String sessionManagerName = "SM999";
		final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig(sessionManagerName);
		
		assertFalse(manager.sessionManagerNameExists(sessionManagerName));
		
		manager.addSessionManager(sessionManagerConfig);
		
		assertTrue(manager.sessionManagerNameExists(sessionManagerName));
	}

	@Test
	public void addAndOverrideSessionManager() throws Exception {
		final String sessionManagerName = "SM066";
		final SessionManagerConfig sessionManagerConfig01 = new SessionManagerConfig(sessionManagerName);
		
		sessionManagerConfig01.addAnnotatedClass(SAPFunctionModuleSearch.class);
		manager.addSessionManager(sessionManagerConfig01);
		
		final SessionManagerConfig sessionManagerConfig02 = new SessionManagerConfig(sessionManagerName);
		
		sessionManagerConfig02.addAnnotatedClass(String.class);
		manager.addAndOverrideSessionManager(sessionManagerConfig02);

		final List<String> annotatedClasses = manager.getHibersapConfig().getSessionManager(sessionManagerName).getAnnotatedClasses();

		assertTrue(annotatedClasses.contains(String.class.getName()));
		assertFalse(annotatedClasses.contains(SAPFunctionModuleSearch.class.getName()));
	}

	@Test
	public void getSessionManagerNames() throws Exception {
		final SessionManagerConfig sessionManagerConfig01 = new SessionManagerConfig("SM011");
		final SessionManagerConfig sessionManagerConfig02 = new SessionManagerConfig("SM022");
		final SessionManagerConfig sessionManagerConfig03 = new SessionManagerConfig("SM033");
		
		manager.addSessionManager(sessionManagerConfig01);
		manager.addSessionManager(sessionManagerConfig02);
		manager.addSessionManager(sessionManagerConfig03);
		
		final List<String> sessionManagerNames = manager.getSessionManagerNames();
		final List<String> toCompare = new ArrayList<String>();
		
		toCompare.add(sessionManagerConfig01.getName());
		toCompare.add(sessionManagerConfig02.getName());
		toCompare.add(sessionManagerConfig03.getName());
		
		assertTrue(sessionManagerNames.size() == 3);
		assertTrue(sessionManagerNames.containsAll(toCompare));
	}

	@Test
	public void updateSessionManager() throws Exception {
		final String sessionManagerName01 = "SM055";
		final SessionManagerConfig sessionManagerConfig01 = new SessionManagerConfig(sessionManagerName01);
		final SessionManagerConfig sessionManagerConfig02 = new SessionManagerConfig("SM077");
		
		manager.addSessionManager(sessionManagerConfig01);

		sessionManagerConfig02.addAnnotatedClass(SAPFunctionModuleSearch.class);
		manager.updateSessionManager(sessionManagerName01, sessionManagerConfig02);

		final List<String> annotatedClasses = manager.getHibersapConfig().getSessionManager(sessionManagerName01).getAnnotatedClasses();
		
		assertTrue(annotatedClasses.size() == 1);
		assertTrue(annotatedClasses.contains(SAPFunctionModuleSearch.class.getName()));
	}
	
	@Test
	public void readHibersapXML() throws Exception {
		hibersapXMLStorePath = getClass().getResource("/META-INF/").getPath();
		manager = new HibersapXMLManager(hibersapXMLStorePath);
		
		assertTrue(manager.sessionManagerNameExists("SM001"));
	}

	@Test
	public void writeHibersapXML() throws Exception {
		final String sessionManagerName = "SM033";
		final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig(sessionManagerName);
		final HibersapJaxbXmlParser parser = new HibersapJaxbXmlParser();
		
		manager.addSessionManager(sessionManagerConfig);
		manager.writeHibersapXML();

		final String fileName = "hibersap.xml";
		final FileInputStream inputStream = new FileInputStream(hibersapXMLStorePath + fileName);
		final HibersapConfig hibersapConfig = parser.parseResource(inputStream, fileName);
		
		inputStream.close();
		
		assertEquals(sessionManagerConfig.getName(), hibersapConfig.getSessionManagers().get(0).getName());
	}

}
