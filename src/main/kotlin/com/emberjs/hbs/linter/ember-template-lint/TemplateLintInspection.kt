import com.intellij.lang.javascript.linter.JSLinterInspection
import com.intellij.lang.javascript.linter.JSLinterWithInspectionExternalAnnotator
import com.intellij.openapi.options.OptionsBundle
import com.intellij.util.containers.ContainerUtil

class TemplateLintInspection : JSLinterInspection() {
    override fun getExternalAnnotatorForBatchInspection(): JSLinterWithInspectionExternalAnnotator<*, *> {
        return TemplateLintExternalAnnotator.INSTANCE_FOR_BATCH_INSPECTION
    }

    override fun getSettingsPath(): List<String?> {
        return ContainerUtil.newArrayList(
                OptionsBundle.message("configurable.group.language.settings.display.name", *arrayOfNulls(0)),
                TemplateLintBundle.message("hbs.lint.configurable.title"),
                this.displayName
        )
    }
}