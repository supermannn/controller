/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.netconf.confignetconfconnector.operations.editconfig;

import java.util.Map;
import java.util.Map.Entry;
import javax.management.Attribute;
import javax.management.ObjectName;
import org.opendaylight.controller.config.util.ConfigTransactionClient;
import org.opendaylight.controller.netconf.api.NetconfDocumentedException;
import org.opendaylight.controller.netconf.confignetconfconnector.exception.NetconfConfigHandlingException;
import org.opendaylight.controller.netconf.confignetconfconnector.mapping.attributes.fromxml.AttributeConfigElement;
import org.opendaylight.controller.netconf.confignetconfconnector.mapping.config.ServiceRegistryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplaceEditConfigStrategy extends AbstractEditConfigStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceEditConfigStrategy.class);

    @Override
    void handleMissingInstance(Map<String, AttributeConfigElement> configuration, ConfigTransactionClient ta,
                               String module, String instance, ServiceRegistryWrapper services) throws NetconfConfigHandlingException {
        throw new NetconfConfigHandlingException(
                String.format("Unable to handle missing instance, no missing instances should appear at this point, missing: %s : %s ",
                        module,
                        instance),
                NetconfDocumentedException.ErrorType.application,
                NetconfDocumentedException.ErrorTag.operation_failed,
                NetconfDocumentedException.ErrorSeverity.error);
    }

    @Override
    void executeStrategy(Map<String, AttributeConfigElement> configuration, ConfigTransactionClient ta, ObjectName on, ServiceRegistryWrapper services) throws NetconfConfigHandlingException {
        for (Entry<String, AttributeConfigElement> configAttributeEntry : configuration.entrySet()) {
            try {
                AttributeConfigElement ace = configAttributeEntry.getValue();

                if (!ace.getResolvedValue().isPresent()) {
                    Object value = ace.getResolvedDefaultValue();
                    ta.setAttribute(on, ace.getJmxName(), new Attribute(ace.getJmxName(), value));
                    LOG.debug("Attribute {} set to default value {} for {}", configAttributeEntry.getKey(), value,
                            on);
                } else {
                    Object value = ace.getResolvedValue().get();
                    ta.setAttribute(on, ace.getJmxName(), new Attribute(ace.getJmxName(), value));
                    LOG.debug("Attribute {} set to value {} for {}", configAttributeEntry.getKey(), value, on);
                }

            } catch (Exception e) {
                throw new IllegalStateException("Unable to set attributes for " + on + ", Error with attribute "
                        + configAttributeEntry.getKey() + ":" + configAttributeEntry.getValue(), e);
            }
        }
    }
}
