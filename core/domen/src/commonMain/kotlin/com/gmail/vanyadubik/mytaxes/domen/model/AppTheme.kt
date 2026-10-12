package com.gmail.vanyadubik.mytaxes.domen.model

sealed class AppTheme(val id: Int) {
    data object System: AppTheme(id = 0)
    data object Dark: AppTheme(id = 1)
    data object Light: AppTheme(id = 2)
}