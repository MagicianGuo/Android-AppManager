package com.gjc.appmanager.bean

import android.graphics.drawable.Drawable

data class BeanApp(val label: String, val packageName: String, val icon: Drawable, val compileSdk: Int, val targetSdk: Int, val minSdk: Int)