/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.mediators.throttle;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.namespace.QName;


/**
 * The Factory for create throttle mediator- key or InLine XMl need to provide
 */

public class ThrottleMediatorFactory extends AbstractMediatorFactory {

    private static final Log log = LogFactory.getLog(ThrottleMediatorFactory.class);

    /**
     * The Tag Name for throttle
     */
    private static final QName TAG_NAME
            = new QName(Constants.SYNAPSE_NAMESPACE + "/throttle", "throttle");

    public Mediator createMediator(OMElement elem) {

        ThrottleMediator throttleMediator = new ThrottleMediator();
        OMElement policy = elem.getFirstChildWithName(
                new QName(Constants.SYNAPSE_NAMESPACE, "policy"));
        if (policy != null) {
            OMAttribute key = policy.getAttribute(new QName(Constants.NULL_NAMESPACE, "key"));
            if (key != null) {
                String keyValue = key.getAttributeValue();
                if (keyValue != null && !"".equals(keyValue)) {
                    throttleMediator.setPolicyKey(keyValue);
                } else {
                    handleException("key attribute should have a value ");
                }
            } else {
                OMElement inLine = policy.getFirstElement();
                if (inLine != null) {
                    throttleMediator.setInLinePolicy(inLine);
                }
            }
        } else {
            handleException("Throttle Mediator must have a policy");
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        initMediator(throttleMediator,elem);
        return throttleMediator;
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public QName getTagQName() {
        return TAG_NAME;
    }
}
