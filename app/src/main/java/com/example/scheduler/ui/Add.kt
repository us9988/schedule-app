package com.example.scheduler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.scheduler.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddScreen(
    defaultDate: String?, // S,
    navController: NavController
) {
    AddScheduleScreen(defaultDate,
        navController = navController )
}

@Composable
fun AddScheduleScreen(
    defaultDate: String?,
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    // 1. CalendarScreen에서 전달받은 날짜 문자열을 LocalDate로 파싱합니다.
    val initialDate = remember(defaultDate) {
        try {
            // defaultDate가 null이 아니면 파싱하고, null이면 오늘 날짜를 사용합니다.
            defaultDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        } catch (e: Exception) {
            // 파싱 중 오류가 발생하면 안전하게 오늘 날짜로 대체합니다.
            LocalDate.now()
        }
    }
    val initialDateInMillis = remember(initialDate) {
        initialDate
            .atStartOfDay(ZoneId.systemDefault()) // 해당 날짜의 자정 시간을 기준으로
            .toInstant()                          // Instant로 변환 후
            .toEpochMilli()                       // Long 타입의 밀리초로 최종 변환
    }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(initialDateInMillis) }

    // 스크롤 가능한 세로 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // 폼이 길어질 경우 스크롤
    ) {
        // 1. 타이틀
        Text(
            text = "일정 추가",
            // 제목 스타일
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
                .align(Alignment.CenterHorizontally)
        )

        FormTitle("제목")
        // 2. 제목 입력 필드
        FormTextField(
            label = "",
            value = title,
            onValueChange = { title = it },
            placeholder = "제목을 입력해 주세요."
        )
        FormTitle("내용")
        // 3. 설명 입력 필드
        FormTextField(
            label = "",
            value = description,
            onValueChange = { description = it },
            placeholder = "내용을 입력해 주세요"
        )
        FormTitle("날짜 선택")
        DatePickerFieldToModal(
            modifier = Modifier
                .padding(bottom = 4.dp),
            initialDate = selectedDate,
            onDateSelectedCallback = { selectedDate = it }
        )

        // 7. 추가 버튼
        Button(
            onClick = {
                viewModel.addNewSchedule(title, description, selectedDate)
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // 이미지의 녹색과 유사하게 설정
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("일정 추가하기", color = Color.White)
        }
    }
}

@Composable
fun FormTitle(
    type: String,
) {
    Text(
        modifier = Modifier
            .padding(bottom = 4.dp),
        text = type,
        style = MaterialTheme.typography.titleSmall
    )
}

/**
 * 라벨과 텍스트 필드를 묶은 공통 컴포저블
 */
@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        // Material 3 Outlined 스타일을 사용하려면:
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = (label != "Description"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = LightGray
            )
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    AddScheduleScreen()
//}
