package me.imtoggle.screenshotplus.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.cornerRadiusOrNull

@Composable
fun CustomChip(
    label: String = "",
    icon: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor by animateColorAsState(
        if (isHovered) LocalTheme.current.chipBackground else LocalTheme.current.chipBackground.copy(alpha = 0f)
    )
    val textColor by animateColorAsState(
        if (isHovered) LocalTheme.current.accentTextColor else LocalTheme.current.textColor
    )
    val borderColor by animateColorAsState(
        if (isHovered) LocalTheme.current.borderColor else LocalTheme.current.chipBackground.copy(alpha = 0f)
    )
    Row(
        modifier = Modifier
            .background(backgroundColor, LocalTheme.current.sideBarNavigationEntryShape)
            .border(1.dp, borderColor, LocalTheme.current.sideBarNavigationEntryShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .height(32.dp)
            .onClick(interactionSource) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.5.dp, horizontal = 7.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            icon?.let { Icon(it, color = textColor, modifier = modifier) }
            if (label.isNotEmpty()) {
                Text(label, fontSize = 12.sp, color = textColor, lineHeight = 12.sp)
            }
        }
    }
}

@Composable
fun Chip(
    label: String = "",
    icon: String? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = rememberInteractionSource()
    Row(
        modifier = Modifier
            .background(LocalTheme.current.chipBackground, LocalTheme.current.sideBarNavigationEntryShape)
            .border(1.dp, LocalTheme.current.borderColor, LocalTheme.current.sideBarNavigationEntryShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .height(36.dp)
            .onClick(interactionSource) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.5.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            icon?.let { Icon(it, color = LocalTheme.current.textColor, modifier = iconModifier) }
            if (label.isNotEmpty()) {
                Text(label, fontSize = 14.sp, color = LocalTheme.current.textColor, lineHeight = 14.sp, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun <T> DropDownMenu(list: List<T>, action: (Int, T) -> Unit, text: (T) -> String) {
    Box(
        modifier = Modifier
            .background(LocalTheme.current.chipBackground.copy(alpha = 0.75f), LocalTheme.current.sideBarNavigationEntryShape)
            .border(1.dp, LocalTheme.current.borderColor, LocalTheme.current.sideBarNavigationEntryShape)
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            list.forEachIndexed { index, item ->
                Box {
                    val interactionSource = rememberInteractionSource()
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    val borderColor = if (isHovered) LocalTheme.current.borderColor else Color.Transparent
                    val backgroundColor = if (isHovered) LocalTheme.current.chipBackground.copy(alpha = 1f) else Color.Transparent
                    val cornerRadius = LocalTheme.current.sideBarNavigationEntryShape.cornerRadiusOrNull() ?: 0.dp
                    val topRadius = if (index == 0) cornerRadius else 0.dp
                    val bottomRadius = if (index == list.lastIndex) cornerRadius else 0.dp
                    val shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor, shape)
                            .border(1.dp, borderColor, shape)
                            .onClick(interactionSource) {
                                action(index, item)
                            }
                    ) {
                        Text(
                            text(item),
                            color = LocalTheme.current.textColor,
                            modifier = Modifier.padding(10.dp, 10.dp)
                        )
                    }
                }
            }
        }
    }
}