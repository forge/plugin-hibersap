<h2>Hibersap Plugin for Forge</h2>


A plugin to generate classes to get used by the Hibersap framework to connect to a SAP system.

The plugin allows to search for function names at a given SAP system and generates all required classes and configuration files to use the given function with the Hibersap framework.

You can find more information about Hibersap at http://hibersap.sourceforge.net/

This plugin is licensed under LGPL, like Forge and Hibersap


<h2>How to use</h2>


This plugin is using Hibersap, which is using the SAP Java Connector (JCo).<br>
Before using download SAP Java Connector 3 (http://service.sap.com/connectors) and extract the sapjco3.jar and the sapjco3 native library.<br>
(We assume you use version 3.0.7, if not you should use the correct version number, please have a look at the project pom.xml too.)

Install sapjco3 jar to your local Maven repository from the command line: 

	mvn install:install -file -DgroupId=com.sap -DartifactId=sap-jco -Dversion=3.0.7 -Dpackaging=jar -Dfile= sapjco3.jar 


* Create or use an existing a Maven project (project may be created with Forge from the command line).
* Navigate into the project
* Set your session manager name and connection properties
* Use the generate-sap-entities command to connect to your SAP system and generate Java classes which are used by Hibersap. 


<h2>Commands</h2>


[list-properties] - Lists all connection properties and the current session manager name.

[set-property] - Sets a new or existing property<br>
[OPTIONS]<br>
[--key] - The property key<br>
[--value] - The property value

[delete-property] - Deletes an existing property<br>
[OPTIONS]<br>
[--key] - The property key<br>

[generate-sap-entities] - Defalt command; generates SAP entities from a SAP system<br>
[OPTIONS]<br>
[--name-pattern] - Pattern to search SAP function names. Use * and ? as wildcards.<br>
[--max-results] - Number of max. results. Use 0 for unlimited result list (Default value is 20)