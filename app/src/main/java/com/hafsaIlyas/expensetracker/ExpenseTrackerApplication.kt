// app/src/main/java/com/hafsaIlyas/expensetracker/ExpenseTrackerApplication.kt
package com.hafsaIlyas.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApplication : Application()
// No code needed inside — the annotation handles everything