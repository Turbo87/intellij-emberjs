package com.emberjs.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile

val AnActionEvent.module: Module?
    get() = getData(LangDataKeys.MODULE)

val AnActionEvent.virtualFile: VirtualFile?
    get() = getData(LangDataKeys.VIRTUAL_FILE)

val AnActionEvent.emberRoot: VirtualFile?
    get() = module?.emberRoot

val AnActionEvent.hasEmberRoot: Boolean
    get() = emberRoot != null

val AnActionEvent.emberInRepoAddonRoot: VirtualFile?
    get() = virtualFile?.parentsAndSelf?.find { it.isInRepoAddon }

fun AnActionEvent.getIdeView() =
        getData(LangDataKeys.IDE_VIEW)
