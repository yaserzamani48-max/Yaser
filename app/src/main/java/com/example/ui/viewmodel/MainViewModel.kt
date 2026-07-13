package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.*
import com.example.data.repository.CrmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CrmRepository

    // Base flows from DB
    val userState: StateFlow<User?>
    val customersState: StateFlow<List<Customer>>
    val projectsState: StateFlow<List<Project>>
    val followUpsState: StateFlow<List<FollowUp>>
    val remindersState: StateFlow<List<Reminder>>
    val pendingRemindersState: StateFlow<List<Reminder>>

    // Search and filter states
    val searchQuery = MutableStateFlow("")
    val cityFilter = MutableStateFlow("")
    val tempFilter = MutableStateFlow("")
    val statusFilter = MutableStateFlow("")
    val timingFilter = MutableStateFlow("")

    // Selected items for navigation / details
    val selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedProjectId = MutableStateFlow<Long?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CrmRepository(
            userDao = database.userDao(),
            customerDao = database.customerDao(),
            projectDao = database.projectDao(),
            followUpDao = database.followUpDao(),
            reminderDao = database.reminderDao()
        )

        userState = repository.user.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        customersState = repository.allCustomers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        projectsState = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        followUpsState = repository.allFollowUps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        remindersState = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pendingRemindersState = repository.pendingReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // User actions
    fun signUp(fullName: String, email: String, phoneNumber: String) {
        viewModelScope.launch {
            repository.saveUser(User(fullName = fullName, email = email, phoneNumber = phoneNumber))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }

    // Customer actions
    fun addOrUpdateCustomer(
        id: Long = 0,
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
        generalNotes: String,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val customer = Customer(
                id = id,
                fullName = fullName,
                companyName = companyName,
                mobileNumber = mobileNumber,
                secondPhone = secondPhone,
                whatsAppNumber = whatsAppNumber,
                jobType = jobType,
                city = city,
                area = area,
                address = address,
                leadSource = leadSource,
                priority = priority,
                temperature = temperature,
                generalNotes = generalNotes,
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.saveCustomer(customer)
            onComplete(newId)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Project actions
    fun addOrUpdateProject(
        id: Long = 0,
        customerId: Long,
        projectName: String,
        projectType: String,
        projectStatus: String,
        estimatedPurchaseTime: String,
        projectArea: Double,
        numberOfFloors: Int,
        requiredStoneUsage: String,
        preferredStone: String,
        projectDescription: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val project = Project(
                id = id,
                customerId = customerId,
                projectName = projectName,
                projectType = projectType,
                projectStatus = projectStatus,
                estimatedPurchaseTime = estimatedPurchaseTime,
                projectArea = projectArea,
                numberOfFloors = numberOfFloors,
                requiredStoneUsage = requiredStoneUsage,
                preferredStone = preferredStone,
                projectDescription = projectDescription,
                latitude = latitude,
                longitude = longitude,
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.saveProject(project)
            onComplete(newId)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // FollowUp actions
    fun addFollowUp(
        customerId: Long,
        projectId: Long?,
        type: String,
        result: String,
        description: String,
        nextAction: String,
        nextFollowUpDate: Long?,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val followUp = FollowUp(
                customerId = customerId,
                projectId = projectId,
                date = System.currentTimeMillis(),
                type = type,
                result = result,
                description = description,
                nextAction = nextAction,
                nextFollowUpDate = nextFollowUpDate
            )
            val newId = repository.saveFollowUp(followUp)

            // Automatically create a Reminder if a next follow-up date is specified
            if (nextFollowUpDate != null && nextFollowUpDate > System.currentTimeMillis()) {
                val customer = customersState.value.find { it.id == customerId }
                val project = projectId?.let { pid -> projectsState.value.find { it.id == pid } }
                val title = if (project != null) {
                    "پیگیری با ${customer?.fullName ?: ""} در مورد پروژه ${project.projectName}"
                } else {
                    "پیگیری با ${customer?.fullName ?: ""}"
                }
                repository.saveReminder(
                    Reminder(
                        followUpId = newId,
                        title = title,
                        description = "نوع پیگیری بعدی: $nextAction\nتوضیحات: $description",
                        dueDate = nextFollowUpDate
                    )
                )
            }
            onComplete(newId)
        }
    }

    fun deleteFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            repository.deleteFollowUp(followUp)
        }
    }

    // Reminder actions
    fun toggleReminderCompleted(reminderId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(reminderId, isCompleted)
        }
    }

    fun addManualReminder(title: String, description: String, dueDate: Long) {
        viewModelScope.launch {
            repository.saveReminder(
                Reminder(
                    followUpId = null,
                    title = title,
                    description = description,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // Insert dummy seed data for initial testing if empty
    fun seedDummyDataIfEmpty() {
        if (customersState.value.isNotEmpty()) return
        viewModelScope.launch {
            // Seed a customer
            val c1 = repository.saveCustomer(
                Customer(
                    fullName = "مهندس علیرضا رضایی",
                    companyName = "گروه ساختمانی پارس",
                    mobileNumber = "09121111111",
                    secondPhone = "02122222222",
                    whatsAppNumber = "09121111111",
                    jobType = "سازنده",
                    city = "تهران",
                    area = "نیاوران",
                    address = "خیابان نیاوران، پلاک ۴۵، طبقه ۳",
                    leadSource = "بازدید حضوری",
                    priority = "A",
                    temperature = "داغ",
                    generalNotes = "بسیار مایل به خرید تراورتن کرم حاجی‌آباد برای نمای کلاسیک."
                )
            )

            val c2 = repository.saveCustomer(
                Customer(
                    fullName = "خانم مهندس سارا کریمی",
                    companyName = "دفتر معماری افق",
                    mobileNumber = "09123333333",
                    secondPhone = "02188888888",
                    whatsAppNumber = "09123333333",
                    jobType = "آرشیتکت",
                    city = "اصفهان",
                    area = "مرداویج",
                    address = "خیابان ملاصدرا، مجتمع کاوه",
                    leadSource = "اینستاگرام",
                    priority = "B",
                    temperature = "گرم",
                    generalNotes = "طراح پروژه‌های لوکس مدرن. علاقه‌مند به اسلب‌های مرمریت تیره و لایمستون."
                )
            )

            // Seed projects
            val p1 = repository.saveProject(
                Project(
                    customerId = c1,
                    projectName = "ویلای مدرن نیاوران",
                    projectType = "ویلا",
                    projectStatus = "در حال انتخاب سنگ",
                    estimatedPurchaseTime = "۲ هفته آینده",
                    projectArea = 1200.0,
                    numberOfFloors = 4,
                    requiredStoneUsage = "نمای خارجی, کف داخلی, لابی",
                    preferredStone = "تراورتن",
                    projectDescription = "ویلای تریپلکس لاکچری با نمای رومی کلاسیک سنگین. نیاز به متراژ بالا سنگ تراورتن عباس‌آباد یا حاجی‌آباد.",
                    latitude = 35.8116,
                    longitude = 51.4693
                )
            )

            val p2 = repository.saveProject(
                Project(
                    customerId = c2,
                    projectName = "آپارتمان مسکونی هشت‌بهشت",
                    projectType = "آپارتمان",
                    projectStatus = "سفت‌کاری",
                    estimatedPurchaseTime = "۲ ماه آینده",
                    projectArea = 2500.0,
                    numberOfFloors = 6,
                    requiredStoneUsage = "کف داخلی, پله, لابی",
                    preferredStone = "مرمریت",
                    projectDescription = "یک مجتمع ۶ طبقه لوکس. کف سالن‌ها مرمریت کرم دهبید سایز ۸۰*۸۰ مورد نیاز است.",
                    latitude = 32.6546,
                    longitude = 51.6680
                )
            )

            // Seed follow-ups
            val f1 = repository.saveFollowUp(
                FollowUp(
                    customerId = c1,
                    projectId = p1,
                    date = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                    type = "بازدید حضوری",
                    result = "درخواست قیمت",
                    description = "نمونه تراورتن کرم حاجی آباد و عباس آباد را روی پروژه به مهندس رضایی نشان دادم. بسیار خوششان آمد و درخواست قیمت تمام شده سنگ متری و فرآوری شده را داشتند.",
                    nextAction = "ارسال لیست قیمت",
                    nextFollowUpDate = System.currentTimeMillis() + 86400000 // tomorrow
                )
            )

            repository.saveReminder(
                Reminder(
                    followUpId = f1,
                    title = "ارسال لیست قیمت تراورتن برای مهندس رضایی - ویلای نیاوران",
                    description = "لیست قیمت تراورتن کرم درجه ممتاز عباس آباد فرآوری شده برای نمای خارجی ارسال شود.",
                    dueDate = System.currentTimeMillis() + 86400000 // tomorrow
                )
            )

            repository.saveFollowUp(
                FollowUp(
                    customerId = c2,
                    projectId = p2,
                    date = System.currentTimeMillis() - 86400000 * 5, // 5 days ago
                    type = "تماس تلفنی",
                    result = "علاقه‌مند",
                    description = "با خانم مهندس کریمی تماس گرفتم. گفتند پروژه هنوز در مرحله سفت‌کاری است اما کاتالوگ اسلب‌های مرمریت ما را دیده‌اند و برای لابی حتماً مایلند اسلب بوک‌مچ انتخاب کنند.",
                    nextAction = "ارسال کاتالوگ دیجیتال نمونه کارها",
                    nextFollowUpDate = System.currentTimeMillis() + 86400000 * 4 // in 4 days
                )
            )
        }
    }
}
