package com.example.tipclik4

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            MainDB.getInstance(context).dao().insertRole(Role(name = "Пользователь"))
            MainDB.getInstance(context).dao().insertRole(Role(name = "Менеджер"))

            MainDB.getInstance(context).dao().insertPaperType(PaperType(name = "А3"))
            MainDB.getInstance(context).dao().insertPaperType(PaperType(name = "А4"))
            MainDB.getInstance(context).dao().insertPaperType(PaperType(name = "А6"))

            MainDB.getInstance(context).dao().insertPrintType(PrintType(name = "Цветная"))
            MainDB.getInstance(context).dao().insertPrintType(PrintType(name = "Чёрно-белая"))

            MainDB.getInstance(context).dao().insertOrderType(OrderType(name = "Дизайн"))
            MainDB.getInstance(context).dao().insertOrderType(OrderType(name = "Файл"))
        }
    }
}