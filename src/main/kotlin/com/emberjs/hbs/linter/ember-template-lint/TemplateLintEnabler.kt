import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.JSBundle
import com.intellij.lang.javascript.linter.JSLinterConfiguration.getInstance
import com.intellij.lang.javascript.linter.JSLinterGuesser
import com.intellij.lang.javascript.linter.JSLinterUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.ContainerUtil

class TemplateLintEnabler : DirectoryProjectConfigurator {
    override fun configureProject(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, newProject: Boolean) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized {
            guessTemplateLint(project)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(TemplateLintEnabler::class.java)

        fun guessTemplateLint(project: Project) {
            if (Registry.`is`("javascript.linters.prevent.detection")) {
                LOG.info("Linters detection disabled")
                return
            }

            LOG.debug("Detecting linters for project")
            val packageJsonFiles: Collection<PackageJsonData> =
                    ContainerUtil.map(FilenameIndex.getVirtualFilesByName(project, "package.json", GlobalSearchScope.projectScope(project))) { packageJsonFile: VirtualFile? -> PackageJsonData.getOrCreate(packageJsonFile!!) }

            packageJsonFiles.forEach {
                if (enableIfDependency(it, project, TemplateLintUtil.PACKAGE_NAME)) return
                if (enableIfDependency(it, project, TemplateLintUtil.CLI_PACKAGE_NAME)) return
            }
        }

        fun enableIfDependency(pkg: PackageJsonData, project: Project, dependency: String): Boolean {
            if (!pkg.isDependencyOfAnyType(dependency)) return false

            LOG.debug("$dependency is dependency, enabling it")
            templateLintEnabled(project, true)
            notifyEnabled(project, dependency)
            return true
        }

        fun templateLintEnabled(project: Project, enabled: Boolean) {
            getInstance(project, TemplateLintConfiguration::class.java).isEnabled = enabled
        }

        fun notifyEnabled(project: Project, dependency: String) {
            val message = JSBundle.message("js.linter.guesser.linter.enabled.because.of.package.json.section",
                    "TemplateLint",
                    dependency)

            JSLinterUtil.NOTIFICATION_GROUP.createNotification(message, MessageType.INFO).addAction(object : NotificationAction("Disable TemplateLint") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    JSLinterGuesser.LOG.info("TemplateLint disabled by user")
                    templateLintEnabled(project, false)
                    JSLinterUtil.NOTIFICATION_GROUP.createNotification(JSBundle.message("js.linter.guesser.linter.disabled", "TemplateLint"), MessageType.INFO).notify(project)
                }
            }).notify(project)
        }
    }
}