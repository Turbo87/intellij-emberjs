
import com.intellij.javascript.nodejs.util.JSLinterPackage
import com.intellij.lang.javascript.linter.JSLinterConfiguration
import com.intellij.lang.javascript.linter.JSLinterInspection
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.jdom.Element
import kotlin.test.assertNotNull

@State(name = "TemplateLintConfiguration", storages = [Storage("emberLinters/templatelint.xml")])
class TemplateLintConfiguration(project: Project) : JSLinterConfiguration<TemplateLintState>(project) {
    private val DEFAULT_STATE = TemplateLintState()
    private val myPackage: JSLinterPackage = JSLinterPackage(project, TemplateLintUtil.PACKAGE_NAME)

    override fun loadPrivateSettings(state: TemplateLintState): TemplateLintState {
        this.myPackage.readOrDetect()
        val constantPackage = myPackage.getPackage().constantPackage

        assertNotNull(constantPackage, "TemplateLint does not support non-constant node package refs")

        return TemplateLintState(this.myPackage.interpreter, constantPackage)
    }

    override fun fromXml(element: Element): TemplateLintState {
        return this.defaultState
    }

    override fun savePrivateSettings(state: TemplateLintState) {
        this.myPackage.force(state.interpreterRef, state.templateLintPackage)
    }

    override fun toXml(state: TemplateLintState): Element? {
        return null
    }

    override fun getDefaultState(): TemplateLintState {
        return DEFAULT_STATE
    }

    override fun getInspectionClass(): Class<out JSLinterInspection> {
        return TemplateLintInspection::class.java
    }
}