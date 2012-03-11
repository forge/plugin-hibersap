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

package org.hibersap.forge.plugin;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hibersap.configuration.AnnotationConfiguration;
import org.hibersap.configuration.xml.Property;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.forge.plugin.exception.SessionManagerDuplicateException;
import org.hibersap.forge.plugin.manager.HibersapXMLManager;
import org.hibersap.forge.plugin.manager.ConnectionPropertiesManager;
import org.hibersap.forge.plugin.sap.SAPFunctionModuleSearch;
import org.hibersap.forge.plugin.sap.SAPEntity;
import org.hibersap.forge.plugin.sap.SAPEntityBuilder;
import org.hibersap.forge.plugin.util.Utils;
import org.hibersap.generation.bapi.ReverseBapiMapper;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.session.Session;
import org.hibersap.session.SessionManager;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;

/**
 * Searches for SAP functions, holds SAP connection properties and generates Hibersap Java classes for a selected SAP function. 
 * 
 * @author Max Schwaab
 *
 */
@RequiresProject
@Alias("generate-sap-entities")
@Help("Generate entities from a SAP system.")
public class GenerateSAPEntities implements Plugin {
	
	/** The Hibersap repository URL **/
	private final static String HIBERSAP_REPO_URL = "http://hibersap.svn.sourceforge.net/viewvc/hibersap/m2repo";
	
	/** The Forge shell **/
	private final Shell shell;
	/** The Forge project **/
	private final Project project;
	/** The SAP connection properties **/
	private final ConnectionPropertiesManager sapConnectionPropertiesManager;

	/**
	 * Constructor - Instantiates the plugin 
	 * 
	 * @param project - the Forge project
	 * @param shell - the Forge shell
	 * @throws IOException 
	 */
	@Inject
	public GenerateSAPEntities(final Project project, final Shell shell) throws IOException {
		final String pluginDirPath  = shell.getEnvironment().getPluginDirectory().getFullyQualifiedName();
		final String configDirPath = pluginDirPath + "/org/hibersap/forge/plugin/plugin-hibersap/config/";
		
		this.shell = shell;
		this.project = project;
		this.sapConnectionPropertiesManager = new ConnectionPropertiesManager(configDirPath);
	}
	
	/**
	 * Lists the SAP connection properties
	 * 
	 * @param out
	 */
	@Command(value="list-properties", help="Lists all available connection properties")
	public void listProperties() {
		final Set<Entry<Object, Object>> properties = sapConnectionPropertiesManager.getAllSAPProperties();//TODO sort entries
		
		for(final Entry<Object, Object> property : properties) {
			shell.println(property.getKey() + "=" + property.getValue());
		}		
	}
	
	/**
	 * Sets a SAP connection property
	 * 
	 * @param key - the property key
	 * @param value - the property value
	 * @throws IOException 
	 */
	@Command(value="set-property", help="Sets a connection property")
	public void setProperty(@Option(name="key", help="the property key") final String key, @Option(name="value", help="the property value") final String value) throws IOException {
		sapConnectionPropertiesManager.setSAPProperty(key, value);
		sapConnectionPropertiesManager.writeSAPProperties();
	}
	
	/**
	 * Deletes a SAP connection property
	 * 
	 * @param key - the property key
	 * @throws IOException
	 */
	@Command(value="delete-property", help="Deletes a connection property")
	public void deleteProperty(@Option(name="key", help="the property key") final String key) throws IOException {
		sapConnectionPropertiesManager.deleteSAPProperty(key);
		sapConnectionPropertiesManager.writeSAPProperties();
	}
	
