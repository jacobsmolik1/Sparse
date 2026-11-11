package com.sparse.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent

@Composable
fun PickSwipeAppScreen(vm: LauncherViewModel, direction: String) {
    val font = LocalSparseFont.current
    val allApps by vm.allApps.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, allApps) {
        if (query.isBlank()) allApps
        else allApps.filter { it.displayLabel.lowercase().contains(query.lowercase()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = "swipe $direction",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = font,
        )

        Spacer(Modifier.height(24.dp))

        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light,
                fontFamily = font,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "search apps",
                        fontSize = 17.sp,
                        color = MaterialTheme.dimContent,
                        fontWeight = FontWeight.Light,
                        fontFamily = font,
                    )
                }
                inner()
            },
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "none",
                    fontSize = 17.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.setSwipeApp(direction, null)
                            vm.back()
                        }
                        .padding(vertical = 4.dp),
                )
            }
            items(filtered, key = { it.packageName }) { app ->
                Text(
                    text = app.displayLabel,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.setSwipeApp(direction, app.packageName)
                            vm.back()
                        }
                        .padding(vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { vm.back() }, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = "cancel",
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }
    }
}
