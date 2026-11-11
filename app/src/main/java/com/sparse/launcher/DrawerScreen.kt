package com.sparse.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerScreen(vm: LauncherViewModel) {
    val query by vm.drawerQuery.collectAsState()
    val apps = remember(query, vm.allApps.collectAsState().value) { vm.filteredApps() }
    val notifPackages by NotificationStore.active.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val font = LocalSparseFont.current
    val bodySp = when (vm.themeState.collectAsState().value.bodySize) {
        "small" -> 15.sp
        "large" -> 22.sp
        else -> 18.sp
    }

    LaunchedEffect(Unit) {
        vm.setDrawerQuery("")
        focusRequester.requestFocus()
    }

    LaunchedEffect(apps) {
        if (query.isNotBlank() && apps.size == 1) {
            vm.setDrawerQuery("")
            vm.launchApp(apps[0])
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 40f) vm.back()
                }
            }
            .padding(horizontal = 32.dp, vertical = 56.dp),
    ) {
        BasicTextField(
            value = query,
            onValueChange = { vm.setDrawerQuery(it) },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = bodySp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light,
                fontFamily = font,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(bottom = 8.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "search apps",
                        fontSize = bodySp,
                        color = MaterialTheme.dimContent,
                        fontWeight = FontWeight.Light,
                        fontFamily = font,
                    )
                }
                inner()
            },
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(apps, key = { it.packageName }) { app ->
                Text(
                    text = if (app.packageName in notifPackages) "${app.displayLabel} ·" else app.displayLabel,
                    fontSize = bodySp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                vm.setDrawerQuery("")
                                vm.launchApp(app)
                            },
                            onLongClick = { vm.navigate(Screen.AppSettings(app)) },
                        )
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}
