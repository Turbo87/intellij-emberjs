
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.JSNpmLinterState
import kotlin.test.assertNotNull

class TemplateLintState(
        private val myInterpreterRef: NodeJsInterpreterRef,
        private val myTemplateLintPackage: NodePackage,
        private val myPackageRef: NodePackageRef = NodePackageRef.create(myTemplateLintPackage)
) : JSNpmLinterState<TemplateLintState> {

    constructor() : this(
            NodeJsInterpreterRef.createProjectRef(),
            NodePackage("")
    )

    override fun withLinterPackage(packageRef: NodePackageRef): TemplateLintState {
        val constantPackage = packageRef.constantPackage

        assertNotNull(constantPackage != null, this.javaClass.simpleName + " does not support non-constant package refs")

        return TemplateLintState(this.myInterpreterRef, constantPackage!!)
    }

    override fun getInterpreterRef(): NodeJsInterpreterRef {
        return this.myInterpreterRef
    }

    override fun getNodePackageRef(): NodePackageRef {
        return this.myPackageRef
    }

    override fun withInterpreterRef(interpreterRef: NodeJsInterpreterRef): TemplateLintState {
        return TemplateLintState(interpreterRef, this.myTemplateLintPackage)
    }

    val templateLintPackage: NodePackage
        get() {
            return this.myTemplateLintPackage
        }
}