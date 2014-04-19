package org.wso2.carbon.connector.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class Authenticate extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String providerUrl = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.PROVIDER_URL);
        String dn = LDAPUtils.lookupFunctionParam(messageContext, "dn");
        String password = LDAPUtils.lookupFunctionParam(messageContext, "password");

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(LDAPConstants.CONNECTOR_NAMESPACE, "ns");
        OMElement result = factory.createOMElement("result", ns);
        OMElement message = factory.createOMElement("message", ns);

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);//"ldap://192.168.1.164:389/"
        env.put(Context.SECURITY_PRINCIPAL, dn);// "cn=admin,dc=wso2,dc=com"
        env.put(Context.SECURITY_CREDENTIALS, password);//"comadmin"

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext();


        boolean logged = false;
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            if (ctx != null) {
                logged = true;
            }


            if (logged) {
                message.setText("Success");
                result.addChild(message);
            } else {
                axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.INVALID_CREDENTIALS);
                message.setText("Fail : dn or password is incorrect");
                result.addChild(message);
            }
        } catch (NamingException e) {
            log.error("Error connecting to LDAP server", e);
            axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.INVALID_CREDENTIALS);
            message.setText("Error " + e.getMessage());
            result.addChild(message);
        }
        messageContext.getEnvelope().getBody().setFirstChild(result);
    }

}
