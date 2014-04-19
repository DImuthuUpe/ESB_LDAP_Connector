package org.wso2.carbon.connector.ldap;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class Init extends AbstractConnector{

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		String providerUrl = LDAPUtils.lookupFunctionParam(messageContext, LDAPConstants.PROVIDER_URL);
		String securityPrincipal = LDAPUtils.lookupFunctionParam(messageContext, LDAPConstants.SECURITY_PRINCIPAL);
		String securityCredentials = LDAPUtils.lookupFunctionParam(messageContext, LDAPConstants.SECURITY_CREDENTIALS);
		LDAPUtils.storeAdminLoginDatails(messageContext, providerUrl, securityPrincipal, securityCredentials);
	}

}
