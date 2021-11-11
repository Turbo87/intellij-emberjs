import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.openapi.vfs.VirtualFile

class TemplateLintSessionData(
        val interpreter: NodeJsInterpreter,
        val templateLintPackage: TemplateLintPackage,
        val workingDir: VirtualFile,
        val fileToLint: VirtualFile,
        val fileToLintContent: String,
        val fix: Boolean,
) {
}
