package me.imtoggle.screenshotplus.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.imtoggle.screenshotplus.config.ModConfig
import me.imtoggle.screenshotplus.util.MultiImages
import org.polyfrost.compose.render.ImageLoader
import org.polyfrost.oneconfig.internal.ui.components.Chip
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
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

data class ImageData(val bitmap: ImageBitmap, val file: File)

val images = mutableStateListOf<ImageData>()

val bounds = mutableMapOf<Int, Rect>()

val selected = mutableStateListOf<ImageData>()

val dragSelected = mutableStateListOf<Int>()

var currentIndex by mutableStateOf(-1)

var folders = mutableStateListOf<String>()

var lastSelected = 0

var isCtrl = false

var isShift = false

var currentPath = mutableListOf(ModConfig.screenShotRootFolder)

fun getPath() = File(currentPath.joinToString("/"))

fun getSelected(image: ImageData) = if (selected.isEmpty()) listOf(image) else selected + image

fun refreshImages(together: Boolean = false) {
    val newList = mutableListOf<ImageData>()
    if (!together) images.clear()
    folders.clear()
    runAsync {
        val folder = getPath()
        if (!folder.exists()) return@runAsync
        folder.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "png") {
                ImageLoader.fromFile(file.absolutePath)?.toComposeImageBitmap()?.let { bitMap ->
                    mc.execute {
                        if (together) {
                            newList += ImageData(bitMap, file)
                        } else {
                            images += ImageData(bitMap, file)
                        }
                    }
                }
            } else if (file.isDirectory) {
                folders.add(file.name)
            }
        }
        if (together) {
            mc.execute {
                images.clear()
                images.addAll(newList)
            }
        }
    }
}

fun makeRect(start: Offset, offset: Offset, density: Float): Rect {
    val end = start + offset
    return Rect(min(start.x, end.x), min(start.y, end.y), max(start.x, end.x), max(start.y, end.y))
}

@Composable
fun Screenshots() {
    LaunchedEffect(Unit) {
        lastSelected = 0
        currentPath = mutableListOf(ModConfig.screenShotRootFolder)
        refreshImages(false)
    }
    val interactionSource = rememberInteractionSource()
    var start by remember { mutableStateOf(Offset.Zero) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current.density
    var dragRect by remember { mutableStateOf(makeRect(start, offset, density)) }

    Column(
        verticalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentPath.forEachIndexed { index, name ->
                if (index == 0) {
                    Chip(
                        label = "...",
                        selected = true,
                        icon = "folder",
                        onClick = {
                            currentPath = mutableListOf(ModConfig.screenShotRootFolder)
                            refreshImages(false)
                        }
                    )
                } else {
                    Chip(
                        label = name,
                        selected = true,
                        icon = "folder",
                        onClick = {
                            currentPath = currentPath.subList(0, index)
                            println(getPath())
                            currentPath += name
                            println(getPath())
                            refreshImages(false)
                        }
                    )
                }
            }
            folders.forEach {
                Chip(
                    label = it,
                    selected = false,
                    icon = "folder",
                    onClick = {
                        currentPath += it
                        refreshImages(false)
                    }
                )
            }
        }
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
                    Key.C -> if (isCtrl) {
                        ClipboardHelper.setTransferable(MultiImages(selected.map { it.file }))
                    }
                }
                false
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos: Offset ->
                        start = pos
                    },
                    onDrag = { _, dragAmount ->
                        offset += dragAmount
                        dragRect = makeRect(start, offset, density)
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
                        selected.sortBy { it.file.name }
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
                    verticalArrangement = Arrangement.spacedBy(19.dp),
                    horizontalArrangement = Arrangement.spacedBy(19.dp),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
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
                selected.sortBy { it.file.name }
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
                bitmap = image.bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            if (isHovered) ImageButton(image)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(12f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                images[index].file.name,
                color = LocalTheme.current.textColor,
                modifier = Modifier.padding(horizontal = 4.dp),
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
                modifier = Modifier
                    .size(32.dp),
                shape = LocalTheme.current.buttonShape,
                contentPadding = PaddingValues.Zero,
                colors = ButtonDefaults.buttonColors(backgroundColor = Accent),
                onClick = { println("hi") }
            ) {
                Icon("paintbrush", color = LocalTheme.current.accentTextColor)
            }
            Button(
                modifier = Modifier
                    .size(32.dp),
                shape = LocalTheme.current.buttonShape,
                contentPadding = PaddingValues.Zero,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE5484D)),
                onClick = {
                    try {
                        UiSounds.play(UiSoundEvent.CLICK)
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
            )
            if (currentIndex < images.size - 1) Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight(0.5f)
                    .background(LocalTheme.current.pageBackground)
                    .onClick(interactionSource) {
                        currentIndex = (currentIndex + 1).coerceIn(images.indices)

                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = image.bitmap
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