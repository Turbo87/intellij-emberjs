package com.emberjs.psi

import com.emberjs.resolver.EmberName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberInjectionAnnotatorTest {

    @Test fun emberInjectService() = doTest("intl", "Ember.inject.service()", "service:intl")
    @Test fun emberInjectServiceWithParam() = doTest("foo", "Ember.inject.service('intl')", "service:intl")
    @Test fun emberInjectServiceWithParam2() = doTest("foo", "Ember.inject.service(\"intl\")", "service:intl")
    @Test fun injectService() = doTest("intl", "inject.service()", "service:intl")
    @Test fun service() = doTest("intl", "service()", "service:intl")
    @Test fun emberInjectController() = doTest("posts", "Ember.inject.controller()", "controller:posts")
    @Test fun injectController() = doTest("posts", "inject.controller()", "controller:posts")
    @Test fun fooBarService() = doTest("fooBar", "inject.service()", "service:foo-bar")
    @Test fun fooBarService2() = doTest("foo-bar", "inject.service()", "service:foo-bar")

    @Test fun emberService() = doTest("posts", "Ember.service()", null)
    @Test fun injectFoo() = doTest("posts", "inject.foo()", null)
    @Test fun foo() = doTest("posts", "foo", null)
    @Test fun serviceProperty() = doTest("posts", "service", null)
    @Test fun serviceString() = doTest("posts", "'service'", null)

    private fun doTest(key: String, value: String, expected: String?) {
        assertThat(EmberInjectionAnnotator.extractInjectionName(key, value))
                .isEqualTo(expected?.let { EmberName.from(it) })
    }
}
