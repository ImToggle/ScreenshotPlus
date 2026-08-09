package me.imtoggle.screenshotplus.tree

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastForEach
import me.imtoggle.screenshotplus.config.ModConfig
import java.io.File
import java.nio.file.Files

open class FileNode(val file: File)

class ImageNode(file: File) : FileNode(file) {
    override fun toString(): String {
        return "ImageNode(path='${file.absolutePath}')"
    }
}

class FolderNode(file: File, val depth: Int) : FileNode(file) {
    var subFolders = arrayListOf<FolderNode>()
    var images = arrayListOf<ImageNode>()

    override fun toString(): String {
        return "FolderNode(path=${file.absolutePath}, subFolders=$subFolders, images=$images)"
    }
}

object FileManager {
    var root = File(ModConfig.screenShotRootFolder)
        set(value) {
            field = value
            currentFolder = null
            navigate(FolderNode(value, 0))
        }

    var currentFolder: FolderNode? by mutableStateOf(null)

    val paths = mutableStateListOf<FolderNode>()

    var sortingMethod by mutableStateOf(0)

    private val backStack = ArrayDeque<FolderNode>()
    private val forwardStack = ArrayDeque<FolderNode>()

    val nodeComparator
        get() = when (sortingMethod) {
        0 -> compareBy<FileNode> { it.file.name }
        else -> compareBy<FileNode> { it.file.lastModified() }
    }

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
        images.sortWith(nodeComparator)
    }

    fun FolderNode.cleanup() {
        subFolders.fastForEach {
            it.cleanup()
        }
        images.clear()
        images.trimToSize()
    }

    fun FolderNode.sort() {
        subFolders.fastForEach {
            it.sort()
        }
        images.sortWith(nodeComparator)
        subFolders.sortWith(nodeComparator)
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
        currentFolder?.let {
            it.images.clear()
            it.images.trimToSize()
        }
        currentFolder = folder
        while (paths.size > folder.depth + 1) {
            paths.last().let {
                it.subFolders.clear()
                it.subFolders.trimToSize()
            }
            paths.removeLast()
        }
        paths.add(folder)
        folder.resolve()
        return true
    }

    fun refresh() {
        currentFolder?.resolve()
    }

    fun sort(method: Int) {
        sortingMethod = method
        currentFolder?.resolve()
    }

}