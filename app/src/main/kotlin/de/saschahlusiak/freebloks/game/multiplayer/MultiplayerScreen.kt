package de.saschahlusiak.freebloks.game.multiplayer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.game.multiplayer.NetworkType.*
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@SuppressLint("MissingPermission")
@Composable
fun MultiplayerScreen(
    bluetoothDevices: List<BluetoothDevice>,
    type: NetworkType,
    setType: (NetworkType) -> Unit,
    name: String,
    setName: (String) -> Unit,
    server: String,
    setServer: (String) -> Unit,
    onCancel: () -> Unit,
    onJoinInternet: (String) -> Unit,
    onJoinWifi: (String, String) -> Unit,
    onHost: (String) -> Unit,
    onBluetooth: (String, BluetoothDevice) -> Unit
) {
    Dialog {
        Column(
            Modifier.padding(MaterialTheme.dimensions.dialogPadding),
            verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
        ) {
            Text(
                text = stringResource(id = R.string.join_multiplayer_game),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = name,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                onValueChange = { setName(it) },
                placeholder = { Text(stringResource(id = R.string.player_name_hint)) }
            )

            RadioButtonListItem(stringResource(id = R.string.internet), type == Internet) { setType(Internet) }
            RadioButtonListItem(stringResource(id = R.string.wireless_lan), type == Wifi) { setType(Wifi) }

            AnimatedVisibility(visible = type == Wifi) {
                OutlinedTextField(
                    value = server,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { setServer(it) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        if (server.isNotBlank()) onJoinWifi(name, server)
                    }),
                    singleLine = true,
                    placeholder = { Text(stringResource(id = R.string.server_address_hint)) }
                )
            }

            RadioButtonListItem(stringResource(id = R.string.bluetooth), type == Bluetooth) { setType(Bluetooth) }

            AnimatedVisibility(visible = type == Bluetooth) {
                LazyRow {
                    bluetoothDevices.forEach { device ->
                        item(key = device.address) {
                            BluetoothListItem(
                                name = device.name
                            ) { onBluetooth(name, device) }
                        }
                    }
                }

                if (bluetoothDevices.isEmpty()) {
                    Text(
                        stringResource(id = R.string.bluetooth_disabled_message),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(alignment = Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                AnimatedVisibility(visible = type != Internet) {
                    OutlinedButton(
                        modifier = Modifier.heightIn(min = MaterialTheme.dimensions.buttonSize),
                        onClick = { onHost(name) }) {
                        Text(stringResource(id = R.string.host_game))
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .width(IntrinsicSize.Min),
                    horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = MaterialTheme.dimensions.buttonSize),
                        onClick = onCancel
                    ) {
                        Text(stringResource(id = android.R.string.cancel))
                    }


                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = MaterialTheme.dimensions.buttonSize),
                        onClick = {
                            when (type) {
                                Internet -> onJoinInternet(name)
                                Wifi -> onJoinWifi(name, server)
                                Bluetooth -> Unit
                            }
                        },
                        enabled = (type == Internet || (type == Wifi && server.isNotBlank()))
                    ) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        var type by remember { mutableStateOf(Internet) }
        MultiplayerScreen(
            bluetoothDevices = emptyList(),
            type = type,
            setType = { type = it },
            name = "",
            setName = {},
            server = "",
            setServer = {},
            onCancel = {},
            onJoinInternet = { },
            onJoinWifi = { _, _ -> },
            onHost = {},
            onBluetooth = { _, _ -> }
        )
    }
}