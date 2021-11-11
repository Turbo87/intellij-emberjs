
import com.intellij.ide.actionsOnSave.ActionOnSaveBackedByOwnConfigurable
import com.intellij.ide.actionsOnSave.ActionOnSaveContext
import com.intellij.lang.javascript.linter.JSLinterConfigurable
import com.intellij.lang.javascript.linter.JSLinterView
import com.intellij.openapi.project.Project
import com.intellij.ui.components.ActionLink

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

    override fun getView(): TemplateLintView? {
        return super.getView() as TemplateLintView?
    }

    class TemplateLintOnSaveActionInfo(context: ActionOnSaveContext) :
        ActionOnSaveBackedByOwnConfigurable<TemplateLintConfigurable>(
            context, ID, TemplateLintConfigurable::class.java) {

        override fun getActionOnSaveName(): String {
            return TemplateLintBundle.message("hbs.lint.run.on.save.checkbox.on.actions.on.save.page")
        }

        override fun setActionOnSaveEnabled(configurable: TemplateLintConfigurable, enabled: Boolean) {
            val view = configurable.view
            if (view != null) {
                view.myRunOnSaveCheckBox.isSelected = enabled
            }
        }

        override fun isApplicableAccordingToStoredState(): Boolean {
            return TemplateLintConfiguration.getInstance(this.project).isEnabled
        }

        override fun isApplicableAccordingToUiState(configurable: TemplateLintConfigurable): Boolean {
            val checkbox = configurable.view?.myRunOnSaveCheckBox
            return checkbox != null && checkbox.isVisible && checkbox.isEnabled
        }

        override fun isActionOnSaveEnabledAccordingToStoredState(): Boolean {
            return TemplateLintActionOnSave.isFixOnSaveEnabled(this.project)
        }

        override fun isActionOnSaveEnabledAccordingToUiState(configurable: TemplateLintConfigurable): Boolean {
            val checkbox = configurable.view?.myRunOnSaveCheckBox
            return checkbox != null && checkbox.isVisible && checkbox.isEnabled && checkbox.isSelected
        }

        override fun getActionLinks(): MutableList<out ActionLink> {
            return mutableListOf(createGoToPageInSettingsLink(ID))
        }
    }
}
