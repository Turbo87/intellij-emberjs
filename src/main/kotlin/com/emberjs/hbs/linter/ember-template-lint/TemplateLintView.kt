
import com.intellij.ide.actionsOnSave.ActionsOnSaveConfigurable
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.lang.javascript.linter.JSLinterBaseView
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JPanel

class TemplateLintView(project: Project, fullModeDialog: Boolean) : JSLinterBaseView<TemplateLintState>(fullModeDialog) {
    private val myNodeInterpreterField: NodeJsInterpreterField = NodeJsInterpreterField(project, false)
    private val myTemplateLintPackageField: NodePackageField = NodePackageField(myNodeInterpreterField, TemplateLintUtil.PACKAGE_NAME)
    val myRunOnSaveCheckBox: JCheckBox = JCheckBox(TemplateLintBundle.message("hbs.lint.run.on.save"))

    private val panel = FormBuilder.createFormBuilder()
            .setHorizontalGap(10)
            .setVerticalGap(4)
            .setFormLeftIndent(10)
            .addLabeledComponent(TemplateLintBundle.message("hbs.lint.node.path.label"), myNodeInterpreterField)
            .addLabeledComponent(TemplateLintBundle.message("hbs.lint.package.label"), this.myTemplateLintPackageField)
            .addComponent(myRunOnSaveCheckBox)
            .addComponent(ActionsOnSaveConfigurable.createGoToActionsOnSavePageLink())
            .panel

    private val myCenterPanel: JPanel = SwingHelper.wrapWithHorizontalStretch(panel).apply {
        this.border = BorderFactory.createEmptyBorder(5, 0, 0, 0)
    }

    override fun getState(): TemplateLintState {
        return TemplateLintState(
                myNodeInterpreterField.interpreterRef,
                myTemplateLintPackageField.selected,
                myRunOnSaveCheckBox.isEnabled && myRunOnSaveCheckBox.isSelected)
    }

    override fun setState(state: TemplateLintState) {
        myNodeInterpreterField.interpreterRef = state.interpreterRef
        myTemplateLintPackageField.selected = state.templateLintPackage
        myRunOnSaveCheckBox.isSelected = state.isRunOnSave;
        if (isFullModeDialog) {
            myNodeInterpreterField.setPreferredWidthToFitText()
            myTemplateLintPackageField.setPreferredWidthToFitText()
        }
    }

    override fun createCenterComponent(): Component {
        return this.myCenterPanel
    }
}
