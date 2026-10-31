package com.askornilov.app.ui.people

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.askornilov.app.data.Person
import com.askornilov.app.data.PersonType

/** Создание/редактирование человека. Обязательно только имя. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonEditorDialog(
    initial: Person,
    types: List<PersonType> = PersonType.people,
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit,
) {
    val ctx = LocalContext.current
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var type by rememberSaveable { mutableStateOf(initial.type) }
    var company by rememberSaveable { mutableStateOf(initial.company) }
    var position by rememberSaveable { mutableStateOf(initial.position) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var note by rememberSaveable { mutableStateOf(initial.note) }

    // Выбор контакта из телефонной книги: имя + номер (без лишних разрешений).
    val pickContact = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val uri = res.data?.data ?: return@rememberLauncherForActivityResult
        runCatching {
            ctx.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                null, null, null,
            )?.use { c ->
                if (c.moveToFirst()) {
                    c.getString(0)?.let { if (it.isNotBlank()) name = it }
                    c.getString(1)?.let { if (it.isNotBlank()) phone = it }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.94f),
        title = { Text(if (initial.id == 0L) "Новый контакт" else "Редактирование") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            pickContact.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
                        } catch (_: ActivityNotFoundException) { }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Contacts, null)
                    Text("  Из телефонной книги")
                }
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth(),
                    label = { Text("Имя *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                if (types.size > 1) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.title) })
                        }
                    }
                }
                OutlinedTextField(
                    company, { company = it }, Modifier.fillMaxWidth(),
                    label = { Text("Компания") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    position, { position = it }, Modifier.fillMaxWidth(),
                    label = { Text("Должность") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    phone, { phone = it }, Modifier.fillMaxWidth(),
                    label = { Text("Телефон") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                OutlinedTextField(
                    email, { email = it }, Modifier.fillMaxWidth(),
                    label = { Text("E-mail") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    note, { note = it }, Modifier.fillMaxWidth(),
                    label = { Text("Комментарий") }, minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(), type = type, company = company.trim(),
                            position = position.trim(), phone = phone.trim(),
                            email = email.trim(), note = note.trim(),
                        ),
                    )
                },
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
