import com.tinder.StateMachine

open class DeviceConnection(val stateManager: DeviceStateManager) {

    fun getState(): DeviceState {

        return stateManager.stateMachine.state
    }

    fun sendCommand(command: ConnectionEvent.Command): Boolean {
        val transition = stateManager.stateMachine.transition(command)
        return (transition is StateMachine.Transition.Valid)
    }
}

class NetworkDeviceConnection() : DeviceConnection(DeviceStateManager()) {

}
