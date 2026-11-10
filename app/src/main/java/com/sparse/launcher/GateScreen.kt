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
import com.sparse.launcher.ui.theme.OnBackgroundDim
import kotlinx.coroutines.delay

private const val GATE_SECONDS = 3

@Composable
fun GateScreen(vm: LauncherViewModel, app: AppInfo) {
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
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "opening in $secondsLeft…",
            fontSize = 14.sp,
            color = OnBackgroundDim,
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
                "go back",
                fontSize = 14.sp,
                color = OnBackgroundDim,
            )
        }
    }
}
