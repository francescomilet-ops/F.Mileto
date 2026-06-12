package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Budget
import com.example.data.Transaction
import com.example.ui.theme.EmeraldAlert
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.ShirinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetingScreen(
    viewModel: ShirinViewModel,
    onBack: () -> Unit
) {
    val budgets by viewModel.currentBudgets.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgetierung ($month/$year)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Budget erstellen")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Behalte deine Ausgaben im Blick und setze Limits für deine Kategorien.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Noch keine Budgets festgelegt.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(budgets) { budget ->
                    BudgetCard(
                        budget = budget,
                        transactions = transactions,
                        onDelete = { viewModel.deleteBudget(budget) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { category, limit ->
                viewModel.insertBudget(
                    Budget(
                        category = category,
                        limitAmount = limit,
                        month = month,
                        year = year
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    transactions: List<Transaction>,
    onDelete: () -> Unit,
    viewModel: ShirinViewModel
) {
    val expensesInCategory = transactions.filter {
        !it.isIncome && it.category.equals(budget.category, ignoreCase = true)
    }.sumOf { it.amount }

    val progress = if (budget.limitAmount > 0) (expensesInCategory / budget.limitAmount).toFloat() else 0f
    val displayProgress = progress.coerceIn(0f, 1f)
    val color = when {
        progress >= 1f -> RoseAlert
        progress >= 0.8f -> androidx.compose.ui.graphics.Color(0xFFFFA000)
        else -> EmeraldAlert
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Löschen", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ausgaben: ${viewModel.formatMoney(expensesInCategory)}", style = MaterialTheme.typography.bodySmall)
                Text("Limit: ${viewModel.formatMoney(budget.limitAmount)}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { displayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            if (progress >= 1f) {
                Text(
                    "Achtung: Budget überschritten!",
                    color = RoseAlert,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var limitInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Budget erstellen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategorie (z.B. Lebensmittel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Limit (€)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitInput.replace(",", ".").toDoubleOrNull()
                    if (category.isNotBlank() && limit != null && limit > 0) {
                        onSave(category, limit)
                    }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
