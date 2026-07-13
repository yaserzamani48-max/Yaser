package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY fullName ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE customerId = :customerId ORDER BY updatedAt DESC")
    fun getProjectsForCustomer(customerId: Long): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups ORDER BY date DESC")
    fun getAllFollowUps(): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE customerId = :customerId ORDER BY date DESC")
    fun getFollowUpsForCustomer(customerId: Long): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE projectId = :projectId ORDER BY date DESC")
    fun getFollowUpsForProject(projectId: Long): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE id = :id LIMIT 1")
    fun getFollowUpById(id: Long): Flow<FollowUp?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("UPDATE reminders SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
