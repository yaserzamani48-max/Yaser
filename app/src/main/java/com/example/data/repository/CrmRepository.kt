package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

class CrmRepository(
    private val userDao: UserDao,
    private val customerDao: CustomerDao,
    private val projectDao: ProjectDao,
    private val followUpDao: FollowUpDao,
    private val reminderDao: ReminderDao
) {
    // Flow getters
    val user: Flow<User?> = userDao.getUser()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val allFollowUps: Flow<List<FollowUp>> = followUpDao.getAllFollowUps()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val pendingReminders: Flow<List<Reminder>> = reminderDao.getPendingReminders()

    // User Operations
    suspend fun saveUser(user: User) {
        userDao.clearUsers()
        userDao.insertUser(user)
    }

    suspend fun logoutUser() {
        userDao.clearUsers()
    }

    // Customer Operations
    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)
    suspend fun saveCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Project Operations
    fun getProjectsForCustomer(customerId: Long): Flow<List<Project>> = projectDao.getProjectsForCustomer(customerId)
    fun getProjectById(id: Long): Flow<Project?> = projectDao.getProjectById(id)
    suspend fun saveProject(project: Project): Long = projectDao.insertProject(project)
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    // FollowUp Operations
    fun getFollowUpsForCustomer(customerId: Long): Flow<List<FollowUp>> = followUpDao.getFollowUpsForCustomer(customerId)
    fun getFollowUpsForProject(projectId: Long): Flow<List<FollowUp>> = followUpDao.getFollowUpsForProject(projectId)
    fun getFollowUpById(id: Long): Flow<FollowUp?> = followUpDao.getFollowUpById(id)
    suspend fun saveFollowUp(followUp: FollowUp): Long = followUpDao.insertFollowUp(followUp)
    suspend fun deleteFollowUp(followUp: FollowUp) = followUpDao.deleteFollowUp(followUp)

    // Reminder Operations
    suspend fun saveReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminderStatus(id: Long, isCompleted: Boolean) = reminderDao.updateReminderStatus(id, isCompleted)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)
}
