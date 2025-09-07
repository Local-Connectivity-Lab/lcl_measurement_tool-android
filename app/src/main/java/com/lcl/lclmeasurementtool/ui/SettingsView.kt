package com.lcl.lclmeasurementtool.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcl.lclmeasurementtool.BuildConfig
import com.lcl.lclmeasurementtool.R
import com.lcl.lclmeasurementtool.model.viewmodels.SettingsViewModel

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showData = viewModel.shouldShowData.collectAsStateWithLifecycle()
    
    // Export functions that handle the result
    val exportSignalStrength = { viewModel.exportSignalStrengthData { _ -> } }
    
    val exportConnectivity = { viewModel.exportConnectivityData { _ -> } }

    SettingDialog(
        onDismiss = onDismiss,
        toggleShowData = viewModel::toggleShowData,
        logout = viewModel::logout,
        showData = showData.value,
        exportSignalStrength = exportSignalStrength,
        exportConnectivity = exportConnectivity
    )
}

@Composable
fun SettingDialog(
    onDismiss: () -> Unit,
    toggleShowData: (Boolean) -> Unit,
    logout: () -> Unit,
    showData: Boolean,
    exportSignalStrength: () -> Unit = {},
    exportConnectivity: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(id = R.string.settings_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Divider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsPanel(
                    onSelectShowData = {toggleShowData(!showData)}, 
                    onLogoutClicked = {logout()}, 
                    showData = showData,
                    exportSignalStrength = exportSignalStrength,
                    exportConnectivity = exportConnectivity
                )
                Divider(Modifier.padding(top = 8.dp))
                LinksPanel()
                VersionInfo()
            }
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.dismiss_dialog_button_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDismiss() }
            )
        }
    )
}

@Composable
private fun SettingsPanel(
    onSelectShowData: (Boolean) -> Unit,
    onLogoutClicked: () -> Unit,
    showData: Boolean,
    exportSignalStrength: () -> Unit = {},
    exportConnectivity: () -> Unit = {}
) {
    SettingsDialogSectionTitle(text = "General")
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Checkbox(checked = showData, onCheckedChange = onSelectShowData)
            Column {
                Text(text = "Show Data on SCN website")
                TextSummary(text = "Display signal and speed test data on SCN public map. Your data will help others understand our coverage!")
            }
        }
    }
    SettingsDialogSectionTitle(text = "Data Management")
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        ClickableRow(text = "Export Signal Strength Data", icon = Icons.Rounded.Download, onClick = exportSignalStrength)
        ClickableRow(text = "Export Speed Test Data", icon = Icons.Rounded.Download, onClick = exportConnectivity)
    }
    SettingsDialogSectionTitle(text = "Help")
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        ClickableRow(text = "Send Feedback", icon = Icons.Rounded.Email, onClick = {})
        ClickableRow(text = "Logout", icon = Icons.Rounded.Logout, onClick = onLogoutClicked)
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun ClickableRow(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row (Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start) {
            TextButton(onClick = onClick) {
                Text(text = text)
            }
        }
    }
}

@Composable
private fun TextSummary(text: String) {
    Text(text = text,
        style = TextStyle(
            color = Color.Gray,
            fontSize = 12.sp,)
    )
}

@Composable
private fun VersionInfo() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR})")
        TextSummary(text = "By Local Connectivity Lab @ UWCSE")
    }
}

@Composable
private fun LinksPanel() {
    Row(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                TextLink(
                    text = stringResource(R.string.privacy_policy),
                    url = PRIVACY_POLICY_URL
                )
                Spacer(Modifier.width(16.dp))
                TextLink(
                    text = "Terms of Use",
                    url = TOU
                )
            }
            Spacer(Modifier.height(8.dp))
            Row {
                TextLink(text = "Feature List", url = FEATURE_LIST)
            }
        }
    }
}

@Composable
private fun TextLink(text: String, url: String) {
    val launchResourceIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = LocalContext.current

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable {
                ContextCompat.startActivity(context, launchResourceIntent, null)
            }
    )
}

@Preview
@Composable
private fun PreviewSettingsDialog() {
    SettingDialog(
        onDismiss = {},
        toggleShowData = {},
        logout = {},
        showData = false,
        exportSignalStrength = {},
        exportConnectivity = {}
    )
}
//
//@Preview
//@Composable
//private fun PreviewSettingsDialogLoading() {
//    NiaTheme {
//        SettingsDialog(
//            onDismiss = {},
//            settingsUiState = Loading,
//            onChangeThemeBrand = { },
//            onChangeDarkThemeConfig = { }
//        )
//    }
//}

/* ktlint-disable max-line-length */
private const val PRIVACY_POLICY_URL = "https://seattlecommunitynetwork.org/"
private const val TOU = "https://seattlecommunitynetwork.org/"
private const val FEATURE_LIST = "https://docs.google.com/document/d/e/2PACX-1vTWyg_FFPtKEsNIuD85VauCPy42fLQNk0QoZ6c8NzTH8-d5_AsUX4d-RDAuOoWKPMj_fHppi8Ve8VVv/pub"