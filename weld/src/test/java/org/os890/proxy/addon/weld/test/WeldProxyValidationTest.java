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

import org.apache.bval.jsr.ApacheValidatorFactory;
import org.apache.bval.jsr.resolver.InstanceResolver;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(CdiTestRunner.class)
public class WeldProxyValidationTest {
    @Inject
    private TestProxiedBean proxiedBean;

    @Test
    public void successfulProxyValidation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(1));
    }

    @Test
    public void invalidInstanceResolverWithLowPriority() {
        Validator validator = Validation.buildDefaultValidatorFactory().unwrap(ApacheValidatorFactory.class).usingContext().addInstanceResolvers(new InvalidInstanceResolverWithLowPriority()).getValidator();

        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(1));
    }

    @Test
    public void invalidInstanceResolverWithHighPriority() {
        Validator validator = Validation.buildDefaultValidatorFactory().unwrap(ApacheValidatorFactory.class).usingContext().addInstanceResolvers(new InvalidInstanceResolverWithHighPriority()).getValidator();

        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(1));
    }

    @Test
    public void noViolationDueToInvalidResolver() {
        Validator validator = Validation.buildDefaultValidatorFactory().unwrap(ApacheValidatorFactory.class).usingContext().addInstanceResolvers(new InstanceResolver() {
            @Override
            public Object resolveInstance(Object instance) {
                return "abc";
            }

            @Override
            public int compareTo(InstanceResolver instanceResolver) {
                return -1;
            }
        }).getValidator();

        Set<ConstraintViolation<TestProxiedBean>> violations = validator.validate(proxiedBean);
        assertThat(violations.size(), is(0));
    }

    @Priority(MAX_VALUE)
    private static class InvalidInstanceResolverWithLowPriority implements InstanceResolver {
        @Override
        public <T> T resolveInstance(T instance) {
            return instance;
        }
    }

    @Priority(MIN_VALUE)
    private static class InvalidInstanceResolverWithHighPriority implements InstanceResolver {
        @Override
        public <T> T resolveInstance(T instance) {
            return instance;
        }
    }
}
