package com.example.spamblocker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spamblocker.data.BlockPattern
import com.example.spamblocker.data.MatchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternScreen(viewModel: PatternViewModel = viewModel()) {
    val patterns by viewModel.patterns.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingPattern by remember { mutableStateOf<BlockPattern?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Blocked Patterns") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingPattern = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add pattern")
            }
        }
    ) { padding ->
        if (patterns.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No patterns added yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(patterns, key = { it.id }) { pattern ->
                    PatternRow(
                        pattern = pattern,
                        onEdit = { editingPattern = pattern; showDialog = true },
                        onDelete = { viewModel.deletePattern(pattern) },
                        onToggle = { viewModel.updatePattern(pattern.copy(enabled = !pattern.enabled)) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        PatternDialog(
            existing = editingPattern,
            onDismiss = { showDialog = false },
            onSave = { patternText, matchType ->
                if (editingPattern != null) {
                    viewModel.updatePattern(editingPattern!!.copy(pattern = patternText, matchType = matchType))
                } else {
                    viewModel.addPattern(patternText, matchType)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun PatternRow(
    pattern: BlockPattern,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pattern.pattern, style = MaterialTheme.typography.titleMedium)
                Text(
                    matchTypeLabel(pattern.matchType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = pattern.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun PatternDialog(
    existing: BlockPattern?,
    onDismiss: () -> Unit,
    onSave: (String, MatchType) -> Unit
) {
    var text by remember { mutableStateOf(existing?.pattern ?: "") }
    var matchType by remember { mutableStateOf(existing?.matchType ?: MatchType.STARTS_WITH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Pattern" else "Edit Pattern") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Number or pattern (e.g. +91140)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Match type", style = MaterialTheme.typography.labelMedium)
                MatchType.entries.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = matchType == type, onClick = { matchType = type })
                        Text(matchTypeLabel(type))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onSave(text.trim(), matchType) },
                enabled = text.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun matchTypeLabel(matchType: MatchType): String = when (matchType) {
    MatchType.STARTS_WITH -> "Starts with"
    MatchType.ENDS_WITH -> "Ends with"
    MatchType.CONTAINS -> "Contains"
}