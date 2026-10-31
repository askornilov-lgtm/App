package com.askornilov.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askornilov.app.data.PersonType

fun PersonType.uiColor() = Color(color)

@Composable
fun Avatar(name: String, type: PersonType, size: Dp = 40.dp) {
    val initials = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier.size(size).background(type.uiColor(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials.ifEmpty { "?" },
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.38f).sp,
        )
    }
}

@Composable
fun TypeBadge(type: PersonType) {
    Text(
        type.title,
        color = type.uiColor(),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(type.uiColor().copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
fun OverdueBadge(text: String = "Просрочено") {
    Text(
        text,
        color = MaterialTheme.colorScheme.onError,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun DateText(epochDay: Long, modifier: Modifier = Modifier) {
    val overdue = epochDay < Dates.today()
    val isToday = epochDay == Dates.today()
    Text(
        Dates.label(epochDay),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = when {
            overdue -> MaterialTheme.colorScheme.error
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontWeight = if (overdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
fun EmptyState(title: String, subtitle: String? = null) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirm: String = "Удалить",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirm, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

/** Быстрый выбор даты: Сегодня / Завтра / Через неделю / календарь. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateChooser(value: Long?, onChange: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val today = Dates.today()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = value == today, onClick = { onChange(today) }, label = { Text("Сегодня") })
        FilterChip(selected = value == today + 1, onClick = { onChange(today + 1) }, label = { Text("Завтра") })
        FilterChip(selected = value == today + 7, onClick = { onChange(today + 7) }, label = { Text("Через неделю") })
        if (value != null && value !in listOf(today, today + 1, today + 7)) {
            InputChip(
                selected = true,
                onClick = { showPicker = true },
                label = { Text(Dates.label(value)) },
                leadingIcon = { Icon(Icons.Outlined.Event, null, Modifier.size(18.dp)) },
            )
        } else {
            AssistChip(
                onClick = { showPicker = true },
                label = { Text("Дата…") },
                leadingIcon = { Icon(Icons.Outlined.Event, null, Modifier.size(18.dp)) },
            )
        }
        if (value != null) {
            AssistChip(onClick = { onChange(null) }, label = { Text("Без даты") })
        }
    }
    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = (value ?: today).let(Dates::toPicker))
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(Dates.fromPicker(it)) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } },
        ) {
            DatePicker(state = state)
        }
    }
}
