package com.example.scheduler.ui

import android.widget.NumberPicker
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scheduler.data.Schedule
import com.example.scheduler.data.localDate
import com.example.scheduler.ui.theme.BgColor
import com.example.scheduler.ui.theme.Primary
import com.example.scheduler.ui.theme.Primary80
import com.example.scheduler.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
//
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    CalendarView(
        viewModel = viewModel, // viewModel을 CalendarView에 전달
        selectedDate = selectedDate, // 현재 선택된 날짜 전달
        onDateSelected = viewModel::onDateSelected, // 날짜 선택 이벤트 핸들러 전달
                currentMonth = currentMonth, // <-- 추가: 상태 전달
        onPreviousMonth = viewModel::onPreviousMonth, // <-- 추가: 이벤트 전달
        onNextMonth = viewModel::onNextMonth, // <-- 추가: 이벤트 전달
        onMonthSelected = viewModel::onMonthSelected // <-- 추가: 이벤트 전달
    )
}


@Composable
fun CalendarView(
    viewModel: CalendarViewModel,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth, // <-- 추가: 파라미터로 받기
    onPreviousMonth: () -> Unit, // <-- 추가
    onNextMonth: () -> Unit, // <-- 추가
    onMonthSelected: (Int, Int) -> Unit // <-- 추가
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    // 2. <<추가>> 수정할 Schedule 객체와 다이얼로그 표시 여부를 관리하는 상태
    var scheduleToEdit by remember { mutableStateOf<Schedule?>(null) }
    // 3. <<추가>> 상세 정보 다이얼로그 호출
    scheduleToEdit?.let { schedule ->
        ScheduleDetailDialog(
            schedule = schedule,
            onDismiss = { scheduleToEdit = null },
            onSave = { updatedSchedule ->
                viewModel.updateSchedule(updatedSchedule) // ViewModel 함수 호출
                scheduleToEdit = null // 다이얼로그 닫기
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
    // 5. <<추가>> 삭제 확인 다이얼로그 호출
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
            .fillMaxWidth()
            .background(BgColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick =onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month"
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp)) // 클릭 시 시각적 효과를 위해
                    .clickable { showDatePickerDialog = true } // 클릭 시 다이얼로그 표시
                    .padding(horizontal = 16.dp, vertical = 8.dp) // 터치 범위 확장
            ) {
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MMMM", Locale.getDefault())),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarHeader()

        val chunkedDays = days.chunked(7)

        Column {
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
                                    onDateSelected(clickedDate) // ViewModel의 함수를 호출하도록 변경
                                }
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            thickness = 2.dp,
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
                        text = "선택된 날짜에 일정이 없습니다.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                items(selectedDaySchedules, key = { it.id }) { schedule ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .pointerInput(schedule) { // key로 schedule을 주어 리컴포지션 시 재시작되도록 함
                                detectTapGestures(
                                    onTap = { scheduleToEdit = schedule }, // 일반 탭(클릭) 이벤트
                                    onLongPress = { scheduleToDelete = schedule } // 길게 누르기(롱클릭) 이벤트
                                )
                            },
//                        .combinedClickable(
//                                onClick = { scheduleToEdit = schedule }, // 일반 클릭: 수정
//                                onLongClick = { scheduleToDelete = schedule } // 롱 클릭: 삭제
//                            ),
                        shape = RoundedCornerShape(12.dp), // 타원형(양 끝이 둥근 모양)
                        color = Primary80 // 회색 배경 (투명도 조절 가능)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 12.dp) // 타원 내부 여백
                        ) {
                            // 1. 윗줄: 제목
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold // 제목 강조
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 2. 아랫줄: 설명
                            // schedule 객체에 description이 있다고 가정했습니다.
                            // 없다면 "상세 설명 없음" 등으로 대체하세요.
                            Text(
                                text = schedule.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray // 제목보다 연하게
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

    // 2. 해당 월의 날짜 채우기
    for (i in 1..daysInMonth) {
        days.add(firstDayOfMonth.withDayOfMonth(i))
    }

    val totalCells = days.size
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
        Primary80
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
                color = Color.Black,
                lineHeight = 18.sp
            )

            if (hasSchedule) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
        }
    }
}

@Composable
fun CalendarHeader() {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
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
    // 선택된 년/월 상태
    var selectedYear by remember { mutableIntStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(initialYearMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("날짜 선택") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 년도 선택 NumberPicker
                AndroidView(
                    modifier = Modifier.width(100.dp),
                    factory = {
                        NumberPicker(it).apply {
                            minValue = 1900
                            maxValue = 2100
                            value = selectedYear
                            // 스크롤 시 selectedYear 상태 업데이트
                            setOnValueChangedListener { _, _, newVal ->
                                selectedYear = newVal
                            }
                        }
                    },
                    update = { it.value = selectedYear } // 상태 변경 시 UI 업데이트
                )

                Text("년", modifier = Modifier.padding(horizontal = 8.dp))

                // 월 선택 NumberPicker
                AndroidView(
                    modifier = Modifier.width(100.dp),
                    factory = {
                        NumberPicker(it).apply {
                            minValue = 1
                            maxValue = 12
                            value = selectedMonth
                            setFormatter { value -> "%02d".format(value) } // 01, 02월 형식
                            // 스크롤 시 selectedMonth 상태 업데이트
                            setOnValueChangedListener { _, _, newVal ->
                                selectedMonth = newVal
                            }
                        }
                    },
                    update = { it.value = selectedMonth } // 상태 변경 시 UI 업데이트
                )

                Text("월")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

// 5. <<추가>> 상세 정보 및 수정 다이얼로그 Composable
@Composable
fun ScheduleDetailDialog(
    schedule: Schedule,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var title by remember { mutableStateOf(schedule.title) }
    var description by remember { mutableStateOf(schedule.description) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("일정 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 제목 입력 필드
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                // 설명 입력 필드
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier.height(120.dp),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // schedule 객체의 복사본을 만들고 변경된 값을 적용하여 전달
                    onSave(schedule.copy(title = title, description = description))
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
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
        title = { Text("일정 삭제") },
        text = { Text("'${schedule.title}' 일정을 삭제하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(schedule) }
            ) {
                Text("삭제", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}