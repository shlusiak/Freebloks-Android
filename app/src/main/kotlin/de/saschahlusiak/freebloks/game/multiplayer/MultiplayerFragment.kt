package de.saschahlusiak.freebloks.game.multiplayer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothClientToSocketThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.CrashReporter
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MultiplayerFragment : DialogFragment(), OnBluetoothConnectedListener {

    private val TAG = MultiplayerFragment::class.java.simpleName

    private var bluetoothServer: BluetoothServerThread? = null
    private val listener get() = activity as? OnStartCustomGameListener

    @Inject
    lateinit var analytics: AnalyticsProvider

    @Inject
    lateinit var crashReporter: CrashReporter

    private val viewModel: MultiplayerViewModel by viewModels()

    private val requestBluetoothPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && bluetoothServer == null) {
                bluetoothServer = startBluetoothServer()
            }
        }

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(inflater.context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)
        view as ComposeView
        view.setContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        AppTheme {
            val devices by viewModel.bluetoothDevices.collectAsState()

            MultiplayerScreen(
                bluetoothDevices = devices,
                type = viewModel.type.collectAsState().value,
                setType = {
                    if (bluetoothServer == null) {
                        bluetoothServer = startBluetoothServer()
                    }

                    viewModel.type.value = it
                },
                name = viewModel.name.collectAsState().value,
                setName = { viewModel.name.value = it },
                server = viewModel.server.collectAsState().value,
                setServer = { viewModel.server.value = it },
                onCancel = { dismiss() },
                onJoinInternet = { name -> joinInternet(name) },
                onJoinWifi = { name, server -> joinWifi(name, server) },
                onHost = { name -> onHostGameClicked(name) },
                onBluetooth = { name, device -> onBluetoothDeviceClick(name, device) }
            )
        }
    }

    override fun onStart() {
        super.onStart()

        if (bluetoothServer == null) {
            bluetoothServer = startBluetoothServer()
        }
    }

    override fun onStop() {
        bluetoothServer?.shutdown()
        bluetoothServer = null

        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateBluetoothDevices()
    }

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 31) {
            // Android S introduced permission BLUETOOTH_CONNECT, which is required to connect and listen
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return false
        }
        return true
    }

    private fun startBluetoothServer(): BluetoothServerThread? {
        val adapter = viewModel.bluetoothAdapter ?: return null

        if (!hasBluetoothPermission() && Build.VERSION.SDK_INT >= 31) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return null
        }

        if (!adapter.isEnabled) return null

        return BluetoothServerThread(adapter, crashReporter, this).also {
            it.start()
        }
    }

    private fun onHostGameClicked(name: String) {
        analytics.logEvent("multiplayer_host_click", null)

        viewModel.name.value = name
        viewModel.save()

        val config = GameConfig(isLocal = false, server = null, showLobby = true)
        listener?.onStartClientGameWithConfig(config, name)
        dismiss()
    }

    private fun joinInternet(name: String) {
        viewModel.name.value = name
        viewModel.save()

        analytics.logEvent("multiplayer_internet_click", null)
        val config = GameConfig(
            isLocal = false,
            server = Global.DEFAULT_SERVER_ADDRESS,
            showLobby = true
        )
        listener?.onStartClientGameWithConfig(config, name)

        dismiss()
    }

    private fun joinWifi(name: String, server: String) {
        if (server.isBlank()) return

        viewModel.name.value = name
        viewModel.server.value = server
        viewModel.save()

        analytics.logEvent("multiplayer_wireless_click", null)
        val config = GameConfig(isLocal = false, server = server, showLobby = true)
        listener?.onStartClientGameWithConfig(config, name)

        dismiss()
    }

    @SuppressLint("MissingPermission")
    private fun onBluetoothDeviceClick(name: String, device: BluetoothDevice) {
        val config = GameConfig(isLocal = false, showLobby = true)

        viewModel.name.value = name
        viewModel.save()

        Log.i(TAG, "Device selected: " + device.name)
        analytics.logEvent("multiplayer_bluetooth_click", null)
        listener?.onConnectToBluetoothDevice(config, name, device)
        dismiss()
    }

    @UiThread
    override fun onBluetoothClientConnected(socket: BluetoothSocket) {
        // a client has connected to us. quickly host a game and get the two together by starting the bridge
        val config = GameConfig(isLocal = false, server = null, showLobby = true)

        activity?.lifecycleScope?.launch {
            val listener = listener ?: return@launch
            val job = listener.onStartClientGameWithConfig(config, viewModel.name.value)
            job.join()
            if (job.isCancelled) return@launch

            // we can only run this after the TCP server is running, so we can connect to it and start the bridge
            BluetoothClientToSocketThread(socket, "localhost", GameClient.DEFAULT_PORT).start()
        }

        // dismissing this dialog will stop the listener, which will be started again by the LobbyDialog
        dismiss()
    }
}