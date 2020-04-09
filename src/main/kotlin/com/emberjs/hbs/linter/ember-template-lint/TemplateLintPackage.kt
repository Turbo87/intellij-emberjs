
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.library.yarn.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.project.Project
import java.io.File
import java.io.IOException

class TemplateLintPackage(
        private val myProject: Project,
        private val myPkg: NodePackage
) {
    val versionStr: String
        get() {
            val version = myPkg.getVersion(myProject)
            return version?.rawVersion ?: "<unknown>"
        }

    @Throws(ExecutionException::class)
    fun addMainEntryJsFile(commandLine: GeneralCommandLine, interpreter: NodeJsInterpreter) {
        if (myPkg is YarnPnpNodePackage) {
            myPkg.addYarnRunToCommandLine(commandLine, myProject, interpreter, TemplateLintUtil.PACKAGE_NAME)
        } else {
            val file = myPkg.findBinFile(TemplateLintUtil.PACKAGE_NAME, "dist${File.separator}cli.js")
                    ?: throw ExecutionException("Please specify TemplateLint package correctly: ${TemplateLintUtil.PACKAGE_NAME} binary not found")
            commandLine.addParameter(file.absolutePath)
        }
    }


    companion object {
        @Throws(IOException::class)
        fun fromNodePackage(project: Project, pkg: NodePackage): TemplateLintPackage {
            return TemplateLintPackage(project, pkg);
        }
    }
}
