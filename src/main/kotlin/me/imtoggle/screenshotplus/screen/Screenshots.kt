package me.imtoggle.screenshotplus.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.imtoggle.screenshotplus.config.ModConfig
import me.imtoggle.screenshotplus.util.MultiFiles
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.polyfrost.compose.render.ImageLoader
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.sound.UiSoundEvent
import org.polyfrost.oneconfig.internal.ui.sound.UiSounds
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.oneconfig.utils.v1.dsl.runAsync
import org.polyfrost.oneconfig.utils.v1.dsl.schedule
import java.io.File
import java.nio.file.Files
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

data class ImageData(val thumbnail: ImageBitmap, val image: ImageBitmap, val file: File)

private val images = mutableStateListOf<ImageData>()

private val bounds = mutableMapOf<Int, Rect>()

private val selected = mutableStateListOf<ImageData>()

private val dragSelected = mutableStateListOf<Int>()

private var currentIndex by mutableStateOf(-1)

private var folders = mutableStateListOf<String>()

private var lastSelected = 0

private var isCtrl = false

private var isShift = false

var currentPath by mutableStateOf(mutableListOf(ModConfig.screenShotRootFolder))

fun getPath() = File(currentPath.joinToString("/"))

fun getSelected(image: ImageData) = selected.apply { if (!contains(image)) add(image) }

fun processThumbnail(image: Image): ImageBitmap? {
    if (image.width <= 480 && image.height <= 480) return null
    val size = 480 / max(image.width, image.height).toFloat()
    val width = (image.width * size).toInt()
    val height = (image.height * size).toInt()
    Surface.makeRasterN32Premul(width, height).use { surface ->
        surface.canvas.drawImageRect(image, org.jetbrains.skia.Rect.makeWH(width.toFloat(), height.toFloat()))
        surface.makeImageSnapshot().use { newImage ->
            return newImage.toComposeImageBitmap()
        }
    }
}

fun refreshImages(together: Boolean = false) {
    val newList = mutableListOf<ImageData>()
    if (!together) images.clear()
    folders.clear()
    runAsync {
        val folder = getPath()
        if (!folder.exists()) return@runAsync
        folder.listFiles()?.let { listFiles ->
            listFiles.sortBy { it.lastModified() }
            listFiles.forEach { file ->
                if (file.isFile && Files.probeContentType(file.toPath()).startsWith("image/")) {
                    val image = ImageLoader.fromFile(file.absolutePath) ?: return@runAsync
                    val bitMap = image.toComposeImageBitmap()
                    val thumbnail = processThumbnail(image) ?: bitMap
                    mc.execute {
                        if (folder.absolutePath != getPath().absolutePath) return@execute
                        ImageData(thumbnail, bitMap, file).let {
                            if (together) {
                                newList += it
                            } else {
                                images += it
                            }
                        }
                    }
                } else if (file.isDirectory) {
                    if (folder.absolutePath == getPath().absolutePath) folders.add(file.name)
                }
            }
        }
        folders.sort()
        if (together) {
            mc.execute {
                if (folder.absolutePath != getPath().absolutePath) return@execute
                images.clear()
                images.addAll(newList)
            }
        }
    }
}

fun makeRect(start: Offset, offset: Offset): Rect {
    val end = start + offset
    return Rect(min(start.x, end.x), min(start.y, end.y), max(start.x, end.x), max(start.y, end.y))
}

