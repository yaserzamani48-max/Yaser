package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.Project
import com.example.data.entities.Reminder
import com.example.data.entities.User
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneAccentGreen
import com.example.ui.theme.StoneGold
import com.example.ui.utils.FarsiHelper

@Composable
fun DashboardScreen(
    user: User,
    customers: List<Customer>,
    projects: List<Project>,
    reminders: List<Reminder>,
    onToggleReminder: (Long, Boolean) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onLogout: () -> Unit,
    onAddVisitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalCustomers = customers.size
    val totalProjects = projects.size
    val hotLeads = customers.count { it.temperature == "Hot" }
    val nearPurchase = projects.count { it.projectStatus == "Near Stone Purchase" }

    val todayMillis = System.currentTimeMillis()
    val todayStartMillis = getStartOfTodayMillis()

    // Calculate dynamic trends
    val customersAddedToday = customers.count { it.createdAt >= todayStartMillis }
    val projectsAddedToday = projects.count { it.createdAt >= todayStartMillis }

    // Group reminders
    val todayReminders = reminders.filter {
        isSameDay(it.dueDate, todayMillis)
    }
    val overdueReminders = reminders.filter {
        !it.isCompleted && it.dueDate < todayMillis && !isSameDay(it.dueDate, todayMillis)
    }
    val upcomingReminders = reminders.filter {
        !it.isCompleted && it.dueDate > todayMillis && !isSameDay(it.dueDate, todayMillis)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcoming Greeting & Compact Status
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "برنامه کاری امروز شما",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A), // Slate-900
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "جناب ${user.fullName} • آخرین به‌روزرسانی سیستم",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B) // Slate-500
                    )
                )
            }
        }

        // Stats Grid (Matching Tailwind grid-cols-2 precisely)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "مشتریان",
                        value = totalCustomers.toString(),
                        emoji = "👥",
                        color = Color(0xFF3B82F6), // Blue
                        subtext = if (customersAddedToday > 0) "+$customersAddedToday مورد جدید امروز" else "پیگیری منظم پرونده‌ها",
                        subtextColor = if (customersAddedToday > 0) Color(0xFF059669) else Color(0xFF64748B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCustomers() }
                    )
                    StatCard(
                        title = "پروژه‌ها",
                        value = totalProjects.toString(),
                        emoji = "🏗️",
                        color = Color(0xFF2563EB), // Primary Blue
                        subtext = if (projectsAddedToday > 0) "+$projectsAddedToday پروژه جدید امروز" else "ثبت لوکیشن کارگاه‌ها",
                        subtextColor = if (projectsAddedToday > 0) Color(0xFF059669) else Color(0xFF64748B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProjects() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "فرصت داغ",
                        value = String.format("%02d", hotLeads),
                        emoji = "🔥",
                        color = Color(0xFFF43F5E), // Rose-500
                        subtext = "آماده عقد قرارداد فروش",
                        subtextColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "نزدیک خرید",
                        value = String.format("%02d", nearPurchase),
                        emoji = "⏳",
                        color = Color(0xFFD97706), // Amber-600
                        subtext = "پیگیری استعلام قیمت",
                        subtextColor = Color(0xFF475569),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Button: Register visit and new location (Custom Polish Theme button)
        item {
            Button(
                onClick = onAddVisitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("dashboard_add_visit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)), // Slate-900
                shape = RoundedCornerShape(16.dp), // rounded-2xl
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("📍", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ثبت بازدید و موقعیت جدید",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Overdue reminders (warning banner format if any exist)
        if (overdueReminders.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), // Rose-50
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)), // Rose-200
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Text(
                                text = "پیگیری‌های معوقه و عقب‌افتاده",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9F1239) // Rose-800
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            overdueReminders.forEach { reminder ->
                                ReminderRowItem(
                                    reminder = reminder,
                                    onToggleComplete = { onToggleReminder(reminder.id, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Tasks Section (Framer Card rounded-[32px])
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_reminders_section_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp), // Match rounded-[32px] soft frame
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)), // border-slate-100
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header inside Section Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "پیگیری‌های امروز",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )

                        // Badge count
                        val pendingCount = todayReminders.count { !it.isCompleted }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$pendingCount مانده",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    if (todayReminders.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 36.sp)
                            Text(
                                text = "هیچ برنامه پیگیری برای امروز ثبت نشده است.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF64748B)
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            todayReminders.forEach { reminder ->
                                ReminderRowItem(
                                    reminder = reminder,
                                    onToggleComplete = { onToggleReminder(reminder.id, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Followups Section
        if (upcomingReminders.isNotEmpty()) {
            item {
                SectionHeader(title = "برنامه‌های پیگیری آتی", color = StoneGold)
            }
            items(upcomingReminders.take(5)) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ReminderRowItem(
                        reminder = reminder,
                        onToggleComplete = { onToggleReminder(reminder.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    emoji: String,
    color: Color,
    subtext: String,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )

            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = subtextColor,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReminderRowItem(
    reminder: Reminder,
    onToggleComplete: (Boolean) -> Unit
) {
    // Opacity lowers for completed reminders
    val opacity = if (reminder.isCompleted) 0.6f else 1.0f

    // Dynamic categorizations
    val isCall = reminder.title.contains("تماس") || reminder.title.contains("تلفن") || reminder.description.contains("کاتالوگ") || reminder.title.contains("📞")
    val isMeeting = reminder.title.contains("جلسه") || reminder.title.contains("حضوری") || reminder.title.contains("ملاقات") || reminder.title.contains("🤝")
    
    val emoji = when {
        isCall -> "📞"
        isMeeting -> "🤝"
        else -> "💬"
    }

    val badgeText = when {
        reminder.isCompleted -> "انجام شد"
        isCall -> "۱۰:۳۰" // High-fidelity placeholder clocks matching HTML
        isMeeting -> "۱۴:۰۰"
        else -> "پیگیری"
    }

    val badgeBg = when {
        reminder.isCompleted -> Color(0xFFECFDF5) // Emerald-50
        isCall -> Color(0xFFEFF6FF) // Blue-50
        isMeeting -> Color(0xFFFEF3C7) // Amber-50
        else -> Color(0xFFF1F5F9) // Slate-100
    }

    val badgeColor = when {
        reminder.isCompleted -> Color(0xFF059669) // Emerald-600
        isCall -> Color(0xFF2563EB) // Blue-600
        isMeeting -> Color(0xFFD97706) // Amber-600
        else -> Color(0xFF475569) // Slate-600
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .clickable { onToggleComplete(!reminder.isCompleted) }
            .padding(12.dp)
            .testTag("reminder_item_${reminder.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Icon Box (White squircle with shadow)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }

        // Center Details Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp)
        ) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textDecoration = if (reminder.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right side: Badge or clock indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        )
    }
}

@Composable
fun EmptyStateCard(text: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
    return fmt.format(java.util.Date(t1)) == fmt.format(java.util.Date(t2))
}

private fun getStartOfTodayMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
