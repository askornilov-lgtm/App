package com.askornilov.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.askornilov.app.data.AppDatabase
import com.askornilov.app.data.Person
import com.askornilov.app.data.Step
import com.askornilov.app.data.Task
import com.askornilov.app.data.TaskStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val db: AppDatabase) : ViewModel() {

    val persons: StateFlow<List<Person>> = db.personDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tasks: StateFlow<List<Task>> = db.taskDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val steps: StateFlow<List<Step>> = db.stepDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- Люди ----

    fun savePerson(person: Person, onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = if (person.id == 0L) db.personDao().insert(person)
        else { db.personDao().update(person); person.id }
        onSaved(id)
    }

    fun deletePerson(person: Person) = viewModelScope.launch {
        db.withTransaction {
            val taskIds = db.taskDao().idsForPerson(person.id)
            if (taskIds.isNotEmpty()) db.stepDao().deleteForTasks(taskIds)
            db.taskDao().deleteForPerson(person.id)
            db.personDao().delete(person)
        }
    }

    // ---- Задачи ----

    /** Сохраняет задачу; для новой можно сразу задать первый шаг. */
    fun saveTask(
        task: Task,
        firstStepText: String? = null,
        firstStepDate: Long? = null,
        onSaved: (Long) -> Unit = {},
    ) = viewModelScope.launch {
        val id = db.withTransaction {
            if (task.id == 0L) {
                val newId = db.taskDao().insert(task)
                if (!firstStepText.isNullOrBlank()) {
                    db.stepDao().insert(Step(taskId = newId, text = firstStepText.trim(), dueDate = firstStepDate))
                }
                newId
            } else {
                db.taskDao().update(task)
                task.id
            }
        }
        onSaved(id)
    }

    fun setStatus(task: Task, status: TaskStatus) = viewModelScope.launch {
        db.taskDao().update(
            task.copy(
                status = status,
                completedAt = if (status == TaskStatus.DONE) System.currentTimeMillis() else null,
            ),
        )
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        db.withTransaction {
            db.stepDao().deleteForTasks(listOf(task.id))
            db.taskDao().delete(task)
        }
    }

    // ---- Шаги ----

    fun addStep(taskId: Long, text: String, dueDate: Long?) = viewModelScope.launch {
        db.stepDao().insert(Step(taskId = taskId, text = text.trim(), dueDate = dueDate))
    }

    fun updateStep(step: Step) = viewModelScope.launch { db.stepDao().update(step) }

    fun completeStep(step: Step) = viewModelScope.launch {
        db.stepDao().update(step.copy(done = true, doneAt = System.currentTimeMillis()))
    }

    fun deleteStep(step: Step) = viewModelScope.launch { db.stepDao().delete(step) }
}
