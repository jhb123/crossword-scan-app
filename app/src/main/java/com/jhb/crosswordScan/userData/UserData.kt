package com.jhb.crosswordScan.userData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserData (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name="email")
    val email: String,
    @ColumnInfo(name="userName")
    val userName: String,
    @ColumnInfo(name="password")
    val password: String,
    @ColumnInfo(name="token")
    val token: String? = null,

    )