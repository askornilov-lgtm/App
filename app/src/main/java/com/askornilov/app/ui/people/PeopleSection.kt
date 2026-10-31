package com.askornilov.app.ui.people

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askornilov.app.MainViewModel
import com.askornilov.app.data.Person
import com.askornilov.app.data.PersonType
import com.askornilov.app.data.Task
import com.askornilov.app.ui.common.EmptyState
import com.askornilov.app.ui.tasks.TaskEditorDialog
import com.askornilov.app.ui.tasks.TaskPane

/**
 * Раздел «Люди».
 * Узкий экран: список → человек → задача (по одному экрану).
 * Широкий (развёрнутый Fold): слева список, справа человек или задача.
 */
@Composable
fun PeopleSection(vm: MainViewModel, wide: Boolean) {
    val allPersons by vm.persons.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val steps by vm.steps.collectAsStateWithLifecycle()
    val persons = remember(allPersons) { allPersons.filter { it.type in PersonType.people } }
    val summaries = remember(persons, tasks, steps) { summarize(persons, tasks, steps) }

    var personId by rememberSaveable { mutableStateOf<Long?>(null) }
    var taskId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editPerson by remember { mutableStateOf<Person?>(null) }
    var editTask by remember { mutableStateOf<Task?>(null) }

    val person = persons.firstOrNull { it.id == personId }
    val task = tasks.firstOrNull { it.id == taskId }

    // Если человек/задача удалены — закрываем.
    LaunchedEffect(person, task, allPersons, tasks) {
        if (personId != null && person == null && allPersons.isNotEmpty()) personId = null
        if (taskId != null && task == null && tasks.isNotEmpty()) taskId = null
    }

    BackHandler(enabled = taskId != null || personId != null) {
        if (taskId != null) taskId = null else personId = null
    }

    val list: @Composable () -> Unit = {
        PeopleListPane(
            persons = persons,
            summaries = summaries,
            selectedId = if (wide) personId else null,
            onSelect = { personId = it; taskId = null },
            onAdd = { editPerson = Person(name = "", type = PersonType.COLLEAGUE) },
        )
    }

    val detail: @Composable () -> Unit = {
        when {
            task != null -> TaskPane(
                task = task,
                person = allPersons.firstOrNull { it.id == task.personId },
                steps = steps.filter { it.taskId == task.id },
                vm = vm,
                onBack = { taskId = null },
                onEdit = { editTask = task },
            )
            person != null -> PersonPane(
                person = person,
                tasks = sortTasks(tasks.filter { it.personId == person.id }, steps),
                steps = steps,
                showBack = !wide,
                onBack = { personId = null },
                onEdit = { editPerson = person },
                onDelete = { vm.deletePerson(person); personId = null },
                onOpenTask = { taskId = it },
                onAddTask = { editTask = Task(personId = person.id, title = "") },
            )
            else -> EmptyState("Выберите человека", "Слева — список коллег, партнёров и контрагентов.")
        }
    }

    if (wide) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.width(360.dp).fillMaxHeight()) { list() }
            VerticalDivider()
            Box(Modifier.weight(1f).fillMaxHeight()) { detail() }
        }
    } else {
        if (personId == null && taskId == null) list() else detail()
    }

    editPerson?.let { p ->
        PersonEditorDialog(
            initial = p,
            onDismiss = { editPerson = null },
            onSave = { saved ->
                vm.savePerson(saved) { id -> if (p.id == 0L) personId = id }
                editPerson = null
            },
        )
    }
    editTask?.let { t ->
        TaskEditorDialog(
            initial = t,
            onDismiss = { editTask = null },
            onSave = { saved, stepText, stepDate ->
                vm.saveTask(saved, stepText, stepDate)
                editTask = null
            },
        )
    }
}
