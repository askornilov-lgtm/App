package com.askornilov.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askornilov.app.MainViewModel
import com.askornilov.app.data.Person
import com.askornilov.app.data.Priority
import com.askornilov.app.data.Step
import com.askornilov.app.data.Task
import com.askornilov.app.data.TaskStatus
import com.askornilov.app.ui.common.ConfirmDialog
import com.askornilov.app.ui.common.DateText
import com.askornilov.app.ui.common.Dates
import com.askornilov.app.ui.common.screenInsets

private sealed interface StepAction {
    data object Add : StepAction
    data class Edit(val step: Step) : StepAction
    data object AfterDone : StepAction
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskPane(
    task: Task,
    person: Person?,
    steps: List<Step>,
    vm: MainViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var stepAction by remember { mutableStateOf<StepAction?>(null) }
    val current = steps.firstOrNull { !it.done }
    val history = steps.filter { it.done }.sortedByDescending { it.doneAt ?: it.createdAt }

    Scaffold(
        contentWindowInsets = screenInsets,
        topBar = {
            TopAppBar(
                title = { Text("Задача") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Изменить") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Удалить") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(task.title, style = MaterialTheme.typography.headlineSmall)
            if (person != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(person.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (task.priority != Priority.NORMAL) {
                    Text("Приоритет: ${task.priority.title.lowercase()}", style = MaterialTheme.typography.labelLarge)
                }
                task.dueDate?.let {
                    Text("Срок:", style = MaterialTheme.typography.labelLarge)
                    DateText(it)
                }
            }
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodyLarge)
            }

            // Статус
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskStatus.entries.forEach { s ->
                    FilterChip(
                        selected = task.status == s,
                        onClick = { if (task.status != s) vm.setStatus(task, s) },
                        label = { Text(s.title) },
                        leadingIcon = { StatusDot(s) },
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
            }

            // Следующий шаг — главное
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Следующий шаг", style = MaterialTheme.typography.labelLarge)
                    if (current != null) {
                        Text(current.text, style = MaterialTheme.typography.titleMedium)
                        current.dueDate?.let { DateText(it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                vm.completeStep(current)
                                stepAction = StepAction.AfterDone
                            }) {
                                Icon(Icons.Filled.Check, null)
                                Text(" Выполнено")
                            }
                            OutlinedButton(onClick = { stepAction = StepAction.Edit(current) }) { Text("Изменить") }
                        }
                    } else if (task.status == TaskStatus.DONE) {
                        Text("Задача завершена", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text("Не задан — задача может «зависнуть».", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { stepAction = StepAction.Add }) {
                            Icon(Icons.Filled.Add, null)
                            Text(" Добавить шаг")
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                Text("История шагов", style = MaterialTheme.typography.titleMedium)
                history.forEach { s ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Outlined.CheckCircle, null, Modifier.size(20.dp),
                            tint = TaskStatus.DONE.color(),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.text, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            s.doneAt?.let {
                                Text(
                                    "выполнено: ${Dates.labelMillis(it)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.size(32.dp))
        }
    }

    when (val a = stepAction) {
        null -> Unit
        StepAction.Add -> StepDialog(
            title = "Следующий шаг",
            onDismiss = { stepAction = null },
            onSave = { text, date -> vm.addStep(task.id, text, date); stepAction = null },
        )
        is StepAction.Edit -> StepDialog(
            title = "Изменить шаг",
            initialText = a.step.text,
            initialDate = a.step.dueDate,
            onDismiss = { stepAction = null },
            onSave = { text, date -> vm.updateStep(a.step.copy(text = text, dueDate = date)); stepAction = null },
        )
        StepAction.AfterDone -> StepDialog(
            title = "Шаг выполнен ✓",
            afterDone = true,
            onDismiss = { stepAction = null },
            onSave = { text, date ->
                vm.addStep(task.id, text, date)
                if (task.status == TaskStatus.NEW) vm.setStatus(task, TaskStatus.IN_PROGRESS)
                stepAction = null
            },
            onFinishTask = { vm.setStatus(task, TaskStatus.DONE); stepAction = null },
        )
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Удалить задачу?",
            text = "«${task.title}» и вся история шагов будут удалены.",
            onConfirm = { vm.deleteTask(task); onBack() },
            onDismiss = { confirmDelete = false },
        )
    }
}
