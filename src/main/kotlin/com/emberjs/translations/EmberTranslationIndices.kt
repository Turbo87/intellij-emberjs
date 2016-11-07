package com.emberjs.translations

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object EmberTranslationIndices {

    fun getTranslationKeys(project: Project): Set<String> =
            EmberI18nIndex.getTranslationKeys(project) + EmberIntlIndex.getTranslationKeys(project)

    fun getTranslations(key: String, project: Project): Map<String, String> =
            EmberI18nIndex.getTranslations(key, project) + EmberIntlIndex.getTranslations(key, project)

    fun getFilesWithKey(key: String, project: Project): List<VirtualFile> =
            EmberI18nIndex.getFilesWithKey(key, project) + EmberIntlIndex.getFilesWithKey(key, project)
}
