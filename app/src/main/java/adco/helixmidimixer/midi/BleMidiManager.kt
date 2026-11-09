package adco.helixmidimixer.midi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface BleConnectionState {
    data object Disconnected : BleConnectionState
    data object Scanning : BleConnectionState
    data class Connecting(val deviceName: String?) : BleConnectionState
    data class Connected(val deviceName: String?, val address: String) : BleConnectionState
    data class Error(val message: String) : BleConnectionState
}

data class BleDeviceInfo(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int
)

class BleMidiManager(private val context: Context) {

    companion object {
        private const val TAG = "BleMidiManager"

        val MIDI_SERVICE_UUID: UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
        val MIDI_CHARACTERISTIC_UUID: UUID = UUID.fromString("7772E5DB-3868-4112-A1A9-F2669D106BF3")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private var scanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var midiCharacteristic: BluetoothGattCharacteristic? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceInfo>> = _scannedDevices.asStateFlow()

    private val _incomingMidiCc = MutableSharedFlow<MidiCcMessage>(extraBufferCapacity = 64)
    val incomingMidiCc: SharedFlow<MidiCcMessage> = _incomingMidiCc.asSharedFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val deviceName = device.name ?: result.scanRecord?.deviceName ?: "Unknown Device"
                val info = BleDeviceInfo(
                    device = device,
                    name = deviceName,
                    address = device.address,
                    rssi = result.rssi
                )
                val currentList = _scannedDevices.value.toMutableList()
                val index = currentList.indexOfFirst { it.address == device.address }
                if (index >= 0) {
                    currentList[index] = info
                } else {
                    currentList.add(info)
                }
                _scannedDevices.value = currentList
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code $errorCode")
            _connectionState.value = BleConnectionState.Error("Scan failed: error code $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceName = gatt?.device?.name
            val deviceAddress = gatt?.device?.address ?: ""

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server. Discovering services...")
                _connectionState.value = BleConnectionState.Connecting(deviceName)
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.")
                midiCharacteristic = null
                bluetoothGatt = null
                _connectionState.value = BleConnectionState.Disconnected
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                val service = gatt.getService(MIDI_SERVICE_UUID)
                if (service != null) {
                    val char = service.getCharacteristic(MIDI_CHARACTERISTIC_UUID)
                    if (char != null) {
                        midiCharacteristic = char
                        // Enable notifications
                        gatt.setCharacteristicNotification(char, true)

                        val descriptor = char.getDescriptor(CCCD_UUID)
                        if (descriptor != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            } else {
                                @Suppress("DEPRECATION")
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                @Suppress("DEPRECATION")
                                gatt.writeDescriptor(descriptor)
                            }
                        }

                        val deviceName = gatt.device?.name
                        val address = gatt.device?.address ?: ""
                        _connectionState.value = BleConnectionState.Connected(deviceName, address)
                        Log.d(TAG, "BLE MIDI Service and Characteristic discovered successfully.")
                    } else {
                        Log.e(TAG, "MIDI Characteristic not found!")
                        _connectionState.value = BleConnectionState.Error("MIDI Characteristic missing")
                    }
                } else {
                    Log.e(TAG, "MIDI Service not found!")
                    _connectionState.value = BleConnectionState.Error("MIDI Service missing")
                }
            } else {
                Log.e(TAG, "Service discovery failed with status $status")
                _connectionState.value = BleConnectionState.Error("Service discovery failed ($status)")
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { bytes ->
                parseAndEmitMidiBytes(bytes)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseAndEmitMidiBytes(value)
        }
    }

    private fun parseAndEmitMidiBytes(bytes: ByteArray) {
        val messages = BleMidiPacketParser.parse(bytes)
        for (msg in messages) {
            scope.launch {
                _incomingMidiCc.emit(msg)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is disabled")
            return
        }

        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            _connectionState.value = BleConnectionState.Error("BLE Scanner not available")
            return
        }

        _scannedDevices.value = emptyList()
        _connectionState.value = BleConnectionState.Scanning

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MIDI_SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner?.startScan(listOf(filter), settings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing Bluetooth permissions for scan", e)
            _connectionState.value = BleConnectionState.Error("Bluetooth permission missing")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE scan", e)
            _connectionState.value = BleConnectionState.Error("Failed to start scan: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping scan", e)
        }
        if (_connectionState.value == BleConnectionState.Scanning) {
            _connectionState.value = BleConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        stopScan()
        _connectionState.value = BleConnectionState.Connecting(device.name)
        try {
            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission to connect BLE device", e)
            _connectionState.value = BleConnectionState.Error("Bluetooth permission missing")
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting BLE device", e)
            _connectionState.value = BleConnectionState.Error("Connection failed: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectByAddress(address: String) {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _connectionState.value = BleConnectionState.Error("Bluetooth unavailable")
            return
        }
        try {
            val device = adapter.getRemoteDevice(address)
            connect(device)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get remote device for address $address", e)
            _connectionState.value = BleConnectionState.Error("Invalid BLE device address")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error during GATT disconnect", e)
        }
        bluetoothGatt = null
        midiCharacteristic = null
        _connectionState.value = BleConnectionState.Disconnected
    }

    @SuppressLint("MissingPermission")
    fun sendControlChange(channel: Int, ccNumber: Int, value: Int): Boolean {
        val gatt = bluetoothGatt
        val characteristic = midiCharacteristic

        if (gatt == null || characteristic == null) {
            Log.w(TAG, "Cannot send CC: BLE GATT not connected or MIDI characteristic missing")
            return false
        }

        val packet = BleMidiPacketParser.createCcPacket(channel, ccNumber, value)
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        return try {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    characteristic,
                    packet,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
                // Android 13+ writeCharacteristic returns Int status code
                true // or check status == 0
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = packet
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(characteristic)
            }
            Log.d(TAG, "Sent MIDI CC result: $result for channel=$channel, cc=$ccNumber, val=$value")
            result
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission missing to write BLE characteristic", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error sending CC packet over BLE", e)
            false
        }
    }
}
