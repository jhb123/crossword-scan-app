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

enum class ConnectionStatus {
    Connected, Disconnected
}


class CrosswordWebSocketClient(private val puzzleId: Int) : WebSocketListener() {

    private companion object {
        val TAG = "CrosswordWebSocketClient"
    }

    private val client = createClient()
    private val typeToken = object : TypeToken<Cell>() {}.type
    private val gson = Gson()

    var ws = createWebSocket(client)

    var connectionStatus: MutableLiveData<ConnectionStatus>
    lateinit var cellUpdates: MutableLiveData<Cell>

    init {
        connectionStatus = MutableLiveData(ConnectionStatus.Disconnected)
    }

    private fun createWebSocket(client: OkHttpClient): WebSocket {
        val request = Request.Builder()
            .url("${BuildConfig.WEBSOCKET_URL}/puzzle/$puzzleId/live")
            .build()
        return client.newWebSocket(request, this)
    }

    private fun createClient(): OkHttpClient {
        cellUpdates = MutableLiveData<Cell>()

        val client = OkHttpClient.Builder()
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .build()
        return client
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(TAG, text)
        val cell = gson.fromJson<Cell>(text, typeToken)
        cellUpdates.postValue(cell)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        connectionStatus.postValue(ConnectionStatus.Connected)
    }
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        connectionStatus.postValue(ConnectionStatus.Disconnected)
        webSocket.close(1000, null)
        Log.i(TAG, "closing websocket")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.w(TAG, "Failure of websocket")
        connectionStatus.postValue(ConnectionStatus.Disconnected)
//        super.onFailure(webSocket, t, response)
        webSocket.close(1000, null);
        Thread.sleep(1000);
        Log.w(TAG, "Reconnecting websocket...")
        ws = createWebSocket(client)
    }
}