package com.askornilov.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import com.askornilov.app.MainViewModel
import com.askornilov.app.ui.common.EmptyState
import com.askornilov.app.ui.people.PeopleSection

enum class Section(val title: String, val icon: ImageVector) {
    PEOPLE("Люди", Icons.Outlined.Groups),
    CALENDAR("Календарь", Icons.Outlined.CalendarMonth),
    HABITS("Привычки", Icons.Outlined.TaskAlt),
    CLIENTS("Клиенты", Icons.Outlined.ShoppingCart),
    NOTES("Заметки", Icons.Outlined.EditNote),
}

/** Ширина ≥ 600dp — развёрнутый Fold / планшет: боковая панель и два окна. */
@Composable
fun isWide(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

@Composable
fun AppRoot(vm: MainViewModel) {
    var section by rememberSaveable { mutableStateOf(Section.PEOPLE) }
    val wide = isWide()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    Section.entries.forEach { s ->
                        NavigationRailItem(
                            selected = s == section,
                            onClick = { section = s },
                            icon = { Icon(s.icon, null) },
                            label = { Text(s.title) },
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxSize()) { SectionContent(section, vm, wide) }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        Section.entries.forEach { s ->
                            NavigationBarItem(
                                selected = s == section,
                                onClick = { section = s },
                                icon = { Icon(s.icon, null) },
                                label = { Text(s.title, maxLines = 1) },
                                alwaysShowLabel = false,
                            )
                        }
                    }
                },
            ) { pad ->
                Box(Modifier.padding(bottom = pad.calculateBottomPadding()).fillMaxSize()) {
                    SectionContent(section, vm, wide)
                }
            }
        }
    }
}

@Composable
private fun SectionContent(section: Section, vm: MainViewModel, wide: Boolean) {
    when (section) {
        Section.PEOPLE -> PeopleSection(vm, wide)
        Section.CALENDAR -> EmptyState("Календарь и доска", "Появится на этапе 2: календарь, канбан-доска и личные дела.")
        Section.HABITS -> EmptyState("Привычки", "Появится на этапе 3: бады, тренировки, вода — с отметками и сериями.")
        Section.CLIENTS -> EmptyState("Клиенты и заказы", "Появится на этапе 4: клиенты, заказы, база позиций и выгрузка в Excel.")
        Section.NOTES -> EmptyState("Заметки", "Появится на этапе 5.")
    }
}
