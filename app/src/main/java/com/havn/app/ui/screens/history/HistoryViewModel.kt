package com.havn.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.DoseLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class HistoryUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val monthLogs: List<DoseLog> = emptyList(),
    val selectedDate: LocalDate? = null,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        prefs.activeUserId,
        _month,
        _selectedDate,
    ) { userId, month, date -> Triple(userId, month, date) }
        .flatMapLatest { (userId, month, date) ->
            if (userId < 0) flowOf(HistoryUiState(currentMonth = month))
            else repository.getDoseLogsForMonth(userId, month.year, month.monthValue)
                .map { logs ->
                    HistoryUiState(
                        currentMonth = month,
                        monthLogs = logs,
                        selectedDate = date,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun previousMonth() { _month.update { it.minusMonths(1) } }
    fun nextMonth() { _month.update { it.plusMonths(1) } }
    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }
}
