# CDI-Proxy BVal Add-on

An OWB-specific CDI add-on that provides `OwbProxyResolver`, a helper bean for unwrapping
OpenWebBeans normal-scope CDI proxies before passing instances to Apache Bean Validation.

## Problem

Apache BVal 3.x removed the `InstanceResolver` SPI. When validating a CDI `@ApplicationScoped`
(or other normal-scoped) bean, Bean Validation reads fields via reflection on the proxy object
rather than the underlying contextual instance. The proxy's fields are unset (null), causing
constraints to pass incorrectly.

## Solution

Inject `OwbProxyResolver` and call `resolveActualInstance(bean)` to obtain the real instance
before calling `validator.validate()`.

## Usage

```java
@Inject OwbProxyResolver proxyResolver;
@Inject Validator validator;
@Inject MyBean myBean;

MyBean actual = (MyBean) proxyResolver.resolveActualInstance(myBean);
Set<ConstraintViolation<MyBean>> violations = validator.validate(actual);
```

## Requirements

- Java 25+
- Jakarta CDI 4.1 (Apache OpenWebBeans 4.0.3)
- Apache BVal 3.x
- Jakarta Validation 3.1

## Build

```bash
mvn clean verify
```

## Testing

Tests use the [dynamic-cdi-test-bean-addon](https://github.com/os890/dynamic-cdi-test-bean-addon)
with `@EnableTestBeans` to boot an OWB SE container and inject CDI beans directly into JUnit
Jupiter test classes.

## Quality

The build enforces: compiler lint warnings, enforcer (Java 25, Maven 3.6.3, dependency
convergence), checkstyle, Apache RAT license headers, JaCoCo coverage, and Javadoc.

## License

Apache License, Version 2.0. See [LICENSE](LICENSE).
