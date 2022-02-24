sealed class ConnectionEvent {

    sealed class Command : ConnectionEvent() {
        data class Connect internal constructor(val tryCount: Int) : Command()
        object Disconnect : Command()
    }

    sealed class StateChange(val deviceState: DeviceState) : ConnectionEvent() {
        object Disconnecting : StateChange(DeviceState.DISCONNECTING)
        object Connecting : StateChange(DeviceState.CONNECTING)
    }

    sealed class ActivityEvent : ConnectionEvent() {
        object Connected : ConnectionEvent()
        object Disconnected : ConnectionEvent()
        object IncomingData: ConnectionEvent()
        object OutcomeData: ConnectionEvent()
    }

    override fun toString(): String {
        return this::class.simpleName ?: "DeviceStateEvent"
    }


}
