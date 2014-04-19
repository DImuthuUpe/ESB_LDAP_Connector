package org.wso2.carbon.connector.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class AddEntry extends AbstractConnector {
    public static final String OBJECT_CLASS = "objectClass"; //class1,class2
    public static final String ATTRIBUTES = "attributes"; // name=dimuthu,mail=dimuhtuu@wso2.com
    public static final String DN = "dn";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String objectClass = LDAPUtils.lookupFunctionParam(messageContext, OBJECT_CLASS); // inetOrgPerson,organizationalPerson
        String attributesString = LDAPUtils.lookupFunctionParam(messageContext, ATTRIBUTES);
        String dn = LDAPUtils.lookupFunctionParam(messageContext, DN);

        String providerUrl = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.PROVIDER_URL);
        String securityPrincipal = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_PRINCIPAL);
        String securityCredentials = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_CREDENTIALS);

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(LDAPConstants.CONNECTOR_NAMESPACE, "ns");
        OMElement result = factory.createOMElement("result", ns);
        OMElement message = factory.createOMElement("message", ns);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext();

        try {

            DirContext context = LDAPUtils.getContext(providerUrl,securityPrincipal, securityCredentials);

            String classes[] = objectClass.split(",");
            Attributes entry = new BasicAttributes();
            Attribute obClassAttr = new BasicAttribute("objectClass");
            for (int i = 0; i < classes.length; i++) {
                obClassAttr.add(classes[i]);
            }
            entry.put(obClassAttr);
            if (attributesString != null) {
                String attrSet[] = attributesString.split(","); // name=dimuthu
                for (int i = 0; i < attrSet.length; i++) {
                    String keyVals[] = attrSet[i].split("=");
                    Attribute newAttr = new BasicAttribute(keyVals[0]);
                    newAttr.add(keyVals[1]);
                    entry.put(newAttr);
                }
            }
            try {
                context.createSubcontext(dn, entry);
                message.setText("Success");
                result.addChild(message);
            } catch (NamingException e) {
                log.error("Failed to create ldap entry with dn = " + dn);
                axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.BAD_REQUEST);
                message.setText("Error " + e.getMessage());
                result.addChild(message);
            }


        } catch (NamingException ex) {
            axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.INVALID_CREDENTIALS);
            //Authentication error
            message.setText("Error "+ ex.getMessage());
            result.addChild(message);
        }
        messageContext.getEnvelope().getBody().setFirstChild(result);
    }


}
