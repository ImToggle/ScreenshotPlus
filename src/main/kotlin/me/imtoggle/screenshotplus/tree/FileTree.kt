package me.imtoggle.screenshotplus.tree

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastForEach
import me.imtoggle.screenshotplus.config.ModConfig
import java.io.File
import java.nio.file.Files

class ImageNode(val file: File) {
    override fun toString(): String {
        return "ImageNode(path='${file.absolutePath}')"
    }
}

open class FolderNode(val file: File, val depth: Int) {
    var subFolders = arrayListOf<FolderNode>()
    var images = arrayListOf<ImageNode>()

    override fun toString(): String {
        return "FolderNode(path=${file.absolutePath}, subFolders=$subFolders, images=$images)"
    }
}

object FileTreeManager {
    var root = File(ModConfig.screenShotRootFolder)
        set(value) {
            field = value
            currentFolder = null
            tree = FolderNode(value, 0).apply {
                navigate(this)
            }
        }

    var tree: FolderNode? = null

    var currentFolder: FolderNode? by mutableStateOf(null)

    val paths = mutableStateListOf<FolderNode>()

    private val backStack = ArrayDeque<FolderNode>()
    private val forwardStack = ArrayDeque<FolderNode>()

    fun FolderNode.resolve() {
        images.clear()
        subFolders.clear()
        file.listFiles()?.forEach { file ->
            if (file.isFile && Files.probeContentType(file.toPath()).startsWith("image/")) {
                images.add(ImageNode(file))
            } else if (file.isDirectory) {
                subFolders.add(FolderNode(file, depth + 1))
            }
        }
    }

    fun FolderNode.cleanup(force: Boolean = false) {
        subFolders.fastForEach {
            it.cleanup(force)
        }
        if (!paths.contains(this) || force) {
            images.clear()
            images.trimToSize()
            subFolders.clear()
            subFolders.trimToSize()
        }
    }

    fun clearHistory() {
        backStack.clear()
        forwardStack.clear()
    }

    fun back(): Boolean {
        val previous = backStack.lastOrNull() ?: return false
        backStack.removeLast()
        currentFolder?.let { forwardStack.addLast(it) }
        return setFolder(previous)
    }

    fun forward(): Boolean {
        val next = forwardStack.lastOrNull() ?: return false
        forwardStack.removeLast()
        currentFolder?.let { backStack.addLast(it) }
        return setFolder(next)
    }

    fun navigate(folder: FolderNode): Boolean {
        currentFolder?.let { backStack.addLast(it) }
        forwardStack.clear()
        return setFolder(folder)
    }

    fun setFolder(folder: FolderNode): Boolean {
        if (folder == currentFolder) return false
        currentFolder = folder
        while (paths.size > folder.depth) {
            paths.removeLast()
        }
        tree?.cleanup()
        paths.add(folder)
        folder.resolve()
        return true
    }

    fun refresh() {
        currentFolder?.resolve()
    }

}