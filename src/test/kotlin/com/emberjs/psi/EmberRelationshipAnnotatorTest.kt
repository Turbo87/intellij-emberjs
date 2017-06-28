package com.emberjs.psi

import com.emberjs.resolver.EmberName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberRelationshipAnnotatorTest {

    @Test fun fooBelongsTo() = doTest("foo", "belongsTo()", "model:foo")
    @Test fun belongsTo() = doTest("bar", "belongsTo('foo')", "model:foo")
    @Test fun fooBarBelongsTo() = doTest("bar", "belongsTo('foo-bar')", "model:foo-bar")
    @Test fun dsBelongsTo() = doTest("bar", "DS.belongsTo('foo')", "model:foo")

    @Test fun variableFooBarBelongsTo() = doTest("bar", "belongsTo('foo-bar',config)", "model:foo-bar")
    @Test fun variableDsBelongsTo() = doTest("bar", "DS.belongsTo('foo' , config)", "model:foo")

    @Test fun objectBelongsTo() = doTest("bar", "belongsTo('foo', config)", "model:foo")
    @Test fun objectFooBarBelongsTo() = doTest("bar", "belongsTo('foo-bar',config)", "model:foo-bar")
    @Test fun objectDsBelongsTo() = doTest("bar", "DS.belongsTo('foo' , config)", "model:foo")

    @Test fun barHasMany() = doTest("bar", "hasMany()", "model:bar")
    @Test fun hasMany() = doTest("foo", "hasMany('bar')", "model:bar")
    @Test fun barBazHasMany() = doTest("foo", "hasMany('bar-baz')", "model:bar-baz")
    @Test fun dsHasMany() = doTest("foo", "DS.hasMany('bar')", "model:bar")

    @Test fun variableHasMany() = doTest("foo", "hasMany('bar', config)", "model:bar")
    @Test fun variableBarBazHasMany() = doTest("foo", "hasMany('bar-baz',config)", "model:bar-baz")
    @Test fun variableDsHasMany() = doTest("foo", "DS.hasMany('bar' , config)", "model:bar")

    @Test fun objectHasMany() = doTest("foo", "hasMany('bar', {})", "model:bar")
    @Test fun objectBarBazHasMany() = doTest("foo", "hasMany('bar-baz',{})", "model:bar-baz")
    @Test fun objectDsHasMany() = doTest("foo", "DS.hasMany('bar' , {})", "model:bar")

    @Test fun emberBelongsTo() = doTest("foo", "Ember.belongsTo", null)
    @Test fun belongsToFoo() = doTest("foo", "belongsTo.foo()", null)
    @Test fun foo() = doTest("foo", "foo", null)
    @Test fun noCallBelongsTo() = doTest("foo", "belongsTo", null)
    @Test fun noCallHasMany() = doTest("foo", "hasMany", null)

    private fun doTest(key: String, value: String, expected: String?) {
        assertThat(EmberRelationshipAnnotator.extractRelationshipModel(key, value))
                .isEqualTo(expected?.let { EmberName.from(it) })
    }
}
