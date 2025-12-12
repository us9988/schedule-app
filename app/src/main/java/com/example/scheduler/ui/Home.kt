package com.example.scheduler.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scheduler.data.Schedule
import com.example.scheduler.viewmodel.HomeViewModel
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun SampleScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

// 색상 정의 (이미지 기반)
val LightGreen = Color(0xFFC8E6C9) // 오늘의 할 일 카드 배경
val DarkGreen = Color(0xFF4CAF50)  // 프로그레스 바
val LightGray = Color(0xFFDCDCDC)  // 마감일 카드 배경
val TextGray = Color(0xFF555555)   // 보조 텍스트

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val upcomingSchedules = remember(schedules) {
        val todayStartMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
//        Timber.e("1: $todayStartMillis")
//        val startIndex = schedules.binarySearch { schedule ->
//            if (schedule.date >= todayStartMillis) 0 else -1
//        }
//        Timber.e("2: $startIndex")
//        val firstIndex = if (startIndex < 0) -startIndex - 1 else startIndex
//        if (firstIndex >= schedules.size || schedules[firstIndex].date < todayStartMillis) {
//            return@remember emptyList()
//        }
//        // 찾은 시작 인덱스로부터 최대 5개의 일정을 가져옵니다.
//        val endIndex = (firstIndex + 5).coerceAtMost(schedules.size)
//        schedules.subList(firstIndex, endIndex)

        val fiveDaysLater = todayStartMillis + TimeUnit.DAYS.toMillis(5)

        schedules.filter { schedule ->
            // 스케줄의 날짜가 오늘과 5일 후 사이에 있는지 확인합니다.
            schedule.date in todayStartMillis..<fiveDaysLater
        }
    }
    Timber.e("$upcomingSchedules")
//    schedules.isNotEmpty() {
//
//    }
    ScheduleList(upcomingSchedules)

}

@Composable
fun ScheduleList(schedules: List<Schedule>) {
    if (schedules.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp), // 화면 상단에 약간의 여백을 줌
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "다가오는 일정이 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // 폼이 길어질 경우 스크롤
        ) {
            schedules.forEachIndexed { i, it ->
                if (i > 0 && it.date == schedules[i - 1].date) {
                    TaskCard(it, LightGreen)
                } else {
                    Text(
                        modifier = Modifier.padding(bottom = 5.dp),
                        text = if (isTimestampToday(it.date)) "오늘 일정" else convertMillisToDate(it.date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TaskCard(it, LightGreen)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

// --- 카드 아이템 컴포저블 ---

@Composable
fun TaskCard(schedule: Schedule, color: Color) {
    // Card 대신 Surface 사용 (M3와 호환성 좋음)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color, // 카드 배경색
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = schedule.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = schedule.description,
                fontSize = 14.sp,
                color = TextGray
            )
        }
    }


}

private fun isTimestampToday(timestampMillis: Long): Boolean {
    // 시스템 기본 시간대 (예: Asia/Seoul)
    val zoneId = ZoneId.systemDefault()

    // 1. 오늘 날짜 구하기
    val today = LocalDate.now(zoneId)

    // 2. 타임스탬프를 날짜로 변환하기
    val timestampDate = Instant.ofEpochMilli(timestampMillis)
        .atZone(zoneId)
        .toLocalDate()

    // 3. 두 날짜가 같은지 비교
    return today == timestampDate
}
