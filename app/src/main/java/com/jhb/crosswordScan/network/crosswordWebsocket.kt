package com.jhb.crosswordScan.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.BuildConfig
import com.jhb.crosswordScan.data.Cell
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


class CrosswordWebSocketClient(private val puzzleId: Int) : WebSocketListener() {

    private companion object {
        val TAG = "CrosswordWebSocketClient"
    }

    private val client = createClient()
    private val typeToken = object : TypeToken<Cell>() {}.type
    private val gson = Gson()

    val ws = createWebSocket(client)
    val cellUpdates : MutableLiveData<Cell> by lazy {
        MutableLiveData<Cell>()
    }

    private fun createWebSocket(client: OkHttpClient): WebSocket {

        val request = Request.Builder()
            .url("${BuildConfig.WEBSOCKET_URL}/puzzle/$puzzleId/live")
            .build()
        return client.newWebSocket(request, this)

        // Trigger shutdown of the dispatcher's executor so this process exits immediately.
        // client.dispatcher().executorService().shutdown()
    }

    private fun createClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        return client
    }


//    override fun onOpen(webSocket: WebSocket, response: Response) {
//        webSocket.send("Hello...")
//        webSocket.send("...World!")
//        webSocket.send(ByteString.decodeHex("deadbeef"))
//        webSocket.close(1000, "Goodbye, World!")
//    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(TAG, text)
        val cell = gson.fromJson<Cell>(text, typeToken)
        cellUpdates.postValue(cell)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        Log.i(TAG, "closing websocket")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.w(TAG, "Failure of websocket")
        super.onFailure(webSocket, t, response)
    }

}