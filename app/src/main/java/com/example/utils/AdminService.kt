package com.example.utils

object AdminService {
    // Erlaubte Admin-E-Mail-Adressen (inklusive Papa, Mama und später die Kinder)
    val ADMIN_EMAILS = listOf(
        "pfa.workers@gmail.com",
        "sedisabrina@gmail.com"
    )

    // Prüft, ob der aktuell angemeldete Nutzer in der Admin-Liste ist
    fun isAdmin(currentUserEmail: String?): Boolean {
        if (currentUserEmail == null) return false
        val email = currentUserEmail.trim().lowercase()
        return ADMIN_EMAILS.any { it.lowercase() == email }
    }
}
