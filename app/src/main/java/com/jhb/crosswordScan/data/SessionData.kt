package com.jhb.crosswordScan.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
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


    fun setContext(context: Context) {
        applicationContext = context
        masterKey = androidx.security.crypto.MasterKey.Builder(
            applicationContext,
            androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS
        )
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build();

        val fileToRead = "user.txt"
        encryptedFile = EncryptedFile.Builder(
            applicationContext,
            File(applicationContext.filesDir, fileToRead),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

    }




     fun getUser() : String? {



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

         }
         catch (e : IOException){
             Log.w(TAG,"No user file found")
             return null
         }
     }

     fun setUser(id: Int) {
         Log.i(TAG,"updating user.txt")

         val fileToWrite = "user.txt"
         val file = File(applicationContext.filesDir, fileToWrite)

         if (file.exists()) { file.delete() }


         val fileContent = id
             .toString()
             .toByteArray(StandardCharsets.UTF_8)

         encryptedFile.openFileOutput().apply {

             write(fileContent)
             flush()
             close()
         }
     }
}