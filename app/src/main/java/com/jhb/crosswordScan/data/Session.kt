package com.jhb.crosswordScan.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

private const val TAG = "Session"

object Session {

    @Volatile
    lateinit var applicationContext: Context
    lateinit var masterKey: MasterKey
    lateinit var encryptedFile: EncryptedFile

    //private lateinit var sessionData: SessionData
    //private lateinit var token: String

    private val sessionFileName = "session.txt"
    private val tokenFileName = "token.txt"

    private val _tokenState = MutableStateFlow<String?>(null)
    val tokenState: StateFlow<String?> = _tokenState

    private val _sessionDataState = MutableStateFlow<SessionData?>(null)
    val sessionDataState: StateFlow<SessionData?> = _sessionDataState


    //val flowTest = Flow<>

    fun setContext(context: Context) {
        applicationContext = context
        masterKey = androidx.security.crypto.MasterKey.Builder(
            applicationContext,
            androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS
        )
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build();
    }

    suspend fun logOut(){
        CrosswordApi.retrofitService.logOut()
        deleteSessionData()
        _sessionDataState.update { null }
    }

    private fun deleteSessionData(){
        val file = File(applicationContext.filesDir, sessionFileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun updateSession(sessionData: SessionData?){
        if(sessionData!=null){
            writeSession(sessionData)
            _sessionDataState.update { sessionData }
        }
    }

    fun writeSession(sessionData: SessionData?) {
        val fileContent = sessionDataToJson(sessionData)
        Log.i(TAG,"writing $sessionFileName $fileContent")
        writeEncryptedFile(sessionFileName,fileContent)
    }

    fun readSession(): SessionData? {
        val sessionPlainText = readEncryptedFile(sessionFileName)
        if(sessionPlainText!=null){
            val sessionData = sessionDataFromJson(sessionPlainText)
            Log.i(TAG,"from $sessionFileName $sessionPlainText")
            _sessionDataState.update { sessionData }
            return sessionData
        } else{
            return null
        }
    }

    fun writeToken(sessionData: SessionData) {
        val fileContent = sessionDataToJson(sessionData)
        writeEncryptedFile(tokenFileName,fileContent)
    }

    fun readToken(): String? {
        val tokenPlainText = readEncryptedFile(tokenFileName)
        if(tokenPlainText!=null){
            //token = tokenPlainText
            _tokenState.update { tokenPlainText }

            return tokenPlainText
        } else{
            return null
        }
    }


    private fun readEncryptedFile(filename: String) : String? {
        Log.i(TAG,"Reading encrypted file : $filename")
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

    private fun sessionDataFromJson(json: String): SessionData {
        val typeToken = object : TypeToken<SessionData>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    private fun sessionDataToJson(sessiondata: SessionData?): String {
        val gson = Gson()
        return gson.toJson(sessiondata)
    }


}

data class SessionData(
    val username : String?,
    val password : String?,
    val token : String?,
)