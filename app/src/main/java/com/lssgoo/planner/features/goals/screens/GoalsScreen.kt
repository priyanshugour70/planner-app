package com.lssgoo.planner.features.goals.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lssgoo.planner.data.model.GoalCategory
import com.lssgoo.planner.features.goals.components.*
import com.lssgoo.planner.features.goals.models.GoalPriority
import com.lssgoo.planner.features.goals.models.GoalStatus
import com.lssgoo.planner.ui.components.AppIcons
import com.lssgoo.planner.ui.components.getIcon
import com.lssgoo.planner.ui.viewmodel.GoalsViewModel

enum class ViewMode { LIST, GRID }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    onGoalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()
    var selectedCategory by remember { mutableStateOf<GoalCategory?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var sortByDate by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<GoalStatus?>(null) }
    var showInfoTip by remember { mutableStateOf(false) }

    val filteredGoals = remember(goals, selectedCategory, sortByDate, searchQuery, selectedStatus) {
        var result = goals.toList()

        if (selectedCategory != null) {
            result = result.filter { it.category == selectedCategory }
        }
        if (selectedStatus != null) {
            result = result.filter { it.status == selectedStatus }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        (it.tags?.lowercase()?.contains(q) == true)
            }
        }
        if (sortByDate) {
            result = result.sortedBy { it.targetDate ?: Long.MAX_VALUE }
        }

        result.sortedByDescending { it.isPinned }
    }

    val overallProgress = if (goals.isNotEmpty()) goals.sumOf { it.progress.toDouble() }.toFloat() / goals.size else 0f
    val favoriteCount = goals.count { it.isFavorite }
    val completedCount = goals.count { it.status == GoalStatus.COMPLETED }
    val inProgressCount = goals.count { it.status == GoalStatus.IN_PROGRESS }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcons.Goal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Goals",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (goals.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${goals.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { showInfoTip = !showInfoTip }) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "Tips",
                                    tint = if (showInfoTip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showSearch = !showSearch }) {
                                Icon(
                                    if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (showSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST }) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                    contentDescription = "Toggle View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { sortByDate = !sortByDate }) {
                                Icon(
                                    imageVector = if (sortByDate) Icons.Default.SortByAlpha else Icons.Default.CalendarToday,
                                    contentDescription = "Sort",
                                    tint = if (sortByDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search goals...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Goal", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (viewMode == ViewMode.LIST) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    AnimatedVisibility(visible = showInfoTip) {
                        GoalInfoTipCard(
                            onDismiss = { showInfoTip = false },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    CategoryCarousel(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    StatusFilterRow(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it },
                        totalCount = goals.size,
                        inProgressCount = inProgressCount,
                        completedCount = completedCount,
                        favoriteCount = favoriteCount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OverallProgressCard(
                        progress = overallProgress,
                        totalGoals = goals.size,
                        completedGoals = completedCount,
                        inProgressGoals = inProgressCount,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(filteredGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(goal.id) },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                                placementSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (filteredGoals.isEmpty()) {
                    item {
                        EmptyGoalsState(
                            hasFilters = selectedCategory != null || selectedStatus != null || searchQuery.isNotBlank(),
                            onClearFilters = {
                                selectedCategory = null
                                selectedStatus = null
                                searchQuery = ""
                            },
                            modifier = Modifier.padding(48.dp)
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredGoals, key = { it.id }) { goal ->
                    GoalGridCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(goal.id) }
                    )
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddEditGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = {
                viewModel.addGoal(it)
                showAddGoalDialog = false
            }
        )
    }
}
