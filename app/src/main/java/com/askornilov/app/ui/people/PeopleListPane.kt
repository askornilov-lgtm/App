package com.askornilov.app.ui.people

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askornilov.app.data.Person
import com.askornilov.app.data.PersonType
import com.askornilov.app.ui.common.Avatar
import com.askornilov.app.ui.common.DateText
import com.askornilov.app.ui.common.EmptyState
import com.askornilov.app.ui.common.OverdueBadge
import com.askornilov.app.ui.common.TypeBadge
import com.askornilov.app.ui.common.screenInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListPane(
    persons: List<Person>,
    summaries: Map<Long, PersonSummary>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<PersonType?>(null) }
    var asCards by rememberSaveable { mutableStateOf(true) }

    val shown = persons.filter { p ->
        (filter == null || p.type == filter) &&
            (query.isBlank() || listOf(p.name, p.company, p.position, p.phone)
                .any { it.contains(query.trim(), ignoreCase = true) })
    }

    Scaffold(
        contentWindowInsets = screenInsets,
        topBar = {
            TopAppBar(
                title = { Text("Люди") },
                actions = {
                    IconButton(onClick = { asCards = !asCards }) {
                        Icon(
                            if (asCards) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                            if (asCards) "Списком" else "Карточками",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Filled.Add, "Добавить человека") }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Поиск по имени, компании, телефону") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Очистить") } }
                } else null,
                singleLine = true,
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Все") })
                PersonType.people.forEach { t ->
                    FilterChip(
                        selected = filter == t,
                        onClick = { filter = if (filter == t) null else t },
                        label = { Text(t.plural) },
                    )
                }
            }

            when {
                persons.isEmpty() -> EmptyState("Пока никого нет", "Нажмите «+», чтобы добавить коллегу, партнёра или контрагента.")
                shown.isEmpty() -> EmptyState("Ничего не найдено")
                asCards -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(shown, key = { it.id }) { p ->
                        PersonCard(p, summaries[p.id], p.id == selectedId) { onSelect(p.id) }
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(shown, key = { it.id }) { p ->
                        PersonRow(p, summaries[p.id], p.id == selectedId) { onSelect(p.id) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(p: Person, s: PersonSummary?, selected: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        colors = if (selected) CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) else CardDefaults.elevatedCardColors(),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(p.name, p.type)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (p.company.isNotBlank()) {
                        Text(
                            p.company, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TypeBadge(p.type)
                if (s?.overdue == true) OverdueBadge("!")
            }
            Spacer(Modifier.height(6.dp))
            Text(
                tasksLabel(s?.openTasks ?: 0),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            s?.nextStep?.let { step ->
                Spacer(Modifier.height(4.dp))
                Text("→ ${step.text}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                step.dueDate?.let { DateText(it) }
            }
        }
    }
}

@Composable
private fun PersonRow(p: Person, s: PersonSummary?, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickableRow(onClick),
        colors = if (selected) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else ListItemDefaults.colors(),
        leadingContent = { Avatar(p.name, p.type) },
        headlineContent = { Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            val next = s?.nextStep
            Text(
                when {
                    next != null -> "→ ${next.text}"
                    p.company.isNotBlank() -> p.company
                    else -> p.type.title
                },
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                if (s?.overdue == true) OverdueBadge("!")
                s?.nextStep?.dueDate?.let { DateText(it) }
                if ((s?.openTasks ?: 0) > 0) {
                    Text("${s?.openTasks}", style = MaterialTheme.typography.labelMedium)
                }
            }
        },
    )
}

fun tasksLabel(n: Int): String {
    if (n == 0) return "Нет открытых задач"
    val mod10 = n % 10
    val mod100 = n % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "задача"
        mod10 in 2..4 && mod100 !in 12..14 -> "задачи"
        else -> "задач"
    }
    return "$n $word"
}
