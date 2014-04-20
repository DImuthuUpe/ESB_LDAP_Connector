Product: Integration tests for WSO2 ESB LDAP connector

Pre-requisites:

 - Maven 3.x
 - Java 1.6 or above
h
Tested Platform: 

 - Mac OSX 10.9.2
 - WSO2 ESB 4.8.1
 
 STEPS:

1. Make sure the ESB 4.8.1 zip file with latest patches available at Integration_Test/products/esb/4.8.1/modules/integration/connectors/repository folder.

2. You should have a working LDAP server. If not you can easily start a new LDAP server using Apache DS

3. Change Integration_Test/products/esb/4.8.1/modules/integration/connectors/src/test/resources/artifacts/ESB/connector/config/ldapConnector.properties properties
	
	providerUrl : <URL of the LDAP server>
	securityPrincipal : <Admin user name of LDAP server>
	securityCredentials : <Admin user password>
	ldapUserBase : <user base of LDAP server>
	testUserId : <An UID of a user who is not available in given user base>
	
4. Copy proxy files to location "Integration_Test/products/esb/4.8.1/modules/integration/connectors/src/test/resources/artifacts/ESB/config/proxies/ldap/"

5. Copy request files to location "Integration_Test/products/esb/4.8.1/modules/integration/connectors/src/test/resources/artifacts/ESB/config/restRequests/ldap/" 

6. Navigate to "Integration_Test/products/esb/4.8.1/modules/integration/connectors/" and run the following command.
     $ mvn clean install
