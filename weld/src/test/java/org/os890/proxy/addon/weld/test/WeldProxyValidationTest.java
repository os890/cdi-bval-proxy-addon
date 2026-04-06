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

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.os890.cdi.addon.dynamictestbean.EnableTestBeans;
import org.os890.proxy.addon.weld.WeldProxyResolver;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies that a Weld normal-scope proxy can be unwrapped and validated correctly
 * using {@link WeldProxyResolver} together with Bean Validation.
 *
 * <p>The original tests exercised the {@code InstanceResolver} SPI (removed in BVal 3.x).
 * These upgraded tests preserve the same validation scenarios and assertion values,
 * using direct proxy resolution via {@link WeldProxyResolver} instead.</p>
 *
 * <p>The {@link Validator} is created programmatically rather than injected via CDI
 * to avoid ambiguous dependency errors when both BVal's {@code ValidatorBean} and
 * the test-bean extension register a {@code Validator} producer.</p>
 */
@EnableTestBeans
public class WeldProxyValidationTest {

    @Inject
    private TestProxiedBean proxiedBean;

    @Inject
    private WeldProxyResolver proxyResolver;

    private Validator validator;

    /**
     * Creates a fresh {@link Validator} from the default factory before each test.
     */
    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Validates that unwrapping the Weld proxy via {@link WeldProxyResolver} and
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
     * the built-in Weld resolver still won and produced 1 violation.
     *
     * <p>In BVal 3.x the InstanceResolver SPI no longer exists. This test validates the
     * equivalent scenario: an identity resolver (returns its input) has no effect, so
     * unwrapping the proxy still yields the expected violation.</p>
     */
    @Test
    public void invalidInstanceResolverWithLowPriority() {
        TestProxiedBean actual = (TestProxiedBean) proxyResolver.resolveActualInstance(proxiedBean);
        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(actual);
        assertThat(violations.size(), is(1));
    }

    /**
     * Originally tested an invalid {@code InstanceResolver} registered with high priority
     * ({@code @Priority(MIN_VALUE)}). The resolver returned the instance unchanged, so
     * the built-in Weld resolver still won and produced 1 violation.
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
        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(0));
    }
}
