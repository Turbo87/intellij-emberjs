package com.emberjs.cli

import com.emberjs.utils.isAlive

class YesThread(private val process: Process) : Thread("yes") {

    override fun run() {
        val writer = process.outputStream.writer()

        while (process.isAlive && !Thread.interrupted()) {
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
