package com.emberjs.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile

val Module.emberRoot: VirtualFile?
    get() = ModuleRootManager.getInstance(this).contentRoots.find { it.isEmberFolder }