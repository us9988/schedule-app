package com.usnine.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usnine.scheduler.data.Schedule
import com.usnine.scheduler.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Schedule>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val searchCache = mutableMapOf<String, List<Schedule>>()

    init {
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _searchQuery
                .debounce(400L)
                .map { it.trim() }
                .distinctUntilChanged() // 이전 검색어와 같으면 무시
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@collectLatest
                    }
                    // 검색어 캐싱 여부 확인
                    if (searchCache.containsKey(query)) {
                        _searchResults.value = searchCache[query] ?: emptyList()
                        return@collectLatest
                    }
                    // db조회 및 검색어 캐싱
                    repository.searchSchedules(query).collect { results ->
                        _searchResults.value = results
                        searchCache[query] = results
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        searchCache.clear()
    }
}
