package com.example.ui.utils

object FarsiHelper {

    fun mapJobType(job: String): String {
        return when (job) {
            "Builder" -> "سازنده"
            "Architect" -> "آرشیتکت / طراح"
            "Contractor" -> "پیمانکار"
            "Employer" -> "کارفرما"
            "Developer" -> "توسعه‌دهنده"
            "Real Estate Company" -> "شرکت انبوه سازی / املاک"
            else -> job
        }
    }

    fun mapLeadSource(source: String): String {
        return when (source) {
            "Visit" -> "بازدید حضوری"
            "Referral" -> "معرفی همکاران"
            "Exhibition" -> "نمایشگاه"
            "Instagram" -> "اینستاگرام"
            "Website" -> "وبسایت"
            "Cold Call" -> "تماس سرد"
            else -> source
        }
    }

    fun mapPriority(prio: String): String {
        return when (prio) {
            "A" -> "اولویت الف (بالا)"
            "B" -> "اولویت ب (متوسط)"
            "C" -> "اولویت ج (پایین)"
            else -> prio
        }
    }

    fun mapTemperature(temp: String): String {
        return when (temp) {
            "Hot" -> "داغ 🔥"
            "Warm" -> "گرم ☀️"
            "Cold" -> "سرد ❄️"
            else -> temp
        }
    }

    fun mapProjectType(type: String): String {
        return when (type) {
            "Villa" -> "ویلا"
            "Apartment" -> "آپارتمان"
            "Tower" -> "برج / مجتمع مسکونی"
            "Commercial Building" -> "ساختمان تجاری"
            "Hotel" -> "هتل"
            "Lobby" -> "لابی مجزا"
            "Office" -> "اداری"
            else -> type
        }
    }

    fun mapProjectStatus(status: String): String {
        return when (status) {
            "Land Preparation" -> "آماده‌سازی زمین"
            "Structure" -> "سفت‌کاری"
            "Brick Work" -> "دیوار چینی"
            "Finishing" -> "نازک‌کاری"
            "Facade Preparation" -> "زیرسازی نما"
            "Selecting Stone" -> "در حال انتخاب سنگ"
            "Near Stone Purchase" -> "نزدیک به خرید سنگ ⏳"
            "Purchased" -> "خرید انجام شده ✅"
            "Lost" -> "خرید از رقیب / آرشیو ❌"
            "Stopped" -> "متوقف شده"
            else -> status
        }
    }

    fun mapPurchaseTime(time: String): String {
        return when (time) {
            "This Week" -> "همین هفته"
            "2 Weeks" -> "۲ هفته آینده"
            "1 Month" -> "۱ ماه آینده"
            "2 Months" -> "۲ ماه آینده"
            "Long Term" -> "بلند مدت"
            "Unknown" -> "نامشخص"
            else -> time
        }
    }

    fun mapStoneUsage(usage: String): String {
        return usage.split(", ")
            .map { u ->
                when (u.trim()) {
                    "Exterior Facade" -> "نمای خارجی"
                    "Interior Floor" -> "کف داخلی"
                    "Stair" -> "پله و پاگرد"
                    "Lobby" -> "لابی"
                    "Bathroom" -> "سرویس بهداشتی"
                    "Wall" -> "دیوارهای داخلی"
                    "Slab" -> "اسلب تزئینی"
                    else -> u
                }
            }.joinToString("، ")
    }

    fun mapPreferredStone(stone: String): String {
        return when (stone) {
            "Travertine" -> "تراورتن"
            "Marble" -> "مرمریت"
            "Limestone" -> "لایمستون"
            "Onyx" -> "مرمر (انیکس)"
            "Crystal" -> "کریستال (چینی)"
            "Granite" -> "گرانیت"
            else -> stone
        }
    }

    fun mapFollowUpType(type: String): String {
        return when (type) {
            "Phone Call" -> "تماس تلفنی 📞"
            "WhatsApp" -> "پیام واتس‌اپ 💬"
            "Site Visit" -> "بازدید پروژه 🏗️"
            "Meeting" -> "جلسه حضوری 🤝"
            "Send Catalog" -> "ارسال کاتالوگ 📖"
            "Send Price" -> "ارسال قیمت / پیش‌فاکتور 💵"
            "Send Sample" -> "ارسال نمونه سنگ 💎"
            "Negotiation" -> "مذاکره نهایی 📈"
            else -> type
        }
    }

    fun mapFollowUpResult(result: String): String {
        return when (result) {
            "No Answer" -> "عدم پاسخگویی"
            "Call Later" -> "تماس در زمان دیگر"
            "Interested" -> "علاقه‌مند و پیگیر"
            "Requested Price" -> "درخواست قیمت / فاکتور"
            "Requested Sample" -> "درخواست نمونه سنگ"
            "Waiting Decision" -> "در حال تصمیم‌گیری"
            "Competitor Offer" -> "پیشنهاد از رقیب دارد"
            "Purchase Soon" -> "خرید به زودی"
            "Cancelled" -> "انصراف از خرید"
            else -> result
        }
    }

    fun formatFarsiDate(timestamp: Long): String {
        // Since standard simple date formats are sufficient for offline MVPs, we format nicely.
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.US)
        return sdf.format(java.util.Date(timestamp))
    }
}