	/**
	 * Generates all necessary classes to access a chosen SAP function.
	 * Searches for SAP functions with the given name pattern and shows results according to given max. result number (0 shows all results).
	 * 
	 * @param namePattern - the name pattern to search for SAP functions
	 * @param maxResults - the number of max. results showing in the search result list (type 0 for all results)
	 * @throws JAXBException 
	 * @throws ParserConfigurationException 
	 * @throws FileNotFoundException 
	 * @throws TransformerException 
	 * @throws SessionManagerDuplicateException 
	 * @throws ClassNotFoundException 
	 */
	@DefaultCommand(help="Generates the necessary Java classes for a given SAP function")
	public void generateSAPEntities(
			@Option(name="name-pattern", help="Pattern to search SAP function names. Use * and ? as wildcards.") final String namePattern, 
			@Option(name="max-results", help="Number of max. results. Use 0 for unlimited result list. Default value is 20", defaultValue="20") final int maxResults) throws JAXBException, ParserConfigurationException, FileNotFoundException, TransformerException, SessionManagerDuplicateException, ClassNotFoundException {
		final SessionManagerConfig sessionManagerConfig = createSessionManagerConfig(); 
		final AnnotationConfiguration configuration = new AnnotationConfiguration(sessionManagerConfig);
		final SessionManager sessionManager = configuration.buildSessionManager();
		final SAPFunctionModuleSearch functionModuleSearch = new SAPFunctionModuleSearch(namePattern, maxResults);
		final Session session =  sessionManager.openSession();
		
		try{
			session.execute(functionModuleSearch);
		} finally {
			session.close();
		}
		
		final List<String> functionNames = functionModuleSearch.getFunctionNames();
		functionNames.add("Cancel");
		final String functionName = shell.promptChoiceTyped("\nSelect a function to generate the necessary Java classes:", functionNames);
		
		if(!functionName.equals("Cancel")) {
			final ReverseBapiMapper reverseBAPIMapper = new ReverseBapiMapper();
			final BapiMapping functionMapping = reverseBAPIMapper.map(functionName, sessionManager);
			
			shell.println();
			
			final String defaultClassName = Utils.toCamelCase(functionMapping.getBapiName(), '_');
			final String className = shell.prompt("Please enter a class name. Leave empty for default\n", defaultClassName);
			
			shell.println();
			
			final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
			final String defaultJavaPackage = java.getBasePackage() + ".hibersap";
			final String javaPackage = shell.prompt("Please enter a Java package. Leave empty for default\n", defaultJavaPackage);
			
			final SAPEntityBuilder sapEntityBuilder = new SAPEntityBuilder();
			sapEntityBuilder.createNew(className, javaPackage, functionMapping);
			
			final SAPEntity sapEntity = sapEntityBuilder.getSAPEntity();
			final Set<JavaClass> javaClasses = sapEntity.getStructureClasses();
			
			javaClasses.add(sapEntity.getBapiClass());
			shell.println();
			
			for(final JavaClass javaClass : javaClasses) {
				java.saveJavaSource(javaClass);
				shell.println("Created SAP entity [" + javaClass.getQualifiedName() + "]");
			}	
			
			final String bapiClassName = sapEntity.getBapiClass().getName();
			sessionManagerConfig.setAnnotatedClasses(Collections.singleton(bapiClassName));
			
			handleConfiguration(sessionManagerConfig);
		} else {
			shell.println();
			shell.println("Command canceled...");
		}
		
	}

	/**
	 * 
	 * 
	 * @param sessionManagerConfig
	 * @param bapiClassName
	 * @throws JAXBException
	 * @throws SessionManagerDuplicateException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException 
	 */
	private void handleConfiguration(final SessionManagerConfig sessionManagerConfig) throws JAXBException, SessionManagerDuplicateException, ClassNotFoundException, FileNotFoundException {
		final DirectoryResource metaInfDir = project.getProjectRoot().getChildDirectory("src/main/resources/META-INF");		
		final String metaInfDirPath = metaInfDir.getFullyQualifiedName() + "/";
		final HibersapXMLManager xmlManager = new HibersapXMLManager(metaInfDirPath);
		final String sessionManagerName = sessionManagerConfig.getName();
		final String messageBody = "\nSession manager [" + sessionManagerName + "] ";
		final List<String> sessionManagerNames = xmlManager.getSessionManagerNames();
		final String newSessionManager = "New session manager from current properties";
		final String sessionManagerNameChoice;
		
		if(!sessionManagerNames.isEmpty()) {
			sessionManagerNames.add(newSessionManager);
			
			shell.println();
			
			sessionManagerNameChoice = shell.promptChoiceTyped("Please choose a session manager", sessionManagerNames, newSessionManager);
		} else {
			sessionManagerNameChoice = newSessionManager;
		}
		
		final boolean update;
		if(sessionManagerNameChoice.equals(newSessionManager)) {
			shell.println();
			
			final String adapter = shell.promptRegex("Would you like to use JCo or JCA adapter for the current session manager?\nLeave empty for default", "[jJ][cC][aAoO]", "JCo");//Boolean("\nSession manager " + sessionManagerName + " already exists.\nReplace session manager? [" + sessionManagerName + "]", false);
			
			if(adapter.matches("[jJ][cC][aA]")) {
				//Set session manager for JCA environment
				sessionManagerConfig.setContext(sapConnectionPropertiesManager.getSAPProperty("jca.context"));
				sessionManagerConfig.setJcaConnectionFactory(sapConnectionPropertiesManager.getSAPProperty("jca.connection.factory"));
				sessionManagerConfig.setJcaConnectionSpecFactory(sapConnectionPropertiesManager.getSAPProperty("jca.connectionspec.factory"));
				sessionManagerConfig.setProperties(Collections.<Property> emptySet());//Set properties empty; Nullpointer if set null
				//Handle dependencies for JCA environment
				handleDependencies(false);
			} else {
				//Set session manager for JCo environment
				sessionManagerConfig.setJcaConnectionFactory(null);
				sessionManagerConfig.setJcaConnectionSpecFactory(null);
				//Handle dependencies for JCo environment
				handleDependencies(true);
			}
			//TODO Test!
			if(!xmlManager.sessionManagerNameExists(sessionManagerName)) {
				xmlManager.addSessionManager(sessionManagerConfig);
				update = false;
//				shell.println();
				shell.println(messageBody + "added...");
			} else {
				shell.println();
				final boolean replace = shell.promptBoolean("\nSession manager " + sessionManagerName + " already exists.\nReplace session manager? [" + sessionManagerName + "]", false);
				
				
				if(replace) {
					xmlManager.addAndOverrideSessionManager(sessionManagerConfig);
					update = false;
					shell.println();
					shell.println(messageBody + "replaced...");
				} else {
					update = true;
				}
			}
			
		} else {
			update = true;
		}
		
		if(update) {
			xmlManager.updateSessionManager(sessionManagerNameChoice, sessionManagerConfig);
			shell.println();
			shell.println(messageBody + "updated...");
		}
		xmlManager.writeHibersapXML();
//		shell.println();
		shell.println("\nWrote configuration file [hibersap.xml]\n");
	}
	
