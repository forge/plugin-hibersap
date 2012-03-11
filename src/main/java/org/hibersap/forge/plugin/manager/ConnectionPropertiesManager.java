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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.hibersap.forge.plugin.util.FilterCollection;
import org.hibersap.forge.plugin.util.Utils;

/**
 * Manager class for Hibersap SAP connection properties
 * 
 * Provides functionality to read/create, update and write the SAP connection properties
 *  
 * @author Max Schwaab
 *
 */
public class ConnectionPropertiesManager {
	
	/** The path to the default properties **/
	private final static String DEFAULT_PROPERTIES_PATH = "/META-INF/";
	/** The filename for the connection properties **/
	private final static String SAP_PROPERTIES_FILENAME = "sap-connection.properties";	
	
	/** The SAP connection properties **/
	private final Properties sapConnection = new Properties();//TODO sorted properties
	
	/** The path to store the individual SAP connection properties **/
	private final String sapPropertiesStorePath;
	
	/**
	 * Constructor - Instantiates a new SAPConnectionPropertiesManager
	 * 
	 * The default SAP connection properties will be loaded at creation
	 * 
	 * @param storePath - path to store the SAP connection properties without filename
	 * @throws IOException
	 */
	public ConnectionPropertiesManager(final String sapPropertiesStorePath) throws IOException {
		Utils.checkPath(sapPropertiesStorePath);
		this.sapPropertiesStorePath = sapPropertiesStorePath;
		
		//Read or create SAP connection properties
		final String filePath = sapPropertiesStorePath + SAP_PROPERTIES_FILENAME;
		final File file = new File(filePath);
		
		if(file.exists()) {
			readSAPProperties();
		} else {
			readDefaultSAPProperties();
			writeSAPProperties();
		}
	}
	
	/**
	 * Loads the default SAP connection properties
	 * 
	 * @throws IOException 
	 */
	private void readDefaultSAPProperties() throws IOException {
		readSAPProperties(DEFAULT_PROPERTIES_PATH, true);
	}
	
	/**
	 * Reads the SAP connection properties from the given sapPropertiesStorePath (see {@link ConnectionPropertiesManager#setSAPPropertiesStorePath(String)})
	 * 
	 * @throws IOException
	 */
	private void readSAPProperties() throws IOException {
		readSAPProperties(sapPropertiesStorePath, false);
	}
	
	/**
	 * Reads the SAP connection properties from the given path
	 * 
	 * @param path - the path to the properties file without filename
	 * @param readDefault - indicator whether to read default properties or not
	 * @throws IOException 
	 */
	private void readSAPProperties(final String path, final boolean readDefault) throws IOException {
		final String filePath = path + SAP_PROPERTIES_FILENAME;
		final InputStream inputStream = readDefault ? getClass().getResourceAsStream(filePath) : new FileInputStream(filePath);
		
		this.sapConnection.clear();
		this.sapConnection.load(inputStream);
		
		inputStream.close();
	}

	/**
	 * Stores the SAP connection properties to the given sapPropertiesStorePath (see {@link ConnectionPropertiesManager#setSAPPropertiesStorePath(String)})
	 * 
	 * @throws IOException 
	 */
	public void writeSAPProperties() throws IOException {
		final File file = new File(sapPropertiesStorePath + SAP_PROPERTIES_FILENAME);
		final File fileDir = file.getParentFile();		
		final FileOutputStream outputStream;
		
		if(!fileDir.exists()) {
			fileDir.mkdir();
		}		
		
		outputStream = new FileOutputStream(file);
		sapConnection.store(outputStream, "forge hibersap plugin \nSAP connection properties");
		outputStream.close();
	}
	
	/**
	 * Gets all SAP connection properties
	 * 
	 * @return the SAP connection properties
	 */
	public Set<Entry<Object, Object>> getAllSAPProperties() {
		return sapConnection.entrySet();
	}
	
	/**
	 * Gets the JCo SAP connection properties
	 * 
	 * @return the JCo SAP connection properties
	 */
	public Set<Entry<Object, Object>> getSAPJcoProperties() {
		//New Set necessary, because the sapConnection properties shall not be affected
		final Set<Entry<Object, Object>> jco = new HashSet<Entry<Object,Object>>(sapConnection.entrySet());
		final FilterCollection filter = new FilterCollection(jco, "jco", "context");
		
		filter.filter();
		
		return jco;
	}
	
	/**
	 * Gets the property value for the given property key
	 * 
	 * @param key - the key
	 * @return the value belonging to the given key
	 */
	public String getSAPProperty(final String key) {
		final String property = sapConnection.getProperty(key);
		
		return property;
	}
	
	/**
	 * Sets a SAP connection property
	 * 
	 * @param key - the property key
	 * @param value - the property value
	 */
	public void setSAPProperty(final String key, final String value) {
		sapConnection.setProperty(key, value);
	}
	
	/**
	 * Deletes a SAP connection property
	 * 
	 * @param key - the property key
	 */
	public void deleteSAPProperty(final String key) {
		sapConnection.remove(key);
	}
	
}
