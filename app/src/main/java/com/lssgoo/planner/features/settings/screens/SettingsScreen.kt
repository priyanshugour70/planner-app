package com.lssgoo.planner.features.settings.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.lssgoo.planner.data.model.Goal
import com.lssgoo.planner.data.model.GoalCategory
import com.lssgoo.planner.features.settings.models.ThemeMode
import com.lssgoo.planner.features.settings.models.DashboardStats
import com.lssgoo.planner.features.settings.models.AppSettings
import com.lssgoo.planner.features.settings.models.UserProfile
import com.lssgoo.planner.features.settings.models.Gender
import com.lssgoo.planner.ui.theme.*
import com.lssgoo.planner.ui.components.AppIcons
import com.lssgoo.planner.ui.viewmodel.PlannerViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import com.lssgoo.planner.ui.components.dialogs.QuickConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PlannerViewModel,
    onBack: () -> Unit,
    onNavigateToPin: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonData = reader.readText()
                    val success = viewModel.importData(jsonData)
                    if (success) {
                        showImportSuccessDialog = true
                    } else {
                        importError = "Invalid backup file format"
                    }
                }
            } catch (e: Exception) {
                importError = "Failed to read backup file: ${e.message}"
            }
        }
    }
    
    // Share launcher for export
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        AppIcons.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile card
            item {
                var showEditProfileDialog by remember { mutableStateOf(false) }
                
                ProfileCard(
                    userProfile = userProfile,
                    stats = stats,
                    onEditClick = { showEditProfileDialog = true }
                )
                
                // Edit Profile Dialog
                if (showEditProfileDialog) {
                    EditProfileDialog(
                        userProfile = userProfile,
                        onDismiss = { showEditProfileDialog = false },
                        onSave = { updatedProfile ->
                            viewModel.updateUserProfile(updatedProfile)
                            showEditProfileDialog = false
                        }
                    )
                }
            }
            
            // Appearance Section
            item {
                var showThemeDialog by remember { mutableStateOf(false) }
                
                SettingsSection(
                    title = "Appearance",
                    icon = Icons.Outlined.Palette
                ) {
                    SettingsItem(
                        icon = if (settings.themeMode == ThemeMode.DARK) Icons.Filled.DarkMode 
                               else if (settings.themeMode == ThemeMode.LIGHT) Icons.Filled.LightMode
                               else Icons.Filled.BrightnessAuto,
                        title = "Theme",
                        subtitle = when (settings.themeMode) {
                            ThemeMode.LIGHT -> "Classic Light"
                            ThemeMode.DARK -> "Classic Dark"
                            ThemeMode.SYSTEM -> "System Default"
                            ThemeMode.OCEAN -> "Deep Ocean"
                            ThemeMode.SUNSET -> "Sunset Glow"
                            ThemeMode.FOREST -> "Forest Green"
                            ThemeMode.MIDNIGHT -> "Midnight Purple"
                            ThemeMode.ROSE_GOLD -> "Rose Gold ✨"
                            ThemeMode.NORD -> "Nord Arctic"
                            ThemeMode.SOLARIZED -> "Solarized"
                            ThemeMode.LAVENDER -> "Lavender Dream"
                            ThemeMode.MOCHA -> "Mocha Latte"
                        },
                        onClick = { showThemeDialog = true },
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Security Section

                
                // Theme Selection Dialog
                if (showThemeDialog) {
                    Dialog(
                        onDismissRequest = { showThemeDialog = false },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .heightIn(max = 620.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            Column {
                                // Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        )
                                        .padding(24.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.2f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Palette,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                "Theme & Vibe",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                "Personalize your experience",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        SectionTitle("System")
                                        ThemeOption(
                                            icon = Icons.Filled.BrightnessAuto,
                                            title = "System Default",
                                            isSelected = settings.themeMode == ThemeMode.SYSTEM,
                                            colorPreview = MaterialTheme.colorScheme.outline,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.SYSTEM))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    
                                    item { SectionTitle("Classic") }
                                    item {
                                        ThemeOption(
                                            icon = Icons.Filled.LightMode,
                                            title = "Classic Light",
                                            isSelected = settings.themeMode == ThemeMode.LIGHT,
                                            colorPreview = Color.White,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.LIGHT))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    item {
                                        ThemeOption(
                                            icon = Icons.Filled.DarkMode,
                                            title = "Classic Dark",
                                            isSelected = settings.themeMode == ThemeMode.DARK,
                                            colorPreview = Color.Black,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.DARK))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    
                                    item { SectionTitle("Nature") }
                                    item {
                                        ThemeOption(
                                            icon = Icons.Filled.WaterDrop,
                                            title = "Deep Ocean",
                                            isSelected = settings.themeMode == ThemeMode.OCEAN,
                                            colorPreview = ThemePreviewColors.ocean,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.OCEAN))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    item {
                                        ThemeOption(
                                            icon = Icons.Filled.WbSunny,
                                            title = "Sunset Glow",
                                            isSelected = settings.themeMode == ThemeMode.SUNSET,
                                            colorPreview = ThemePreviewColors.sunset,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.SUNSET))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    item {
                                        ThemeOption(
                                            icon = Icons.Filled.Park,
                                            title = "Forest Green",
                                            isSelected = settings.themeMode == ThemeMode.FOREST,
                                            colorPreview = ThemePreviewColors.forest,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = ThemeMode.FOREST))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                    
                                    item { SectionTitle("Premium ✨") }
                                    val premiumThemes = listOf(
                                        Triple(ThemeMode.MIDNIGHT, "Midnight Purple", ThemePreviewColors.midnight),
                                        Triple(ThemeMode.ROSE_GOLD, "Rose Gold", ThemePreviewColors.roseGold),
                                        Triple(ThemeMode.NORD, "Nord Arctic", ThemePreviewColors.nord),
                                        Triple(ThemeMode.SOLARIZED, "Solarized", ThemePreviewColors.solarized),
                                        Triple(ThemeMode.LAVENDER, "Lavender Dream", ThemePreviewColors.lavender),
                                        Triple(ThemeMode.MOCHA, "Mocha Latte", ThemePreviewColors.mocha)
                                    )
                                    
                                    items(premiumThemes) { (mode, title, color) ->
                                        ThemeOption(
                                            icon = Icons.Filled.AutoAwesome,
                                            title = title,
                                            isSelected = settings.themeMode == mode,
                                            colorPreview = color,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(themeMode = mode))
                                                showThemeDialog = false
                                            }
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = { showThemeDialog = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
            
            // Security Section
            item {
                SettingsSection(
                    title = "Security",
                    icon = Icons.Outlined.Lock
                ) {
                     SettingsItem(
                        icon = Icons.Filled.Lock,
                        title = "App Lock / PIN",
                        subtitle = if (settings.pinCode.isNullOrEmpty()) "Setup a PIN to protect your data" else "PIN is enabled",
                        onClick = onNavigateToPin,
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Account Section
            item {
                SettingsSection(
                    title = "Account",
                    icon = Icons.Filled.AccountCircle
                ) {
                    SettingsItem(
                        icon = Icons.Filled.Logout,
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        onClick = {
                            viewModel.logout()
                        },
                        iconColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Data Management Section
            item {
                SettingsSection(
                    title = "Data Management",
                    icon = AppIcons.SettingsBackupRestore
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.CloudUpload,
                        title = "Export Backup",
                        subtitle = "Save your data to a file",
                        onClick = {
                            val uri = viewModel.exportDataToFile(context)
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                shareLauncher.launch(Intent.createChooser(shareIntent, "Save Backup"))
                                showExportSuccessDialog = true
                            }
                        },
                        iconColor = GoalColors.career
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.CloudDownload,
                        title = "Import Backup",
                        subtitle = "Restore data from a backup file",
                        onClick = {
                            importLauncher.launch("application/json")
                        },
                        iconColor = GoalColors.health
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Clear All Data",
                        subtitle = "Delete all data and start fresh",
                        onClick = { showClearDataDialog = true },
                        iconColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // App Info Section
            item {
                SettingsSection(
                    title = "About",
                    icon = AppIcons.Info
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        subtitle = "2.0.0 (Click to see history)",
                        onClick = { onNavigate(com.lssgoo.planner.ui.navigation.Routes.VERSION_HISTORY) },
                        iconColor = GoalColors.learning
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Code,
                        title = "Developer",
                        subtitle = "Built with ❤️ by LSSGOO",
                        onClick = { onNavigate(com.lssgoo.planner.ui.navigation.Routes.ABOUT_DEVELOPER) },
                        iconColor = GoalColors.startup
                    )
                }
            }
            
            // Legal Section
            item {
                SettingsSection(
                    title = "Legal",
                    icon = Icons.Outlined.Gavel
                ) {
                    SettingsItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = { onNavigate(com.lssgoo.planner.ui.navigation.Routes.PRIVACY_POLICY) },
                        iconColor = GoalColors.health
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Gavel,
                        title = "Terms of Service",
                        subtitle = "App usage agreements",
                        onClick = { onNavigate(com.lssgoo.planner.ui.navigation.Routes.TERMS_OF_SERVICE) },
                        iconColor = GoalColors.career
                    )
                }
            }
            
            // Quick Stats
            item {
                SettingsSection(
                    title = "Statistics",
                    icon = AppIcons.Assessment
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow("Total Goals", stats.totalGoals.toString())
                        StatRow("Milestones Completed", "${stats.completedMilestones}/${stats.totalMilestones}")
                        StatRow("Tasks Completed Today", "${stats.tasksCompletedToday}/${stats.totalTasksToday}")
                        StatRow("Current Streak", "${stats.currentStreak} days")
                        StatRow("Longest Streak", "${stats.longestStreak} days")
                        StatRow("Overall Progress", "${(stats.overallProgress * 100).toInt()}%")
                    }
                }
            }
            
            // Tips Section
            item {
                TipsCard()
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Clear Data Dialog
    if (showClearDataDialog) {
        QuickConfirmDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            title = "Clear All Data?",
            message = "This will delete all your goals progress, notes, tasks, and calendar events. This action cannot be undone. Consider exporting a backup first.",
            isDestructive = true,
            confirmText = "Clear Permanently"
        )
    }
    
    // Export Success Dialog
    if (showExportSuccessDialog) {
        QuickConfirmDialog(
            onDismiss = { showExportSuccessDialog = false },
            onConfirm = { showExportSuccessDialog = false },
            title = "Backup Ready!",
            message = "Your backup file is ready to be saved or shared. Keep it safe to restore your data later!",
            confirmText = "Got it"
        )
    }
    
    // Import Success Dialog
    if (showImportSuccessDialog) {
        QuickConfirmDialog(
            onDismiss = { showImportSuccessDialog = false },
            onConfirm = { showImportSuccessDialog = false },
            title = "Data Restored!",
            message = "Your data has been successfully restored from the backup file.",
            confirmText = "Awesome"
        )
    }
    
    // Import Error Dialog
    if (importError != null) {
        QuickConfirmDialog(
            onDismiss = { importError = null },
            onConfirm = { importError = null },
            title = "Import Failed",
            message = importError!!,
            isDestructive = true,
            confirmText = "OK"
        )
    }
}

@Composable
fun ProfileCard(
    userProfile: com.lssgoo.planner.data.model.UserProfile?,
    stats: DashboardStats,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userName = userProfile?.firstName ?: "Goal Achiever"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with Outer Ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.first().uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = userProfile?.occupation?.uppercase() ?: "VISIONARY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Detailed Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(
                        value = "${(stats.overallProgress * 100).toInt()}%",
                        label = "Goal Progress",
                        icon = Icons.Default.TrendingUp
                    )
                    VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    ProfileStat(
                        value = "${stats.currentStreak}",
                        label = "Day Streak",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    ProfileStat(
                        value = "${stats.completedMilestones}",
                        label = "Victories",
                        icon = Icons.Default.EmojiEvents
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStat(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        trailing?.invoke() ?: Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ThemeOption(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    colorPreview: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Theme Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and Preview
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorPreview)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSelected) "Active" else "Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isSelected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    userProfile: com.lssgoo.planner.data.model.UserProfile?,
    onDismiss: () -> Unit,
    onSave: (com.lssgoo.planner.data.model.UserProfile) -> Unit
) {
    var firstName by remember { mutableStateOf(userProfile?.firstName ?: "") }
    var lastName by remember { mutableStateOf(userProfile?.lastName ?: "") }
    var email by remember { mutableStateOf(userProfile?.email ?: "") }
    var phoneNumber by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var occupation by remember { mutableStateOf(userProfile?.occupation ?: "") }
    var selectedGender by remember { mutableStateOf(userProfile?.gender ?: com.lssgoo.planner.data.model.Gender.PREFER_NOT_TO_SAY) }
    
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    val dateOfBirthDisplay = remember(userProfile?.dateOfBirth) {
        userProfile?.dateOfBirth?.let { dateFormat.format(java.util.Date(it)) } ?: "Not set"
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(userProfile?.dateOfBirth) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column {
                // Header with gradient and avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (firstName.isNotBlank()) {
                                Text(
                                    "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Edit Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Update your personal information",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("your@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 9876543210") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    // Occupation
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation") },
                        placeholder = { Text("Software Developer, Student, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Date of Birth
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline
                        ),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Date of Birth",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        dateOfBirthDisplay,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Gender Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Gender",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            com.lssgoo.planner.data.model.Gender.entries.forEach { gender ->
                                val isSelected = selectedGender == gender
                                Surface(
                                    onClick = { selectedGender = gender },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(min = 90.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = gender.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Action Buttons
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val updatedProfile = (userProfile ?: com.lssgoo.planner.data.model.UserProfile()).copy(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phoneNumber = phoneNumber,
                                occupation = occupation,
                                gender = selectedGender,
                                dateOfBirth = selectedDate,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(updatedProfile)
                        },
                        enabled = firstName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
    
    // Date Picker
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        // If selectedDate is already set, use it
        selectedDate?.let { calendar.timeInMillis = it }
        
        android.app.DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDate = selectedCalendar.timeInMillis
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
        showDatePicker = false
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
fun TipsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    AppIcons.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pro Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "Regularly backup your data to avoid losing progress",
                "Share backup file to your email for safe storage",
                "Save backup to Google Drive or other cloud storage",
                "Before changing phones, export your data first"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        AppIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
