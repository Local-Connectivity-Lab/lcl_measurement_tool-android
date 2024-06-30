package com.lcl.lclmeasurementtool.ui.navigation

import com.lcl.lclmeasurementtool.R
import com.lcl.lclmeasurementtool.ui.Icon
import com.lcl.lclmeasurementtool.ui.LCLIcons


enum class TopLevelDestination(
    val selectedIcon: Icon,
    val unselectedIcon: Icon,
    val iconTextId: Int,
    val titleTextId: Int
){
    HOME(
        selectedIcon = Icon.ImageVectorIcon(LCLIcons.Home),
        unselectedIcon = Icon.ImageVectorIcon(LCLIcons.HomeBorder),
        iconTextId = R.string.home_icon_text,
        titleTextId = R.string.home_title_text
    ),
    HISTORY(
        selectedIcon = Icon.ImageVectorIcon(LCLIcons.HistoryData),
        unselectedIcon = Icon.ImageVectorIcon(LCLIcons.HistoryDataBorder),
        iconTextId = R.string.history_data_icon_text,
        titleTextId = R.string.history_data_title_text
    )
}