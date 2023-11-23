package com.gjc.appmanager.constant

annotation class SortType {
    companion object {
        const val DEFAULT = 0
        const val TARGET_SDK_DESCENDING = 1
        const val TARGET_SDK_ASCENDING = 2
        const val MIN_SDK_DESCENDING = 3
        const val MIN_SDK_ASCENDING = 4
        const val COMPILE_SDK_DESCENDING = 5
        const val COMPILE_SDK_ASCENDING = 6
    }
}