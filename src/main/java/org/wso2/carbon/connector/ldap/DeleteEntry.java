package org.wso2.carbon.connector.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class DeleteEntry extends AbstractConnector {
    public static final String DN = "dn";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String dn = LDAPUtils.lookupFunctionParam(messageContext, DN);

        String providerUrl = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.PROVIDER_URL);
        System.out.println("provider url 3 " + providerUrl);
        String securityPrincipal = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_PRINCIPAL);
        String securityCredentials = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_CREDENTIALS);

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(LDAPConstants.CONNECTOR_NAMESPACE, "ns");
        OMElement result = factory.createOMElement("result", ns);
        OMElement message = factory.createOMElement("message", ns);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        try {
            DirContext context = LDAPUtils.getContext(providerUrl, securityPrincipal, securityCredentials); //root login
            try {
                Attributes matchingAttributes = new BasicAttributes(); //search for the existance of dn
                matchingAttributes.put(new BasicAttribute("dn"));
                NamingEnumeration<SearchResult> searchResult = context.search(dn, matchingAttributes);
                try {
                    context.destroySubcontext(dn);
                    message.setText("Success");
                    result.addChild(message);
                } catch (NamingException e) {
                    log.error("Failed to delete ldap entry with dn = " + dn);
                    axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.BAD_REQUEST);
                    message.setText("Error " + e.getMessage());
                    result.addChild(message);
                }
            } catch (NamingException ex) {
                axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.BAD_REQUEST);
                message.setText("Error : Entity "+dn+" does not exist");
                result.addChild(message);
            }
        } catch (NamingException ex) {
            axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.INVALID_CREDENTIALS);
            //Authentication error
            message.setText("Error " + ex.getMessage());
            result.addChild(message);
        }

        messageContext.getEnvelope().getBody().setFirstChild(result);


    }
}
