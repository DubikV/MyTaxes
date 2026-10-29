package com.gmail.vanyadubik.myassistant.commonui.language

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gmail.vanyadubik.myassistant.domen.model.AppLanguage
import com.gmail.vanyadubik.myassistant.domen.usecase.DataStoreUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Applies the platform locale override to the resources locale for the selected [AppLanguage]. */
expect fun applyAppLanguage(language: AppLanguage)

@Stable
class LanguageHandler(
    private val dataStoreUseCase: DataStoreUseCase,
    private val coroutineScope: CoroutineScope,
) {
    var language: AppLanguage by mutableStateOf(AppLanguage.System)
        private set

    init {
        coroutineScope.launch {
            dataStoreUseCase.language.collect { language = it }
        }
    }

    fun changeLanguage(newLanguage: AppLanguage) {
        coroutineScope.launch { dataStoreUseCase.setLanguage(newLanguage) }
    }
}
