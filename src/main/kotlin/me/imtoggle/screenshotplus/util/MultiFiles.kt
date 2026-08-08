package me.imtoggle.screenshotplus.util

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.IOException

class MultiFiles(vararg file: File) : Transferable {

    private val files = file.toList()

    private val flavors = arrayOf(DataFlavor.javaFileListFlavor)

    override fun getTransferDataFlavors(): Array<DataFlavor> = flavors

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        DataFlavor.javaFileListFlavor.equals(flavor)

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) {
            throw UnsupportedFlavorException(flavor)
        }
        return files
    }
}