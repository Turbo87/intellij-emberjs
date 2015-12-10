package com.emberjs.utils

import com.emberjs.project.EmberProjectComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.openapi.vfs.VirtualFile


val AnActionEvent.virtualFile: VirtualFile?
    get() = getData(LangDataKeys.VIRTUAL_FILE)

val AnActionEvent.emberRoot: VirtualFile?
    get() {
        val project = project ?: return null
        val file = virtualFile ?: return null
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return null

        return roots.filter { isAncestor(it, file, true) }.firstOrNull()
    }

val AnActionEvent.hasEmberRoot: Boolean
    get() = emberRoot != null

fun AnActionEvent.getIdeView() =
        getData(LangDataKeys.IDE_VIEW)
