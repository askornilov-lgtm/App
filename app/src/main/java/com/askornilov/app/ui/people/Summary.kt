package com.askornilov.app.ui.people

import com.askornilov.app.data.Person
import com.askornilov.app.data.Step
import com.askornilov.app.data.Task
import com.askornilov.app.data.TaskStatus
import com.askornilov.app.ui.common.Dates

/** Текущий (невыполненный) следующий шаг задачи. */
fun currentStep(task: Task, steps: List<Step>): Step? =
    steps.firstOrNull { it.taskId == task.id && !it.done }

data class PersonSummary(
    val openTasks: Int,
    val nextStep: Step?,
    val nextStepTask: Task?,
    val overdue: Boolean,
)

fun summarize(persons: List<Person>, tasks: List<Task>, steps: List<Step>): Map<Long, PersonSummary> {
    val today = Dates.today()
    val stepsByTask = steps.filter { !it.done }.groupBy { it.taskId }
    val open = tasks.filter { it.status != TaskStatus.DONE && it.personId != null }.groupBy { it.personId!! }
    return persons.associate { p ->
        val pTasks = open[p.id].orEmpty()
        val pairs = pTasks.flatMap { t -> stepsByTask[t.id].orEmpty().map { it to t } }
        val nearest = pairs.minWithOrNull(compareBy<Pair<Step, Task>, Long?>(nullsLast<Long>()) { it.first.dueDate })
        val overdue = pairs.any { (it.first.dueDate ?: Long.MAX_VALUE) < today } ||
            pTasks.any { (it.dueDate ?: Long.MAX_VALUE) < today }
        p.id to PersonSummary(pTasks.size, nearest?.first, nearest?.second, overdue)
    }
}

/** Сортировка задач: сначала с ближайшим шагом / сроком, потом по приоритету. */
fun sortTasks(tasks: List<Task>, steps: List<Step>): List<Task> =
    tasks.sortedWith(
        compareBy<Task, Long?>(nullsLast<Long>()) { t -> currentStep(t, steps)?.dueDate ?: t.dueDate }
            .thenByDescending { it.priority.ordinal }
            .thenByDescending { it.createdAt },
    )
