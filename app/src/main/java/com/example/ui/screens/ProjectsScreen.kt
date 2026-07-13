package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.Project
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneAccentGreen
import com.example.ui.theme.StoneGold
import com.example.ui.utils.FarsiHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projects: List<Project>,
    customers: List<Customer>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onProjectSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("جستجوی پروژه، مشتری، تلفن، شهر...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("project_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Status filters (scrollable horizontal chips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "" to "همه",
                "Selecting Stone" to "در حال انتخاب سنگ 💎",
                "Near Stone Purchase" to "نزدیک خرید ⏳",
                "Purchased" to "برنده شده ✅"
            )
            filters.forEach { (value, label) ->
                val isSelected = selectedStatusFilter == value
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusFilterChange(value) },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Projects list
        if (projects.isEmpty()) {
            EmptyStateCard(
                text = "هیچ پروژه عمرانی یافت نشد. به بخش مشتریان بروید و برای مشتری مورد نظر پروژه ثبت کنید.",
                icon = Icons.Default.HomeWork
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(projects) { project ->
                    val customer = customers.find { it.id == project.customerId }
                    ProjectCard(
                        project = project,
                        customerName = customer?.fullName ?: "نامشخص",
                        customerPhone = customer?.mobileNumber ?: "",
                        onClick = { onProjectSelect(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    customerName: String,
    customerPhone: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Status tag
                val tagColor = when (project.projectStatus) {
                    "Purchased" -> StoneAccentGreen
                    "Near Stone Purchase", "Selecting Stone" -> StoneAccentRed
                    else -> StoneGold
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = FarsiHelper.mapProjectStatus(project.projectStatus),
                        color = tagColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(
                        text = "کارفرما: $customerName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(
                        text = "خرید: ${FarsiHelper.mapPurchaseTime(project.estimatedPurchaseTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Specs badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = FarsiHelper.mapProjectType(project.projectType),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "سنگ پیشنهادی: ${FarsiHelper.mapPreferredStone(project.preferredStone)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// PROJECT DETAIL SCREEN
// --------------------------------------------------
@Composable
fun ProjectDetailScreen(
    project: Project,
    customer: Customer,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onAddFollowUpClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف پروژه") },
            text = { Text("آیا از حذف پروژه ${project.projectName} مطمئن هستید؟ این عمل غیرقابل بازگشت است.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("بله، حذف شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("جزییات پروژه عمرانی") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_project_button")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color.White)
                            }

                            Column {
                                Text(
                                    text = project.projectName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "مالک: ${customer.fullName} • ${customer.companyName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider()

                        DetailRow(label = "نوع سازه", value = FarsiHelper.mapProjectType(project.projectType), icon = Icons.Default.Category)
                        DetailRow(label = "وضعیت پیشرفت فیزیکی", value = FarsiHelper.mapProjectStatus(project.projectStatus), icon = Icons.Default.TrendingUp)
                        DetailRow(label = "تخمین زمان خرید سنگ", value = FarsiHelper.mapPurchaseTime(project.estimatedPurchaseTime), icon = Icons.Default.AccessTime)
                        DetailRow(label = "متراژ پروژه", value = "${project.projectArea} متر مربع", icon = Icons.Default.AspectRatio)
                        DetailRow(label = "تعداد طبقات", value = "${project.numberOfFloors} طبقه", icon = Icons.Default.Layers)
                        DetailRow(label = "سنگ طبیعی مورد علاقه", value = FarsiHelper.mapPreferredStone(project.preferredStone), icon = Icons.Default.Diamond)
                        DetailRow(label = "محل مصارف سنگ در پروژه", value = FarsiHelper.mapStoneUsage(project.requiredStoneUsage), icon = Icons.Default.CheckCircleOutline)

                        // GPS Location coordinates
                        if (project.latitude != null && project.longitude != null) {
                            DetailRow(
                                label = "موقعیت جغرافیایی GPS پروژه",
                                value = "عرض جغرافیایی: ${project.latitude} • طول جغرافیایی: ${project.longitude}",
                                icon = Icons.Default.LocationOn
                            )
                        } else {
                            DetailRow(label = "موقعیت جغرافیایی", value = "ثبت نشده است. ویرایش پروژه را بزنید تا موقعیت فعلی را ثبت کنید.", icon = Icons.Default.LocationOff)
                        }

                        if (project.projectDescription.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "توضیحات و نیازمندی‌ها پروژه:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = project.projectDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick actions
            item {
                Button(
                    onClick = onAddFollowUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("project_add_followup_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت پیگیری و نتیجه مذاکره برای این پروژه")
                }
            }
        }
    }
}

// --------------------------------------------------
// PROJECT ADD / EDIT FORM SCREEN
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    project: Project?,
    customerId: Long,
    customers: List<Customer>,
    onBackClick: () -> Unit,
    onGetCurrentLocation: (onLocationFetched: (Double, Double) -> Unit) -> Unit,
    onSaveClick: (
        projectName: String,
        projectType: String,
        projectStatus: String,
        estimatedPurchaseTime: String,
        projectArea: Double,
        numberOfFloors: Int,
        requiredStoneUsage: String,
        preferredStone: String,
        projectDescription: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectName by remember { mutableStateOf(project?.projectName ?: "") }
    var projectType by remember { mutableStateOf(project?.projectType ?: "Apartment") }
    var projectStatus by remember { mutableStateOf(project?.projectStatus ?: "Selecting Stone") }
    var estimatedPurchaseTime by remember { mutableStateOf(project?.estimatedPurchaseTime ?: "1 Month") }
    var projectAreaStr by remember { mutableStateOf(project?.projectArea?.toString() ?: "") }
    var numberOfFloorsStr by remember { mutableStateOf(project?.numberOfFloors?.toString() ?: "") }
    var preferredStone by remember { mutableStateOf(project?.preferredStone ?: "Travertine") }
    var projectDescription by remember { mutableStateOf(project?.projectDescription ?: "") }

    // GPS coordinates
    var latitude by remember { mutableStateOf(project?.latitude) }
    var longitude by remember { mutableStateOf(project?.longitude) }

    // Multi-select Checkbox state for Stone Usage
    val usages = listOf("Exterior Facade", "Interior Floor", "Stair", "Lobby", "Bathroom", "Wall", "Slab")
    val selectedUsages = remember {
        mutableStateListOf<String>().apply {
            project?.requiredStoneUsage?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let {
                addAll(it)
            }
        }
    }

    var showError by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }

    // Dropdowns
    var typeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var timingExpanded by remember { mutableStateOf(false) }
    var stoneExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (project == null) "ثبت پروژه جدید" else "ویرایش پروژه") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "اطلاعات پروژه عمرانی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("نام پروژه عمرانی (اجباری)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_projectName"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Project Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapProjectType(projectType),
                    onValueChange = {},
                    label = { Text("نوع ساختمان") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    val types = listOf("Villa", "Apartment", "Tower", "Commercial Building", "Hotel", "Lobby", "Office")
                    types.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapProjectType(t)) },
                            onClick = {
                                projectType = t
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = projectAreaStr,
                    onValueChange = { projectAreaStr = it },
                    label = { Text("متراژ بنا (مترمربع)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = numberOfFloorsStr,
                    onValueChange = { numberOfFloorsStr = it },
                    label = { Text("تعداد طبقات") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Text(
                text = "وضعیت پیشرفت و فاز خرید سنگ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapProjectStatus(projectStatus),
                    onValueChange = {},
                    label = { Text("وضعیت پروژه") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    val statuses = listOf("Land Preparation", "Structure", "Brick Work", "Finishing", "Facade Preparation", "Selecting Stone", "Near Stone Purchase", "Purchased", "Lost", "Stopped")
                    statuses.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapProjectStatus(s)) },
                            onClick = {
                                projectStatus = s
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Timing Dropdown
            ExposedDropdownMenuBox(
                expanded = timingExpanded,
                onExpandedChange = { timingExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapPurchaseTime(estimatedPurchaseTime),
                    onValueChange = {},
                    label = { Text("تخمین زمان خرید نهایی سنگ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timingExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = timingExpanded,
                    onDismissRequest = { timingExpanded = false }
                ) {
                    val timings = listOf("This Week", "2 Weeks", "1 Month", "2 Months", "Long Term", "Unknown")
                    timings.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapPurchaseTime(t)) },
                            onClick = {
                                estimatedPurchaseTime = t
                                timingExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "نیازمندی‌ها و مصارف سنگ پروژه",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            // Preferred Stone Dropdown
            ExposedDropdownMenuBox(
                expanded = stoneExpanded,
                onExpandedChange = { stoneExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapPreferredStone(preferredStone),
                    onValueChange = {},
                    label = { Text("نوع سنگ ترجیحی کارفرما") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stoneExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = stoneExpanded,
                    onDismissRequest = { stoneExpanded = false }
                ) {
                    val stones = listOf("Travertine", "Marble", "Limestone", "Onyx", "Crystal", "Granite")
                    stones.forEach { st ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapPreferredStone(st)) },
                            onClick = {
                                preferredStone = st
                                stoneExpanded = false
                            }
                        )
                    }
                }
            }

            // Checklist of Stone Usage
            Text(text = "محل استفاده سنگ در پروژه:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            Column(modifier = Modifier.fillMaxWidth()) {
                usages.forEach { usage ->
                    val isChecked = selectedUsages.contains(usage)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedUsages.remove(usage) else selectedUsages.add(usage)
                            }
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) selectedUsages.add(usage) else selectedUsages.remove(usage)
                            }
                        )
                        Text(FarsiHelper.mapStoneUsage(usage), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // GPS LOCATION TRIGGER SECTION
            Text(
                text = "موقعیت جغرافیایی پروژه (GPS)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "با زدن دکمه زیر، موقعیت مکانی فعلی شما به عنوان لوکیشن دقیق پروژه ساختمانی ثبت خواهد شد تا بازاریاب در دفعات بعدی بتواند پروژه را روی نقشه مکان‌یابی کند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            isLocating = true
                            onGetCurrentLocation { lat, lng ->
                                latitude = lat
                                longitude = lng
                                isLocating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_current_location_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ثبت موقعیت مکانی پروژه با GPS گوشی")
                        }
                    }

                    if (latitude != null && longitude != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طول جغرافیایی (Lng): ${String.format("%.6f", longitude)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "عرض جغرافیایی (Lat): ${String.format("%.6f", latitude)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        Text(
                            text = "موقعیتی ثبت نشده است.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = projectDescription,
                onValueChange = { projectDescription = it },
                label = { Text("توضیحات سنگ‌ها و شرایط پروژه") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                shape = RoundedCornerShape(10.dp)
            )

            if (showError) {
                Text(
                    text = "لطفا نام پروژه را وارد کنید.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        val area = projectAreaStr.toDoubleOrNull() ?: 0.0
                        val floors = numberOfFloorsStr.toIntOrNull() ?: 0
                        val stoneUsage = selectedUsages.joinToString(", ")
                        onSaveClick(
                            projectName, projectType, projectStatus, estimatedPurchaseTime,
                            area, floors, stoneUsage, preferredStone, projectDescription, latitude, longitude
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_project_submit"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره مشخصات پروژه", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}
