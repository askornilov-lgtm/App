package com.askornilov.app.ui.people

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askornilov.app.data.Person
import com.askornilov.app.data.Priority
import com.askornilov.app.data.Step
import com.askornilov.app.data.Task
import com.askornilov.app.data.TaskStatus
import com.askornilov.app.ui.common.Avatar
import com.askornilov.app.ui.common.ConfirmDialog
import com.askornilov.app.ui.common.DateText
import com.askornilov.app.ui.common.TypeBadge
import com.askornilov.app.ui.common.screenInsets
import com.askornilov.app.ui.tasks.StatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonPane(
    person: Person,
    tasks: List<Task>,
    steps: List<Step>,
    showBack: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
) {
    val ctx = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    var showDone by rememberSaveable { mutableStateOf(false) }
    val open = tasks.filter { it.status != TaskStatus.DONE }
    val done = tasks.filter { it.status == TaskStatus.DONE }

    Scaffold(
        contentWindowInsets = screenInsets,
        topBar = {
            TopAppBar(
                title = { Text(person.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (showBack) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Изменить") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Удалить") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Задача") },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(person.name, person.type, 56.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(person.name, style = MaterialTheme.typography.titleLarge)
                                TypeBadge(person.type)
                            }
                        }
                        // Пустые поля не показываем.
                        if (person.company.isNotBlank()) InfoLine(Icons.Outlined.Business, person.company)
                        if (person.position.isNotBlank()) InfoLine(Icons.Outlined.Badge, person.position)
                        if (person.phone.isNotBlank()) {
                            InfoLine(Icons.Outlined.Phone, person.phone, link = true) {
                                ctx.open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}")))
                            }
                        }
                        if (person.email.isNotBlank()) {
                            InfoLine(Icons.Outlined.Email, person.email, link = true) {
                                ctx.open(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${person.email}")))
                            }
                        }
                        if (person.note.isNotBlank()) InfoLine(Icons.Outlined.Notes, person.note)
                    }
                }
            }

            item {
                Text(
                    if (open.isEmpty()) "Открытых задач нет" else "Задачи · ${open.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(open, key = { it.id }) { t -> TaskRow(t, currentStep(t, steps)) { onOpenTask(t.id) } }

            if (done.isNotEmpty()) {
                item {
                    TextButton(onClick = { showDone = !showDone }) {
                        Text("Завершённые · ${done.size}")
                        Icon(if (showDone) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null)
                    }
                }
                if (showDone) {
                    items(done, key = { it.id }) { t -> TaskRow(t, null) { onOpenTask(t.id) } }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Удалить «${person.name}»?",
            text = "Будут удалены и все задачи с шагами по этому человеку.",
            onConfirm = onDelete,
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String, link: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().let { if (onClick != null) it.clickableRow(onClick) else it }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (link) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TaskRow(task: Task, step: Step?, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { StatusDot(task.status) },
            headlineContent = {
                Text(
                    task.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                )
            },
            supportingContent = {
                Column {
                    if (step != null) {
                        Text("→ ${step.text}", maxLines = 2, overflow = TextOverflow.Ellipsis)
                    } else if (task.status != TaskStatus.DONE) {
                        Text("Нет следующего шага", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(task.status.title)
                    }
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    if (task.priority == Priority.HIGH) Text("❗", style = MaterialTheme.typography.labelMedium)
                    (step?.dueDate ?: task.dueDate)?.let { DateText(it) }
                }
            },
        )
    }
}

private fun Context.open(intent: Intent) {
    try { startActivity(intent) } catch (_: ActivityNotFoundException) { }
}
