package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Customer
import com.example.data.entities.Project
import com.example.data.entities.User
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.components.InteractiveStoneMap
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StoneAccentRed
import com.example.ui.theme.StoneAccentGreen
import com.example.ui.theme.StoneGold
import com.example.ui.utils.FarsiHelper
import com.example.ui.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

sealed class AppScreen {
    object Dashboard : AppScreen()
    object Customers : AppScreen()
    object Projects : AppScreen()
    object Map : AppScreen()
    object FollowUps : AppScreen()

    // Nested detailed overlays
    data class CustomerDetail(val id: Long) : AppScreen()
    data class CustomerForm(val id: Long?) : AppScreen()
    data class ProjectDetail(val id: Long) : AppScreen()
    data class ProjectForm(val id: Long?, val customerId: Long) : AppScreen()
    data class AddFollowUp(val customerId: Long, val projectId: Long?) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Register permission launcher safely
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Toast.makeText(this, "مجوز موقعیت‌یابی دقیق فعال شد", Toast.LENGTH_SHORT).show()
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Toast.makeText(this, "مجوز موقعیت‌یابی تقریبی فعال شد", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "مجوز موقعیت‌یابی داده نشد. از مکان پیش‌فرض استفاده می‌شود", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val viewModel: MainViewModel by viewModels()

            // State observers
            val user by viewModel.userState.collectAsState()
            val customers by viewModel.customersState.collectAsState()
            val projects by viewModel.projectsState.collectAsState()
            val followUps by viewModel.followUpsState.collectAsState()
            val reminders by viewModel.remindersState.collectAsState()

            // Local App UI States
            var currentTab by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
            var detailStack by remember { mutableStateOf<List<AppScreen>>(emptyList()) }

            // Search query flows
            val searchQuery by viewModel.searchQuery.collectAsState()
            val tempFilter by viewModel.tempFilter.collectAsState()
            val statusFilter by viewModel.statusFilter.collectAsState()
            val cityFilter by viewModel.cityFilter.collectAsState()

            // Local filters reactive lists
            val filteredCustomers = remember(customers, searchQuery, tempFilter, cityFilter) {
                customers.filter { customer ->
                    val matchesQuery = searchQuery.isEmpty() ||
                            customer.fullName.contains(searchQuery, ignoreCase = true) ||
                            customer.mobileNumber.contains(searchQuery) ||
                            customer.companyName.contains(searchQuery, ignoreCase = true) ||
                            customer.city.contains(searchQuery, ignoreCase = true)

                    val matchesTemp = tempFilter.isEmpty() || customer.temperature.equals(tempFilter, ignoreCase = true)
                    val matchesCity = cityFilter.isEmpty() || customer.city.contains(cityFilter, ignoreCase = true)

                    matchesQuery && matchesTemp && matchesCity
                }
            }

            val filteredProjects = remember(projects, customers, searchQuery, statusFilter, cityFilter) {
                projects.filter { project ->
                    val customer = customers.find { it.id == project.customerId }
                    val customerName = customer?.fullName ?: ""
                    val customerPhone = customer?.mobileNumber ?: ""
                    val customerCity = customer?.city ?: ""

                    val matchesQuery = searchQuery.isEmpty() ||
                            project.projectName.contains(searchQuery, ignoreCase = true) ||
                            customerName.contains(searchQuery, ignoreCase = true) ||
                            customerPhone.contains(searchQuery) ||
                            customerCity.contains(searchQuery, ignoreCase = true)

                    val matchesStatus = statusFilter.isEmpty() || project.projectStatus.equals(statusFilter, ignoreCase = true)
                    val matchesCity = cityFilter.isEmpty() || customerCity.contains(cityFilter, ignoreCase = true)

                    matchesQuery && matchesStatus && matchesCity
                }
            }

            // Trigger data seed when user is logged in
            LaunchedEffect(user) {
                user?.let {
                    viewModel.seedDummyDataIfEmpty()
                }
            }

            // Standard navigation stack handler (Back pops stack or shifts tab)
            BackHandler(enabled = detailStack.isNotEmpty() || currentTab != AppScreen.Dashboard) {
                if (detailStack.isNotEmpty()) {
                    detailStack = detailStack.dropLast(1)
                } else {
                    currentTab = AppScreen.Dashboard
                }
            }

            MyApplicationTheme {
                // FORCE PERSian RTL Support for entire CRM layout!
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (user == null) {
                            OnboardingScreen(
                                onSignUp = { name, email, phone ->
                                    viewModel.signUp(name, email, phone)
                                }
                            )
                        } else {
                            Scaffold(
                                topBar = {
                                    if (detailStack.isEmpty()) {
                                        CrmTopAppBar(
                                            user = user,
                                            onLogoutClick = { viewModel.logout() }
                                        )
                                    }
                                },
                                bottomBar = {
                                    if (detailStack.isEmpty()) {
                                        CrmBottomNavigation(
                                            selectedTab = currentTab,
                                            onTabSelected = {
                                                currentTab = it
                                                // Reset local queries
                                                viewModel.searchQuery.value = ""
                                                viewModel.tempFilter.value = ""
                                                viewModel.statusFilter.value = ""
                                                viewModel.cityFilter.value = ""
                                            }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    if (detailStack.isNotEmpty()) {
                                        // Render Detail Screens
                                        when (val screen = detailStack.last()) {
                                            is AppScreen.CustomerDetail -> {
                                                val customer = customers.find { it.id == screen.id }
                                                if (customer != null) {
                                                    val associatedProjects = projects.filter { it.customerId == customer.id }
                                                    CustomerDetailScreen(
                                                        customer = customer,
                                                        projects = associatedProjects,
                                                        onBackClick = { detailStack = detailStack.dropLast(1) },
                                                        onEditClick = { detailStack = detailStack + AppScreen.CustomerForm(customer.id) },
                                                        onAddProjectClick = { detailStack = detailStack + AppScreen.ProjectForm(null, customer.id) },
                                                        onAddFollowUpClick = { detailStack = detailStack + AppScreen.AddFollowUp(customer.id, null) },
                                                        onProjectClick = { detailStack = detailStack + AppScreen.ProjectDetail(it) },
                                                        onDeleteClick = {
                                                            viewModel.deleteCustomer(customer)
                                                            detailStack = detailStack.dropLast(1)
                                                        }
                                                    )
                                                }
                                            }
                                            is AppScreen.CustomerForm -> {
                                                val customer = screen.id?.let { sid -> customers.find { it.id == sid } }
                                                CustomerFormScreen(
                                                    customer = customer,
                                                    onBackClick = { detailStack = detailStack.dropLast(1) },
                                                    onSaveClick = { name, comp, mob, sec, wa, job, city, area, addr, source, pri, temp, notes ->
                                                        viewModel.addOrUpdateCustomer(
                                                            id = customer?.id ?: 0,
                                                            fullName = name,
                                                            companyName = comp,
                                                            mobileNumber = mob,
                                                            secondPhone = sec,
                                                            whatsAppNumber = wa,
                                                            jobType = job,
                                                            city = city,
                                                            area = area,
                                                            address = addr,
                                                            leadSource = source,
                                                            priority = pri,
                                                            temperature = temp,
                                                            generalNotes = notes,
                                                            onComplete = {
                                                                detailStack = detailStack.dropLast(1)
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                            is AppScreen.ProjectDetail -> {
                                                val project = projects.find { it.id == screen.id }
                                                val customer = project?.let { p -> customers.find { it.id == p.customerId } }
                                                if (project != null && customer != null) {
                                                    ProjectDetailScreen(
                                                        project = project,
                                                        customer = customer,
                                                        onBackClick = { detailStack = detailStack.dropLast(1) },
                                                        onEditClick = { detailStack = detailStack + AppScreen.ProjectForm(project.id, customer.id) },
                                                        onAddFollowUpClick = { detailStack = detailStack + AppScreen.AddFollowUp(customer.id, project.id) },
                                                        onDeleteClick = {
                                                            viewModel.deleteProject(project)
                                                            detailStack = detailStack.dropLast(1)
                                                        }
                                                    )
                                                }
                                            }
                                            is AppScreen.ProjectForm -> {
                                                val project = screen.id?.let { pid -> projects.find { it.id == pid } }
                                                ProjectFormScreen(
                                                    project = project,
                                                    customerId = screen.customerId,
                                                    customers = customers,
                                                    onBackClick = { detailStack = detailStack.dropLast(1) },
                                                    onGetCurrentLocation = { callback ->
                                                        fetchCurrentLocation(callback)
                                                    },
                                                    onSaveClick = { name, type, status, timing, area, floors, usage, pref, desc, lat, lng ->
                                                        viewModel.addOrUpdateProject(
                                                            id = project?.id ?: 0,
                                                            customerId = screen.customerId,
                                                            projectName = name,
                                                            projectType = type,
                                                            projectStatus = status,
                                                            estimatedPurchaseTime = timing,
                                                            projectArea = area,
                                                            numberOfFloors = floors,
                                                            requiredStoneUsage = usage,
                                                            preferredStone = pref,
                                                            projectDescription = desc,
                                                            latitude = lat,
                                                            longitude = lng,
                                                            onComplete = {
                                                                detailStack = detailStack.dropLast(1)
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                            is AppScreen.AddFollowUp -> {
                                                val customer = customers.find { it.id == screen.customerId }
                                                val project = screen.projectId?.let { pid -> projects.find { it.id == pid } }
                                                if (customer != null) {
                                                    AddFollowUpScreen(
                                                        customerId = screen.customerId,
                                                        projectId = screen.projectId,
                                                        customerName = customer.fullName,
                                                        projectName = project?.projectName,
                                                        onBackClick = { detailStack = detailStack.dropLast(1) },
                                                        onSaveClick = { type, result, desc, next, nextDate ->
                                                            viewModel.addFollowUp(
                                                                customerId = screen.customerId,
                                                                projectId = screen.projectId,
                                                                type = type,
                                                                result = result,
                                                                description = desc,
                                                                nextAction = next,
                                                                nextFollowUpDate = nextDate,
                                                                onComplete = {
                                                                    detailStack = detailStack.dropLast(1)
                                                                }
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                            else -> {}
                                        }
                                    } else {
                                        // Render Root Tabs
                                        when (currentTab) {
                                            is AppScreen.Dashboard -> {
                                                user?.let { u ->
                                                    DashboardScreen(
                                                        user = u,
                                                        customers = customers,
                                                        projects = projects,
                                                        reminders = reminders,
                                                        onToggleReminder = { id, done ->
                                                            viewModel.toggleReminderCompleted(id, done)
                                                        },
                                                        onNavigateToCustomers = { currentTab = AppScreen.Customers },
                                                        onNavigateToProjects = { currentTab = AppScreen.Projects },
                                                        onLogout = { viewModel.logout() },
                                                        onAddVisitClick = { detailStack = listOf(AppScreen.CustomerForm(null)) }
                                                    )
                                                }
                                            }
                                            is AppScreen.Customers -> {
                                                CustomersScreen(
                                                    customers = filteredCustomers,
                                                    searchQuery = searchQuery,
                                                    onSearchChange = { viewModel.searchQuery.value = it },
                                                    selectedTempFilter = tempFilter,
                                                    onTempFilterChange = { viewModel.tempFilter.value = it },
                                                    onCustomerSelect = { detailStack = listOf(AppScreen.CustomerDetail(it)) },
                                                    onAddCustomerClick = { detailStack = listOf(AppScreen.CustomerForm(null)) }
                                                )
                                            }
                                            is AppScreen.Projects -> {
                                                ProjectsScreen(
                                                    projects = filteredProjects,
                                                    customers = customers,
                                                    searchQuery = searchQuery,
                                                    onSearchChange = { viewModel.searchQuery.value = it },
                                                    selectedStatusFilter = statusFilter,
                                                    onStatusFilterChange = { viewModel.statusFilter.value = it },
                                                    onProjectSelect = { detailStack = listOf(AppScreen.ProjectDetail(it)) }
                                                )
                                            }
                                            is AppScreen.Map -> {
                                                MapTabScreen(
                                                    projects = projects,
                                                    customers = customers,
                                                    onProjectClick = { detailStack = listOf(AppScreen.ProjectDetail(it)) }
                                                )
                                            }
                                            is AppScreen.FollowUps -> {
                                                FollowUpsScreen(
                                                    followUps = followUps,
                                                    customers = customers,
                                                    projects = projects
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchCurrentLocation(onLocationFetched: (Double, Double) -> Unit) {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Permission request trigger
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            // Fallback immediately to a central coordination node in Tehran (with minor random offset)
            val baseLat = 35.6892
            val baseLng = 51.3890
            onLocationFetched(baseLat, baseLng)
            Toast.makeText(this, "لوکیشن فرضی ثبت شد (مجوز دسترسی موقعیت داده نشده است)", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationFetched(location.latitude, location.longitude)
                    Toast.makeText(this, "موقعیت دقیق GPS با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                } else {
                    // Try to generate a close local stone quarry / tehran point as a responsive fallback
                    val randomOffsetLat = (Math.random() - 0.5) * 0.05
                    val randomOffsetLng = (Math.random() - 0.5) * 0.05
                    onLocationFetched(35.6892 + randomOffsetLat, 51.3890 + randomOffsetLng)
                    Toast.makeText(this, "موقعیت مکانی تقریبی بازاریاب ثبت شد", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                onLocationFetched(35.6892, 51.3890)
                Toast.makeText(this, "خطا در برقراری ارتباط با سنسور GPS. لوکیشن فرضی ثبت شد", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun CrmTopAppBar(
    user: User?,
    onLogoutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Side (in RTL, this is rightmost): Logo & App Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo squircle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // Title and Subtitle
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "کاریز استون",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.2).sp
                        ),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "CRM مدیریت فروش",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        lineHeight = 12.sp
                    )
                }
            }

            // Left Side: Notification & Profile actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bell button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            // Non-blocking interaction feedback
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔔", fontSize = 18.sp)
                }

                // Profile / Logout Trigger
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { showMenu = true }
                            .testTag("top_bar_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName?.take(1) ?: "U"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("جناب ${user?.fullName ?: "کاربر"}") },
                            onClick = {},
                            enabled = false
                        )
                        Divider(color = Color(0xFFE2E8F0))
                        DropdownMenuItem(
                            text = { Text("خروج از حساب", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            modifier = Modifier.testTag("top_bar_logout_option")
                        )
                    }
                }
            }
        }
        Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
    }
}

@Composable
fun CrmBottomNavigation(
    selectedTab: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("crm_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedTab == AppScreen.Dashboard,
            onClick = { onTabSelected(AppScreen.Dashboard) },
            label = { Text("داشبورد", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = selectedTab == AppScreen.Customers,
            onClick = { onTabSelected(AppScreen.Customers) },
            label = { Text("مشتریان", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
            icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
            modifier = Modifier.testTag("nav_customers")
        )

        NavigationBarItem(
            selected = selectedTab == AppScreen.Projects,
            onClick = { onTabSelected(AppScreen.Projects) },
            label = { Text("پروژه‌ها", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
            icon = { Icon(Icons.Default.LocationCity, contentDescription = "Projects") },
            modifier = Modifier.testTag("nav_projects")
        )

        NavigationBarItem(
            selected = selectedTab == AppScreen.Map,
            onClick = { onTabSelected(AppScreen.Map) },
            label = { Text("نقشه پروژه‌ها", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            modifier = Modifier.testTag("nav_map")
        )

        NavigationBarItem(
            selected = selectedTab == AppScreen.FollowUps,
            onClick = { onTabSelected(AppScreen.FollowUps) },
            label = { Text("پیگیری‌ها", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
            icon = { Icon(Icons.Default.Timeline, contentDescription = "Followups") },
            modifier = Modifier.testTag("nav_followups")
        )
    }
}

// --------------------------------------------------
// MAP TAB SCREEN WITH FILTERS AND ACTION POPUP
// --------------------------------------------------
@Composable
fun MapTabScreen(
    projects: List<Project>,
    customers: List<Customer>,
    onProjectClick: (Long) -> Unit
) {
    var filterHotOnly by remember { mutableStateOf(false) }
    var filterNearPurchaseOnly by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }

    val filteredProjects = projects.filter { project ->
        val customer = customers.find { it.id == project.customerId }
        val matchesHot = !filterHotOnly || customer?.temperature == "Hot"
        val matchesPurchase = !filterNearPurchaseOnly || project.projectStatus == "Near Stone Purchase"
        matchesHot && matchesPurchase
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen stylized interactive map
        InteractiveStoneMap(
            projects = filteredProjects,
            customers = customers,
            onProjectSelect = { selectedProject = it },
            modifier = Modifier.fillMaxSize()
        )

        // Filters Overlay Panel at the top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "فیلتر مکان‌یابی پروژه‌ها روی نقشه",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { filterHotOnly = !filterHotOnly }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = filterHotOnly, onCheckedChange = { filterHotOnly = it })
                        Text("مشتریان داغ 🔥", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { filterNearPurchaseOnly = !filterNearPurchaseOnly }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = filterNearPurchaseOnly, onCheckedChange = { filterNearPurchaseOnly = it })
                        Text("نزدیک به خرید سنگ ⏳", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Details Overlay Card at the Bottom when a pin is selected
        selectedProject?.let { project ->
            val customer = customers.find { it.id == project.customerId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                    .testTag("map_marker_popup"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = project.projectName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "کارفرما: ${customer?.fullName ?: "نامشخص"} • ${customer?.mobileNumber ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = FarsiHelper.mapProjectStatus(project.projectStatus),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "زمان خرید: ${FarsiHelper.mapPurchaseTime(project.estimatedPurchaseTime)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { selectedProject = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { onProjectClick(project.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("مشاهده جزئیات", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
