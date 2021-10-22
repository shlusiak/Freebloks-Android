package de.saschahlusiak.freebloks.game.dialogs

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothClientToSocketThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.databinding.JoinBluetoothDeviceBinding
import de.saschahlusiak.freebloks.databinding.MultiplayerFragmentBinding
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.viewBinding
import kotlinx.coroutines.launch

class MultiplayerFragment : MaterialDialogFragment(R.layout.multiplayer_fragment), RadioGroup.OnCheckedChangeListener, View.OnClickListener, TextWatcher, OnBluetoothConnectedListener {
    private val TAG = MultiplayerFragment::class.java.simpleName

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothServer: BluetoothServerThread? = null
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val listener get() = activity as? OnStartCustomGameListener

    private val binding by viewBinding(MultiplayerFragmentBinding::bind)

    private val clientName: String?
        get() = binding.name.text.toString().trim { it <= ' ' }.ifBlank { null }

    private val customServerAddress: String?
        get() = binding.serverAddress.text.toString().trim { it <= ' ' }.ifBlank { null }

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        closeButton.setOnClickListener { dismiss() }
        okButton.setOnClickListener(this@MultiplayerFragment)

        hostGame.setOnClickListener {
            analytics.logEvent("multiplayer_host_click", null)

            saveSettings()
            val config = GameConfig(server = null, showLobby = true)
            listener?.onStartClientGameWithConfig(config, clientName)
            dismiss()
        }
        serverAddress.apply {
            setText(prefs.getString("custom_server", ""))
            addTextChangedListener(this@MultiplayerFragment)
        }
        name.setText(prefs.getString("player_name", null))

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothList.visibility = View.GONE
        if (bluetoothAdapter == null) {
            // bluetooth not supported
            radioButtonBluetooth.visibility = View.GONE
        }
        serverType.setOnCheckedChangeListener(this@MultiplayerFragment)
        radioButtonInternet.isChecked = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.join_multiplayer_game)
        }
    }

    override fun onStart() {
        super.onStart()

        // FIXME: probably need to ask for runtime permission BLUETOOTH_CONNECT here
        if (bluetoothServer == null) {
            bluetoothServer = startBluetoothServer()
        }
    }

    override fun onStop() {
        bluetoothServer?.shutdown()
        bluetoothServer = null

        super.onStop()
    }

    private fun startBluetoothServer(): BluetoothServerThread? {
        val adapter = bluetoothAdapter ?: return null
        if (!adapter.isEnabled) return null

        if (Build.VERSION.SDK_INT >= 31) {
            // Android S introduced permission BLUETOOTH_CONNECT, which is required to connect and listen
            val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_CONNECT)
                return null
            }
        }

        return BluetoothServerThread(this).also {
            it.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothServer == null) {
                    bluetoothServer = startBluetoothServer()
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        updateOkButtonEnabled()
    }

    override fun afterTextChanged(s: Editable) {}

    private fun updateOkButtonEnabled() = with(binding) {
        val checkedId = serverType.checkedRadioButtonId
        okButton.isEnabled = when {
            checkedId == R.id.radioButtonWifi && customServerAddress == null -> false
            checkedId == R.id.radioButtonBluetooth -> false
            else -> true
        }
    }

    override fun onClick(view: View) {
        when (binding.serverType.checkedRadioButtonId) {
            R.id.radioButtonInternet -> {
                analytics.logEvent("multiplayer_internet_click", null)
                val config = GameConfig(server = Global.DEFAULT_SERVER_ADDRESS, showLobby = true)
                listener?.onStartClientGameWithConfig(config, clientName)
            }
            R.id.radioButtonWifi -> {
                customServerAddress ?: return
                analytics.logEvent("multiplayer_wireless_click", null)
                val config = GameConfig(server = customServerAddress, showLobby =  true)
                listener?.onStartClientGameWithConfig(config, clientName)
            }
        }

        saveSettings()
        dismiss()
    }

    private fun saveSettings() {
        prefs.edit()
            .putString("player_name", clientName)
            .putString("custom_server", customServerAddress)
            .apply()
    }

    override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) = with(binding) {
        when(i) {
            R.id.radioButtonInternet -> {
                serverAddress.visibility = View.GONE
                hostGame.visibility = View.INVISIBLE
                bluetoothList.visibility = View.GONE
            }
            R.id.radioButtonWifi -> {
                serverAddress.visibility = View.VISIBLE
                hostGame.visibility = View.VISIBLE
                bluetoothList.visibility = View.GONE

                serverAddress.requestFocus()
            }
            R.id.radioButtonBluetooth -> {
                serverAddress.visibility = View.GONE
                hostGame.visibility = View.VISIBLE
                bluetoothList.visibility = View.VISIBLE

                if (bluetoothServer == null) {
                    bluetoothServer = startBluetoothServer()
                }
                updateBluetoothDeviceList()
            }
        }

        updateOkButtonEnabled()
    }

    private fun updateBluetoothDeviceList() {
        val bluetoothList = binding.bluetoothList
        val deviceSelectedListener = View.OnClickListener { v ->
            val config = GameConfig(showLobby = true)
            saveSettings()
            val device = v.tag as BluetoothDevice
            Log.i(TAG, "Device selected: " + device.name)
            analytics.logEvent("multiplayer_bluetooth_click", null)
            listener?.onConnectToBluetoothDevice(config, clientName, device)
            dismiss()
        }

        bluetoothList.removeAllViews()

        val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        Log.d(TAG, "Paired devices: " + pairedDevices.size)

        if ((bluetoothAdapter?.isEnabled != true) || pairedDevices.isEmpty()) {
            val binding = JoinBluetoothDeviceBinding.inflate(layoutInflater, bluetoothList, false)
            binding.image.visibility = View.GONE
            binding.text1.apply {
                setText(R.string.bluetooth_disabled_message)
            }
            bluetoothList.addView(binding.root, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            return
        }

        for (device in pairedDevices) {
            Log.d(TAG, String.format("device %s [%s]", device.name, device.address))

            with(JoinBluetoothDeviceBinding.inflate(layoutInflater, bluetoothList, false)) {
                text1.text = device.name
                root.tag = device
                root.setOnClickListener(deviceSelectedListener)
                bluetoothList.addView(root, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }
    }

    @UiThread
    override fun onBluetoothClientConnected(socket: BluetoothSocket) {
        // a client has connected to us. quickly host a game and get the two together by starting the bridge
        val config = GameConfig(server = null, showLobby = true)

        activity?.lifecycleScope?.launch {
            val listener = listener ?: return@launch
            val job = listener.onStartClientGameWithConfig(config, clientName)
            job.join()
            if (job.isCancelled) return@launch

            // we can only run this after the TCP server is running, so we can connect to it and start the bridge
            BluetoothClientToSocketThread(socket, "localhost", GameClient.DEFAULT_PORT).start()
        }

        // dismissing this dialog will stop the listener, which will be started again by the LobbyDialog
        dismiss()
    }

    companion object {
        private const val REQUEST_BLUETOOTH_CONNECT = 10004
    }
}