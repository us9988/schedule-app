package com.example.scheduler.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.scheduler.R
import com.example.scheduler.data.Schedule
import com.example.scheduler.ui.theme.BaseGreen

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

@Composable
fun HomeScreen() {
    MySchedules()

}

@Composable
fun ScheduleListItem(
    data: Schedule,
    modifier: Modifier = Modifier // 외부에서 Modifier를 전달받을 수 있도록 추가
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* 아이템 전체 클릭 이벤트 (필요하다면) */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 왼쪽: 색상을 수정할 수 있는 동그란 작은 원
        Box(
            modifier = Modifier
                .size(12.dp) // 원의 크기
                .clip(CircleShape) // 원 모양으로 클리핑
//                .clickable { onColorChange(itemData) } // 원 클릭 시 색상 변경
        ) {
            val circleColor = if (data.isImportant) Color.Red else BaseGreen
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = circleColor)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. 중앙: 위아래로 텍스트
        Column(
            modifier = Modifier.weight(1f) // 남은 공간을 모두 차지하도록
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis

            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 3. 오른쪽: 날짜 텍스트
        Text(
            text = data.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MySchedules(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        // 1. "일정" 제목
        Text(
            text = stringResource(R.string.home_schedule), // "일정" (strings.xml에 정의되어 있다고 가정)
            // 만약 없다면 그냥 "일정" 문자열 사용
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,// 좀 더 큰 제목 스타일
            // fontSize = 24.sp, // 또는 직접 크기 지정
            modifier = Modifier
                .fillMaxWidth() // 너비를 꽉 채워서 왼쪽 정렬 효과 (기본값)
        )

        LazyColumn {
            items(
                items = sampleListData,
                key = { itemData -> itemData.id } // (선택 사항) 각 아이템에 고유 키를 제공하면 성능 최적화에 도움
            ) { itemData ->
                ScheduleListItem(
                    data = itemData,
                )
            }

            // 단일 아이템을 추가하는 다른 방법들:
            // item { TextListItem(icon = Icons.Filled.Info, title = "고정된 첫 번째 아이템", description = "설명") }
            // items(count = 5) { index ->
            //     TextListItem(icon = Icons.Filled.Info, title = "인덱스 아이템 ${index + 1}", description = "인덱스로 생성된 아이템")
            // }
        }
    }

}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Content Preview")
@Composable
fun HomeContentPreview() {
    // 만약 HomeContent가 특정 테마에 의존한다면,
    // 여기서 해당 테마로 감싸주는 것이 좋습니다.
    // 예: YourAppTheme { HomeContent() }
    // 현재는 MaterialTheme을 내부에서 사용하므로 바로 호출해도 괜찮을 수 있습니다.
    HomeScreen()
}

val sampleListData = List(20) { index ->
    Schedule(
        id = index.toLong(),
        date = "20${index + 1}년 1월 1일",
        title = "아이템 제목 ${index + 1}",
        description = "20${index + 1}에 대한 설명입니다. 충분히 길게 작성하여 여러 줄을 차지하도록 합니다.",
        isImportant = index % 3 == 0
    )
}
