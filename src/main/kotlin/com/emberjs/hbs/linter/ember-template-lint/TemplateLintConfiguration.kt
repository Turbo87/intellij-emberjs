
import com.intellij.javascript.nodejs.util.JSLinterPackage
import com.intellij.lang.javascript.linter.JSLinterConfiguration
import com.intellij.lang.javascript.linter.JSLinterInspection
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import org.jdom.Element

@State(name = "TemplateLintConfiguration", storages = [Storage("emberLinters/templatelint.xml")])
class TemplateLintConfiguration(project: Project) : JSLinterConfiguration<TemplateLintState>(project) {
    private val myPackage: JSLinterPackage = JSLinterPackage(project, TemplateLintUtil.PACKAGE_NAME)

    override fun loadPrivateSettings(state: TemplateLintState): TemplateLintState {
        this.myPackage.readOrDetect()
        val constantPackage = myPackage.getPackage().constantPackage
                ?: throw AssertionError("TemplateLint does not support non-constant node package refs")

        return state.copy(
                myInterpreterRef = this.myPackage.interpreter,
                myTemplateLintPackage = constantPackage
        )
    }

    override fun savePrivateSettings(state: TemplateLintState) {
        this.myPackage.force(state.interpreterRef, state.templateLintPackage)
    }

    override fun toXml(state: TemplateLintState): Element? {
        if (state == defaultState) {
            return null
        }
        val parent = Element(TemplateLintUtil.PACKAGE_NAME)
        JDOMExternalizerUtil.writeField(parent, TAG_RUN_ON_SAVE, state.isRunOnSave.toString(), false.toString())
        return parent
    }

    override fun fromXml(element: Element): TemplateLintState {
        val isRunOnSave = JDOMExternalizerUtil.readField(element, TAG_RUN_ON_SAVE, false.toString()).toBoolean()
        return this.defaultState.copy(isRunOnSave = isRunOnSave)
    }

    override fun getDefaultState(): TemplateLintState {
        return TemplateLintState.DEFAULT
    }

    override fun getInspectionClass(): Class<out JSLinterInspection> {
        return TemplateLintInspection::class.java
    }

    companion object {
        private const val TAG_RUN_ON_SAVE = "fix-on-save"

        fun getInstance(project: Project) = getInstance(project, TemplateLintConfiguration::class.java)
    }
}
