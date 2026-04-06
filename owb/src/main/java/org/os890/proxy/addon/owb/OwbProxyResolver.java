/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.os890.proxy.addon.owb;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.proxy.OwbNormalScopeProxy;

/**
 * CDI bean that resolves the actual contextual instance behind an OWB normal-scope proxy.
 *
 * <p>Use this to unwrap CDI proxies before passing them to Bean Validation, which reads
 * fields via reflection and would otherwise operate on the proxy object rather than the
 * underlying instance. Note: Apache BVal removed {@code InstanceResolver} in 3.x, so
 * proxy unwrapping must be done explicitly.</p>
 */
@ApplicationScoped
public class OwbProxyResolver {

    /**
     * Returns the actual contextual instance behind an OWB normal-scope proxy,
     * or the original object if it is not an OWB proxy.
     *
     * @param instance the object to inspect; may be an OWB proxy or a regular instance
     * @return the actual instance behind the proxy, or {@code instance} unchanged
     */
    public Object resolveActualInstance(Object instance) {
        if (instance instanceof OwbNormalScopeProxy proxy) {
            return WebBeansContext.currentInstance()
                    .getNormalScopeProxyFactory().getInstanceProvider(proxy).get();
        }
        return instance;
    }
}
