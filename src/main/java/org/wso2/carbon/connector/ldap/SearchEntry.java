package org.wso2.carbon.connector.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class SearchEntry extends AbstractConnector {
    public static final String OBJECT_CLASS = "objectClass";
    public static final String FILTERS = "filters";
    public static final String DN = "dn";
    public static final String ATTRIBUTES = "attributes";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String objectClass = LDAPUtils.lookupFunctionParam(messageContext, OBJECT_CLASS);
        String filter = LDAPUtils.lookupFunctionParam(messageContext, FILTERS); // "uid=dimuthuu,name=dimuthu"
        String dn = LDAPUtils.lookupFunctionParam(messageContext, DN);
        String returnAttributes[] = LDAPUtils.lookupFunctionParam(messageContext, ATTRIBUTES).split(","); // uid,name,email

        String providerUrl = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.PROVIDER_URL);
        String securityPrincipal = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_PRINCIPAL);
        String securityCredentials = LDAPUtils.lookupContextParams(messageContext, LDAPConstants.SECURITY_CREDENTIALS);

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(LDAPConstants.CONNECTOR_NAMESPACE, "ns");
        OMElement result = factory.createOMElement("result", ns);
        OMElement message = factory.createOMElement("message", ns);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        try {
            DirContext context = LDAPUtils.getContext(providerUrl, securityPrincipal, securityCredentials);

            String attrFilter = generateAttrFilter(filter);
            String searchFilter = generateSearchFilter(objectClass, attrFilter);
            NamingEnumeration<SearchResult> results = null;
            try {
                results = searchInUserBase(dn, searchFilter, returnAttributes, SearchControls.SUBTREE_SCOPE, context);

                SearchResult entityResult = null;

                if (results != null && results.hasMore()) {
                    while (results.hasMore()) {
                        entityResult = results.next();
                        Attributes attributes = entityResult.getAttributes();
                        Attribute attribute;
                        OMElement entry = factory.createOMElement("entry", ns);
                        OMElement dnattr = factory.createOMElement("dn", ns);
                        dnattr.setText(entityResult.getNameInNamespace());
                        entry.addChild(dnattr);

                        for (int i = 0; i < returnAttributes.length; i++) {
                            attribute = attributes.get(returnAttributes[i]);
                            if (attribute != null) {
                                NamingEnumeration ne = attribute.getAll();
                                while (ne.hasMoreElements()) {
                                    String value = (String) ne.next();
                                    OMElement attr = factory.createOMElement(returnAttributes[i], ns);
                                    attr.setText(value);
                                    entry.addChild(attr);
                                }
                            }
                        }
                        result.addChild(entry);
                    }
                } else {
                    axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.BAD_REQUEST);
                    message.setText("Error : No such entity");
                    result.addChild(message);
                }

                if (context != null) {
                    context.close();
                }

            } catch (NamingException e) { //LDAP Errors are catched
                axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.BAD_REQUEST);
                message.setText("Error " + e.getMessage());
                result.addChild(message);
            }

        } catch (NamingException ex) { //Authentication failures are catched
            axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, LDAPConstants.INVALID_CREDENTIALS);
            //Authentication error
            message.setText("Error " + ex.getMessage());
            result.addChild(message);
        }
        messageContext.getEnvelope().getBody().setFirstChild(result); //addChild

    }

    private NamingEnumeration<SearchResult> searchInUserBase(String dn,
                                                             String searchFilter, String[] returningAttributes, int searchScope,
                                                             DirContext rootContext) throws NamingException {
        String userBase = dn; // user search base "ou=staff,dc=wso2,dc=com"
        SearchControls userSearchControl = new SearchControls();
        userSearchControl.setReturningAttributes(returningAttributes);
        userSearchControl.setSearchScope(searchScope);
        NamingEnumeration<SearchResult> userSearchResults;
        userSearchResults = rootContext.search(userBase, searchFilter,userSearchControl);
        return userSearchResults;

    }

    private String generateAttrFilter(String filter) {
        String attrFilter = "";
        if (filter != null && filter.trim().length() > 0 && !filter.trim().equals("null")) {
            String filterArray[] = filter.split(",");
            if (filterArray != null && filterArray.length > 0) {
                for (int i = 0; i < filterArray.length; i++) {
                    attrFilter += "(";
                    attrFilter += filterArray[i];
                    attrFilter += ")";
                }
                // (uid=dimuthu)(name=dimuthu)
            }
        }
        return attrFilter;
    }

    private String generateSearchFilter(String objectClass, String attrFilter) {
        return "(&(objectClass=" + objectClass + ")" + attrFilter + ")"; // 'inetOrgPerson'
    }

}
