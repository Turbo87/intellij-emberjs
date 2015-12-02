package com.emberjs.cli

import com.intellij.execution.process.ProcessHandler

class YesThread(private val handler: ProcessHandler) : Thread("yes") {

    override fun run() {
        val writer = handler.processInput!!.writer()

        while (!handler.isProcessTerminated && !Thread.interrupted()) {
            writer.apply {
                write("y\n")
                flush()
            }

            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                break;
            }
        }
    }
}
