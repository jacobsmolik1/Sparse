package com.sparse.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageHomeAppsScreen(vm: LauncherViewModel) {
    val pinnedApps by vm.pinnedApps.collectAsState()
    val allApps by vm.allApps.collectAsState()
    val font = LocalSparseFont.current
    var addQuery by remember { mutableStateOf("") }

    val unpinned = remember(addQuery, allApps, pinnedApps) {
        val pinnedPkgs = pinnedApps.map { it.packageName }.toSet()
        allApps
            .filter { it.packageName !in pinnedPkgs }
            .filter { addQuery.isBlank() || it.displayLabel.lowercase().contains(addQuery.lowercase()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 56.dp),
    ) {
        Text(
            text = "home screen apps",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = font,
        )

        Spacer(Modifier.height(28.dp))

        if (pinnedApps.isEmpty()) {
            Text(
                text = "no apps pinned yet — add one below",
                fontSize = 14.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        } else {
            Text(
                text = "pinned",
                fontSize = 12.sp,
                color = MaterialTheme.dimContent,
                letterSpacing = 1.sp,
                fontFamily = font,
            )
            Spacer(Modifier.height(12.dp))
        }

        // Pinned list with reorder + remove
        pinnedApps.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = app.displayLabel,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReorderButton("↑", enabled = index > 0, font = font) { vm.movePinnedUp(app.packageName) }
                    ReorderButton("↓", enabled = index < pinnedApps.size - 1, font = font) { vm.movePinnedDown(app.packageName) }
                    ReorderButton("×", enabled = true, font = font) { vm.unpinApp(app.packageName) }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "add app",
            fontSize = 12.sp,
            color = MaterialTheme.dimContent,
            letterSpacing = 1.sp,
            fontFamily = font,
        )
        Spacer(Modifier.height(10.dp))

        BasicTextField(
            value = addQuery,
            onValueChange = { addQuery = it },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = font,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            decorationBox = { inner ->
                if (addQuery.isEmpty()) {
                    Text(
                        text = "search…",
                        fontSize = 16.sp,
                        color = MaterialTheme.dimContent,
                        fontFamily = font,
                    )
                }
                inner()
            },
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(unpinned, key = { it.packageName }) { app ->
                Text(
                    text = app.displayLabel,
                    fontSize = 16.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.pinApp(app.packageName)
                            addQuery = ""
                        }
                        .padding(vertical = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { vm.back() }, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = "done",
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }
    }
}

@Composable
private fun ReorderButton(
    label: String,
    enabled: Boolean,
    font: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        fontSize = 18.sp,
        color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.dimContent,
        fontFamily = font,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    )
}
