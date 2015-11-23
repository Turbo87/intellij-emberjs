package com.emberjs.resolver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberNameTest {

    @Test fun test() {
        val name = EmberName.from("route:crate/index")!!
        assertThat(name.type).isEqualTo("route")
        assertThat(name.name).isEqualTo("crate/index")
        assertThat(name.fullName).isEqualTo("route:crate/index")
    }

    @Test fun testInvalidName() {
        val name = EmberName.from("foobar")
        assertThat(name).isNull()
    }
}
