package com.emberjs.hbs

import com.dmarcotte.handlebars.psi.HbMustacheName
import com.emberjs.resolver.EmberName

class HbsComponentReference(element: HbMustacheName) : HbsModuleReference(element, "component") {
    override fun matches(module: EmberName): Boolean {
        return super.matches(module) || (module.type == "template" && module.name == "components/$value")
    }
}
