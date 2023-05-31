package com.jhb.crosswordScan.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.userData.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

private const val TAG = "SessionData"

object SessionData {

    @Volatile
    lateinit var applicationContext: Context
    lateinit var masterKey: MasterKey
    lateinit var encryptedFile: EncryptedFile

    private lateinit var userData: UserData
    private lateinit var token: String

    private val userFileName = "user.txt"
    private val tokenFileName = "token.txt"

    val _tokenState = MutableStateFlow<String?>(null)
    val tokenState: StateFlow<String?> = _tokenState

    val _userDataState = MutableStateFlow<UserData?>(null)
    val userDataState: StateFlow<UserData?> = _userDataState


    //val flowTest = Flow<UserData>

    fun setContext(context: Context) {
        applicationContext = context
        masterKey = androidx.security.crypto.MasterKey.Builder(
            applicationContext,
            androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS
        )
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build();
    }

    fun writeUser(userData: UserData) {
        val fileContent = userToJson(userData)
        writeEncryptedFile(userFileName,fileContent)
    }

    fun readUser(): UserData? {
        val userPlainText = readEncryptedFile(userFileName)
        if(userPlainText!=null){
            userData = userFromJson(userPlainText)
            _userDataState.update { userData }
            return userData
        } else{
            return null
        }
    }

    fun writeToken(userData: UserData) {
        val fileContent = userToJson(userData)
        writeEncryptedFile(tokenFileName,fileContent)
    }

    fun readToken(): String? {
        val tokenPlainText = readEncryptedFile(tokenFileName)
        if(tokenPlainText!=null){
            token = tokenPlainText
            _tokenState.update { token }

            return token
        } else{
            return null
        }
    }


    private fun readEncryptedFile(filename: String) : String? {
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            File(applicationContext.filesDir, filename),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        try {
            val inputStream = encryptedFile.openFileInput()
            val byteArrayOutputStream = ByteArrayOutputStream()
            var nextByte: Int = inputStream.read()
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte)
                nextByte = inputStream.read()
            }
            val plaintext: ByteArray = byteArrayOutputStream.toByteArray()

            return String(plaintext)

        } catch (e: IOException) {
            Log.w(TAG, "$filename not found")
            return null
        }
    }

    private fun writeEncryptedFile(filename : String, content: String){
        Log.i(TAG, "updating $filename")
        val file = File(applicationContext.filesDir, filename)
        if (file.exists()) {
            file.delete()
        }
        val fileContent = content.toByteArray(StandardCharsets.UTF_8)

        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            File(applicationContext.filesDir, filename),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
    }

    private fun userFromJson(json: String): UserData {
        val typeToken = object : TypeToken<UserData>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    private fun userToJson(user: UserData?): String {
        val gson = Gson()
        return gson.toJson(user)
    }


}