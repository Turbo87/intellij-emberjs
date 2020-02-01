
import com.intellij.lang.javascript.linter.JSLinterConfigurable
import com.intellij.lang.javascript.linter.JSLinterView
import com.intellij.openapi.project.Project

class TemplateLintConfigurable(project: Project, fullModeDialog: Boolean = false) :
        JSLinterConfigurable<TemplateLintState>(
        project, TemplateLintConfiguration::class.java, fullModeDialog) {

    companion object {
        const val ID = "configurable.emberjs.hbs.lint"
    }

    constructor(project: Project) : this(project, false)

    override fun getId(): String {
        return ID
    }

    override fun createView(): JSLinterView<TemplateLintState> {
        return TemplateLintView(this.project, this.isFullModeDialog)
    }

    override fun getDisplayName(): String {
        return TemplateLintBundle.message("hbs.lint.configurable.name")
    }

}