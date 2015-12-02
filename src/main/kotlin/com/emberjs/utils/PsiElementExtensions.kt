package com.emberjs.utils

import com.emberjs.project.EmberModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleUtilCore.findModuleForFile
import com.intellij.psi.PsiElement


val PsiElement.module: Module?
    get() = containingFile?.virtualFile?.let { findModuleForFile(it, project) }

val PsiElement.emberModule: Module?
    get() = module?.let { if (ModuleType.get(it) is EmberModuleType) it else null }
