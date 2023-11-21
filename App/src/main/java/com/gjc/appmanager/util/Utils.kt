package com.gjc.appmanager.util

import android.widget.Toast
import com.gjc.appmanager.App

private var toast: Toast? = null

fun String.shortToast() {
    toast?.cancel()
    toast = Toast.makeText(App.get(), this, Toast.LENGTH_SHORT)
    toast?.show()
}