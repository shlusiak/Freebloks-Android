package de.saschahlusiak.freebloks.game.multiplayer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device.Major
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MultiplayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) : ViewModel() {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    val bluetoothDevices = MutableStateFlow(emptyList<BluetoothDevice>())
    val type = MutableStateFlow(NetworkType.Internet)

    val name = MutableStateFlow(prefs.getString("player_name", "") ?: "")
    val server = MutableStateFlow(prefs.getString("custom_server", "") ?: "")

    init {
        type.onEach {
            if (it == NetworkType.Bluetooth) {
                updateBluetoothDevices()
            }
        }.launchIn(viewModelScope)
    }

    fun saveName() {
        prefs.edit()
            .putString("player_name", name.value)
            .apply()

    }

    fun saveServer() {
        prefs.edit()
            .putString("custom_server", server.value)
            .apply()
    }

    fun hasBluetoothPermission(): Boolean {
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

        val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
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