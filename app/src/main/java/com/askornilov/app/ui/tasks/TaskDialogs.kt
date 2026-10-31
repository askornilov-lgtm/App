package com.askornilov.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.askornilov.app.data.Priority
import com.askornilov.app.data.Task
import com.askornilov.app.data.TaskStatus
import com.askornilov.app.ui.common.DateChooser

fun TaskStatus.color(): Color = when (this) {
    TaskStatus.NEW -> Color(0xFF8E8E93)
    TaskStatus.IN_PROGRESS -> Color(0xFF3D7BF7)
    TaskStatus.WAITING -> Color(0xFFE08A1E)
    TaskStatus.DONE -> Color(0xFF2EAD6B)
}

@Composable
fun StatusDot(status: TaskStatus) {
    Box(Modifier.size(12.dp).background(status.color(), CircleShape))
}

private val sentences = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)

/** Создание/редактирование задачи. Для новой — сразу можно задать первый шаг. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    initial: Task,
    onDismiss: () -> Unit,
    onSave: (task: Task, firstStepText: String?, firstStepDate: Long?) -> Unit,
) {
    val isNew = initial.id == 0L
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var description by rememberSaveable { mutableStateOf(initial.description) }
    var priority by rememberSaveable { mutableStateOf(initial.priority) }
    var dueDate by rememberSaveable { mutableStateOf(initial.dueDate) }
    var stepText by rememberSaveable { mutableStateOf("") }
    var stepDate by rememberSaveable { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.94f),
        title = { Text(if (isNew) "Новая задача" else "Редактирование задачи") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    title, { title = it }, Modifier.fillMaxWidth(),
                    label = { Text("Задача *") }, keyboardOptions = sentences,
                )
                OutlinedTextField(
                    description, { description = it }, Modifier.fillMaxWidth(),
                    label = { Text("Описание") }, minLines = 2, keyboardOptions = sentences,
                )
                Text("Приоритет", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p.title) })
                    }
                }
                Text("Срок задачи", style = MaterialTheme.typography.labelLarge)
                DateChooser(dueDate) { dueDate = it }

                if (isNew) {
                    HorizontalDivider()
                    Text("Первый шаг", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        stepText, { stepText = it }, Modifier.fillMaxWidth(),
                        label = { Text("Что сделать") }, placeholder = { Text("Например: позвонить, уточнить цену") },
                        keyboardOptions = sentences,
                    )
                    if (stepText.isNotBlank()) DateChooser(stepDate) { stepDate = it }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        initial.copy(title = title.trim(), description = description.trim(), priority = priority, dueDate = dueDate),
                        stepText.takeIf { it.isNotBlank() },
                        stepDate,
                    )
                },
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

/**
 * Диалог шага. После выполнения шага (afterDone = true) спрашивает следующий
 * и предлагает завершить задачу.
 */
@Composable
fun StepDialog(
    title: String,
    initialText: String = "",
    initialDate: Long? = null,
    afterDone: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (text: String, date: Long?) -> Unit,
    onFinishTask: (() -> Unit)? = null,
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    var date by rememberSaveable { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    text, { text = it }, Modifier.fillMaxWidth(),
                    label = { Text(if (afterDone) "Следующий шаг" else "Что сделать") },
                    keyboardOptions = sentences,
                )
                DateChooser(date) { date = it }
                if (onFinishTask != null) {
                    TextButton(onClick = onFinishTask) { Text("✓ Задача выполнена полностью") }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim(), date) }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (afterDone) "Позже" else "Отмена") }
        },
    )
}
