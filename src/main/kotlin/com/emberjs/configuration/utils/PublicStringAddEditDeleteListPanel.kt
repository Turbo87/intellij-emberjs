package com.emberjs.configuration.utils

import com.intellij.ui.AddEditDeleteListPanel

abstract class PublicStringAddEditDeleteListPanel(
        title: String?,
        initialList: List<String>
) : AddEditDeleteListPanel<String>(title, initialList) {
    fun replaceItems(items: List<String>) {
        myListModel.clear()

        if (items.isNotEmpty()) {

            items.forEach { myListModel.addElement(it) }

            myList.setSelectedValue(items.last(), false)
        }
    }
}