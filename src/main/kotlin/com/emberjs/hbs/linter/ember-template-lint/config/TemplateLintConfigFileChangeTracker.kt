
import TemplateLintUtil.Companion.isTemplateLintConfigFile
import com.intellij.lang.javascript.linter.JSLinterConfigChangeTracker
import com.intellij.lang.javascript.linter.JSLinterConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TemplateLintConfigFileChangeTracker(project: Project) : JSLinterConfigChangeTracker(project, ::isTemplateLintConfigFile) {
    override fun isAnalyzerRestartNeeded(project: Project, file: VirtualFile): Boolean {
        return JSLinterConfiguration.getInstance(project, TemplateLintConfiguration::class.java).extendedState.isEnabled
    }

}