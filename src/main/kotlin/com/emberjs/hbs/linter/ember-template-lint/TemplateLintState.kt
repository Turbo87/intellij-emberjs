
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.JSNpmLinterState

data class TemplateLintState(
        private val myInterpreterRef: NodeJsInterpreterRef,
        private val myTemplateLintPackage: NodePackage,
        val isRunOnSave: Boolean,
) : JSNpmLinterState<TemplateLintState> {

    private val myPackageRef: NodePackageRef = NodePackageRef.create(myTemplateLintPackage)

    override fun withLinterPackage(packageRef: NodePackageRef): TemplateLintState {
        val constantPackage = packageRef.constantPackage
                ?: throw AssertionError(this.javaClass.simpleName + " does not support non-constant package refs")

        return copy(myTemplateLintPackage = constantPackage)
    }

    override fun getInterpreterRef(): NodeJsInterpreterRef {
        return this.myInterpreterRef
    }

    override fun getNodePackageRef(): NodePackageRef {
        return this.myPackageRef
    }

    override fun withInterpreterRef(interpreterRef: NodeJsInterpreterRef): TemplateLintState {
        return copy(myInterpreterRef = interpreterRef)
    }

    val templateLintPackage: NodePackage
        get() {
            return this.myTemplateLintPackage
        }

    companion object {
        val DEFAULT = TemplateLintState(
                NodeJsInterpreterRef.createProjectRef(),
                NodePackage(""),
                false)
    }
}
