package com.emberjs.utils

import com.emberjs.project.EmberModuleType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.ModuleType


fun AnActionEvent.getEmberModule() =
        getData(LangDataKeys.MODULE)?.let {
            if (ModuleType.get(it) is EmberModuleType) it else null
        }

fun AnActionEvent.hasEmberModule() =
        getEmberModule() != null

fun AnActionEvent.getIdeView() =
        getData(LangDataKeys.IDE_VIEW)