@Composable
fun ScreenshotHeader() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .border(1.dp, LocalTheme.current.borderColor, LocalTheme.current.sideBarNavigationEntryShape),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentPath.forEachIndexed { index, name ->
                if (index == 0) {
                    CustomChip(
                        icon = "folder",
                        onClick = {
                            currentPath = mutableListOf(ModConfig.screenShotRootFolder)
                            selected.clear()
                            lastSelected = 0
                            refreshImages(false)
                        }
                    )
                } else {
                    CustomChip(
                        label = name,
                        onClick = {
                            currentPath = currentPath.subList(0, index)
                            currentPath += name
                            selected.clear()
                            lastSelected = 0
                            refreshImages(false)
                        }
                    )
                }

                var expanded by remember { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    if (expanded) 180f else 90f
                )
                Box {
                    CustomChip(
                        icon = "up",
                        modifier = Modifier.rotate(rotation),
                        onClick = {
                            expanded = !expanded
                        }
                    )
                    if (expanded) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            offset = IntOffset(0, 32),
                            onDismissRequest = { expanded = false },
                            properties = PopupProperties(focusable = true),
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LocalTheme.current.chipBackground, LocalTheme.current.sideBarNavigationEntryShape)
                                    .border(1.dp, LocalTheme.current.borderColor, LocalTheme.current.sideBarNavigationEntryShape)
                            ) {
                                Column(
                                    modifier = Modifier.width(IntrinsicSize.Max)
                                ) {
                                    folders.forEach { folder ->
                                        Box {
                                            val interactionSource = rememberInteractionSource()
                                            val isHovered by interactionSource.collectIsHoveredAsState()
                                            val backgroundColor = if (isHovered) LocalTheme.current.chipBackground.copy(alpha = 1f) else Color.Transparent
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(backgroundColor, LocalTheme.current.sideBarNavigationEntryShape)
                                                    .onClick(interactionSource) {
                                                        expanded = false
                                                        currentPath += folder
                                                        selected.clear()
                                                        lastSelected = 0
                                                        refreshImages(false)
                                                    }
                                            ) {
                                                Text(
                                                    folder,
                                                    color = LocalTheme.current.textColor,
                                                    modifier = Modifier.padding(10.dp, 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Screenshots() {
    LaunchedEffect(Unit) {
        currentIndex = -1
        lastSelected = 0
        selected.clear()
        refreshImages(false)
    }
    val interactionSource = rememberInteractionSource()
    var start by remember { mutableStateOf(Offset.Zero) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current.density
    var dragRect by remember { mutableStateOf(makeRect(start, offset)) }
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(8),
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            items(folders) { folder ->
//                Chip(
//                    label = folder,
//                    icon = "folder",
//                    onClick = {
//                        currentPath = (currentPath + folder).toMutableList()
//                        selected.clear()
//                        lastSelected = 0
//                        refreshImages(false)
//                    }
//                )
//            }
//        }
        Box(modifier = Modifier
            .weight(1f)
            .onClick(interactionSource) {
                selected.clear()
            }
            .onKeyEvent { keyEvent ->
                isCtrl = keyEvent.isCtrlPressed
                isShift = keyEvent.isShiftPressed
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (keyEvent.key) {
                    Key.A -> if (isCtrl) {
                        selected.addAll(images)
                        selected.sortBy { it.file.lastModified() }
                    }
                    Key.C -> if (isCtrl) {
                        runAsync {
                            ClipboardHelper.setTransferable(MultiFiles(*selected.map { it.file }.toTypedArray()))
                        }
                    }
                    Key.Delete -> {
                        runAsync {
                            selected.fastForEach { it.file.delete() }
                            refreshImages(true)
                        }
                    }
                }
                false
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos: Offset ->
                        start = pos
                        dragSelected.clear()
                    },
                    onDrag = { _, dragAmount ->
                        offset += dragAmount
                        dragRect = makeRect(start, offset)
                        dragSelected.clear()
                        bounds.entries.forEach { (index, bound) ->
                            if (dragRect.overlaps(bound)) {
                                dragSelected.add(index)
                            }
                        }
                    },
                    onDragEnd = {
                        offset = Offset.Zero
                        selected.addAll(dragSelected.map { images[it] })
                        selected.sortBy { it.file.lastModified() }
                        dragSelected.clear()
                    }
                )
            }
        ) {
            if (currentIndex == -1) {
                val gridState = rememberLazyGridState()
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    bounds.clear()
                    itemsIndexed(
                        items = images,
                    ) { index, image ->
                        ImageComponent(image, index)
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(gridState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )

                if (offset != Offset.Zero) Box(
                    modifier = Modifier
                        .offset { dragRect.topLeft.round() }
                        .size((dragRect.width / density).dp, (dragRect.height / density).dp)
                        .border(4.dp, Accent.copy(alpha = 0.5f))
                )
            } else {
                ImageView(image = images[currentIndex])
            }
        }
    }
}

@Composable
fun ImageComponent(image: ImageData, index: Int) {
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var lastClick by remember { mutableStateOf(System.currentTimeMillis()) }
    Column(
        modifier = Modifier
            .onGloballyPositioned { position ->
                bounds[index] = position.boundsInParent()
            }
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interactionSource) {
                if (!isCtrl) {
                    selected.clear()
                }
                if (isShift) {
                    val start = min(index, lastSelected)
                    val end = max(index, lastSelected)
                    for (i in start..end) {
                        selected.add(images[i])
                    }
                } else {
                    selected.add(image)
                }
                selected.sortBy { it.file.lastModified() }
                if (System.currentTimeMillis() - lastClick < 500L) {
                    currentIndex = index
                }
                schedule(500.milliseconds) {
                    if (currentIndex != -1) {
                        selected.remove(image)
                    }
                }
                lastClick = System.currentTimeMillis()
                lastSelected = index
            }
            .background(if (selected.contains(image) || dragSelected.contains(index)) LocalTheme.current.popupBackground else Color.Transparent)
            .border(2.dp, if (selected.contains(image) || dragSelected.contains(index)) LocalTheme.current.borderColor.copy(alpha = 0.75f) else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(LocalTheme.current.modCardBackground)
        ) {
            Image(
                bitmap = image.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            if (isHovered) ImageButton(image)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                images[index].file.name,
                color = LocalTheme.current.textColor,
                modifier = Modifier.padding(horizontal = 4.dp),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ImageButton(image: ImageData) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.size(32.dp),
                shape = LocalTheme.current.buttonShape,
                contentPadding = PaddingValues.Zero,
                colors = ButtonDefaults.buttonColors(backgroundColor = Accent),
                onClick = {
                    UiSounds.play(UiSoundEvent.CLICK)
                    runAsync {
                        val selection = getSelected(image)
                        selection.sortedBy { it.file.lastModified() }
                        ClipboardHelper.setTransferable(MultiFiles(*selection.map { it.file }.toTypedArray()))
                    }
                }
            ) {
                Icon("copy", color = LocalTheme.current.accentTextColor)
            }
            Button(
                modifier = Modifier.size(32.dp),
                shape = LocalTheme.current.buttonShape,
                contentPadding = PaddingValues.Zero,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE5484D)),
                onClick = {
                    UiSounds.play(UiSoundEvent.CLICK)
                    try {
                        runAsync {
                            getSelected(image).fastForEach { it.file.delete() }
                            refreshImages(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                Icon("trash", color = LocalTheme.current.accentTextColor)
            }
        }
    }

}

@Composable
fun ImageView(image: ImageData) {
    val interactionSource = rememberInteractionSource()

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {
            currentIndex = -1
        },
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
        onKeyEvent = { keyEvent ->
            if (keyEvent.type != KeyEventType.KeyDown) return@Popup false
            when (keyEvent.key) {
                Key.DirectionLeft -> {
                    currentIndex = (currentIndex - 1).coerceIn(images.indices)
                }

                Key.DirectionRight -> {
                    currentIndex = (currentIndex + 1).coerceIn(images.indices)
                }
            }
            false
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .onClick(interactionSource) { currentIndex = -1 },
            contentAlignment = Alignment.Center
        ) {
            if (currentIndex > 0) Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight(0.5f)
                    .background(LocalTheme.current.pageBackground)
                    .onClick(interactionSource) {
                        currentIndex = (currentIndex - 1).coerceIn(images.indices)
                    }
            ) {
                Icon("left-arrow", color = LocalTheme.current.accentTextColor,
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxSize(0.5f)
                )
            }
            if (currentIndex < images.size - 1) Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight(0.5f)
                    .background(LocalTheme.current.pageBackground)
                    .onClick(interactionSource) {
                        currentIndex = (currentIndex + 1).coerceIn(images.indices)

                    }
            ) {
                Icon("right-arrow", color = LocalTheme.current.accentTextColor,
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxSize(0.5f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = image.image
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                        .onClick(interactionSource) {},
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }

}