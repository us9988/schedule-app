package com.usnine.scheduler.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.usnine.scheduler.R
import com.usnine.scheduler.data.Schedule
import com.usnine.scheduler.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onItemClick: (Long) -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearch()
        }
    }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    if (searchQuery.isBlank()) {
        EmptyStateView(stringResource(R.string.search_screen_empty_text))
    } else if (searchResults.isEmpty()) {
        EmptyStateView(stringResource(R.string.search_screen_empty_result, searchQuery))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = searchResults,
                key = { it.id }
            ) { schedule ->
                SearchItem(
                    schedule = schedule,
                    onItemClick = { onItemClick(schedule.date) }
                )
            }
        }
    }
}

@Composable
fun SearchItem(schedule: Schedule, onItemClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                //물결 효과(Ripple)를 비활성화(뒤로가기 시 animation ui 문제)
                indication = null,
                onClick = onItemClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondary,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = convertMillisToDate(schedule.date),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            Text(
                text = schedule.title,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = schedule.memo,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
private fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message)
    }
}
