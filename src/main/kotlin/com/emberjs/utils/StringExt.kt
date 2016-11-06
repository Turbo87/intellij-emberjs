package com.emberjs.utils

import com.intellij.openapi.util.text.StringUtil

fun String.unquote() = StringUtil.unquoteString(this)
