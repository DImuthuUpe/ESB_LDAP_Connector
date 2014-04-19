package org.wso2.carbon.connector.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

public class LDAPUtils {
	protected static  Log log = LogFactory.getLog(LDAPUtils.class);
	
	protected static DirContext getContext(String providerUrl,String securityPrincipal,String credentials) throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		env.put(Context.PROVIDER_URL, providerUrl);//"ldap://192.168.1.164:389/"
		env.put(Context.SECURITY_PRINCIPAL,securityPrincipal);// "cn=admin,dc=wso2,dc=com"
		env.put(Context.SECURITY_CREDENTIALS, credentials);//"comadmin"

		DirContext ctx = null;
		ctx = new InitialDirContext(env);
		return ctx;
	}

	public static String lookupFunctionParam(MessageContext ctxt, String paramName) {
        return (String)ConnectorUtils.lookupTemplateParamater(ctxt, paramName);
    }
	
	public static String lookupContextParams(MessageContext ctxt, String paramName){
		return (String)ctxt.getProperty(paramName);
	}
	
	public static void storeAdminLoginDatails(MessageContext ctxt, String url, String principal,String password){
        ctxt.setProperty(LDAPConstants.PROVIDER_URL, url);
        ctxt.setProperty(LDAPConstants.SECURITY_PRINCIPAL, principal);
        ctxt.setProperty(LDAPConstants.SECURITY_CREDENTIALS, password);
        
        String providerUrl = LDAPUtils.lookupFunctionParam(ctxt, "providerUrl");
		System.out.println("provider url 2 "+providerUrl);
    }

}
