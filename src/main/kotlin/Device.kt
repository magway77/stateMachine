interface Device {
    fun start()
    fun stop()
}

class NetworkDevice() : Device {
    private val connection: DeviceConnection = NetworkDeviceConnection()

    override fun start() {
        connection.sendCommand(ConnectionEvent.Command.Connect(4))
    }

    override fun stop() {
        connection.sendCommand(ConnectionEvent.Command.Disconnect)
    }

}
