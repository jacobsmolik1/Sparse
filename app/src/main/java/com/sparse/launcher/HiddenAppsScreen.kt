package com.sparse.launcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent

@Composable
fun HiddenAppsScreen(vm: LauncherViewModel) {
    val font = LocalSparseFont.current
    val allApps by vm.allApps.collectAsState()
    val hiddenApps = remember(allApps) {
        val hidden = vm.prefs.hiddenApps
        allApps.filter { it.packageName in hidden }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = "hidden apps",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = font,
        )

        Spacer(Modifier.height(28.dp))

        if (hiddenApps.isEmpty()) {
            Text(
                text = "no hidden apps",
                fontSize = 16.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(hiddenApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = app.displayLabel,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = font,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { vm.unhideApp(app.packageName) }) {
                            Text(
                                text = "show",
                                fontSize = 14.sp,
                                color = MaterialTheme.dimContent,
                                fontFamily = font,
                            )
                        }
                    }
                }
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
