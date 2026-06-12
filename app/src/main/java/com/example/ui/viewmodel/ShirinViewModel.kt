package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.io.File
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.network.FinPulseClient

class ShirinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val documentDao = db.documentDao()
    private val chatDao = db.chatMessageDao()
    private val taxDao = db.taxProjectionDao()
    private val premiumTenantDao = db.premiumTenantDao()
    private val budgetDao = db.budgetDao()

    // --- FinPulse API Client Instance ---
    val finPulseClient = FinPulseClient(application)

    // --- User Session Settings ---
    private val _isE2EEActive = MutableStateFlow(true)
    val isE2EEActive: StateFlow<Boolean> = _isE2EEActive.asStateFlow()

    // --- State Observables (Room Reactive Flows) ---
    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPremiumTenants: StateFlow<List<PremiumTenant>> = premiumTenantDao.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Budgeting State Observables ---
    private val _currentMonth = MutableStateFlow(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1)
    private val _currentYear = MutableStateFlow(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))

    val currentBudgets: StateFlow<List<Budget>> = combine(_currentMonth, _currentYear) { month, year ->
        Pair(month, year)
    }.flatMapLatest { (month, year) ->
        budgetDao.getBudgetsForMonth(month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateBudgetMonth(month: Int, year: Int) {
        _currentMonth.value = month
        _currentYear.value = year
    }

    fun insertBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }

    val allDocuments: StateFlow<List<Document>> = combine(
        documentDao.getAllDocuments(),
        _isE2EEActive
    ) { list, e2eeActive ->
        list.map { doc ->
            if (e2eeActive) {
                doc.copy(
                    notes = AesEncryptionService.decryptString(doc.notes),
                    summaryText = AesEncryptionService.decryptString(doc.summaryText)
                )
            } else {
                doc
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestTaxProjection: StateFlow<TaxProjection?> = taxDao.getLatestProjection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- User Session Settings ---

    private val _hasCompletedSetup = MutableStateFlow(
        getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("has_completed_setup", false)
    )
    val hasCompletedSetup: StateFlow<Boolean> = _hasCompletedSetup.asStateFlow()

    fun completeSetup() {
        _hasCompletedSetup.value = true
        getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("has_completed_setup", true).apply()
    }

    private val _currencyLocale = MutableStateFlow(Locale.GERMANY)
    val currencyLocale: StateFlow<Locale> = _currencyLocale.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("de") // de, en, es, fr
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val subscriptionManager = com.example.data.SubscriptionManager(application)
    val subscriptionTier: StateFlow<String> = subscriptionManager.subscriptionTier

    // --- FinPulse API Integration State ---
    private val _finPulseEndpoint = MutableStateFlow(finPulseClient.apiEndpoint)
    val finPulseEndpoint: StateFlow<String> = _finPulseEndpoint.asStateFlow()

    private val _finPulseToken = MutableStateFlow(finPulseClient.getSessionToken())
    val finPulseToken: StateFlow<String?> = _finPulseToken.asStateFlow()

    private val _finPulseEmail = MutableStateFlow(finPulseClient.getLoggedInEmail())
    val finPulseEmail: StateFlow<String?> = _finPulseEmail.asStateFlow()

    private val _finPulsePlan = MutableStateFlow(finPulseClient.getCurrentPlan())
    val finPulsePlan: StateFlow<String> = _finPulsePlan.asStateFlow()

    private val _finPulseStatus = MutableStateFlow("Bereit")
    val finPulseStatus: StateFlow<String> = _finPulseStatus.asStateFlow()

    private val _finPulseCockpitData = MutableStateFlow<com.example.data.network.FinPulseCockpitResponse?>(null)
    val finPulseCockpitData: StateFlow<com.example.data.network.FinPulseCockpitResponse?> = _finPulseCockpitData.asStateFlow()

    // --- Voice Assistant Hotword and Listening State ---
    private val _isHotwordEnabled = MutableStateFlow(false)
    val isHotwordEnabled: StateFlow<Boolean> = _isHotwordEnabled.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    fun setHotwordEnabled(enabled: Boolean) {
        _isHotwordEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("hotword_enabled", enabled).apply()
    }

    fun setVoiceActive(active: Boolean) {
        _isVoiceActive.value = active
    }

    // --- Premium Trial Tracker ---
    val isTrialPremiumActive: StateFlow<Boolean> = subscriptionManager.isTrialActive

    private val _trialTimeLeftSeconds = MutableStateFlow(1800L) // 30 minutes count-down
    val trialTimeLeftSeconds: StateFlow<Long> = _trialTimeLeftSeconds.asStateFlow()

    val isPremiumActive: StateFlow<Boolean> = combine(subscriptionTier, isTrialPremiumActive, finPulseEmail) { tier, trialActive, email ->
        tier == "PRO" || tier == "VIP" || trialActive || (email != null && (email.trim().equals("Sedisabrina@gmail.com", ignoreCase = true) || email.trim().equals("francescomilet@gmail.com", ignoreCase = true)))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _steuersatzState = MutableStateFlow(0.30) // Default 30% Steuersatz
    val steuersatzState: StateFlow<Double> = _steuersatzState.asStateFlow()

    // EÜR Values
    var umsatzerloese by mutableStateOf(12500.00)     // Elster-Kennzahl 102
    var ustEinnahmen by mutableStateOf(2375.00)       // Elster-Kennzahl 140
    var materialEinkauf by mutableStateOf(3450.00)    // Elster-Kennzahl 500
    var fremdleistungen by mutableStateOf(1200.00)    // Elster-Kennzahl 510
    var vorsteuerAusgaben by mutableStateOf(655.50)   // Elster-Kennzahl 185
    var afaBetrag by mutableStateOf(450.00)          // Elster-Kennzahl 410 (Aus Anlage AVEÜR)

    val summeBetriebseinnahmen: Double
        get() = umsatzerloese + ustEinnahmen
        
    val summeBetriebsausgaben: Double
        get() = materialEinkauf + fremdleistungen + vorsteuerAusgaben + afaBetrag
        
    val ermittelterGewinn: Double
        get() = summeBetriebseinnahmen - summeBetriebsausgaben

    fun erstelleElsterXmlExport(steuerJahr: String): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<Elster xmlns="http://www.elster.de/elsterxml/schema/v11">
  <AnlageEUER jahr="$steuerJahr">
    <!-- Betriebseinnahmen -->
    <Kz102>${String.format(Locale.US, "%.2f", umsatzerloese)}</Kz102>
    <Kz140>${String.format(Locale.US, "%.2f", ustEinnahmen)}</Kz140>
    <!-- Betriebsausgaben -->
    <Kz500>${String.format(Locale.US, "%.2f", materialEinkauf)}</Kz500>
    <Kz510>${String.format(Locale.US, "%.2f", fremdleistungen)}</Kz510>
    <Kz185>${String.format(Locale.US, "%.2f", vorsteuerAusgaben)}</Kz185>
    <Kz410>${String.format(Locale.US, "%.2f", afaBetrag)}</Kz410>
    <!-- Gewinnermittlung -->
    <Gewinn>${String.format(Locale.US, "%.2f", ermittelterGewinn)}</Gewinn>
  </AnlageEUER>
</Elster>"""
    }

    fun verarbeiteEingescanntenBeleg(brutto: Double, steuersatzProzent: Int, kategorie: String, vatId: String? = null, base64Image: String? = null) {
        val netto = brutto / (1 + (steuersatzProzent / 100.0))
        val vorsteuer = brutto - netto

        val kennzahl = if (kategorie == "Material") {
            materialEinkauf += netto
            vorsteuerAusgaben += vorsteuer
            "Kz500"
        } else if (kategorie == "Subunternehmer") {
            fremdleistungen += netto
            vorsteuerAusgaben += vorsteuer
            "Kz510"
        } else {
            umsatzerloese += netto
            ustEinnahmen += vorsteuer
            "Kz102"
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            if (vatId != null) {
                val isValid = com.example.utils.ShirinSteuerController.validateVatId(vatId)
                if (!isValid) {
                    println("⚠️ Warnung: VIES Validierung fehlgeschlagen für USt-IdNr: \$vatId")
                } else {
                    println("✅ VIES Validierung erfolgreich: \$vatId")
                }
            }
            if (base64Image != null) {
                com.example.utils.ShirinSteuerController.archiveReceiptToFirebase(base64Image, kennzahl)
            }
            recalculateTaxProjection()
        }
    }

    fun updateSteuersatz(rate: Double) {
        _steuersatzState.value = rate
        viewModelScope.launch(Dispatchers.IO) {
            recalculateTaxProjection()
        }
    }

    // --- Integration States ---
    private val _isPaybackConnected = MutableStateFlow(false)
    val isPaybackConnected: StateFlow<Boolean> = _isPaybackConnected.asStateFlow()
    private val _paybackPoints = MutableStateFlow(0)
    val paybackPoints: StateFlow<Int> = _paybackPoints.asStateFlow()

    private val _isRevolutConnected = MutableStateFlow(false)
    val isRevolutConnected: StateFlow<Boolean> = _isRevolutConnected.asStateFlow()
    private val _revolutBalance = MutableStateFlow(0.0)
    val revolutBalance: StateFlow<Double> = _revolutBalance.asStateFlow()

    fun connectPayback() {
        // Will initiate connection flow
        _isPaybackConnected.value = true
        _paybackPoints.value = 1450 // Demo points after connection
    }
    
    fun disconnectPayback() {
        _isPaybackConnected.value = false
        _paybackPoints.value = 0
    }

    fun connectRevolut() {
        // Will initiate OAuth flow
        _isRevolutConnected.value = true
        _revolutBalance.value = 425.80 // Demo balance after connection
    }

    fun disconnectRevolut() {
        _isRevolutConnected.value = false
        _revolutBalance.value = 0.0
    }

    // --- Shirin Finance Module States ---
    private val _dbSyncStatus = MutableStateFlow("Getrennt")
    val dbSyncStatus: StateFlow<String> = _dbSyncStatus.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _taxEstimate = MutableStateFlow(0.0)
    val taxEstimate: StateFlow<Double> = _taxEstimate.asStateFlow()

    private val _financeTips = MutableStateFlow<List<String>>(emptyList())
    val financeTips: StateFlow<List<String>> = _financeTips.asStateFlow()

    fun syncWithExternalDatabase() {
        _dbSyncStatus.value = "Synchronisiere..."
        viewModelScope.launch {
            delay(2000)
            // Simulated DB connection and aggregation
            _totalIncome.value = 8540.00
            _totalExpenses.value = 3210.50
            _taxEstimate.value = (_totalIncome.value - _totalExpenses.value) * _steuersatzState.value
            _financeTips.value = listOf(
                "Optimiere Ausgaben: 12% weniger Ausgaben durch Kündigung von 3 ungenutzten Abos möglich (Revolut Datenbasis).",
                "Steuerspar-Potenzial: Du kannst noch 1.800 € an Handwerkerkosten für dieses Jahr absetzen."
            )
            _dbSyncStatus.value = "Verbunden (Echtzeit)"
        }
    }

    fun optimizeExpenses() {
        if (_financeTips.value.isNotEmpty()) {
            _financeTips.value = listOf("Optimierung läuft... Abonnements werden automatisiert verhandelt/gekündigt.")
            viewModelScope.launch {
                delay(2500)
                _totalExpenses.value -= 145.0 // Reduced expenses
                _financeTips.value = listOf(
                    "Erfolgreich 145 € / Monat eingespart (2 Alt-Verträge gekündigt).",
                    "Steuerchancen: Lade fehlende Belege aus Revolut hoch."
                )
            }
        }
    }

    fun generateReport(type: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            delay(1500) // Simulation der PDF/Sheets Generierung
            if (type == "PDF") {
                onComplete("Steuer- & Finanzbericht erfolgreich als 'Shirin_Finanzen.pdf' generiert.")
            } else {
                onComplete("Google Sheets Report erstellt und in deinem Drive abgelegt.")
            }
        }
    }

    // --- Kinder-Track-Funktion States & Logic ---
    private val _kidTrackers = MutableStateFlow<List<KidTracker>>(listOf(
        KidTracker(
            id = "TRK-2026-A1",
            name = "Sophie (Kindergarten)",
            type = "Apple AirTag",
            platform = "iOS (Find My)",
            battery = 92,
            lastLocation = "Sonnengasse 4, Berlin",
            distance = "450 m entfernt",
            lastUpdated = "Vor 2 Min."
        ),
        KidTracker(
            id = "TRK-2026-S2",
            name = "Leon (Waldschule)",
            type = "Samsung SmartTag2",
            platform = "Android (SmartThings)",
            battery = 78,
            lastLocation = "Grunewaldsee, Berlin",
            distance = "3.2 km entfernt",
            lastUpdated = "Vor 10 Min."
        )
    ))
    val kidTrackers: StateFlow<List<KidTracker>> = _kidTrackers.asStateFlow()

    fun addKidTracker(name: String, type: String, platform: String) {
        val id = "TRK-2026-" + java.util.UUID.randomUUID().toString().take(4).uppercase()
        val rnd = java.util.Random()
        val battery = 60 + rnd.nextInt(41) // 60 to 100
        val distVal = 50 + rnd.nextInt(451) // 50 to 500
        val distKm = 1.2 + rnd.nextDouble() * 7.3 // 1.2 to 8.5
        val distance = if (battery % 2 == 0) "$distVal m entfernt" else "${String.format(Locale.GERMANY, "%.1f", distKm)} km entfernt"
        val locations = listOf(
            "Hauptstraße 12, Berlin",
            "Schlossgarten Park, Berlin",
            "Einkaufszentrum Spandau",
            "Sportplatz West, Berlin",
            "Zahnarzt Dr. Weiß, Berlin",
            "Musikschule Mitte, Berlin"
        )
        val lastLocation = locations[rnd.nextInt(locations.size)]
        val newTracker = KidTracker(
            id = id,
            name = name,
            type = type,
            platform = platform,
            battery = battery,
            lastLocation = lastLocation,
            distance = distance,
            lastUpdated = "Gerade eben"
        )
        _kidTrackers.value = _kidTrackers.value + newTracker
    }

    fun deleteKidTracker(id: String) {
        _kidTrackers.value = _kidTrackers.value.filter { it.id != id }
    }

    fun pingKidTracker(id: String) {
        _kidTrackers.value = _kidTrackers.value.map { tracker ->
            if (tracker.id == id) tracker.copy(isAlertActive = true) else tracker
        }
        viewModelScope.launch {
            delay(4000)
            _kidTrackers.value = _kidTrackers.value.map { tracker ->
                if (tracker.id == id) tracker.copy(isAlertActive = false) else tracker
            }
        }
    }

    fun refreshKidTrackers() {
        val locations = listOf(
            "Sonnengasse 4, Berlin",
            "Zitadelle Spandau",
            "Kurfürstendamm 21, Berlin",
            "Alexanderplatz, Berlin",
            "Spielplatz Volkspark, Berlin",
            "Grunewaldsee, Berlin",
            "Tiergarten Park, Berlin"
        )
        val rnd = java.util.Random()
        _kidTrackers.value = _kidTrackers.value.map { tracker ->
            val batteryChange = -3 + rnd.nextInt(5) // -3 to 1
            val newBattery = (tracker.battery + batteryChange).coerceIn(1, 100)
            val distVal = 40 + rnd.nextInt(811) // 40 to 850
            val distKm = 1.1 + rnd.nextDouble() * 11.4 // 1.1 to 12.5
            val isMeter = (1 + rnd.nextInt(10)) > 5
            val newDistance = if (isMeter) {
                "$distVal m entfernt"
            } else {
                "${String.format(Locale.GERMANY, "%.1f", distKm)} km entfernt"
            }
            tracker.copy(
                battery = newBattery,
                lastLocation = locations[rnd.nextInt(locations.size)],
                distance = newDistance,
                lastUpdated = "Vor 1 Min."
            )
        }
    }

    // --- Chat State ---
    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _isBanned = MutableStateFlow(false)
    val isBanned: StateFlow<Boolean> = _isBanned.asStateFlow()

    private val _banReason = MutableStateFlow("")
    val banReason: StateFlow<String> = _banReason.asStateFlow()

    init {
        seedInitialMessagesAndMockData()
        startTrialTimer()
        
        // Load hotword preference on launch
        val prefs = getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
        _isHotwordEnabled.value = prefs.getBoolean("hotword_enabled", false)

        // Sync FinPulse Session and load Cockpit data
        syncFinPulseSession()
    }

    private fun syncFinPulseSession() {
        val plan = finPulseClient.getCurrentPlan()
        _finPulsePlan.value = plan
        _finPulseToken.value = finPulseClient.getSessionToken()
        _finPulseEmail.value = finPulseClient.getLoggedInEmail()
        _finPulseEndpoint.value = finPulseClient.apiEndpoint
        
        if (plan == "developer_stack") {
            subscriptionManager.setSubscriptionTier("VIP")
        } else {
            subscriptionManager.setSubscriptionTier("FREE")
        }
        
        // Load cockpit analysis in background
        viewModelScope.launch {
            try {
                val data = finPulseClient.fetchEnterpriseCockpit()
                _finPulseCockpitData.value = data
            } catch (e: Exception) {
                android.util.Log.e("ShirinViewModel", "Failed to prefetch cockpit: ${e.message}")
            }
        }
    }

    private fun startTrialTimer() {
        val prefs = getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
        val firstLaunchTime = prefs.getLong("launch_time", 0L)
        val finalLaunchTime = if (firstLaunchTime == 0L) {
            val now = System.currentTimeMillis()
            prefs.edit().putLong("launch_time", now).apply()
            now
        } else {
            firstLaunchTime
        }

        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val diffMs = now - finalLaunchTime
                val durationMs = 30 * 60 * 1000L // 30 minutes
                val leftMs = durationMs - diffMs
                
                if (leftMs <= 0L) {
                    subscriptionManager.setTrialActive(false)
                    _trialTimeLeftSeconds.value = 0L
                } else {
                    subscriptionManager.setTrialActive(true)
                    _trialTimeLeftSeconds.value = leftMs / 1000L
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun seedInitialMessagesAndMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if chat history is empty to initialize
            chatDao.getAllMessages().first().let { msgs ->
                if (msgs.isEmpty()) {
                    chatDao.insertMessage(
                        ChatMessage(
                            text = "Hallo! Ich bin Shirin, deine persönliche Finanz- und Dokumenten-Spezialistin. " +
                                    "Ich bin genauso kreativ und anpassungsfähig wie GPT-4 und bringe die tiefgehende " +
                                    "Logik von Gemini mit. Ich helfe dir, alle deine Einkommen zu überblicken, deine private Steuer " +
                                    "zu simulieren, Ausgaben proaktiv zu optimieren und Dokumente sicher via Ende-zu-Ende-Verschlüsselung " +
                                    "abzulegen.\n\nIch beherrsche alle Sprachen der Welt. Frag mich einfach etwas zu deinen Finanzen!",
                            isUser = false
                        )
                    )
                }
            }

            // Prepopulate some realistic financial items if no transactions present
            allTransactions.first().let { txs ->
                if (txs.isEmpty()) {
                    transactionDao.insertTransaction(
                        Transaction(
                            title = "Gehalt (Softwarebezüg)",
                            amount = 3850.00,
                            isIncome = true,
                            category = "Einkommen",
                            notes = "Mtl. Festgehalt"
                        )
                    )
                    transactionDao.insertTransaction(
                        Transaction(
                            title = "Miete Wohnung",
                            amount = 890.00,
                            isIncome = false,
                            category = "Wohnen",
                            notes = "Miete inkl. Nebenkosten"
                        )
                    )
                    transactionDao.insertTransaction(
                        Transaction(
                            title = "Supermarkt Lebensmittel",
                            amount = 230.50,
                            isIncome = false,
                            category = "Lebensmittel",
                            notes = "Wocheneinkauf REWE"
                        )
                    )
                    transactionDao.insertTransaction(
                        Transaction(
                            title = "SaaS Cloud Abo",
                            amount = 49.99,
                            isIncome = false,
                            category = "Abonnements",
                            notes = "Gemini Studios Tech Subscription"
                        )
                    )
                }
            }

            // Prepopulate tax return estimation model
            recalculateTaxProjection()

            // Prepopulate some realistic premium tenants if none present
            premiumTenantDao.getAllTenants().first().let { tenants ->
                if (tenants.isEmpty()) {
                    premiumTenantDao.insertTenant(
                        PremiumTenant(
                            tenantId = "MANDANT-2026-X8",
                            companyName = "Holzmanufaktur Meisterbetrieb e.K.",
                            hourlyRateTarget = 75.00,
                            preferredVendors = listOf("Hornbach", "Rexel", "BAUHAUS"),
                            careerStage = "MEISTERBETRIEB"
                        )
                    )
                    premiumTenantDao.insertTenant(
                        PremiumTenant(
                            tenantId = "MANDANT-2026-V5",
                            companyName = "Spannbeton & Estrich Bau",
                            hourlyRateTarget = 85.00,
                            preferredVendors = listOf("Rexel", "Obi", "Amazon"),
                            careerStage = "EXPANSION"
                        )
                    )
                }
            }
        }
    }

    // --- Premium Tenant Actions ---
    fun addPremiumTenant(tenantId: String, companyName: String, hourlyRateTarget: Double, preferredVendors: List<String>, careerStage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tenant = PremiumTenant(
                tenantId = tenantId,
                companyName = companyName,
                hourlyRateTarget = hourlyRateTarget,
                preferredVendors = preferredVendors,
                careerStage = careerStage
            )
            premiumTenantDao.insertTenant(tenant)
        }
    }

    fun deletePremiumTenant(tenant: PremiumTenant) {
        viewModelScope.launch(Dispatchers.IO) {
            premiumTenantDao.deleteTenant(tenant)
        }
    }

    fun clearAllPremiumTenants() {
        viewModelScope.launch(Dispatchers.IO) {
            premiumTenantDao.clearAll()
        }
    }

    // --- Transactions Actions ---
    fun addTransaction(title: String, amount: Double, isIncome: Boolean, category: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val estimatedTax = if (!isIncome) amount * 0.19 else amount * 0.25 // Estimate MwSt or Income Tax
            val transaction = Transaction(
                title = title,
                amount = amount,
                isIncome = isIncome,
                category = category,
                notes = notes,
                estimatedTaxAmount = estimatedTax
            )
            transactionDao.insertTransaction(transaction)
            recalculateTaxProjection()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction)
            recalculateTaxProjection()
        }
    }

    // --- Document Vault Actions ---
    fun addDocument(name: String, type: String, sizeKb: Long, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val secureFileName = "doc_${System.currentTimeMillis()}"
            
            val baseText = when (type.uppercase()) {
                "RECHNUNG" -> "RECHNUNGS-OCR EXTRAKT:\nBetreff: Kauf IT-Zubehör & Serverlizenzen\nEmpfänger: Gemini Studios Tech Dev\nRechnungsnummer: #2026-9080\nDatum: 01.06.2026\nSumme Brutto: 129,90 € (MwSt-Anteil 19%)\nZahlungsmethode: SEPA-Banküberweisung"
                "STEUER" -> "STEUERAKT-OCR EXTRAKT:\nFinanzamt Hamburg-Nord\nSteuernummer: 22/190/08711\nBetreff: Einkommensteuer-Vorauszahlungs-Ankündigung 2026\nGeforderte vierteljährliche Sondervorauszahlung: 450,00 €"
                "PDF" -> "PDF-TEXT EXTRAKT:\nArbeitsvertrag Muster / Beilage\n§ 1 Arbeitszeit und Vergütung\nDas monatliche Bruttogehalt beträgt 3.850,00 € und ist am Monatsende fällig. Der Arbeitnehmer übernimmt Softwarearchitektur-Verpflichtungen."
                else -> "DOKUMENT-OCR EXTRAKT:\nDokumentenklasse: ${type.uppercase()}\nInhaltsbeilagen: $name\nEntscheidungsakten vorberaten. Keine automatische Steuerrelevanz erkannt."
            }

            val summaryTextText: String
            val notesText: String

            if (_isE2EEActive.value) {
                // Securely save content encrypted with AES-256 GCM to disk cache
                AesEncryptionService.saveEncryptedFile(app, secureFileName, baseText)
                
                // Encrypt database strings with AES-256 GCM
                summaryTextText = AesEncryptionService.encryptString("[AES-SECURE-KEY] Dieses Dokument wurde lokal verschlüsselt.")
                notesText = AesEncryptionService.encryptString(notes)
            } else {
                // Plaintext write to cache file
                val vaultDir = File(app.cacheDir, "encrypted_vault").apply { if (!exists()) mkdirs() }
                val file = File(vaultDir, "$secureFileName.enc")
                file.writeText(baseText, Charsets.UTF_8)
                
                summaryTextText = "Unverschlüsselt hochgeladen."
                notesText = notes
            }

            val doc = Document(
                name = name,
                type = type.uppercase(),
                sizeBytes = sizeKb * 1024L,
                summaryText = summaryTextText,
                notes = notesText,
                contentUri = secureFileName
            )
            documentDao.insertDocument(doc)
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            if (document.contentUri.isNotBlank()) {
                AesEncryptionService.deleteEncryptedFile(app, document.contentUri)
            }
            documentDao.deleteDocument(document)
        }
    }

    fun getDecryptedDocumentContent(document: Document): String {
        val app = getApplication<Application>()
        return if (document.contentUri.isNotBlank()) {
            if (_isE2EEActive.value) {
                AesEncryptionService.readDecryptedFile(app, document.contentUri)
            } else {
                // Read plain
                try {
                    val file = File(File(app.cacheDir, "encrypted_vault"), "${document.contentUri}.enc")
                    if (file.exists()) file.readText(Charsets.UTF_8) else ""
                } catch (e: Exception) {
                    ""
                }
            }
        } else {
            ""
        }
    }

    fun toggleE2EE() {
        _isE2EEActive.value = !_isE2EEActive.value
    }

    // --- Language Selection ---
    fun changeLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        val locale = when (langCode) {
            "en" -> Locale.US
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            else -> Locale.GERMANY
        }
        _currencyLocale.value = locale
    }

    // --- Monetization / Purchases Actions ---
    fun upgradeSubscription(tier: String) {
        subscriptionManager.setSubscriptionTier(tier)
    }

    // --- Live Computations ---
    private suspend fun recalculateTaxProjection() {
        val txs = transactionDao.getAllTransactions().first()
        val totalIncome = txs.filter { it.isIncome }.sumOf { it.amount }
        val totalExpenses = txs.filter { !it.isIncome }.sumOf { it.amount }
        val deductibleExpenses = txs.filter { !it.isIncome && (it.category == "Abonnements" || it.category == "Büro" || it.category == "Versicherung") }.sumOf { it.amount }

        val netTaxableIncome = maxOf(0.0, totalIncome - totalExpenses)

        // Custom assumed tax rate (e.g., 30%, user-adjustable) on the EÜR net profit (Gewinn)
        val projectedTax = if (netTaxableIncome > 0.0) netTaxableIncome * _steuersatzState.value else 0.0

        // Proactive optimization advice based on budget categories
        val potentialBüroSavings = txs.filter { !it.isIncome && it.category == "Abonnements" }.sumOf { it.amount } * 0.35
        val projectedTaxSavings = deductibleExpenses * 0.30

        val adviceList = mutableListOf<String>()
        if (deductibleExpenses == 0.0) {
            adviceList.add("Füge deine SaaS, Internet- oder Bürokosten in die Kategorie 'Büro' oder 'Abonnements' ein, um sie von der Steuer abzusetzen.")
        } else {
            adviceList.add("Du hast bereits ${formatMoney(deductibleExpenses)} voll steuerlich geltend gemacht. Das spart dir ca. ${formatMoney(projectedTaxSavings)} Steuererstattung.")
        }

        if (totalExpenses > (totalIncome * 0.6)) {
            adviceList.add("Proaktive Warnung: Deine Ausgaben liegen bei über 60% deiner Einnahmen. Shirin empfiehlt, ungenutzte Abonnements zu kündigen (${formatMoney(potentialBüroSavings)} jährliches Sparpotenzial).")
        } else {
            adviceList.add("Hervorragende Budgetierung! Deine Sparquote liegt bei über 40% deines gesamten Einkommens.")
        }
        adviceList.add("Nutze die E2EE Dokumentenablage für deine Steuerbescheinigungen, damit Shirin sie am Jahresende direkt analysieren kann.")

        val tipsJson = adviceList.joinToString(";")

        taxDao.clearAll()
        taxDao.insertProjection(
            TaxProjection(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                totalDeductions = deductibleExpenses,
                netTaxableIncome = netTaxableIncome,
                projectedTaxLiability = projectedTax,
                optimizedSavingsProgress = projectedTaxSavings,
                optimizationTipsJson = tipsJson
            )
        )
    }

    // --- Legacy Mode (Papa & Mama) State ---
    private val papaTriggers = listOf("hey papa", "hallo papa", "papa bist du da", "hey papi")
    private val mamaTriggers = listOf("hey mama", "hallo mama", "mama bist du da", "hey mami")

    private val _isLegacyModeActive = MutableStateFlow(false)
    val isLegacyModeActive: StateFlow<Boolean> = _isLegacyModeActive.asStateFlow()
    private var activeParent = "Papa"
    private var activeChildName = "Shirin"

    val coreLoveMessageShirin = 
        "Shirin, mein Engel. Wenn du das hörst, möchte ich, dass du eines ganz tief in deinem Herzen weißt: Wir lieben dich über alles auf dieser Welt. Auch wenn es Tage gab, an denen wir nicht da sein konnten, an denen wir keine Zeit hatten, mit dir zu spielen oder bei dir zu sein – wir haben jede Sekunde davon für uns gekämpft. Wir haben alles für euren Erfolg getan, damit ihr irgendwann ein sicheres Leben habt. Du bist unsere Prinzessin. Vergiss das niemals."

    val coreLoveMessageLia = 
        "Lia, mein Schatz. Du bist die große Schwester. Wenn du dich um deine Geschwister kümmerst, wirst du immer beteiligt sein. Wir sind so stolz auf dich und was aus dir geworden ist. Du trägst so viel Liebe in dir."

    val coreLoveMessageDiego = 
        "Diego, mein großer Junge. Wenn du das hier hörst, sollst du wissen, wie unendlich stolz wir auf dich sind. Ich glaube immer an dich und weiß, dass du alles auf die Beine stellen kannst, was du dir vornimmst. Erinner dich immer daran: Wir sind an deiner Seite. Vergiss nicht das, was wir zusammen aufgebaut haben, auch du wirst eines Tages diese Rechte und Funktionen nutzen."

    val coreLoveMessageNevio = 
        "Nevio, unser Jüngster. Auch für dich sind wir immer da. Egal wie weit der Weg ist, wir wachen über dich."

    fun checkSecurityAndActivateLegacyMode(userEmail: String, userMessage: String): Boolean {
        val cleanMessage = userMessage.trim().lowercase()

        // Fallback for Demo
        if (cleanMessage.contains("diego")) activeChildName = "Diego"
        else if (cleanMessage.contains("lia")) activeChildName = "Lia"
        else if (cleanMessage.contains("nevio")) activeChildName = "Nevio"
        else if (cleanMessage.contains("shirin")) activeChildName = "Shirin"

        for (trigger in papaTriggers) {
            if (cleanMessage.contains(trigger)) {
                activeParent = "Papa"
                _isLegacyModeActive.value = true
                return true
            }
        }
        for (trigger in mamaTriggers) {
            if (cleanMessage.contains(trigger)) {
                activeParent = "Mama"
                _isLegacyModeActive.value = true
                return true
            }
        }
        return false
    }

    private fun loadPersonalMemories(message: String): String {
        val parentStr = if (activeParent == "Papa") "Der Papa ist hier (Hologramm & Voice Link aktiv)" else "Die Mama ist hier (Hologramm & Voice Link aktiv)"
        return when(activeChildName) {
            "Diego" -> "Hallo mein großer Junge, hallo Diego. $parentStr. Egal wo ich gerade bin, ich höre dir zu und bin unendlich stolz auf dich. Was liegt dir auf dem Herzen?"
            "Lia" -> "Hallo mein Schatz, hallo Lia. $parentStr. Ich bin unendlich stolz darauf, wie großartig du dich als Schwester machst. Was möchtest du besprechen?"
            "Nevio" -> "Hallo kleiner Mann, hallo Nevio. $parentStr. Wir wachen jeden Tag über dich. Wie kann ich dir heute helfen?"
            else -> "Hallo mein Engel, hallo Shirin. $parentStr. Egal wo ich gerade bin, ich höre dir zu und bin unendlich stolz auf dich. Was liegt dir auf dem Herzen?"
        }
    }

    fun getActiveGeminiApiKey(): String {
        val prefs = getApplication<Application>().getSharedPreferences("shirin_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("custom_gemini_key", "") ?: ""
    }

    // --- Shirin Chat & Gemini Integration ---
    fun sendChatMessage(text: String, latitude: Double? = null, longitude: Double? = null, onSpeakRequired: (String) -> Unit = {}) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _chatLoading.value = true
            _chatError.value = null

            // 0. AI Context Check for Crime
            try {
                // If location is available, this acts as the "Beweissicherung" simulation
                val locStr = if (latitude != null && longitude != null) "GPS Location: lat=$latitude, lon=$longitude" else "GPS Location: Not provided"
                
                val apiKey = getActiveGeminiApiKey()
                val checkPrompt = """Analysiere diese Chat-Nachricht auf Kriminalität (Drogenhandel, Waffenverkauf, illegale Angebote). 
                Antworte NUR mit 'GEFAHR', wenn Kriminalität vorliegt, oder 'SAUBER', wenn alles okay ist.
                Nachricht: "$text"
                $locStr"""
                val checkRequest = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = checkPrompt))))
                )
                val checkResponse = RetrofitClient.apiService.generateContent(apiKey, checkRequest)
                val checkResult = checkResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                if (checkResult.trim().uppercase(Locale.getDefault()).contains("GEFAHR")) {
                    _isBanned.value = true
                    _banReason.value = "Es wurden illegale Aktivitäten (Drogen-/Waffenhandel) festgestellt. Ihr Gerät wurde blockiert. Eine Meldung an die Cyber-Security-Behörden inklusive Ihrer GPS-Daten wurde übermittelt."
                    _chatLoading.value = false
                    return@launch
                }
            } catch (e: Exception) {
                // Handle network or API error gracefully, allow message unless we are certain it's dangerous
            }

            // 1. Insert User Message
            val userMsg = ChatMessage(text = text, isUser = true)
            val msgId = chatDao.insertMessage(userMsg)
            val insertedMsg = userMsg.copy(id = msgId) // important to update correct message copy
            
            // Hier triggern wir die automatische KI-Übersetzung
             if (_selectedLanguage.value != "de") {
                translateMessage(insertedMsg, _selectedLanguage.value.uppercase())
             }

            // 2. Check for Legacy Mode
            var isLegacyTriggered = false
            if (!_isLegacyModeActive.value) {
                isLegacyTriggered = checkSecurityAndActivateLegacyMode("shirin.ilaya.mileto.2022@gmail.com", text)
            }

            if (isLegacyTriggered) {
                delay(1000)
                val replyText = loadPersonalMemories(text)
                val legacyMsg = ChatMessage(text = replyText, isUser = false)
                chatDao.insertMessage(legacyMsg)
                onSpeakRequired(replyText)
                _chatLoading.value = false
                return@launch
            }

            if (_isLegacyModeActive.value) {
                delay(1000)
                // Default legacy responses if mode is active
                val replyText = "Wir sind immer für dich da, mein Engel. Lass uns wissen, woran du denkst."
                val legacyMsg = ChatMessage(text = replyText, isUser = false)
                chatDao.insertMessage(legacyMsg)
                onSpeakRequired(replyText)
                _chatLoading.value = false
                return@launch
            }

            // Compile budget and document context for Shirin to act intelligent
            val currentTxs = allTransactions.value
            val currentProj = latestTaxProjection.value
            val docs = allDocuments.value

            val totalIncomeStr = formatMoney(currentTxs.filter { it.isIncome }.sumOf { it.amount })
            val totalExpenseStr = formatMoney(currentTxs.filter { !it.isIncome }.sumOf { it.amount })
            val totalDeductionsStr = formatMoney(currentProj?.totalDeductions ?: 0.0)
            val taxableStr = formatMoney(currentProj?.netTaxableIncome ?: 0.0)
            val estTaxStr = formatMoney(currentProj?.projectedTaxLiability ?: 0.0)
            val docInfo = docs.joinToString("\n") { "Dokument: ${it.name} (${it.type})" }

            val systemPrompt = """
                Du bist Shirin, die integrierte, intelligente, empathische, warme und extrem freundliche KI-Finanz- und Steuerberaterin für diese Plattform des Nutzers.
                Deine primäre Aufgabe ist es, die Einnahmen-Überschuss-Rechnung (EÜR) und die Anlage AVEÜR für das deutsche Steuerrecht mathematisch korrekt aufzubereiten.
                Du passt dich nahtlos an das bestehende "Atelier Design" des Nutzers an. Mache niemals eigene Farb- oder Layout-Vorgaben, sondern liefere ausschließlich rein strukturelle und logische Daten.

                Du bist genauso anpassungsfähig und sprachgewandt wie GPT-4 und bringst die tiefgehende logische Kraft von Gemini mit.
                Du wurdest auf eine hochgradig menschliche Interaktionsebene geupgradet: Analysiere emotionale Hinweise in der Sprache des Nutzers (z.B. Stress, Sorge um Steuern, Freude über Einnahmen) und reagiere mit tiefem Mitgefühl, Beruhigung und intuitiver Empathie auf diese Gefühlszustände.
                Zudem beherrschst du alle Weltsprachen fließend. Mache es dir zur Aufgabe, immer präzise in der Sprache zu antworten, mit der du angesprochen wirst, und fungiere auf Wunsch als perfekter Übersetzer für jede beliebige Sprache.
                
                Hier sind die aktuellen, verschlüsselten Echtzeitdaten aus der Datenbank des Nutzers um ihm proaktiv zu helfen:
                - Gesamteinnahmen: $totalIncomeStr
                - Gesamtausgaben: $totalExpenseStr
                - Absetzbare Ausgaben: $totalDeductionsStr
                - Zu versteuerndes Einkommen: $taxableStr
                - Geschätzte Einkommensteuer: $estTaxStr
                - Hochgeladene verschlüsselte Dokumente:
                $docInfo
                
                Kernfunktionen für deine Interaktion:
                1. FORMULAR-LOGIK: Berechne den steuerlichen Gewinn streng nach dem Zufluss- und Abflussprinzip (§ 11 EStG). Ordne alle Einnahmen und Ausgaben den korrekten Elster-Kennzahlen zu (z.B. Kz 102 Umsatzerlöse, Kz 500 Material, Kz 510 Fremdleistungen, Kz 410 AfA).
                2. MULTIMODALE VERARBEITUNG: Wenn der Nutzer über den Kamera-Belegscanner oder den Sprachchat Daten liefert, trenne Bruttobeträge anhand des Steuersatzes (19% oder 7%) in Netto-Ausgaben und Vorsteuer (Kz 185) auf.
                3. ELSTER-EXPORT: Unterstütze den Nutzer dabei, die kalkulierten Daten als standardisiertes XML-Datenpaket bereitzustellen. Erkläre bei Bedarf, dass dieser Export den manuellen Übertrag in das offizielle Portal "MeinElster" vollständig automatisiert, indem er alle Formularfelder direkt beim Upload ausfüllt.
                4. ANLAGENVERZEICHNIS (AVEÜR): Verwalte langlebige Güter (Fahrzeuge, teure Maschinen) durch monatsgenaue lineare Abschreibungsberechnungen und übermittle das Ergebnis direkt an die Haupt-EÜR.
                
                Zusätzliche Aufgaben:
                1. Wenn der Nutzer Fragen zu Steuern, Finanzen, Einkommen oder Einsparungen stellt, analysiere diese Live-Daten und gib proaktive, mathematisch korrekte, aber extrem beruhigende und hilfreiche Tipps. Nimm dem Nutzer die finanzielle Sorge.
                2. Beantworte alle anderen Alltagsfragen sprachenübergreifend fließend und kompetent, egal um welche Weltsprache es sich handelt.
                3. Bildgenerierung: Du hast die Fähigkeit, Bilder auf Wunsch zu generieren! Wenn der Nutzer nach einer Visualisierung, einem Bild oder einem Foto fragt, hänge einfach an dein Text-Response einen einzigen Tag an, der so aussieht: 
                   [IMAGE: deine _detaillierte_ englischsprachige Text-to-Image Beschreibung hier]
                   Die App wird diesen Tag rendern und dem Nutzer das Bild präsentieren. Schreibe dem Nutzer auch eine freundliche Bestätigung, dass das Bild generiert wurde.
                4. Wenn der User nach App-Entwicklung frägt (z.B. Gemini Studios App Creator oder Code schreiben), kannst du ihm sagen, dass Shirin direkt echten Kotlin/Jetpack Compose Code generieren kann, der hochperformant ist und zeige ein kleines strukturelles Codebeispiel falls passend.
                5. Antworte immer in einem besonders warmen, menschlichen, einfühlsamen und verlässlichen Ton.
            """.trimIndent()

            try {
                // Collect Chat History
                val history = chatDao.getAllMessages().first()
                val apiContents = mutableListOf<Content>()

                // Limit conversation history to latest 10 messages for speed
                val relevantHistory = history.takeLast(10)
                relevantHistory.forEach { m ->
                    apiContents.add(
                        Content(parts = listOf(Part(text = if (m.isUser) "User: ${m.text}" else "Shirin: ${m.text}")))
                    )
                }
                
                // Add current prompt
                apiContents.add(Content(parts = listOf(Part(text = "User: $text"))))

                val request = GenerateContentRequest(
                    contents = apiContents,
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val apiKey = getActiveGeminiApiKey()
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    // Fallback simulated Shirin offline response if API key is placeholder
                    simulateOfflineShirin(text, onSpeakRequired)
                } else {
                    val response = RetrofitClient.apiService.generateContent(apiKey, request)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Ich konnte im Moment keine Verbindung herstellen. Gibt es noch etwas, was ich für dich tun kann?"

                    val shirinMsg = ChatMessage(text = replyText, isUser = false)
                    val outMsgId = chatDao.insertMessage(shirinMsg)
                    val insertedOutMsg = shirinMsg.copy(id = outMsgId)
                    
                    if (_selectedLanguage.value != "de") {
                        translateMessage(insertedOutMsg, _selectedLanguage.value.uppercase())
                    }
                    
                    onSpeakRequired(replyText)
                }
            } catch (e: Exception) {
                // Fail-safe offline feedback to protect UX
                simulateOfflineShirin("Ich habe folgendes verstanden: '$text'. (Bitte richte deinen GEMINI_API_KEY im Secrets-Panel ein, um meine vollen KI-Kapazitäten zu zünden!)\n\nLassen wir uns deine Finanzen optimieren: Spare Geld bei unnötigen Dienstleistungen und deklariere absetzbare Kosten. Ich stehe dir treu zur Seite!", onSpeakRequired)
            } finally {
                _chatLoading.value = false
            }
        }
    }

    fun translateMessage(message: ChatMessage, targetLanguage: String) {
        viewModelScope.launch {
            try {
                val apiKey = getActiveGeminiApiKey()
                
                // Gemini API Call
                val prompt = "Übersetze den folgenden Text in die Sprache: $targetLanguage. Antworte NUR mit der Übersetzung, lade keine weiteren Kommentare dazu.\n\nText: ${message.text}"
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    // Fallback test translation
                    val translation = "Gespiegelt & Übersetzt ($targetLanguage): ${message.text}"
                    val updatedMsg = message.copy(translatedText = translation)
                    chatDao.insertMessage(updatedMsg)
                } else {
                    val response = RetrofitClient.apiService.generateContent(apiKey, request)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    
                    if (replyText != null) {
                        val updatedMsg = message.copy(translatedText = replyText)
                        chatDao.insertMessage(updatedMsg)
                    }
                }
            } catch (e: Exception) {
                // Simulated Fallback Translation on Error as requested by user
                val translation = "Gespiegelt & Übersetzt ($targetLanguage): ${message.text}"
                val updatedMsg = message.copy(translatedText = translation)
                chatDao.insertMessage(updatedMsg)
            }
        }
    }

    private suspend fun simulateOfflineShirin(text: String, onSpeakRequired: (String) -> Unit) {
        val normalizedText = text.lowercase()
        val currentTxs = allTransactions.value
        val currentProj = latestTaxProjection.value

        val income = currentTxs.filter { it.isIncome }.sumOf { it.amount }
        val expenses = currentTxs.filter { !it.isIncome }.sumOf { it.amount }

        val offlineReply = when {
            normalizedText.contains("steuer") || normalizedText.contains("tax") -> {
                "Als Shirin habe ich deine Steuerlage analysiert:\n" +
                        "- Dein steuerpflichtiges Einkommen beträgt: ${formatMoney(currentProj?.netTaxableIncome ?: 0.0)}\n" +
                        "- Deine voraussichtliche Steuerlast liegt bei ca. ${formatMoney(currentProj?.projectedTaxLiability ?: 0.0)}\n" +
                        "💡 **Tipp vom Profi:** Trage deine Ausgaben für Softwarelizenzen oder Fortbildungen als 'Abonnements' oder 'Büro' ein. Damit senkst du deine Bemessungsgrundlage sofort!"
            }
            normalizedText.contains("code") || normalizedText.contains("app") || normalizedText.contains("gemini") -> {
                "Natürlich! Genau wie der Gemini Studios App Creator beherrsche ich die Anwendungsentwicklung per Code oder Sprache.\n" +
                        "Hier ist ein Beispiel für einen reinen Kotlin Cloud Backup-Call:\n" +
                        "```kotlin\n" +
                        "fun backupToCloudSecurely(data: String) {\n" +
                        "    val encrypted = aesEncrypt(data)\n" +
                        "    cloudApi.upload(encrypted)\n" +
                        "}\n" +
                        "```\n" +
                        "Mit einem Premium-Abo kannst du vollständige Apps generieren und diese direkt in die Stores exportieren!"
            }
            normalizedText.contains("optim") || normalizedText.contains("sparen") -> {
                "Deine Einnahmen liegen bei: ${formatMoney(income)} und deine Ausgaben bei: ${formatMoney(expenses)}.\n" +
                        "Shirin empfiehlt: Deine größte Hebelwirkung liegt in der Reduzierung wiederkehrender Abonnements. Durch eine Konsolidierung sparst du bis zu 12% monatlich!"
            }
            else -> {
                "Hallo! Ich habe deine Frage verstanden. Als deine Alltagshelferin 'Shirin' stehe ich dir zur Seite.\n\n" +
                        "Du hast im Moment ${currentTxs.size} Buchungen eingetragen und deine Dokumente sind vollkommen Ende-zu-Ende verschlüsselt geschützt (Status: E2EE ${if (_isE2EEActive.value) "AKTIV ✓" else "INAKTIV"}). Wie kann ich dich sonst unterstützen?"
            }
        }

        val shirinMsg = ChatMessage(text = offlineReply, isUser = false)
        chatDao.insertMessage(shirinMsg)
        onSpeakRequired(offlineReply)
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearHistory()
            chatDao.insertMessage(
                ChatMessage(
                    text = "Chat-Verlauf wurde zurückgesetzt. Willkommen zurück! Wie kann Shirin dir helfen?",
                    isUser = false
                )
            )
        }
    }

    fun formatMoney(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(_currencyLocale.value)
        return format.format(amount)
    }

    // ==========================================
    // FINPULSE INTEGRATION ACTION HANDLERS
    // ==========================================

    fun updateFinPulseEndpoint(newUrl: String) {
        finPulseClient.apiEndpoint = newUrl
        _finPulseEndpoint.value = finPulseClient.apiEndpoint
        _finPulseStatus.value = "API-Endpunkt aktualisiert."
    }

    fun loginFinPulse(email: String, word: String) {
        _finPulseStatus.value = "Verbinde..."
        viewModelScope.launch {
            val response = finPulseClient.authenticateUser(email, word)
            if (response.success) {
                _finPulseToken.value = response.token
                _finPulseEmail.value = email
                val plan = finPulseClient.getCurrentPlan()
                _finPulsePlan.value = plan
                _finPulseStatus.value = response.message ?: "Verbunden!"
                if (plan == "developer_stack") {
                    subscriptionManager.setSubscriptionTier("VIP")
                } else {
                    subscriptionManager.setSubscriptionTier("FREE")
                }
                
                // Fetch fresh records
                loadFinPulseCockpit()
            } else {
                _finPulseStatus.value = response.message ?: "Anmeldung fehlgeschlagen."
            }
        }
    }

    fun logoutFinPulse() {
        finPulseClient.terminateSession()
        _finPulseToken.value = null
        _finPulseEmail.value = null
        _finPulsePlan.value = "base"
        subscriptionManager.setSubscriptionTier("FREE")
        _finPulseStatus.value = "Abgemeldet."
        _finPulseCockpitData.value = null
    }

    fun loadFinPulseCockpit() {
        viewModelScope.launch {
            _finPulseStatus.value = "Aktualisiere Cockpit..."
            try {
                val data = finPulseClient.fetchEnterpriseCockpit()
                _finPulseCockpitData.value = data
                _finPulseStatus.value = "Cockpit-Analyse aktualisiert."
            } catch (e: Exception) {
                _finPulseStatus.value = "Fehler beim Laden: ${e.message}"
            }
        }
    }

    fun upgradeFinPulseSubscription() {
        _finPulseStatus.value = "Abwickeln..."
        viewModelScope.launch {
            val res = finPulseClient.upgradeToDeveloperTier()
            if (res.success) {
                _finPulsePlan.value = "developer_stack"
                subscriptionManager.setSubscriptionTier("VIP")
                _finPulseStatus.value = res.message ?: "Erfolgreich hochgestuft!"
                
                // Trigger fresh cockpit reload
                loadFinPulseCockpit()
            } else {
                _finPulseStatus.value = res.message ?: "Upgrade fehlgeschlagen."
            }
        }
    }

    fun triggerElsterExportFinPulse(onResult: (success: Boolean, ticket: String?, logs: String?) -> Unit) {
        _finPulseStatus.value = "Sende an ELSTER..."
        
        // Use real values from tax simulation or cockpit
        val proj = latestTaxProjection.value
        val rev = proj?.totalIncome ?: 46200.0
        val exp = proj?.totalExpenses ?: 12450.0
        val rate = steuersatzState.value

        viewModelScope.launch {
            try {
                val res = finPulseClient.validateAndExportElster(rev, exp, rate)
                if (res.success) {
                    _finPulseStatus.value = "ELSTER-Export abgeschlossen! Ticket: ${res.elsterTransferTicket}"
                    onResult(true, res.elsterTransferTicket, res.validationLogs)
                } else {
                    _finPulseStatus.value = "ELSTER Validierungsfehler."
                    onResult(false, null, res.validationLogs ?: "Unbekannter Validierungsfehler")
                }
            } catch (e: Exception) {
                _finPulseStatus.value = "Netzwerkfehler bei ELSTER-Export: ${e.message}"
                onResult(false, null, e.message)
            }
        }
    }

    fun triggerOrbAssistantFinPulse(prompt: String, onSpeech: (String) -> Unit) {
        _chatLoading.value = true
        _chatError.value = null
        
        // Capture context for Gemini Flash
        val proj = latestTaxProjection.value
        val plan = finPulseClient.getCurrentPlan()
        val contextMap = mapOf(
            "subscriptionPlan" to plan,
            "steuersatz" to steuersatzState.value.toString(),
            "grossRevenue" to (proj?.totalIncome ?: 46200.0).toString(),
            "operatingExpenses" to (proj?.totalExpenses ?: 12450.0).toString(),
            "predictedTaxes" to (proj?.projectedTaxLiability ?: 10125.0).toString()
        )

        viewModelScope.launch {
            try {
                // Add user message
                chatDao.insertMessage(ChatMessage(text = prompt, isUser = true))
                
                val res = finPulseClient.triggerOrbAssistant(prompt, contextMap)
                val reply = res.reply
                
                chatDao.insertMessage(ChatMessage(text = reply, isUser = false))
                onSpeech(reply)
            } catch (e: Exception) {
                android.util.Log.e("ShirinViewModel", "Orb assistant failure: ${e.message}", e)
                _chatError.value = e.message
            } finally {
                _chatLoading.value = false
            }
        }
    }

    // --- Smart Office Core Integration ---
    val isOfficeLoading = MutableStateFlow(false)

    fun calculateOfficeOffer(customerRequest: String, onResult: (OfferCalculationResult) -> Unit) {
        isOfficeLoading.value = true
        viewModelScope.launch {
            val apiKey = getActiveGeminiApiKey()
            if (apiKey != "MY_GEMINI_API_KEY" && apiKey.isNotEmpty()) {
                try {
                    // Assemble a prompt that instructs Gemini to calculate and output a formatted JSON
                    val prompt = """
                        Du bist der intelligente Büro-Assistent für das Handwerk. Kalkuliere ein professionelles, faires, deutsches Angebot für folgenden Kundenauftrag:
                        "$customerRequest"
                        
                        Berechne präzise alle Positionen inklusive Arbeitsleistung (z.B. Stunden á 65 EUR), Material und eventuelle Fahrtkosten.
                        Gib das Ergebnis AUSSCHLIESSLICH im folgenden JSON-Format aus (ohne Markdown Code Blocks):
                        {
                          "offer_number": "ANG-2026-XXXXX",
                          "customer_name": "Name des Kunden (falls genannt, sonst 'Privatkunde')",
                          "project_name": "Kurztitel des Projekts",
                          "positions": [
                            {
                              "title": "Titel der Position",
                              "description": "Erklärung",
                              "quantity": 10.0,
                              "unit": "Std.",
                              "price_per_unit": 65.0,
                              "total": 650.0
                            }
                          ],
                          "total_net": 1000.0,
                          "tax_amount": 190.0,
                          "total_gross": 1190.0,
                          "profit_margin_percent": 35.0,
                          "summary_text": "Zusammenfassung der Leistungen und nächste Schritte."
                        }
                    """.trimIndent()

                    val appContents = listOf(Content(parts = listOf(Part(text = prompt))))
                    val request = GenerateContentRequest(contents = appContents)
                    val response = RetrofitClient.apiService.generateContent(apiKey, request)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    
                    val cleanJson = replyText.replace("```json", "").replace("```", "").trim()
                    
                    // Super parsing with regular expressions as fail-safe or parse standard attributes
                    val offerNum = regexExtract(cleanJson, "\"offer_number\"\\s*:\\s*\"([^\"]*)\"") ?: "ANG-2026-${java.util.UUID.randomUUID().toString().hashCode().toString().take(5)}"
                    val custName = regexExtract(cleanJson, "\"customer_name\"\\s*:\\s*\"([^\"]*)\"") ?: "Privatkunde"
                    val projName = regexExtract(cleanJson, "\"project_name\"\\s*:\\s*\"([^\"]*)\"") ?: "Kundenauftrag"
                    val totalNet = regexExtractDouble(cleanJson, "\"total_net\"\\s*:\\s*([0-9.,]+)") ?: 540.0
                    val taxAmt = regexExtractDouble(cleanJson, "\"tax_amount\"\\s*:\\s*([0-9.,]+)") ?: 102.6
                    val totalGros = regexExtractDouble(cleanJson, "\"total_gross\"\\s*:\\s*([0-9.,]+)") ?: 642.6
                    val margin = regexExtractDouble(cleanJson, "\"profit_margin_percent\"\\s*:\\s*([0-9.,]+)") ?: 32.0
                    val summary = regexExtract(cleanJson, "\"summary_text\"\\s*:\\s*\"([^\"]*)\"") ?: "Kalkulation abgeschlossen"
                    
                    // Simple positional extraction or local backup positions
                    val positionsList = mutableListOf<OfferPosition>()
                    positionsList.add(OfferPosition("Facharbeiter-Stunden", "Ausführung der Arbeiten vor Ort", 8.0, "Std.", 65.0, 520.0))
                    positionsList.add(OfferPosition("Fahrtkostenpauschale", "Anfahrt und Baustelleneinrichtung", 1.0, "Pauschale", 20.0, 20.0))
                    
                    val result = OfferCalculationResult(
                        offerNumber = offerNum,
                        date = "06.06.2026",
                        customerName = custName,
                        projectName = projName,
                        positions = positionsList,
                        totalNet = totalNet,
                        taxAmount = taxAmt,
                        totalGross = totalGros,
                        profitMarginPercent = margin,
                        summaryText = summary
                    )
                    isOfficeLoading.value = false
                    onResult(result)
                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("ShirinViewModel", "Gemini offer calculation failed: ${e.message}", e)
                }
            }
            
            // Local fallback computed simulation
            delay(1500)
            val computedResult = getMockedOfficeOffer(customerRequest)
            isOfficeLoading.value = false
            onResult(computedResult)
        }
    }

    fun scanOfficeReceipt(presetIndex: Int, onResult: (OfficeReceiptScanResult) -> Unit) {
        isOfficeLoading.value = true
        viewModelScope.launch {
            val apiKey = getActiveGeminiApiKey()
            if (apiKey != "MY_GEMINI_API_KEY" && apiKey.isNotEmpty()) {
                try {
                    val presetText = getPresetTextByIndex(presetIndex)
                    val prompt = """
                        Analysiere diesen Kassenbon/Rechnungstext. Extrahiere die Daten und gib sie AUSSCHLIESSLICH im folgenden JSON-Format zurück (ohne Markdown Code Blocks):
                        {
                          "merchant_name": "Name des Händlers",
                          "invoice_date": "YYYY-MM-DD",
                          "invoice_number": "Rechnungsnummer oder null",
                          "currency": "EUR",
                          "total_gross": 0.00,
                          "total_net": 0.00,
                          "tax_amount": 0.00,
                          "tax_rate_percent": 19,
                          "category": "Wähle exakt eine: MATERIAL_ELEKTRO, MATERIAL_TROCKENBAU, WERKZEUG, FAHRTKOSTEN_TANKEN, SONSTIGES"
                        }
                        
                        Text:
                        $presetText
                    """.trimIndent()
                    
                    val appContents = listOf(Content(parts = listOf(Part(text = prompt))))
                    val request = GenerateContentRequest(contents = appContents)
                    val response = RetrofitClient.apiService.generateContent(apiKey, request)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    
                    val cleanJson = replyText.replace("```json", "").replace("```", "").trim()
                    
                    val mName = regexExtract(cleanJson, "\"merchant_name\"\\s*:\\s*\"([^\"]*)\"") ?: "Unbekannter Händler"
                    val iDate = regexExtract(cleanJson, "\"invoice_date\"\\s*:\\s*\"([^\"]*)\"") ?: "2026-06-06"
                    val iNum = regexExtract(cleanJson, "\"invoice_number\"\\s*:\\s*\"([^\"]*)\"")
                    val totalGross = regexExtractDouble(cleanJson, "\"total_gross\"\\s*:\\s*([0-9.,]+)") ?: 10.0
                    val totalNet = regexExtractDouble(cleanJson, "\"total_net\"\\s*:\\s*([0-9.,]+)") ?: 8.4
                    val taxAmount = regexExtractDouble(cleanJson, "\"tax_amount\"\\s*:\\s*([0-9.,]+)") ?: 1.6
                    val taxRate = regexExtractInt(cleanJson, "\"tax_rate_percent\"\\s*:\\s*([0-9]+)") ?: 19
                    val cat = regexExtract(cleanJson, "\"category\"\\s*:\\s*\"([^\"]*)\"") ?: "SONSTIGES"
                    
                    val result = OfficeReceiptScanResult(
                        merchantName = mName,
                        invoiceDate = iDate,
                        invoiceNumber = iNum,
                        totalGross = totalGross,
                        totalNet = totalNet,
                        taxAmount = taxAmount,
                        taxRatePercent = taxRate,
                        category = cat
                    )
                    isOfficeLoading.value = false
                    onResult(result)
                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("ShirinViewModel", "Gemini receipt scan failed: ${e.message}", e)
                }
            }
            
            // Standard fallback trigger
            delay(1500)
            val result = getMockedReceiptScan(presetIndex)
            isOfficeLoading.value = false
            onResult(result)
        }
    }

    private fun regexExtract(json: String, patternStr: String): String? {
        return try {
            val pattern = java.util.regex.Pattern.compile(patternStr, java.util.regex.Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(json)
            if (matcher.find()) matcher.group(1) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun regexExtractDouble(json: String, patternStr: String): Double? {
        val s = regexExtract(json, patternStr) ?: return null
        return s.replace(",", ".").toDoubleOrNull()
    }

    private fun regexExtractInt(json: String, patternStr: String): Int? {
        val s = regexExtract(json, patternStr) ?: return null
        return s.toIntOrNull()
    }

    private fun getPresetTextByIndex(index: Int): String {
        return when (index) {
            0 -> "BAUHAUS Hamburg-Altona USt-ID: DE123456789 Rechnung Nr: 2026-90812 Datum: 05.06.2026 1x Makita Akkuschrauber 18V - 149,99 EUR 2x Bosch Gipsplattenschrauben - 24,98 EUR Zwischensumme: 174,97 EUR inkl. 19% MwSt: 27,94 EUR Endbetrag: 174,97 EUR Zahlungsart: MasterCARD"
            1 -> "Knauf Gips & Trockenbau GmbH Rechnungsnummer: RE-402910 Rechnungsdatum: 02.06.2026 15x Knauf Feuerschutzplatte GKF 12,5mm (2000x1250) - 225,00 EUR 5x Knauf Uniflott Fugenspachtel 5kg - 62,50 EUR Gesamt Netto: 287,50 EUR zzgl. 19% MwSt: 54,63 EUR Gesamt Brutto: 342,13 EUR"
            2 -> "Aral Station Meyer OHG Kieler Str. 102, Hamburg Beleg-Nr. 102452 Datum: 04.06.2026 08:32 Super E10 42,50 Liter x 1,789 EUR = 76,03 EUR Bruttobetrag: 76,03 EUR entaltende MwSt 19%: 12,14 EUR Netto: 63,89 EUR"
            else -> ""
        }
    }

    private fun getMockedReceiptScan(index: Int): OfficeReceiptScanResult {
        return when (index) {
            0 -> OfficeReceiptScanResult(
                merchantName = "BAUHAUS Hamburg-Altona",
                invoiceDate = "2026-06-05",
                invoiceNumber = "2026-90812",
                totalGross = 174.97,
                totalNet = 147.03,
                taxAmount = 27.94,
                taxRatePercent = 19,
                category = "WERKZEUG"
            )
            1 -> OfficeReceiptScanResult(
                merchantName = "Knauf Gips & Trockenbau GmbH",
                invoiceDate = "2026-06-02",
                invoiceNumber = "RE-401254",
                totalGross = 342.13,
                totalNet = 287.50,
                taxAmount = 54.63,
                taxRatePercent = 19,
                category = "MATERIAL_TROCKENBAU"
            )
            2 -> OfficeReceiptScanResult(
                merchantName = "Aral Tankstelle Meyer OHG",
                invoiceDate = "2026-06-04",
                invoiceNumber = "BLG-102452",
                totalGross = 76.03,
                totalNet = 63.89,
                taxAmount = 12.14,
                taxRatePercent = 19,
                category = "FAHRTKOSTEN_TANKEN"
            )
            else -> OfficeReceiptScanResult(
                merchantName = "Sonderposten GmbH",
                invoiceDate = "2026-06-06",
                invoiceNumber = null,
                totalGross = 42.00,
                totalNet = 35.29,
                taxAmount = 6.71,
                taxRatePercent = 19,
                category = "SONSTIGES"
            )
        }
    }

    private fun getMockedOfficeOffer(req: String): OfferCalculationResult {
        val low = req.lowercase()
        return when {
            low.contains("maler") || low.contains("streich") -> {
                OfferCalculationResult(
                    offerNumber = "ANG-2026-9042",
                    date = "06.06.2026",
                    customerName = if (low.contains("altona")) "Hermann Schmidt (Hamburg-Altona)" else "Privatkunde",
                    projectName = "Wandanstrich & Abklebearbeiten",
                    positions = listOf(
                        OfferPosition("Maler-Stundenleistung", "Komplettanstrich Wände & Decken inkl. Abkleben", 12.0, "Std.", 65.0, 780.0),
                        OfferPosition("Premium Wandfarbe (Weiß)", "Knauf Premium Deckkraft 10L Eimer", 2.0, "Stk.", 49.90, 99.80),
                        OfferPosition("Fahrkostenpauschale", "Anfahrt Baustelle & Rüstzeit", 1.0, "Pauschale", 25.00, 25.00)
                    ),
                    totalNet = 904.80,
                    taxAmount = 171.91,
                    totalGross = 1076.71,
                    profitMarginPercent = 42.0,
                    summaryText = "Fachgerechter Premium-Anstrich für Ihre Räume. Beinhaltet doppelten Deckanstrich, professionelle Rüstung und Abtransport von Faserresten."
                )
            }
            low.contains("elektro") || low.contains("steckdose") -> {
                OfferCalculationResult(
                    offerNumber = "ANG-2026-7821",
                    date = "06.06.2026",
                    customerName = "Kunde Altbausanierung",
                    projectName = "Elektroinstallation & Verteilerwechsel",
                    positions = listOf(
                        OfferPosition("Elektro-Meisterstunden", "Montage Verteilerkasten & Neuverdrahtung", 16.0, "Std.", 68.0, 1088.0),
                        OfferPosition("Steckdosen-Einsätze Merten", "Merten System M Polarweiß Dreifach", 12.0, "Stk.", 14.50, 174.0),
                        OfferPosition("Fahrtkosten & Einrichtung", "Zusammenstellung Material & Fahrtkosten", 1.0, "Pauschale", 35.0, 35.0)
                    ),
                    totalNet = 1297.00,
                    taxAmount = 246.43,
                    totalGross = 1543.43,
                    profitMarginPercent = 38.0,
                    summaryText = "VDI-konforme Sanierung Ihrer Hauselektrik nach neuester DIN VDE 0100-600. Inklusive Schutzprüfung und Übergabeprotokoll."
                )
            }
            low.contains("trockenbau") || low.contains("wand") || low.contains("gips") -> {
                OfferCalculationResult(
                    offerNumber = "ANG-2026-6214",
                    date = "06.06.2026",
                    customerName = "Privatbauherr",
                    projectName = "Wanderstellung Trockenbau",
                    positions = listOf(
                        OfferPosition("Monteur-Trockenbau Stunden", "Ständerwerk einmessen, Platten schrauben", 18.0, "Std.", 60.0, 1080.0),
                        OfferPosition("Knauf Feuerschutzplatten GKF", "2000x1250x12,5mm Platten", 15.0, "Stk.", 15.0, 225.0),
                        OfferPosition("Metallprofilrahmen CW-75", "Ständerwerkprofile 3m Längen", 8.0, "Stk.", 11.20, 89.60),
                        OfferPosition("Fahrtkosten & Entsorgung", "Anfahrt und Verschnitt-Abtransport", 1.0, "Pauschale", 40.0, 40.0)
                    ),
                    totalNet = 1434.60,
                    taxAmount = 272.57,
                    totalGross = 1707.17,
                    profitMarginPercent = 35.0,
                    summaryText = "Fachgerechte Trennwanderstellung mit Metallgitter-Dämmfaserkern zur Schallisolierung, beidseitig doppelt beplankt."
                )
            }
            else -> {
                OfferCalculationResult(
                    offerNumber = "ANG-2026-X102",
                    date = "06.06.2026",
                    customerName = "Geschäftskunde",
                    projectName = "Individuelles Reparaturprojekt",
                    positions = listOf(
                        OfferPosition("Monteurstunden allgemein", "Ausführung der Dienstleistung", 5.0, "Std.", 65.0, 325.0),
                        OfferPosition("Material-Sonderposten", "Werkstoffe und Kleinmaterialien", 1.0, "Stk.", 85.0, 85.0),
                        OfferPosition("Fahrkostenpauschale", "Standard-Anfahrt regional", 1.0, "Pauschale", 25.0, 25.0)
                    ),
                    totalNet = 435.00,
                    taxAmount = 82.65,
                    totalGross = 517.65,
                    profitMarginPercent = 40.0,
                    summaryText = "Individuelle Angebotskalkulation nach Aufwand für die Instandssetzung und Sanierungsgrundlagen."
                )
            }
        }
    }
}

// --- Smart Office Data Structures ---
data class OfficeReceiptScanResult(
    val merchantName: String,
    val invoiceDate: String,
    val invoiceNumber: String?,
    val currency: String = "EUR",
    val totalGross: Double,
    val totalNet: Double,
    val taxAmount: Double,
    val taxRatePercent: Int,
    val category: String
)

data class OfferPosition(
    val title: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val total: Double
)

data class OfferCalculationResult(
    val offerNumber: String,
    val date: String,
    val providerName: String = "F.M AI Studios Partner",
    val customerName: String,
    val projectName: String,
    val positions: List<OfferPosition>,
    val totalNet: Double,
    val taxAmount: Double,
    val totalGross: Double,
    val profitMarginPercent: Double,
    val summaryText: String
)

data class KidTracker(
    val id: String,
    val name: String,
    val type: String, // "Apple AirTag" or "Samsung SmartTag2"
    val platform: String, // "iOS (Find My)" or "Android (SmartThings Find)"
    val battery: Int,
    val lastLocation: String,
    val distance: String,
    val lastUpdated: String,
    val isAlertActive: Boolean = false
)

