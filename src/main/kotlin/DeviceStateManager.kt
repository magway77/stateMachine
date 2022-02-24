import com.tinder.StateMachine
import java.util.logging.Level
import java.util.logging.Logger

object DebugLogger {
    val logger: Logger = Logger.getLogger(DebugLogger::class.java.name)
    var debugMode: Boolean = true
}

private fun debugLog(str: String, level: Level = Level.INFO) {
    if (!DebugLogger.debugMode) {
        return
    }
    with(DebugLogger) {
        when (level) {
            Level.SEVERE -> logger.severe(str)
            Level.WARNING -> logger.warning(str)
            else -> logger.info(str)
        }
    }
}

sealed class DeviceState constructor(val description: String) {
    object UNKNOWN : DeviceState("Unknown")
    object DISCONNECTED : DeviceState("Disconnected")
    object DISCONNECTING : DeviceState("Disconnecting")
    data class CONNECTED internal constructor(val tryCount: Int) : DeviceState("Connected")
    object CONNECTING : DeviceState("Connecting")

    override fun toString(): String {
        return description
    }
}

sealed class DeviceStateSideEffect {

    object DeviceDisconnected : DeviceStateSideEffect() {
        override fun execute(transition: StateMachine.Transition.Valid<DeviceState, ConnectionEvent, DeviceStateSideEffect>) {
            debugLog("disconnected")
        }
    }

    object LogInfo : DeviceStateSideEffect() {
        override fun execute(transition: StateMachine.Transition.Valid<DeviceState, ConnectionEvent, DeviceStateSideEffect>) {
            debugLog(
                "side effect for transition: < ${transition.fromState}, ${transition.toState}, ${transition.event} >"
            )
        }
    }

    abstract fun execute(transition: StateMachine.Transition.Valid<DeviceState, ConnectionEvent, DeviceStateSideEffect>)
}

class DeviceStateManager{
    val stateMachine = StateMachine.create<DeviceState, ConnectionEvent, DeviceStateSideEffect> {
        initialState(DeviceState.UNKNOWN)
        state<DeviceState.UNKNOWN> {
            onEnter {
                debugLog("StateMachine.onEnter")
            }
            onExit {
                debugLog("StateMachine.onExit")
            }

            on<ConnectionEvent.Command> { commandEvent ->
                debugLog("**come command: ${commandEvent}")
                when (commandEvent) {
                    is ConnectionEvent.Command.Connect -> {
                        transitionTo(
                            state = DeviceState.CONNECTING,
                            sideEffect = DeviceStateSideEffect.LogInfo)
                    }
                    is ConnectionEvent.Command.Disconnect -> {
                        transitionTo(
                            state = DeviceState.DISCONNECTING,
                            sideEffect = DeviceStateSideEffect.LogInfo)
                    }
                }
            }

            on<ConnectionEvent.StateChange> { stateEvent ->
                debugLog("**come command: ${stateEvent}")
                transitionTo(stateEvent.deviceState)
            }
        }

        state<DeviceState.DISCONNECTED> {
            on<ConnectionEvent.StateChange.Connecting> {
                transitionTo(DeviceState.CONNECTING)
            }
        }
        state<DeviceState.CONNECTING> {
        }
        state<DeviceState.DISCONNECTING> { }
        state<DeviceState.CONNECTING> { }

        onTransition {
            when (it) {
                is StateMachine.Transition.Valid -> {
                    it.sideEffect?.execute(it)
                }
                is StateMachine.Transition.Invalid -> {
                    debugLog("invalid event '${it.event}' for state '${it.fromState}'", Level.WARNING)
                }

            }

/*
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            validTransition.sideEffect?.execute()
*/
        }
    }

}
