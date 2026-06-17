package com.example.spamblocker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spamblocker.data.BlockedCallLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedHistoryScreen(viewModel: BlockedHistoryViewModel = viewModel()) {
    val blockedCalls by viewModel.blockedCalls.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Blocked Calls") }) }
    ) { padding ->
        if (blockedCalls.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No calls have been blocked yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockedCalls, key = { it.id }) { call ->
                    BlockedCallRow(call)
                }
            }
        }
    }
}

@Composable
fun BlockedCallRow(call: BlockedCallLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(call.number, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Matched pattern: \"${call.matchedPattern}\" (${matchTypeLabel(call.matchType)})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                formatTimestamp(call.blockedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}