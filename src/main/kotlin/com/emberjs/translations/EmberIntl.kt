package com.emberjs.translations

import com.emberjs.Ember
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

object EmberIntl {

    fun findConfigFile(file: VirtualFile) =
            Ember.findProjectFolder(file)?.findFileByRelativePath("config/ember-intl.js")

    fun findBaseLocale(file: PsiFile): String? {
        val configFile = findConfigFile(file.virtualFile) ?: return null
        val configPsiFile = PsiManager.getInstance(file.project).findFile(configFile) ?: return null

        return EmberIntlBaseLocaleFinder().findIn(configPsiFile)
    }
}
