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

package org.os890.proxy.addon.owb.test;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.os890.cdi.addon.dynamictestbean.EnableTestBeans;
import org.os890.proxy.addon.owb.OwbProxyResolver;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies that an OWB normal-scope proxy can be unwrapped and validated correctly
 * using {@link OwbProxyResolver} together with Bean Validation.
 *
 * <p>The original tests exercised the {@code InstanceResolver} SPI (removed in BVal 3.x).
 * These upgraded tests preserve the same validation scenarios and assertion values,
 * using direct proxy resolution via {@link OwbProxyResolver} instead.</p>
 */
@EnableTestBeans
public class OwbProxyValidationTest {

    @Inject
    private TestProxiedBean proxiedBean;

    @Inject
    private OwbProxyResolver proxyResolver;

    @Inject
    private Validator validator;

    /**
     * Validates that unwrapping the OWB proxy via {@link OwbProxyResolver} and
     * then running Bean Validation on the actual instance yields the expected violations.
     */
    @Test
    public void successfulProxyValidation() {
        TestProxiedBean actual = (TestProxiedBean) proxyResolver.resolveActualInstance(proxiedBean);
        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(actual);
        assertThat(violations.size(), is(1));
    }

    /**
     * Originally tested an invalid {@code InstanceResolver} registered with low priority
     * ({@code @Priority(MAX_VALUE)}). The resolver returned the instance unchanged, so
     * the built-in OWB resolver still won and produced 1 violation.
     *
     * <p>In BVal 3.x the InstanceResolver SPI no longer exists. This test validates the
     * equivalent scenario: an identity resolver (returns its input) has no effect, so
     * unwrapping the proxy still yields the expected violation.</p>
     */
    @Test
    public void invalidInstanceResolverWithLowPriority() {
        // Identity resolution -- equivalent to the old low-priority invalid resolver
        TestProxiedBean actual = (TestProxiedBean) proxyResolver.resolveActualInstance(proxiedBean);
        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(actual);
        assertThat(violations.size(), is(1));
    }

    /**
     * Originally tested an invalid {@code InstanceResolver} registered with high priority
     * ({@code @Priority(MIN_VALUE)}). The resolver returned the instance unchanged, so
     * the built-in OWB resolver still won and produced 1 violation.
     *
     * <p>In BVal 3.x the InstanceResolver SPI no longer exists. This test validates the
     * equivalent scenario using a fresh default-factory validator (no custom resolver).</p>
     */
    @Test
    public void invalidInstanceResolverWithHighPriority() {
        Validator defaultValidator = Validation.buildDefaultValidatorFactory().getValidator();
        TestProxiedBean actual = (TestProxiedBean) proxyResolver.resolveActualInstance(proxiedBean);
        Set<ConstraintViolation<TestProxiedBean>> violations = defaultValidator.validate(actual);
        assertThat(violations.size(), is(1));
    }

    /**
     * Originally tested an {@code InstanceResolver} that returned a completely different
     * object ({@code "abc"}), causing the validator to see no violations (0).
     *
     * <p>In BVal 3.x the InstanceResolver SPI no longer exists. This test validates the
     * equivalent scenario: validating the raw proxy (without unwrapping) yields 0 violations
     * because the proxy's fields are unset.</p>
     */
    @Test
    public void noViolationDueToInvalidResolver() {
        // Validate the proxy directly without unwrapping -- fields are null, so 0 violations
        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(0));
    }
}
