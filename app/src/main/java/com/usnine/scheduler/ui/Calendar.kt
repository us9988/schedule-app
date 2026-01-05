package com.usnine.scheduler.ui

import android.Manifest
import android.os.Build
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.usnine.scheduler.R
import com.usnine.scheduler.data.Schedule
import com.usnine.scheduler.data.localDate
import com.usnine.scheduler.util.HorizontalDivider
import com.usnine.scheduler.util.Text
import com.usnine.scheduler.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {}
        )
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    CalendarView(
        viewModel = viewModel,
        selectedDate = selectedDate,
        onDateSelected = viewModel::onDateSelected,
        currentMonth = currentMonth,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onMonthSelected = viewModel::onMonthSelected
    )
}


@Composable
fun CalendarView(
    viewModel: CalendarViewModel,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<Schedule?>(null) }
    scheduleToEdit?.let { schedule ->
        ScheduleDetailDialog(
            schedule = schedule,
            onDismiss = { scheduleToEdit = null },
            onSave = { updatedSchedule ->
                viewModel.updateSchedule(updatedSchedule)
                scheduleToEdit = null
            }
        )
    }
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    val days = remember(currentMonth) { getDaysInMonth(currentMonth) }
    val schedulesByDate = remember(schedules, currentMonth) {
        schedules
            .filter { schedule ->
                val scheduleDate = schedule.localDate
                scheduleDate.year == currentMonth.year && scheduleDate.month == currentMonth.month
            }
            .groupBy {
                Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    }
    if (showDatePickerDialog) {
        YearMonthPickerDialog(
            initialYearMonth = currentMonth,
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { year, month ->
                onMonthSelected(year, month)
                showDatePickerDialog = false
            }
        )
    }
    scheduleToDelete?.let { schedule ->
        DeleteConfirmDialog(
            schedule = schedule,
            onDismiss = { scheduleToDelete = null },
            onConfirm = {
                viewModel.deleteSchedule(it)
                scheduleToDelete = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.content_desc_prev_month)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDatePickerDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MMMM", Locale.getDefault())),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.content_desc_next_month)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarHeader()

        val chunkedDays = days.chunked(7)

        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
        ) {
            chunkedDays.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f)) {
                            val dailySchedules = date?.let { schedulesByDate[it] }
                            CalendarDayCell(
                                date = date,
                                schedules = dailySchedules,
                                isSelected = date == selectedDate,
                                onDateClick = { clickedDate ->
                                    onDateSelected(clickedDate)
                                }
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        val selectedDaySchedules by remember(schedulesByDate, selectedDate) {
            derivedStateOf {
                schedulesByDate[selectedDate] ?: emptyList()
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedDaySchedules.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.content_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            } else {
                items(selectedDaySchedules) { schedule ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .pointerInput(schedule) {
                                detectTapGestures(
                                    onTap = { scheduleToEdit = schedule },
                                    onLongPress = { scheduleToDelete = schedule }
                                )
                            },
                        shape = RoundedCornerShape(12.dp), // 타원형(양 끝이 둥근 모양)
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold // 제목 강조
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.memo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek
    val daysInMonth = yearMonth.lengthOfMonth()
    val days = mutableListOf<LocalDate?>()
    val firstDayIndex = startDayOfWeek.ordinal

    for (i in 0..firstDayIndex) {
        days.add(null)
    }
    // 해당 월의 처음 빈 날짜 채우기
    for (i in 1..daysInMonth) {
        days.add(firstDayOfMonth.withDayOfMonth(i))
    }
    val totalCells = days.size
    // 해당 월 마지막 빈 날짜 채우기
    if (totalCells % 7 != 0) {
        val remaining = 7 - (totalCells % 7)
        for (i in 0 until remaining) {
            days.add(null)
        }
    }

    return days
}

@Composable
fun CalendarDayCell(
    date: LocalDate?,
    schedules: List<Schedule>?,
    isSelected: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val hasSchedule = !schedules.isNullOrEmpty()

    if (date == null) {
        Box(modifier = Modifier.aspectRatio(1f).padding(2.dp))
        return
    }
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.secondary
    } else {
        Color.Transparent
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onDateClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Box {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            if (hasSchedule) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun CalendarHeader() {
    val daysOfWeek = listOf(SUN, MON, TUE, WED, THU, FRI, SAT)

    Row(
        modifier = Modifier
            .widthIn(max = 500.dp),
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun YearMonthPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(initialYearMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.date_picker_title)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    modifier = Modifier.width(100.dp),
                    factory = {
                        NumberPicker(it).apply {
                            minValue = 1900
                            maxValue = 2100
                            value = selectedYear
                            setOnValueChangedListener { _, _, newVal ->
                                selectedYear = newVal
                            }
                        }
                    },
                    update = { it.value = selectedYear }
                )

                Text(R.string.year, modifier = Modifier.padding(horizontal = 8.dp))
                AndroidView(
                    modifier = Modifier.width(100.dp),
                    factory = {
                        NumberPicker(it).apply {
                            minValue = 1
                            maxValue = 12
                            value = selectedMonth
                            setFormatter { value -> "%02d".format(value) }
                            setOnValueChangedListener { _, _, newVal ->
                                selectedMonth = newVal
                            }
                        }
                    },
                    update = { it.value = selectedMonth }
                )
                Text(R.string.month)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text(R.string.dialog_ok)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(R.string.dialog_cancel)
            }
        }
    )
}

@Composable
fun ScheduleDetailDialog(
    schedule: Schedule,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var title by remember { mutableStateOf(schedule.title) }
    var memo by remember { mutableStateOf(schedule.memo) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(R.string.fix_alert_title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(R.string.dialog_title) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text(R.string.dialog_desc) },
                    modifier = Modifier.height(120.dp),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(schedule.copy(title = title, memo = memo))
                }
            ) {
                Text(R.string.dialog_save, style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(R.string.dialog_cancel, style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    schedule: Schedule,
    onDismiss: () -> Unit,
    onConfirm: (Schedule) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(R.string.delete_alert_title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(R.string.delete_alert_content, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(schedule) }
            ) {
                Text(R.string.dialog_delete, style = MaterialTheme.typography.bodySmall, color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(R.string.dialog_cancel, style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}

const val MON = "월"
const val TUE = "화"
const val WED = "수"
const val THU = "목"
const val FRI = "금"
const val SAT = "토"
const val SUN = "일"
