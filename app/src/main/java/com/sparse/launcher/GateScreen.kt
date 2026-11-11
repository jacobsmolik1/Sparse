package com.sparse.launcher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent
import kotlinx.coroutines.delay

private const val GATE_SECONDS = 3

@Composable
fun GateScreen(vm: LauncherViewModel, app: AppInfo, reason: GateReason = GateReason.Mindful) {
    val font = LocalSparseFont.current

    when (reason) {
        GateReason.Mindful -> MindfulGate(vm, app, font)
        GateReason.LimitReached -> LimitReachedGate(vm, app, font)
        GateReason.FocusBlock -> FocusBlockGate(vm, app, font)
    }
}

@Composable
private fun MindfulGate(
    vm: LauncherViewModel,
    app: AppInfo,
    font: androidx.compose.ui.text.font.FontFamily,
) {
    var secondsLeft by remember { mutableIntStateOf(GATE_SECONDS) }
    val progress by remember { derivedStateOf { 1f - secondsLeft / GATE_SECONDS.toFloat() } }

    LaunchedEffect(app.packageName) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        vm.doLaunch(app)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = app.displayLabel,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontFamily = font,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "take a breath",
            fontSize = 14.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "opening in $secondsLeft…",
            fontSize = 14.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Spacer(Modifier.height(32.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            trackColor = MaterialTheme.colorScheme.surface,
        )

        Spacer(Modifier.height(40.dp))

        TextButton(onClick = { vm.back() }) {
            Text(
                text = "go back",
                fontSize = 14.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }
    }
}

@Composable
private fun LimitReachedGate(
    vm: LauncherViewModel,
    app: AppInfo,
    font: androidx.compose.ui.text.font.FontFamily,
) {
    val opensToday = remember { vm.usage.getOpensToday(app.packageName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = app.displayLabel,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontFamily = font,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "daily limit reached",
            fontSize = 16.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "opened $opensToday times today",
            fontSize = 14.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            TextButton(onClick = { vm.back() }) {
                Text(
                    text = "go back",
                    fontSize = 14.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                )
            }
            TextButton(onClick = { vm.doLaunch(app) }) {
                Text(
                    text = "open anyway",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
            }
        }
    }
}

@Composable
private fun FocusBlockGate(
    vm: LauncherViewModel,
    app: AppInfo,
    font: androidx.compose.ui.text.font.FontFamily,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = app.displayLabel,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontFamily = font,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "focus mode is on",
            fontSize = 16.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Spacer(Modifier.height(40.dp))

        TextButton(onClick = { vm.back() }) {
            Text(
                text = "go back",
                fontSize = 14.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }
    }
}
