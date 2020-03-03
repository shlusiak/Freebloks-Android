package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.model.GameConfig
import kotlinx.android.synthetic.main.join_dialog.*
import java.io.IOException

class JoinDialog(context: Context, private val listener: OnStartCustomGameListener) : Dialog(context, R.style.Theme_Freebloks_Light_Dialog), RadioGroup.OnCheckedChangeListener, View.OnClickListener, TextWatcher, OnBluetoothConnectedListener {
    private lateinit var name: EditText
    private lateinit var server: EditText
    private lateinit var serverType: RadioGroup
    private lateinit var hostGame: Button
    private lateinit var bluetoothList: ViewGroup
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothServer: BluetoothServerThread? = null
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.join_dialog)
        setTitle(R.string.join_multiplayer_game)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        findViewById<View>(R.id.close_button).setOnClickListener { dismiss() }
        findViewById<View>(R.id.ok_button).setOnClickListener(this)

        hostGame = findViewById(R.id.host_game)
        hostGame.setOnClickListener {
            saveSettings()
            dismiss()
            listener.setClientName(getName())
            val config = GameConfig(null, true)
            listener.onStartClientGameWithConfig(config)
        }
        name = findViewById(R.id.name)
        server = findViewById(R.id.server_address)
        server.setText(prefs.getString("custom_server", ""))
        server.addTextChangedListener(this)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothList = findViewById(R.id.bluetoothList)
        bluetoothList.visibility = View.GONE
        if (bluetoothAdapter == null) {
            // bluetooth not supported
            radioButtonBluetooth.visibility = View.GONE
        }
        serverType = findViewById(R.id.server_type)
        serverType.setOnCheckedChangeListener(this)
        radioButtonInternet.isChecked = true
    }

    override fun onStart() {
        super.onStart()

        startBluetoothServer()
    }

    override fun onStop() {
        bluetoothServer?.shutdown()
        bluetoothServer = null

        super.onStop()
    }

    private fun startBluetoothServer() {
        val adapter = bluetoothAdapter ?: return
        if (bluetoothServer == null && adapter.isEnabled) {
            bluetoothServer = BluetoothServerThread(this).also {
                it.start()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        updateOkButtonEnabled()
    }

    override fun afterTextChanged(s: Editable) {}

    private fun updateOkButtonEnabled() {
        val checkedId = serverType.checkedRadioButtonId
        ok_button.isEnabled = when {
            checkedId == R.id.radioButtonWifi && customServerAddress.isEmpty() -> false
            checkedId == R.id.radioButtonBluetooth -> false
            else -> true
        }
    }

    override fun onClick(view: View) {
        listener.setClientName(getName())

        when (serverType.checkedRadioButtonId) {
            R.id.radioButtonInternet -> {
                val config = GameConfig(server = Global.DEFAULT_SERVER_ADDRESS, showLobby = true)
                listener.onStartClientGameWithConfig(config)
            }
            R.id.radioButtonWifi -> {
                if (customServerAddress.isEmpty()) return
                val config = GameConfig(server = customServerAddress, showLobby =  true)
                listener.onStartClientGameWithConfig(config)
            }
        }

        saveSettings()
        dismiss()
    }

    private fun saveSettings() {
        prefs.edit()
            .putString("player_name", getName())
            .putString("custom_server", customServerAddress)
            .apply()
    }

    fun setName(name: String?) {
        this.name.setText(name)
    }

    private fun getName(): String {
        return name.text.toString().trim { it <= ' ' }
    }

    private val customServerAddress: String
        get() = server.text.toString().trim { it <= ' ' }

    override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) {
        when(i) {
            R.id.radioButtonInternet -> {
                server.visibility = View.GONE
                hostGame.visibility = View.GONE
                bluetoothList.visibility = View.GONE
            }
            R.id.radioButtonWifi -> {
                server.visibility = View.VISIBLE
                hostGame.visibility = View.VISIBLE
                bluetoothList.visibility = View.GONE

                server.requestFocus()
            }
            R.id.radioButtonBluetooth -> {
                server.visibility = View.GONE
                hostGame.visibility = View.VISIBLE
                bluetoothList.visibility = View.VISIBLE

                startBluetoothServer()
                updateBluetoothDeviceList()
            }
        }

        updateOkButtonEnabled()
    }

    @Deprecated("Use Thread and set ViewModel status to Connecting to show dialog")
    private inner class ConnectBluetoothTask() : AsyncTask<BluetoothDevice, Void?, BluetoothSocket?>() {
        private var progress: ProgressDialog

        init {
            progress = ProgressDialog(context).apply {
                isIndeterminate = true
                setMessage(context.getString(R.string.connecting))
                setCancelable(true)
                setOnCancelListener {
                    Log.w(tag, "Cancelling connect task")
                    cancel(true)
                }
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress.show()
        }

        override fun doInBackground(vararg bluetoothDevices: BluetoothDevice): BluetoothSocket? {
            val device = bluetoothDevices[0]
            val socket: BluetoothSocket

            return try {
                Log.i(tag, "Connecting to " + device.name + "/" + device.address)
                socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothServerThread.SERVICE_UUID)
                socket.connect()
                Log.i(tag, "Connection successful")
                socket
            } catch (e: IOException) {
                Log.e(tag, "Connection failed")
                e.printStackTrace()
                null
            }
        }

        override fun onCancelled() {
            progress.dismiss()
            super.onCancelled()
        }

        override fun onPostExecute(bluetoothSocket: BluetoothSocket?) {
            progress.dismiss()

            if (bluetoothSocket != null) {
                // success
                dismiss()
                listener.setClientName(getName())
                listener.onJoinGameWithSocket(bluetoothSocket)
            } else {
                // error
                Toast.makeText(context, R.string.connection_refused, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBluetoothDeviceList() {
        val deviceSelectedListener = View.OnClickListener { v ->
            saveSettings()
            val device = v.tag as BluetoothDevice
            Log.i(tag, "Device selected: " + device.name)
            ConnectBluetoothTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device)
        }

        bluetoothList.removeAllViews()

        val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        Log.d(tag, "Paired devices: " + pairedDevices.size)

        if ((bluetoothAdapter?.isEnabled != true) || pairedDevices.isEmpty()) {
            val v = layoutInflater.inflate(R.layout.join_bluetooth_device, bluetoothList, false)
            v.findViewById<View>(R.id.image).visibility = View.GONE
            v.findViewById<TextView>(android.R.id.text1).apply {
                setText(R.string.bluetooth_disabled)
            }
            bluetoothList.addView(v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            return
        }

        for (device in pairedDevices) {
            Log.d(tag, String.format("device %s [%s]", device.name, device.address))
            val v = layoutInflater.inflate(R.layout.join_bluetooth_device, bluetoothList, false).apply {
                findViewById<TextView>(android.R.id.text1).apply {
                    text = device.name
                }
                tag = device
                setOnClickListener(deviceSelectedListener)
            }
            bluetoothList.addView(v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    override fun onBluetoothClientConnected(socket: BluetoothSocket) {
        // a client has connected to us. quickly host a game and get the two together by starting the bridge
        dismiss()
        listener.setClientName(getName())
        listener.onHostBluetoothGameWithClientSocket(socket)
    }

    companion object {
        private val tag = JoinDialog::class.java.simpleName
    }
}