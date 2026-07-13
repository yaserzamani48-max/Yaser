package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.FollowUp
import com.example.data.entities.Project
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneGold
import com.example.ui.utils.FarsiHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpsScreen(
    followUps: List<FollowUp>,
    customers: List<Customer>,
    projects: List<Project>,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تاریخچه پیگیری‌ها و مذاکرات",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        }

        // Quick Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "" to "همه",
                "Site Visit" to "بازدیدها 🏗️",
                "Phone Call" to "تماس‌ها 📞",
                "WhatsApp" to "واتس‌اپ 💬"
            )
            filters.forEach { (value, label) ->
                val isSelected = filterType == value
                FilterChip(
                    selected = isSelected,
                    onClick = { filterType = value },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        val displayedFollowUps = if (filterType.isEmpty()) followUps else followUps.filter { it.type == filterType }

        if (displayedFollowUps.isEmpty()) {
            EmptyStateCard(
                text = "هیچ گزارش پیگیری در تاریخچه ثبت نشده است.",
                icon = Icons.Default.Timeline
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedFollowUps) { followUp ->
                    val customer = customers.find { it.id == followUp.customerId }
                    val project = projects.find { it.id == followUp.projectId }
                    FollowUpTimelineItem(
                        followUp = followUp,
                        customerName = customer?.fullName ?: "نامشخص",
                        projectName = project?.projectName
                    )
                }
            }
        }
    }
}

@Composable
fun FollowUpTimelineItem(
    followUp: FollowUp,
    customerName: String,
    projectName: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("follow_up_item_${followUp.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bullet dot decoration with visual timeline vertical line
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (followUp.type) {
                            "Phone Call" -> Icons.Default.Phone
                            "WhatsApp" -> Icons.Default.Send
                            "Site Visit" -> Icons.Default.Construction
                            "Meeting" -> Icons.Default.People
                            else -> Icons.Default.Article
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FarsiHelper.mapFollowUpType(followUp.type),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = FarsiHelper.formatFarsiDate(followUp.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "مشتری: $customerName" + (projectName?.let { " • پروژه: $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = followUp.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "نتیجه: ${FarsiHelper.mapFollowUpResult(followUp.result)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (followUp.nextAction.isNotBlank() && followUp.nextFollowUpDate != null) {
                        Text(
                            text = "پیگیری بعدی: ${followUp.nextAction} (${FarsiHelper.formatFarsiDate(followUp.nextFollowUpDate)})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = StoneAccentRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// ADD FOLLOW-UP FORM SCREEN
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFollowUpScreen(
    customerId: Long,
    projectId: Long?,
    customerName: String,
    projectName: String?,
    onBackClick: () -> Unit,
    onSaveClick: (
        type: String,
        result: String,
        description: String,
        nextAction: String,
        nextFollowUpDate: Long?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf("Phone Call") }
    var result by remember { mutableStateOf("Interested") }
    var description by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }

    // Next follow-up scheduler toggle
    var setReminder by remember { mutableStateOf(false) }
    var daysAhead by remember { mutableStateOf("3") } // days in future for reminder

    var showError by remember { mutableStateOf(false) }

    // Dropdowns
    var typeExpanded by remember { mutableStateOf(false) }
    var resultExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت گزارش پیگیری و مذاکره") },
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
            // Heading Context Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مشتری: $customerName",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (projectName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "پروژه: $projectName",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text(
                text = "جزییات ارتباط برقرار شده",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            // Follow-up Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapFollowUpType(type),
                    onValueChange = {},
                    label = { Text("نوع ارتباط") },
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
                    val types = listOf("Phone Call", "WhatsApp", "Site Visit", "Meeting", "Send Catalog", "Send Price", "Send Sample", "Negotiation")
                    types.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapFollowUpType(t)) },
                            onClick = {
                                type = t
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Follow-up Result Dropdown
            ExposedDropdownMenuBox(
                expanded = resultExpanded,
                onExpandedChange = { resultExpanded = it }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = FarsiHelper.mapFollowUpResult(result),
                    onValueChange = {},
                    label = { Text("نتیجه مذاکره / وضعیت پیگیری") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resultExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = resultExpanded,
                    onDismissRequest = { resultExpanded = false }
                ) {
                    val results = listOf("No Answer", "Call Later", "Interested", "Requested Price", "Requested Sample", "Waiting Decision", "Competitor Offer", "Purchase Soon", "Cancelled")
                    results.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(FarsiHelper.mapFollowUpResult(r)) },
                            onClick = {
                                result = r
                                resultExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("خلاصه گزارش و یادداشت مذاکره (اجباری)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_followup_description"),
                maxLines = 5,
                shape = RoundedCornerShape(10.dp)
            )

            // Reminder Scheduler Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setReminder = !setReminder }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(checked = setReminder, onCheckedChange = { setReminder = it })
                Text("نیاز به پیگیری مجدد در روزهای آتی دارد؟", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }

            if (setReminder) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nextAction,
                            onValueChange = { nextAction = it },
                            label = { Text("اقدام بعدی چیست؟ (مثلاً تماس برای ارسال کاتالوگ)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_next_action"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Simple days in future dropdown
                        var daysExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = daysExpanded,
                            onExpandedChange = { daysExpanded = it }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = "پس از $daysAhead روز کاری",
                                onValueChange = {},
                                label = { Text("زمان پیگیری مجدد") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = daysExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = daysExpanded,
                                onDismissRequest = { daysExpanded = false }
                            ) {
                                listOf("1" to "فردا", "3" to "۳ روز دیگر", "7" to "۱ هفته دیگر", "14" to "۲ هفته دیگر", "30" to "۱ ماه دیگر").forEach { (days, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            daysAhead = days
                                            daysExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showError) {
                Text(
                    text = "لطفا متن خلاصه گزارش را وارد کنید.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        val nextFollowUpMillis = if (setReminder) {
                            val days = daysAhead.toLongOrNull() ?: 3L
                            System.currentTimeMillis() + (86400000L * days)
                        } else null

                        onSaveClick(type, result, description, nextAction, nextFollowUpMillis)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_followup_submit"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ثبت و ذخیره گزارش پیگیری", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}
