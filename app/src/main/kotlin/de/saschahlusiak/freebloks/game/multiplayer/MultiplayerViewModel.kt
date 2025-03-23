package de.saschahlusiak.freebloks.game.multiplayer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device.Major
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.saschahlusiak.freebloks.app.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MultiplayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: Preferences
) : ViewModel() {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    val bluetoothDevices = MutableStateFlow(emptyList<BluetoothDevice>())
    val type = MutableStateFlow(NetworkType.Internet)

    val name = MutableStateFlow(prefs.playerName)
    val server = MutableStateFlow(prefs.serverAddress)

    init {
        type.onEach {
            if (it == NetworkType.Bluetooth) {
                updateBluetoothDevices()
            }
        }.launchIn(viewModelScope)
    }

    fun save() {
        prefs.playerName = name.value
        prefs.serverAddress = server.value
    }

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 31) {
            // Android S introduced permission BLUETOOTH_CONNECT, which is required to connect and listen
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun updateBluetoothDevices() {
        if (!hasBluetoothPermission() || (bluetoothAdapter?.isEnabled != true)) {
            bluetoothDevices.value = emptyList()
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
        Log.d(tag, "Paired devices: " + pairedDevices.size)

        bluetoothDevices.value = pairedDevices
            .filter {
                it.bluetoothClass.majorDeviceClass in listOf(Major.COMPUTER, Major.PHONE)
            }
            .sortedBy { it.name }
    }

    companion object {
        private val tag = MultiplayerViewModel::class.simpleName
    }
}