	/**
	 * Creates the necessary session manager configuration for the function module search
	 * 
	 * @return the session manager configuration
	 */
	private SessionManagerConfig createSessionManagerConfig() {
		final SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
		
		sessionManagerConfig.setName(sapConnectionPropertiesManager.getSAPProperty("session-manager.name"));
		// Setting JCo context is not necessary, because it's set by default when creating a new SessionManangerConfig object
		sessionManagerConfig.addAnnotatedClass(SAPFunctionModuleSearch.class);
		
		final Set<Entry<Object, Object>> jcoConnectionProperties = sapConnectionPropertiesManager.getSAPJcoProperties();
		
		for(final Entry<Object, Object> entry : jcoConnectionProperties) {
			sessionManagerConfig.setProperty(entry.getKey().toString(), entry.getValue().toString());
		}
		
		return sessionManagerConfig;
	}

	/**
	 * Checks for the necessary Hibersap dependencies and add them to project pom.xml if necessary
	 */
	private void handleDependencies(final boolean jco) {
		final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		
		shell.println();
		shell.println("Checking and updating dependencies...");
		
		//Check not necessary, because its performed in the addRepository method too, that why repo will be removed if present
		dependencyFacet.addRepository("repository.hibersap", HIBERSAP_REPO_URL);
		
		//Add hibersap-core dependency
		final Dependency hibersapCore = DependencyBuilder.create()
				.setGroupId("org.hibersap")
				.setArtifactId("hibersap-core");
		addDependency( dependencyFacet, hibersapCore );
				
		if(jco) {
			//Add hibersap-jco dependency
			final Dependency hibersapJCo = DependencyBuilder.create()
					.setGroupId("org.hibersap")
					.setArtifactId("hibersap-jco");
			addDependency(dependencyFacet, hibersapJCo);
			
			//Add SAP JCo dependency
			final Dependency sapJCo = DependencyBuilder.create()
					.setGroupId("com.sap")
					.setArtifactId("sap-jco");
			addDependency(dependencyFacet, sapJCo);
		} else {
			//Add hibersap-jca dependency
			final Dependency hibersapJCA = DependencyBuilder.create()
					.setGroupId("org.hibersap")
					.setArtifactId("hibersap-jca");
			addDependency(dependencyFacet, hibersapJCA);
		}
		
		//Add javax validation api for bean validation
		final Dependency beanValidation = DependencyBuilder.create()
				.setGroupId("javax.validation")
				.setArtifactId("validation-api");
		addDependency(dependencyFacet, beanValidation);
	}

	/**
	 * Adds a dependency to project pom.xml
	 * 
	 * @param dependencyFacet - the project dependency facet
	 * @param dependency - the dependency
	 */
	private void addDependency(final DependencyFacet dependencyFacet, final Dependency dependency) {
		if(!dependencyFacet.hasDirectDependency(dependency)) {
			if(dependency.getArtifactId().equals("validation-api")) {
				shell.println();
				
				//Check if user wants to use bean validation
				final boolean useBeanValidation = shell.promptBoolean("Do you want to  use bean validation in your project?", false);
					
				if(!useBeanValidation) {
					return;
				}
			}
			
			shell.println();
			
			final List<Dependency> versions = dependencyFacet.resolveAvailableVersions(dependency);
			final Dependency newDependency = shell.promptChoiceTyped("Which version do you want to install?", versions);
			
			dependencyFacet.addDirectDependency(newDependency);
			
			//Just to have nicer shell view
			if(versions.size() > 1) {
				shell.println();
			}
			shell.println("Dependency added [" + newDependency.getArtifactId() + "]");
		}
	}
	
}