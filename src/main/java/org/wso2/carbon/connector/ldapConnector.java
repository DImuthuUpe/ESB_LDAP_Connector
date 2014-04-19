/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.connector;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.*;

public class ldapConnector extends AbstractConnector implements Connector {

    public void connect(MessageContext messageContext) throws ConnectException {
        Object templateParam = getParameter(messageContext, "generated_param");
        try {
            System.out.println("Hello WORLD " + "ldapConnector !!!" + " : paramter :" + templateParam.toString());
            /**
             * Add your connector code here
             */
        } catch (Exception e) {
            throw new ConnectException(e);
        }
    }
}
