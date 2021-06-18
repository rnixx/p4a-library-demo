package org.bd.activitydemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.android.synthetic.main.activity_main.*
import org.kivy.kivynativeactivity.Echo2Worker
import org.kivy.kivynativeactivity.ServiceBoundecho
import org.kivy.kivynativeactivity.ServiceEcho
import org.kivy.kivynativeactivity.EchoWorker

// additional foo
import androidx.work.multiprocess.RemoteWorkerService
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME
import com.google.common.util.concurrent.ListenableFuture


class MainActivity : AppCompatActivity() {

    val client = HttpClient {
        install(WebSockets)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun testConnection(ip: String, port: Int, message:String = "Howdii") {
        runBlocking {
            client.ws(
                    method = HttpMethod.Get,
                    host = ip,
                    port = port, path = "/"
            ) {
                send(Frame.Text(message))
                // Receive frame.
                val frame = incoming.receive()
                when (frame) {
                    is Frame.Text -> txtOutput.text=frame.readText()
                    is Frame.Binary -> txtOutput.text=frame.readBytes().toString()
                }
            }
        }
    }

    fun onSend(v: View){

        GlobalScope.launch(Dispatchers.IO) {
            testConnection("localhost", 8081, editMessage.text.toString())
        }
    }

    fun onSendBound(v: View){

        GlobalScope.launch(Dispatchers.IO) {
            testConnection("localhost", 8082, editMessage.text.toString())
        }
    }

    fun onStart(v: View) {
        val context = applicationContext
        ServiceEcho.prepare(context)
        ServiceEcho.start(context, "")
        btnSend.setEnabled(true)
    }

    fun onStartEchoWorker(v: View) {
        val text = "Starting EchoWorker"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()

        // val simpleWorkRequest: WorkRequest =
        //     OneTimeWorkRequestBuilder<EchoWorker>()
        //         .build()

        // WorkManager
        //     .getInstance(this)
        //     .enqueue(simpleWorkRequest)

        val serviceName = RemoteWorkerService::class.java.name
        val componentName = ComponentName("org.bd.activitydemo", serviceName)

        val oneTimeWorkRequest = buildOneTimeWorkRemoteWorkRequest(
            componentName,
            EchoWorker::class.java
        )

        val workManager = WorkManager.getInstance(this@MainActivity)
        val result: Operation = workManager?.enqueue(oneTimeWorkRequest)
    }

    private fun buildOneTimeWorkRemoteWorkRequest(
        componentName: ComponentName
        , listenableWorkerClass: Class<out ListenableWorker>
    ): OneTimeWorkRequest {

        // ARGUMENT_PACKAGE_NAME and ARGUMENT_CLASS_NAME are used to determine the service
        // that a Worker binds to. By specifying these parameters, we can designate the process a
        // Worker runs in.
        val data: Data = Data.Builder()
            .putString(ARGUMENT_PACKAGE_NAME, componentName.packageName)
            .putString(ARGUMENT_CLASS_NAME, componentName.className)
            .build()

        return OneTimeWorkRequest.Builder(listenableWorkerClass)
            .setInputData(data)
            .build()
    }

    fun onStartEcho2Worker(v: View) {
        val text = "Starting Echo2Worker"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()

        val simpleWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<Echo2Worker>()
                .build()

        WorkManager
            .getInstance(this)
            .enqueue(simpleWorkRequest)
    }

    fun onStartSampleWorker(v: View) {
        val text = "Starting local SampleWorker"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()

        val simpleWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<SampleWorker>()
                .build()

        val operation: Operation = WorkManager
            .getInstance(this)
            .enqueue(simpleWorkRequest)

        Toast.makeText(applicationContext, "operation enqueued", duration).show()

        // to observe the result
        // in this example we intentionally bind on applicationContext to demonstrate
        // worker use in background tasks

        val workManager = WorkManager.getInstance(applicationContext)
        val status: LiveData<WorkInfo> = workManager.getWorkInfoByIdLiveData(simpleWorkRequest.getId())

        class WMObserver: Observer<WorkInfo> {
            override fun onChanged(it: WorkInfo) {
                if(it.state.name == "SUCCEEDED") {
                    Toast.makeText(applicationContext, "Success!!!", duration).show()
                    status.removeObserver(this)
                }
                Log.d("SampleWorker", "Result incoming: ${it.state.name}")
            }
        }
        status.observeForever(WMObserver())
    }

    private lateinit var mService: SampleBoundService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as SampleBoundService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    fun onStartSampleBoundService(v: View){
        Intent(applicationContext, SampleBoundService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun onUnbindSampleBoundService(v: View){
        println("unbinding bound  service")
        this.unbindService(this.connection)
    }

    private lateinit var mPyService: ServiceBoundecho
    private var mPyBound: Boolean = false

    private val pyconnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            // val binder = service as ServiceBoundecho.BoundechoBinder
            // mPyService = binder.getService()
            btnSendBound.setEnabled(true)
            mPyBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mPyBound = false
        }
    }

    fun onStartPyBoundService(v: View){
        Intent(applicationContext, ServiceBoundecho::class.java).also { intent ->
            bindService(intent, pyconnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun onUnbindPyBoundService(v: View){
        println("unbinding python bound  service")
        this.unbindService(this.pyconnection)
    }

}
