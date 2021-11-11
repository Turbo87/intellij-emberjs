import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.linter.JSLinterError
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import org.codehaus.jettison.json.JSONObject
import java.io.IOException
import java.util.*

class TemplateLintResultParser {
    companion object {
        private val LOG = Logger.getInstance(TemplateLintResultParser::class.java)

        private const val WARNING_SEVERITY = 1
        private const val ERROR_SEVERITY = 2

        private const val LINE = "line"
        private const val COLUMN = "column"
        private const val RULE = "rule"
        private const val SEVERITY = "severity"
        private const val MESSAGE = "message"

        private fun parseItem(map: JSONObject): JSLinterError {
            val line = map.getInt(LINE)
            val column = map.getInt(COLUMN)
            val rule = map.getString(RULE)
            val text = map.getString(MESSAGE)

            val highlightSeverity = when (map.getInt(SEVERITY)) {
                WARNING_SEVERITY -> HighlightSeverity.WARNING
                ERROR_SEVERITY -> HighlightSeverity.ERROR
                else -> null
            }

            return JSLinterError(line, column + 1, text, rule, highlightSeverity)
        }
    }

    @Throws(Exception::class)
    fun parse(stdout: String?): List<JSLinterError>? {
        if (StringUtil.isEmptyOrSpaces(stdout)) {
            return null
        }

        val errorList: ArrayList<JSLinterError> = ArrayList()
        try {
            val obj = JSONObject(stdout)

            val issues = obj.getJSONArray(obj.keys().next() as String)
            for (i in 0 until issues.length()) {
                val issue = issues.getJSONObject(i)

                // we skip fatal errors
                if (issue.has("fatal") && issue.getBoolean("fatal")) continue

                errorList.add(parseItem(issue))
            }

            return errorList
        } catch (ioException: IOException) {
            return null
        } catch (exception: Exception) {
            LOG.warn("TemplateLint result parsing error")
            throw Exception("TemplateLint result parsing error", exception)
        }
    }
}
