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

package org.hibersap.forge.plugin.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.hibersap.configuration.xml.HibersapConfig;
import org.hibersap.configuration.xml.HibersapJaxbXmlParser;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.forge.plugin.exception.SessionManagerDuplicateException;
import org.hibersap.forge.plugin.util.Utils;

/**
 * Manager class for hibersap.xml configuration file
 * 
 * Provides functionality to read/create, update and write the hibersap.xml file 
 * 
 * @author Max Schwaab
 *
 */
public class HibersapXMLManager {

	/** The hibersap.xml filename **/
	private final static String  HIBERSAPXML_FILENAME = "hibersap.xml";
	/** The path to store the hibersap.xml file **/
	private final String hibersapXMLStorePath;
	/** The Hibersap configuration **/
	private final HibersapConfig hibersapConfig;
	
	/**
	 * Constructor - Instantiates a new HibersapXMLManager 
	 * 
	 * @param hibersapXMLStorePath - path to store the hibersap.xml without filename
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public HibersapXMLManager(final String hibersapXMLStorePath) throws JAXBException, FileNotFoundException {
		Utils.checkPath(hibersapXMLStorePath);
		this.hibersapXMLStorePath = hibersapXMLStorePath;
		
		final String filePath = hibersapXMLStorePath + HIBERSAPXML_FILENAME;
		final File file = new File(filePath);
		
		if(file.exists()) {
			this.hibersapConfig = readHibersapXML(file);
		} else {
			this.hibersapConfig = new HibersapConfig();
		}
	}
	
	/**
	 * Adds a session manager to the current Hibersap configuration
	 * 
	 * @param sessionManagerConfig - the session manager configuration
	 * @throws SessionManagerDuplicateException
	 */
	public void addSessionManager(final SessionManagerConfig sessionManagerConfig) throws SessionManagerDuplicateException {
		if(sessionManagerNameExists(sessionManagerConfig.getName())) {
			throw new SessionManagerDuplicateException(sessionManagerConfig.getName());
		}
		
//		prepareSessionManager(sessionManagerConfig);
		sessionManagerConfig.setValidationMode(null);
		
		//Workaround because of hibersap bug/reference problem
		final List<SessionManagerConfig> sessionManagers = hibersapConfig.getSessionManagers();
		sessionManagers.add(sessionManagerConfig);
	}
	
	/**
	 * Adds a session manager to the current Hibersap configuration and removes existing session manager with the same name
	 * 
	 * @param sessionManagerConfig - the session manager configuration
	 * @throws SessionManagerDuplicateException
	 */
	public void addAndOverrideSessionManager(final SessionManagerConfig sessionManagerConfig) throws SessionManagerDuplicateException {
		if(sessionManagerNameExists(sessionManagerConfig.getName())) {
			removeSessionManager(sessionManagerConfig.getName());
		}
		addSessionManager(sessionManagerConfig);
	}
	
//	/**
//	 * Removes unwanted elements from the given session manager configuration
//	 * 
//	 * @param sessionManagerConfig - the session manager configuration
//	 */
//	private void prepareSessionManager(final SessionManagerConfig sessionManagerConfig) {
////		//Set interceptor classes empty; Nullpointer if set null
////		sessionManagerConfig.setExecutionInterceptorClasses(Collections.<String> emptyList());
////		//Set BAPI interceptor classes empty; Nullpointer if set null
////		sessionManagerConfig.setBapiInterceptorClasses(Collections.<String> emptyList());
//		//Set calidation mode null
//		sessionManagerConfig.setValidationMode(null);
//	}
	
	/**
	 * Gets the session manager names of the current configuration
	 * 
	 * @return - the session manager names
	 */
	public List<String> getSessionManagerNames() {
		final List<SessionManagerConfig> sessionManagers = hibersapConfig.getSessionManagers();
		final List<String> sessionManagerNames = new ArrayList<String>();
		
		for(final SessionManagerConfig sessionManager : sessionManagers) {
			sessionManagerNames.add(sessionManager.getName());
		}
		
		return sessionManagerNames; 
	}
	
	/**
	 * Checks if given session manager name already exists in current configuration
	 * 
	 * @param name - the session manager name
	 * @return - the examination result
	 */
	public boolean sessionManagerNameExists(final String name) {
		final List<SessionManagerConfig> sessionManagers = hibersapConfig.getSessionManagers();
		
		for(final SessionManagerConfig sessionManager : sessionManagers) {
			if(sessionManager.getName().equals(name)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Removes the session manager with the given name from the current configuration
	 * 
	 * @param sessionManagerName - the session manager name
	 */
	private void removeSessionManager(final String sessionManagerName) {
		final List<SessionManagerConfig> sessionManagers = hibersapConfig.getSessionManagers();
		final Iterator<SessionManagerConfig> iterator = sessionManagers.iterator();
		
		while(iterator.hasNext()) {
			final SessionManagerConfig sessionManager = iterator.next();
			if(sessionManager.getName().equals(sessionManagerName)) {
				iterator.remove();
			}
		}
	}
	
	/**
	 * Reads the hibersap.xml from the given file
	 * 
	 * @param file - the hibersap.xml file
	 * @return - the Hibersap configration read from the given file
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	private HibersapConfig readHibersapXML(final File file) throws FileNotFoundException, JAXBException {
		final HibersapJaxbXmlParser parser = new HibersapJaxbXmlParser();
		final FileInputStream inputStream = new FileInputStream(file);
		final HibersapConfig hibersapConfig = parser.parseResource(inputStream, HIBERSAPXML_FILENAME);
		
		return hibersapConfig;
	}
	
	/**
	 * Updates annotated classes of the session manager with the given name with the annotated classes from the given session manager configuration 
	 * 
	 * @param sessionManagerName - the name of the session manager to update
	 * @param sessionManagerConfig - the session manager configuration to update from
	 * @throws ClassNotFoundException
	 */
	public void updateSessionManager(final String sessionManagerName, final SessionManagerConfig sessionManagerConfig) throws ClassNotFoundException {
		final SessionManagerConfig sessionManager = hibersapConfig.getSessionManager(sessionManagerName);
		final List<String> annotatedClasses = sessionManager.getAnnotatedClasses();
		final List<String> newAnnotatedClasses = sessionManagerConfig.getAnnotatedClasses();
		final Set<String> mergedAnnotatedClasses = new HashSet<String>(annotatedClasses);
		
		mergedAnnotatedClasses.addAll(newAnnotatedClasses);
		sessionManager.setAnnotatedClasses(new ArrayList<String>(mergedAnnotatedClasses));
	}
	
//	public boolean sessionManagerDuplicate(final SessionManagerConfig sessManagerConfig) {
//		final List<SessionManagerConfig> sessionManagers = hibersapConfig.getSessionManagers();
//		
//		for(final SessionManagerConfig sessionManager : sessionManagers) {
//			if(sessionManager.equals(sessManagerConfig)) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
	
	/**
	 * Writes hibersap.xml file to the stored path
	 * 
	 * @param path - the files path without filename
	 * @throws JAXBException
	 */
	public void writeHibersapXML() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(HibersapConfig.class);
		final Marshaller marshaller = context.createMarshaller();
		final File file = new File(hibersapXMLStorePath + HIBERSAPXML_FILENAME);
		
		marshaller.marshal(hibersapConfig, file);
	}

	/**
	 * Gets the Hibersap configuration
	 * 
	 * @return - the Hibersap configuration
	 */
	public HibersapConfig getHibersapConfig() {
		return hibersapConfig;
	}

}
