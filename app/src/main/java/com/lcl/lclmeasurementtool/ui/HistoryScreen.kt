package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.viewmodels.ConnectivityUiState
import com.lcl.lclmeasurementtool.model.viewmodels.ConnectivityViewModel
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthUiState
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun HistoryRoute(
    signalStrengthViewModel: SignalStrengthViewModel = hiltViewModel(),
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    HistoryScreen(signalStrengthViewModel, connectivityViewModel)
}

@Composable
fun HistoryScreen(
    signalStrengthViewModel: SignalStrengthViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column {
        LCLTabRow(selectedTabIndex = selectedTabIndex) {
            HistoryItem.values().forEachIndexed { index, item ->
                LCLTab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {Text(text = item.name)}
                )
            }
        }
        LCLTabContents(selectedTabIndex = selectedTabIndex, signalStrengthViewModel, connectivityViewModel)
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LCLTabContents(
    selectedTabIndex: Int,
    signalStrengthViewModel: SignalStrengthViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    val data = if (selectedTabIndex == 1) listOf<ConnectivityReportModel>(

    ) else listOf<SignalStrengthReportModel>(
        SignalStrengthReportModel("deviceID1", 123.121, 456.451, "timestamp1", "cellID1", -81, 1),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp2", "cellID2", -82, 2),
    )

    val signalUiState: SignalStrengthUiState by signalStrengthViewModel.dataFlow.collectAsStateWithLifecycle()
    val connectivityUiState: ConnectivityUiState by connectivityViewModel.dataFlow.collectAsStateWithLifecycle()
    LazyColumn {
        when(selectedTabIndex) {
            0 -> {
                item {SignalStrengthItems(signalUiState)}
            }
            1 -> {
                item { ConnectivityItems(connectivityUiState) }
            }
        }
    }
}

@Composable
private fun SignalStrengthItems(uiState: SignalStrengthUiState) {
    when(uiState) {
        SignalStrengthUiState.Loading -> LCLLoadingWheel(contentDesc = "Loading data ...")
        is SignalStrengthUiState.Success -> {
            uiState.signalStrengths.forEach {
                SignalStrengthItem(data = it, onClick = { })
            }
        }
        is SignalStrengthUiState.Error -> ErrorScreen()
    }
}

@Composable
private fun ConnectivityItems(uiState: ConnectivityUiState) {
    when(uiState) {
        ConnectivityUiState.Loading -> LCLLoadingWheel(contentDesc = "Loading data ...")
        is ConnectivityUiState.Success -> {
            uiState.connectivities.forEach {
                ConnectivityItem(data = it, onClick = { })
            }
        }
        is ConnectivityUiState.Error -> ErrorScreen()
    }
}

@Composable
private fun ErrorScreen() {
    Text(text = "Error in loading data")
}

@Composable
fun LCLTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = {
            val style = MaterialTheme.typography.labelLarge.copy(textAlign = TextAlign.Center)
            ProvideTextStyle(value = style) {
                Box(modifier = Modifier.padding(top = 7.dp)) {
                    text()
                }
            }
        }
    )
}

@Composable
fun LCLTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(it[selectedTabIndex]),
                height = 2.dp,
                color = MaterialTheme.colorScheme.onSurface

            )
        },
        tabs = tabs
    )
}

//@Preview
//@Composable
//fun HistoryPreview() {
//    BoxWithConstraints {
//        HistoryScreen()
//    }
//}