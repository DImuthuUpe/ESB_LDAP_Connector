package org.wso2.carbon.connector.integration.test.ldap;

import java.util.Properties;

import org.apache.axis2.context.ConfigurationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.integration.test.common.ConnectorIntegrationUtil;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;
import org.json.JSONObject;

import java.net.URL;

import javax.activation.DataHandler;

public class LdapConnectorIntegrationTest extends ESBIntegrationTest {

    private static final String CONNECTOR_NAME = "ldapConnector";

    private MediationLibraryUploaderStub mediationLibUploadStub = null;

    private MediationLibraryAdminServiceStub adminServiceStub = null;

    private ProxyServiceAdminClient proxyAdmin;

    private String repoLocation = null;

    private String ldapConnectorFileName = CONNECTOR_NAME + ".zip";

    private Properties paypalConnectorProperties = null;

    private String pathToProxiesDirectory = null;

    private String pathToRequestsDirectory = null;

    private String userBase = null;

    private String testUserId = null;

    private String providerUrl =null;

    private String securityPrincipal =null;

    private String securityCredentials =null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        ConfigurationContextProvider configurationContextProvider = ConfigurationContextProvider.getInstance();
        ConfigurationContext cc = configurationContextProvider.getConfigurationContext();

        mediationLibUploadStub =
                new MediationLibraryUploaderStub(cc, esbServer.getBackEndUrl() + "MediationLibraryUploader");
        AuthenticateStub.authenticateStub("admin", "admin", mediationLibUploadStub);

        adminServiceStub =
                new MediationLibraryAdminServiceStub(cc, esbServer.getBackEndUrl() + "MediationLibraryAdminService");

        AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            repoLocation = System.getProperty("connector_repo").replace("\\", "/");
        } else {
            repoLocation = System.getProperty("connector_repo").replace("/", "/");
        }

        proxyAdmin = new ProxyServiceAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        ConnectorIntegrationUtil.uploadConnector(repoLocation, mediationLibUploadStub, ldapConnectorFileName);
        log.info("Sleeping for " + 30000 / 1000 + " seconds while waiting for synapse import");
        Thread.sleep(30000);

        adminServiceStub.updateStatus("{org.wso2.carbon.connector}" + CONNECTOR_NAME, CONNECTOR_NAME,
                "org.wso2.carbon.connector", "enabled");

        paypalConnectorProperties = ConnectorIntegrationUtil.getConnectorConfigProperties(CONNECTOR_NAME);
        pathToProxiesDirectory = repoLocation + paypalConnectorProperties.getProperty("proxyDirectoryRelativePath");
        pathToRequestsDirectory = repoLocation + paypalConnectorProperties.getProperty("requestDirectoryRelativePath");
        userBase = paypalConnectorProperties.getProperty("ldapUserBase");
        testUserId = paypalConnectorProperties.getProperty("testUserId");
        providerUrl = paypalConnectorProperties.getProperty("providerUrl");
        securityPrincipal = paypalConnectorProperties.getProperty("securityPrincipal");
        securityCredentials = paypalConnectorProperties.getProperty("securityCredentials");

    }

    //positive test case for creating LDAP entry with valid parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test with mandatory parameters.")
    public void testCreateEntryWithValidParameters() throws Exception {
        //
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_mandatory.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("result"));
            JSONObject result = jsonObject.getJSONObject("result");
            Assert.assertNotNull(result);
            Assert.assertEquals(result.getString("message"), "Success");

        } finally {
            proxyAdmin.deleteProxy(methodName);
            deleteSampleEntry();
        }

    }

    //negative test case for creating LDAP entry with missing objectclass
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test with with missing objectclass.")
    public void testCreateEntryWithMissingObjectClass() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_missing_objectclass.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    //negative test case for creating LDAP entry with missing dn
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test with missing dn.")
    public void testCreateEntryWithMissingDN() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_missing_dn.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    //negative test case for creating LDAP entry with wrong userbase
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test with wrong user base.")
    public void testCreateEntryWithWrongUserBase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_wrong_userbase.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    //negative test case for creating LDAP entry with wrong objectclass
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test with wrong objectclass.")
    public void testCreateEntryWithWrongObjectClass() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_wrong_objectclass.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    //negative test case for creating LDAP entry without mandatory attributes
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {createEntry} integration test without mandatory attributes.")
    public void testCreateEntryWithoutMandatoryAttributes() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_without_mandatory_attributes.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    //positive test case for deleting LDAP entry with valid parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {deleteEntry} integration test with mandatory parameters.")
    public void testDeleteEntryWithValidParameters() throws Exception {
        //
        createSampleEntity();

        //deleting created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteEntity_ldap.txt";
        String methodName = "ldap_deleteEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("result"));
            JSONObject result = jsonObject.getJSONObject("result");
            Assert.assertNotNull(result);
            Assert.assertEquals(result.getString("message"), "Success");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    //positive test case for deleting LDAP entry wrong DN
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {deleteEntry} integration test with wrong DN.")
    public void testDeleteEntryWrongDN() throws Exception {
        createSampleEntity();

        //deleting created entry with wrong dn
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteEntity_ldap_wrong_dn.txt";
        String methodName = "ldap_deleteEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }

    //positive test case for deleting LDAP entry with valid parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {searchEntry} integration test with valid parameters.")
    public void testSearchEntryWithValidParameters() throws Exception {
        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "searchEntry_ldap.txt";
        String methodName = "ldap_searchEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,userBase,testUserId);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            JSONObject result = jsonObject.getJSONObject("result");
            Assert.assertNotNull(result);
            JSONObject entry = result.getJSONObject("entry");
            Assert.assertNotNull(entry);
            Assert.assertEquals(entry.getString("uid"), "testDim20");
            //log.info("Search Response : " + jsonObject.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }

    //negative test case for searching LDAP entry with wrong parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {searchEntry} integration test with wrong parameters.")
    public void testSearchEntryWithWrongParameters() throws Exception {
        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "searchEntry_ldap_wrong_params.txt";
        String methodName = "ldap_searchEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }


    }

    //positive test case for updating LDAP entry with valid parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {updateEntry} integration test with valid parameters.")
    public void testUpdateEntryWithValidParameters() throws Exception {
        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "updateEntry_ldap_valid_params.txt";
        String methodName = "ldap_updateEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("result"));
            JSONObject result = jsonObject.getJSONObject("result");
            Assert.assertNotNull(result);
            Assert.assertEquals(result.getString("message"), "Success");
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }

    //negative test case for updating LDAP entry with wrong parameters
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {updateEntry} integration test with wrong parameters.")
    public void testUpdateEntryWithWrongParameters() throws Exception {
        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "updateEntry_ldap_wrong_params.txt";
        String methodName = "ldap_updateEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }


    //positive test case for success LDAP authentication
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {authenticateEntry} integration test with valid parameters.")
    public void testSuccessAuthentication() throws Exception {

        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "authenticateUser_ldap.txt";
        String methodName = "ldap_authenticateUser";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        try {
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("result"));
            JSONObject result = jsonObject.getJSONObject("result");
            Assert.assertNotNull(result);
            Assert.assertEquals(result.getString("message"), "Success");
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }


    //negative test case for fail LDAP authentication
    @Test(priority = 1, groups = {"wso2.esb"}, description = "ldap {authenticateEntry} integration test with wrong parameters.")
    public void testFailAuthentication() throws Exception {

        createSampleEntity();

        //searching created entry
        String jsonRequestFilePath = pathToRequestsDirectory + "authenticateUser_ldap_wrong_credentials.txt";
        String methodName = "ldap_authenticateUser";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        try {
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            int statusCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(statusCode, 401);
        } finally {
            proxyAdmin.deleteProxy(methodName);
            //Finally deleting Entry with correct dn
            deleteSampleEntry();
        }
    }


    public void createSampleEntity() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createEntity_ldap_mandatory.txt";
        String methodName = "ldap_createEntity";

        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        //creating an entry
        try {
            ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    public void deleteSampleEntry() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteEntity_ldap.txt";
        String methodName = "ldap_deleteEntity";
        String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,providerUrl,securityPrincipal,securityCredentials,testUserId,userBase);

        try {

            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

}
