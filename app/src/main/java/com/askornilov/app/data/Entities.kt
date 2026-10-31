package com.askornilov.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PersonType(val title: String, val plural: String, val color: Long) {
    COLLEAGUE("Коллега", "Коллеги", 0xFF3D7BF7),
    PARTNER("Партнёр", "Партнёры", 0xFF2EAD6B),
    CONTRACTOR("Контрагент", "Контрагенты", 0xFFE08A1E),
    CLIENT("Клиент", "Клиенты", 0xFF9B51E0);

    companion object {
        /** Типы, которые показываются в разделе «Люди» (клиенты — отдельный раздел). */
        val people = listOf(COLLEAGUE, PARTNER, CONTRACTOR)
    }
}

enum class TaskStatus(val title: String) {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    WAITING("Ждём ответа"),
    DONE("Готово"),
}

enum class Priority(val title: String) {
    LOW("Низкий"),
    NORMAL("Обычный"),
    HIGH("Высокий"),
}

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PersonType,
    val company: String = "",
    val position: String = "",
    val phone: String = "",
    val email: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

/** Задача. personId == null — личное дело, не привязанное к человеку. */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("personId")],
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long?,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.NEW,
    val priority: Priority = Priority.NORMAL,
    /** Срок задачи, LocalDate.toEpochDay(). */
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)

/** Шаг задачи. Невыполненный шаг — это «следующий шаг». */
@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class Step(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val text: String,
    /** Дата шага, LocalDate.toEpochDay(). */
    val dueDate: Long? = null,
    val done: Boolean = false,
    val doneAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
