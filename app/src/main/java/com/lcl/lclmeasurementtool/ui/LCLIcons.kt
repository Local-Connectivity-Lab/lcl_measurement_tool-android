package com.lcl.lclmeasurementtool.ui

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoSim
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object LCLIcons {
    val Home = Icons.Filled.Home
    val HomeBorder = Icons.Outlined.Home
    val HistoryData = Icons.Filled.TableChart
    val HistoryDataBorder = Icons.Outlined.TableChart
    val Settings = Icons.Rounded.Settings
    val NoSIM = Icons.Filled.NoSim
}

sealed class Icon {
    data class ImageVectorIcon(val imageVector: ImageVector) : Icon()
    data class DrawableResourceIcon(@DrawableRes val id: Int) : Icon()
}