package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val companyName: String,
    val mobileNumber: String,
    val secondPhone: String,
    val whatsAppNumber: String,
    val jobType: String, // Builder, Architect, Contractor, Employer, Developer, Real Estate Company
    val city: String,
    val area: String,
    val address: String,
    val leadSource: String, // Visit, Referral, Exhibition, Instagram, Website, Cold Call
    val priority: String, // A, B, C
    val temperature: String, // Hot, Warm, Cold
    val generalNotes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val projectName: String,
    val projectType: String, // Villa, Apartment, Tower, Commercial Building, Hotel, Lobby, Office
    val projectStatus: String, // Land Preparation, Structure, Brick Work, Finishing, Facade Preparation, Selecting Stone, Near Stone Purchase, Purchased, Lost, Stopped
    val estimatedPurchaseTime: String, // This Week, 2 Weeks, 1 Month, 2 Months, Long Term, Unknown
    val projectArea: Double,
    val numberOfFloors: Int,
    val requiredStoneUsage: String, // Comma-separated list (e.g. "Exterior Facade, Interior Floor")
    val preferredStone: String, // Travertine, Marble, Limestone, Onyx, Crystal, Granite
    val projectDescription: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follow_ups")
data class FollowUp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val projectId: Long?, // nullable if general customer follow-up
    val date: Long,
    val type: String, // Phone Call, WhatsApp, Site Visit, Meeting, Send Catalog, Send Price, Send Sample, Negotiation
    val result: String, // No Answer, Call Later, Interested, Requested Price, Requested Sample, Waiting Decision, Competitor Offer, Purchase Soon, Cancelled
    val description: String,
    val nextAction: String,
    val nextFollowUpDate: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val followUpId: Long?,
    val title: String,
    val description: String,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
