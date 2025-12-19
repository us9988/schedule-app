package com.usnine.scheduler.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.usnine.scheduler.R
import com.usnine.scheduler.ui.theme.PrimaryLight
import com.usnine.scheduler.util.Text
import com.usnine.scheduler.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddScreen(
    defaultDate: String?,
    navController: NavController
) {
    AddScheduleView(
        defaultDate,
        navController = navController
    )
}

@Composable
fun AddScheduleView(
    defaultDate: String?,
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val initialDate = remember(defaultDate) {
        try {
            defaultDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    val initialDateInMillis = remember(initialDate) {
        initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(initialDateInMillis) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 아이콘
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        //물결 효과(Ripple)를 비활성화(뒤로가기 시 animation ui 문제)
                        indication = null,
                        onClick = { navController.popBackStack() }
                    )
                )
                Text(
                    stringResId = R.string.add_schedule,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            FormTitle(R.string.add_screen_title_text)
            FormTextField(
                label = "",
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.add_screen_title_placeholder)
            )
            FormTitle(R.string.add_screen_content_text)
            FormTextField(
                label = stringResource(R.string.add_screen_content_text),
                value = description,
                onValueChange = { description = it },
                placeholder = stringResource(R.string.add_screen_content_placeholder)
            )
            FormTitle(R.string.add_screen_date_text)
            DatePickerFieldToModal(
                modifier = Modifier
                    .padding(bottom = 4.dp),
                initialDate = selectedDate,
                onDateSelectedCallback = { selectedDate = it }
            )
            val successMessage = stringResource(R.string.snackbar_add_schedule_success)
            val failMessage = stringResource(R.string.snackbar_add_schedule_cancel)
            Button(
                onClick = {
                    val addSchedule = viewModel.addNewSchedule(title, description, selectedDate)
                    if (addSchedule) {
                        scope.launch {
                            snackbarHostState.showSnackbar(successMessage)
                        }
                        navController.popBackStack()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(failMessage)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(R.string.add_screen_add_button, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun FormTitle(
    stringRes: Int,
) {
    Text(
        stringResId = stringRes,
        modifier = Modifier
            .padding(bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = (label.isEmpty()),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}
