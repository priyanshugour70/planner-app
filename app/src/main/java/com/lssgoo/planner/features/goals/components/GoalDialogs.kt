package com.lssgoo.planner.features.goals.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lssgoo.planner.data.model.Goal
import com.lssgoo.planner.data.model.GoalCategory
import com.lssgoo.planner.features.goals.models.GoalPriority
import com.lssgoo.planner.features.goals.models.GoalStatus
import com.lssgoo.planner.features.goals.models.Milestone
import com.lssgoo.planner.features.goals.models.MilestoneQuality
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalDialog(
    goal: Goal? = null,
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
) {
    var title by remember { mutableStateOf(goal?.title ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var category by remember { mutableStateOf(goal?.category ?: GoalCategory.HEALTH) }
    var color by remember { mutableLongStateOf(goal?.color ?: 0xFF4CAF50) }
    var targetDate by remember { mutableStateOf(goal?.targetDate) }
    var startDate by remember { mutableStateOf(goal?.startDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(goal?.priority ?: GoalPriority.MEDIUM) }
    var tags by remember { mutableStateOf(goal?.tags ?: "") }
    var motivation by remember { mutableStateOf(goal?.motivation ?: "") }
    var showAdvanced by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 800.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
                            Text(category.emoji, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(if (goal == null) "New Life Goal" else "Edit Goal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Define your vision for the future", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Goal Title") },
                        placeholder = { Text("e.g., Run a Marathon, Learn Piano") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Why is this goal important to you?") },
                        modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GoalCategory.entries.forEach { cat ->
                                val isSelected = category == cat
                                Surface(
                                    onClick = { category = cat },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.widthIn(min = 100.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(cat.emoji)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            cat.displayName, style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Priority", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GoalPriority.entries.forEach { p ->
                                val isSelected = priority == p
                                Surface(
                                    onClick = { priority = p },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(p.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(if (isSelected) 2.dp else 0.dp, if (isSelected) Color(p.color) else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(p.emoji)
                                        Text(p.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Target Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Surface(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), color = Color.Transparent
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        targetDate?.let { dateFormat.format(Date(it)) } ?: "Set deadline (Optional)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (targetDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                if (targetDate != null) {
                                    IconButton(onClick = { targetDate = null }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = { showAdvanced = !showAdvanced },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null, tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Advanced Options",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showAdvanced) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), color = Color.Transparent
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        startDate?.let { "Start: ${dateFormat.format(Date(it))}" } ?: "Start Date (Optional)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (startDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (startDate != null) {
                                        IconButton(onClick = { startDate = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = tags, onValueChange = { tags = it },
                                label = { Text("Tags (comma separated)") },
                                placeholder = { Text("e.g., 2026, Q1, important") },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Tag, null, tint = MaterialTheme.colorScheme.secondary) }
                            )

                            OutlinedTextField(
                                value = motivation, onValueChange = { motivation = it },
                                label = { Text("Motivation (Optional)") },
                                placeholder = { Text("Why does this goal matter to you?") },
                                modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.tertiary) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val newGoal = goal?.copy(
                                title = title, description = description, category = category, color = color,
                                targetDate = targetDate, startDate = startDate, priority = priority,
                                tags = tags.ifBlank { null }, motivation = motivation.ifBlank { null },
                                updatedAt = System.currentTimeMillis()
                            ) ?: Goal(
                                number = 0, title = title, description = description, category = category,
                                icon = category.iconName, color = color, targetDate = targetDate, startDate = startDate,
                                priority = priority, tags = tags.ifBlank { null }, motivation = motivation.ifBlank { null },
                                status = if (startDate != null) GoalStatus.IN_PROGRESS else GoalStatus.NOT_STARTED
                            )
                            onConfirm(newGoal)
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Goal")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = targetDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { targetDate = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { startDate = datePickerState.selectedDateMillis; showStartDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun MilestoneCompletionDialog(
    milestone: Milestone,
    onDismiss: () -> Unit,
    onConfirm: (Milestone) -> Unit
) {
    var quality by remember { mutableStateOf(MilestoneQuality.HIGH) }
    var rating by remember { mutableIntStateOf(5) }
    var description by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var actualEffort by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(colors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp)) {
                            Icon(Icons.Default.Stars, null, tint = Color.White, modifier = Modifier.padding(16.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Celebrate Success!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("You completed a milestone", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("How did you complete: ${milestone.title}?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Completion Quality", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MilestoneQuality.entries.forEach { q ->
                                val isSelected = quality == q
                                Surface(
                                    onClick = { quality = q }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color(q.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(2.dp, if (isSelected) Color(q.color) else Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(q.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your Personal Rating", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.Center) {
                            (1..5).forEach { i ->
                                val isSelected = i <= rating
                                IconButton(onClick = { rating = i }, modifier = Modifier.scale(if (isSelected) 1.2f else 1.0f)) {
                                    Icon(
                                        if (isSelected) Icons.Default.Star else Icons.Default.StarBorder, null,
                                        tint = if (isSelected) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reflection, onValueChange = { reflection = it },
                        label = { Text("Reflection (Optional)") },
                        placeholder = { Text("What did you learn? What would you do differently?") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), minLines = 2
                    )

                    OutlinedTextField(
                        value = actualEffort, onValueChange = { actualEffort = it },
                        label = { Text("Actual Effort (Optional)") },
                        placeholder = { Text("e.g., 2 weeks, 10 hours") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Additional Note (Optional)") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Any additional thoughts?") }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
                        Button(
                            onClick = {
                                onConfirm(
                                    milestone.copy(
                                        isCompleted = true, completedAt = System.currentTimeMillis(),
                                        quality = quality, rating = rating,
                                        description = description.ifBlank { null },
                                        reflection = reflection.ifBlank { null },
                                        actualEffort = actualEffort.ifBlank { null }
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Done!")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMilestoneDialog(
    milestone: Milestone? = null,
    onDismiss: () -> Unit,
    onConfirm: (Milestone) -> Unit
) {
    var title by remember { mutableStateOf(milestone?.title ?: "") }
    var targetDate by remember { mutableStateOf(milestone?.targetDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(milestone?.priority ?: GoalPriority.MEDIUM) }
    var estimatedEffort by remember { mutableStateOf(milestone?.estimatedEffort ?: "") }
    var notes by remember { mutableStateOf(milestone?.notes ?: "") }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
                        )).padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.DirectionsRun, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(if (milestone == null) "New Milestone" else "Edit Milestone", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Break down your goal into steps", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("What effort will you put?") },
                        placeholder = { Text("e.g., Read 10 pages, Complete module 1") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Priority", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoalPriority.entries.forEach { p ->
                                val isSelected = priority == p
                                Surface(
                                    onClick = { priority = p }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(p.color).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(if (isSelected) 1.5.dp else 0.dp, if (isSelected) Color(p.color) else Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(p.emoji, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = estimatedEffort, onValueChange = { estimatedEffort = it },
                        label = { Text("Estimated Effort (Optional)") },
                        placeholder = { Text("e.g., 1 week, 5 hours") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.secondary) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Target Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Surface(
                            onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), color = Color.Transparent
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    targetDate?.let { dateFormat.format(Date(it)) } ?: "Select date (optional)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (targetDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Any additional details...") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), minLines = 2,
                        leadingIcon = { Icon(Icons.Outlined.StickyNote2, null, tint = MaterialTheme.colorScheme.tertiary) }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
                        Button(
                            onClick = {
                                val newMilestone = milestone?.copy(
                                    title = title, targetDate = targetDate, priority = priority,
                                    estimatedEffort = estimatedEffort.ifBlank { null }, notes = notes.ifBlank { null }
                                ) ?: Milestone(
                                    title = title, targetDate = targetDate, priority = priority,
                                    estimatedEffort = estimatedEffort.ifBlank { null }, notes = notes.ifBlank { null }
                                )
                                onConfirm(newMilestone)
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = targetDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { targetDate = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
