package com.github.musicyou.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.musicyou.BuildConfig
import com.github.musicyou.LocalPlayerPadding
import com.github.musicyou.R
import com.github.musicyou.models.ReleaseInfo
import com.github.musicyou.ui.styling.Dimensions
import com.github.musicyou.utils.UpdateChecker

@ExperimentalAnimationApi
@Composable
fun About() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val playerPadding = LocalPlayerPadding.current

    var releaseInfo by remember { mutableStateOf<ReleaseInfo?>(null) }
    var isChecking by remember { mutableStateOf(true) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val currentVersion = BuildConfig.VERSION_NAME

    LaunchedEffect(Unit) {
        releaseInfo = UpdateChecker.checkForUpdate()
        isChecking = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(125.dp)
                .aspectRatio(1F),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${stringResource(id = R.string.app_name)} v$currentVersion",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        if (releaseInfo?.isUpdateAvailable == true) {
            Button(
                onClick = { uriHandler.openUri("https://github.com/Shreyas850/Music-You/releases/latest") },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Update,
                    contentDescription = stringResource(id = R.string.check_for_updates)
                )

                Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                Text(text = "Update to v${releaseInfo?.latestVersion}")
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.spacer + 8.dp))

        ListItem(
            headlineContent = { Text("Latest update") },
            supportingContent = {
                if (isChecking) {
                    Text("Checking...")
                } else if (releaseInfo == null) {
                    Text("Unable to fetch latest update info")
                } else {
                    Text("v${releaseInfo?.latestVersion}")
                }
            },
            trailingContent = {
                if (releaseInfo != null) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.clickable(enabled = releaseInfo != null) {
                isExpanded = !isExpanded
            }
        )

        AnimatedVisibility(visible = isExpanded && releaseInfo != null) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Changelog",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = releaseInfo?.changelog ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.github))
            },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = stringResource(id = R.string.github)
                )
            },
            modifier = Modifier.clickable {
                uriHandler.openUri("https://github.com/Shreyas850/Music-You")
            }
        )
    }
}
