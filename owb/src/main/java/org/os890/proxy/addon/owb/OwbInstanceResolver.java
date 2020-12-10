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

import org.apache.bval.jsr.resolver.InstanceResolver;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.proxy.OwbInterceptorProxy;
import org.apache.webbeans.proxy.OwbNormalScopeProxy;

import javax.inject.Provider;
import java.lang.reflect.Field;

import static org.apache.deltaspike.core.util.ProxyUtils.isProxiedClass;

public class OwbInstanceResolver implements InstanceResolver {
    @Override
    public <T> T resolveInstance(T instance) {
        T result = null;
        if (instance instanceof OwbInterceptorProxy) {
            result = extractIntercepted(instance);
        }
        if (instance instanceof OwbNormalScopeProxy) {
            OwbNormalScopeProxy proxy = (OwbNormalScopeProxy) instance;
            Provider<T> provider = WebBeansContext.currentInstance().getNormalScopeProxyFactory().getInstanceProvider(proxy);
            result = provider.get();

            if (isProxiedClass(result.getClass())) {
                result = extractIntercepted(instance);
            }
        }
        return result;
    }

    private <T> T extractIntercepted(T instance) {
        if (instance instanceof OwbNormalScopeProxy) {
            Provider<T> provider = WebBeansContext.currentInstance().getNormalScopeProxyFactory().getInstanceProvider((OwbNormalScopeProxy) instance);
            instance = provider.get();
        }
        try {
            Field proxiedInstanceField = instance.getClass().getDeclaredField("owbIntDecProxiedInstance");
            proxiedInstanceField.setAccessible(true);
            return (T) proxiedInstanceField.get(instance);
        } catch (Exception e) {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }
}
