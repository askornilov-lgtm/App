package com.askornilov.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter fun personTypeToString(v: PersonType): String = v.name
    @TypeConverter fun stringToPersonType(v: String): PersonType = PersonType.valueOf(v)
    @TypeConverter fun statusToString(v: TaskStatus): String = v.name
    @TypeConverter fun stringToStatus(v: String): TaskStatus = TaskStatus.valueOf(v)
    @TypeConverter fun priorityToString(v: Priority): String = v.name
    @TypeConverter fun stringToPriority(v: String): Priority = Priority.valueOf(v)
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Person>>

    @Insert suspend fun insert(person: Person): Long
    @Update suspend fun update(person: Person)
    @Delete suspend fun delete(person: Person)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT id FROM tasks WHERE personId = :personId")
    suspend fun idsForPerson(personId: Long): List<Long>

    @Query("DELETE FROM tasks WHERE personId = :personId")
    suspend fun deleteForPerson(personId: Long)

    @Insert suspend fun insert(task: Task): Long
    @Update suspend fun update(task: Task)
    @Delete suspend fun delete(task: Task)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM steps ORDER BY createdAt")
    fun observeAll(): Flow<List<Step>>

    @Query("DELETE FROM steps WHERE taskId IN (:taskIds)")
    suspend fun deleteForTasks(taskIds: List<Long>)

    @Insert suspend fun insert(step: Step): Long
    @Update suspend fun update(step: Step)
    @Delete suspend fun delete(step: Step)
}

@Database(entities = [Person::class, Task::class, Step::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun taskDao(): TaskDao
    abstract fun stepDao(): StepDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()
    }
}
