package com.emberjs

import com.emberjs.hbs.HbsModuleReference
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.platform.templates.github.DownloadUtil
import com.intellij.psi.PsiElement
import org.apache.xerces.dom.AttrImpl
import org.apache.xerces.dom.DeferredElementNSImpl
import org.apache.xerces.parsers.DOMParser
import org.cyberneko.html.HTMLConfiguration
import org.jsoup.Jsoup
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader


class EmberHbsDocumentationProvider : DocumentationProvider, ExternalDocumentationProvider {

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
        val ref = element?.references?.firstOrNull()
        // if reference does not resolve then its an internal component/helper
        if (element != null && ref == null) {
            val name = element.text
            return arrayOf("https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/$name?anchor=$name").toMutableList()
        }
        return null
    }

    fun download(url: String): String {
        val targetDir = PathManager.getTempPath()
        val name = "ember-template-helpers-doc.html"
        val f = File(targetDir, name)
        if (!f.exists()) {
            DownloadUtil.downloadAtomically(ProgressManager.getInstance().progressIndicator, url, f)
        }
        return f.readText()
    }

    override fun fetchExternalDocumentation(project: Project?, element: PsiElement?, docUrls: MutableList<String>?, onHover: Boolean): String? {
        if (docUrls == null || docUrls.size == 0 || element == null) {
            return null
        }
        val ref = element.references.firstOrNull()
        if (ref != null) {
            return null
        }
        val name = element.text
        val htmlString = this.download(docUrls.first())
        return Jsoup.parse(htmlString).select("[data-anchor='$name']")?.parents()?.first()?.toString()
    }

    override fun hasDocumentationFor(element: PsiElement?, originalElement: PsiElement?): Boolean {
        val ref = element?.references?.firstOrNull()
        // if reference does not resolve then its an internal component/helper
        if (ref?.resolve() == null) {
            return true
        }
        return false
    }

    override fun canPromptToConfigureDocumentation(element: PsiElement?): Boolean {
        return false
    }

    override fun promptToConfigureDocumentation(element: PsiElement?) {
        return
    }

}
