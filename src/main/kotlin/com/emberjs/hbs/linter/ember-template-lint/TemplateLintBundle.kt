
import com.intellij.CommonBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*

class TemplateLintBundle {
    companion object {
        private var ourBundle: Reference<ResourceBundle>? = null
        @NonNls
        private const val BUNDLE = "com.emberjs.locale.TemplateLintBundle"

        private fun getBundle(): ResourceBundle {
            var bundle = SoftReference.dereference(ourBundle)

            if (bundle == null) {
                bundle = ResourceBundle.getBundle(BUNDLE)
                ourBundle = java.lang.ref.SoftReference<ResourceBundle>(bundle)
            }

            return bundle!!
        }

        fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
            return CommonBundle.message(getBundle(), key, *params)
        }
    }
}