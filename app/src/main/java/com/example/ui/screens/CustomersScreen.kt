package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.Project
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneGold
import com.example.ui.utils.FarsiHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    customers: List<Customer>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedTempFilter: String,
    onTempFilterChange: (String) -> Unit,
    onCustomerSelect: (Long) -> Unit,
    onAddCustomerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Add Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text("جستجوی مشتری، شرکت، تلفن، شهر...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("customer_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("add_customer_fab"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }

        // Temperature filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val temps = listOf("" to "همه", "Hot" to "داغ 🔥", "Warm" to "گرم ☀️", "Cold" to "سرد ❄️")
            temps.forEach { (value, label) ->
                val isSelected = selectedTempFilter == value
                FilterChip(
                    selected = isSelected,
                    onClick = { onTempFilterChange(value) },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_temp_$value")
                )
            }
        }

        // Customer List
        if (customers.isEmpty()) {
            EmptyStateCard(
                text = "هیچ مشتری یافت نشد. می‌توانید با دکمه + یک مشتری جدید ثبت کنید.",
                icon = Icons.Default.PersonOutline
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(customers) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerSelect(customer.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    val tempColor = when (customer.temperature) {
        "Hot" -> Color(0xFFEF4444) // Rose-500
        "Warm" -> Color(0xFFF59E0B) // Amber-500
        else -> Color(0xFF64748B) // Slate-500
    }

    val tempBg = when (customer.temperature) {
        "Hot" -> Color(0xFFFEF2F2) // Rose-50
        "Warm" -> Color(0xFFFEF3C7) // Amber-50
        else -> Color(0xFFF8FAFC) // Slate-50
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)), // border-slate-100
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Avatar, Name & Job, and Temperature Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Initials Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.fullName.take(1),
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = customer.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                        if (customer.companyName.isNotBlank()) {
                            Text(
                                text = customer.companyName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Temp Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tempBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = FarsiHelper.mapTemperature(customer.temperature),
                        color = tempColor,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Middle info row: Phone & Area Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Phone Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = customer.mobileNumber,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Location Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${customer.city}، ${customer.area}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom row: Job & Priority Class Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Job type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEFF6FF)) // blue-50
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = FarsiHelper.mapJobType(customer.jobType),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB) // blue-600
                        )
                    )
                }

                // Priority Class
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9)) // slate-100
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "کلاس ${customer.priority}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF475569) // slate-600
                        )
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// CUSTOMER DETAIL SCREEN
// --------------------------------------------------
@Composable
fun CustomerDetailScreen(
    customer: Customer,
    projects: List<Project>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onAddProjectClick: () -> Unit,
    onAddFollowUpClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف مشتری") },
            text = { Text("آیا از حذف اطلاعات مشتری ${customer.fullName} و کلیه پرونده‌های مربوطه مطمئن هستید؟ این عمل غیرقابل بازگشت است.") },
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
                title = { Text("مشخصات مشتری") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_customer_button")) {
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
            // Profile Card
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
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.fullName.take(1),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Column {
                                Text(
                                    text = customer.fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = customer.companyName.ifBlank { "شخص حقیقی" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider()

                        DetailRow(label = "سمت / شغل", value = FarsiHelper.mapJobType(customer.jobType), icon = Icons.Default.Work)
                        DetailRow(label = "تلفن همراه", value = customer.mobileNumber, icon = Icons.Default.Phone, isCopyable = true)
                        if (customer.secondPhone.isNotBlank()) {
                            DetailRow(label = "تلفن دوم", value = customer.secondPhone, icon = Icons.Default.Call)
                        }
                        if (customer.whatsAppNumber.isNotBlank()) {
                            DetailRow(label = "شماره واتس‌اپ", value = customer.whatsAppNumber, icon = Icons.Default.Send)
                        }
                        DetailRow(label = "شهر / منطقه", value = "${customer.city}، ${customer.area}", icon = Icons.Default.LocationOn)
                        DetailRow(label = "آدرس پستی", value = customer.address, icon = Icons.Default.Map)
                        DetailRow(label = "منبع جذب مشتری", value = FarsiHelper.mapLeadSource(customer.leadSource), icon = Icons.Default.Share)
                        DetailRow(label = "اولویت طبقه", value = FarsiHelper.mapPriority(customer.priority), icon = Icons.Default.Star)
                        DetailRow(label = "وضعیت دمای لید", value = FarsiHelper.mapTemperature(customer.temperature), icon = Icons.Default.Whatshot)

                        if (customer.generalNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "یادداشت‌های عمومی:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = customer.generalNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Actions Block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddProjectClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_project_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddHome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت پروژه جدید", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onAddFollowUpClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_followup_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت پیگیری جدید", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Associated Projects list header
            item {
                SectionHeader(title = "پروژه‌های عمرانی این مشتری (${projects.size})", color = MaterialTheme.colorScheme.primary)
            }

            if (projects.isEmpty()) {
                item {
                    EmptyStateCard(text = "هیچ پروژه‌ای برای این مشتری ثبت نشده است.", icon = Icons.Default.HomeWork)
                }
            } else {
                items(projects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProjectClick(project.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.projectName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "نوع پروژه: ${FarsiHelper.mapProjectType(project.projectType)} • وضعیت: ${FarsiHelper.mapProjectStatus(project.projectStatus)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector, isCopyable: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --------------------------------------------------
// CUSTOMER ADD / EDIT FORM SCREEN
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    customer: Customer?,
    onBackClick: () -> Unit,
    onSaveClick: (
        fullName: String,
        companyName: String,
        mobileNumber: String,
        secondPhone: String,
        whatsAppNumber: String,
        jobType: String,
        city: String,
        area: String,
        address: String,
        leadSource: String,
        priority: String,
        temperature: String,
        generalNotes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf(customer?.fullName ?: "") }
    var companyName by remember { mutableStateOf(customer?.companyName ?: "") }
    var mobileNumber by remember { mutableStateOf(customer?.mobileNumber ?: "") }
    var secondPhone by remember { mutableStateOf(customer?.secondPhone ?: "") }
    var whatsAppNumber by remember { mutableStateOf(customer?.whatsAppNumber ?: "") }
    var jobType by remember { mutableStateOf(customer?.jobType ?: "Builder") }
    var city by remember { mutableStateOf(customer?.city ?: "") }
    var area by remember { mutableStateOf(customer?.area ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var leadSource by remember { mutableStateOf(customer?.leadSource ?: "Visit") }
    var priority by remember { mutableStateOf(customer?.priority ?: "B") }
    var temperature by remember { mutableStateOf(customer?.temperature ?: "Warm") }
    var generalNotes by remember { mutableStateOf(customer?.generalNotes ?: "") }

    var showError by remember { mutableStateOf(false) }

    // Dropdown States
    var jobTypeExpanded by remember { mutableStateOf(false) }
    var leadSourceExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var tempExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customer == null) "ثبت مشتری جدید" else "ویرایش مشتری") },
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
            // General info title
            Text(
                text = "اطلاعات هویتی و شغلی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("نام و نام خانوادگی (اجباری)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_fullName"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("نام شرکت ساختمانی / مجموعه") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Job Type Dropdown
            ExposedDropdownMenuBox(
                expanded = jobTypeExpanded,
                onExpandedChange = { jobTypeExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapJobType(jobType),
                    onValueChange = {},
                    label = { Text("نوع فعالیت") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = jobTypeExpanded,
                    onDismissRequest = { jobTypeExpanded = false }
                ) {
                    val jobs = listOf("Builder", "Architect", "Contractor", "Employer", "Developer", "Real Estate Company")
                    jobs.forEach { job ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapJobType(job)) },
                            onClick = {
                                jobType = job
                                jobTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "اطلاعات تماس لید",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("تلفن همراه (اجباری)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_mobileNumber"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = secondPhone,
                    onValueChange = { secondPhone = it },
                    label = { Text("تلفن دوم") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = whatsAppNumber,
                    onValueChange = { whatsAppNumber = it },
                    label = { Text("واتس‌اپ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Text(
                text = "اطلاعات جغرافیایی پروژه",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("شهر (اجباری)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_city"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("منطقه (اجباری)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_area"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس کامل دفتر / پروژه") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Text(
                text = "رده‌بندی فروش و بازاریابی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            // Lead Source Dropdown
            ExposedDropdownMenuBox(
                expanded = leadSourceExpanded,
                onExpandedChange = { leadSourceExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapLeadSource(leadSource),
                    onValueChange = {},
                    label = { Text("منبع جذب مشتری") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leadSourceExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = leadSourceExpanded,
                    onDismissRequest = { leadSourceExpanded = false }
                ) {
                    val sources = listOf("Visit", "Referral", "Exhibition", "Instagram", "Website", "Cold Call")
                    sources.forEach { src ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapLeadSource(src)) },
                            onClick = {
                                leadSource = src
                                leadSourceExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Priority Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = "کلاس " + priority,
                            onValueChange = {},
                            label = { Text("اولویت") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            listOf("A", "B", "C").forEach { pri ->
                                DropdownMenuItem(
                                    text = { Text("کلاس $pri") },
                                    onClick = {
                                        priority = pri
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Temp Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = tempExpanded,
                        onExpandedChange = { tempExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = FarsiHelper.mapTemperature(temperature),
                            onValueChange = {},
                            label = { Text("دمای مشتری") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tempExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = tempExpanded,
                            onDismissRequest = { tempExpanded = false }
                        ) {
                            val temps = listOf("Hot", "Warm", "Cold")
                            temps.forEach { temp ->
                                DropdownMenuItem(
                                    text = { Text(FarsiHelper.mapTemperature(temp)) },
                                    onClick = {
                                        temperature = temp
                                        tempExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = generalNotes,
                onValueChange = { generalNotes = it },
                label = { Text("یادداشت عمومی و توضیحات تکمیلی") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                shape = RoundedCornerShape(10.dp)
            )

            if (showError) {
                Text(
                    text = "لطفا فیلدهای اجباری (نام، شماره همراه، شهر و منطقه) را پر کنید.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isNotBlank() && mobileNumber.isNotBlank() && city.isNotBlank() && area.isNotBlank()) {
                        onSaveClick(
                            fullName, companyName, mobileNumber, secondPhone, whatsAppNumber,
                            jobType, city, area, address, leadSource, priority, temperature, generalNotes
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_customer_submit"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره اطلاعات مشتری", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}
