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

package org.os890.proxy.addon.weld.test;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import org.apache.bval.cdi.BValInterceptor;

/**
 * CDI extension that vetoes {@link BValInterceptor} during Weld bootstrap.
 *
 * <p>When both BVal's {@code BValExtension} and the dynamic-test-bean extension are
 * active, Weld sees two beans of type {@code jakarta.validation.Validator}: the
 * programmatic {@code ValidatorBean} registered by BVal and a {@code MockBean}
 * created by the test framework for unsatisfied injection points. This causes an
 * ambiguous-dependency error at {@code BValInterceptor.validator}.</p>
 *
 * <p>Vetoing the interceptor removes the problematic injection point. The tests
 * create the {@code Validator} programmatically via
 * {@code Validation.buildDefaultValidatorFactory()} so the interceptor is not needed.</p>
 */
public class BValInterceptorVetoExtension implements Extension {

    void vetoBValInterceptor(@Observes ProcessAnnotatedType<BValInterceptor> event) {
        event.veto();
    }
}
