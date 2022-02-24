import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

@Tag("unitTest")
internal class DeviceStateManagerTest {
    private lateinit var deviceStateManager: DeviceStateManager

    @BeforeEach
    fun configureTest() {
        deviceStateManager = DeviceStateManager()
    }

    @Test
    internal fun test_checkMissingDeclaration() {
        val deviceStates: List<DeviceState> = getSubClasses(DeviceState::class)
            .mapNotNull { createKClassInstance(it) }
        val connectionEvents: List<ConnectionEvent> = getSubClasses(ConnectionEvent::class)
            .mapNotNull { createKClassInstance(it) }
        deviceStates.forEach { fromState ->
            connectionEvents.forEach { toStateEvent ->
                deviceStateManager.stateMachine.with { initialState(fromState) }
                deviceStateManager.stateMachine.transition(toStateEvent)
            }
        }
    }

    @Test
    internal fun test_test_someTransitions() {
        Assertions.assertEquals(DeviceState.UNKNOWN, deviceStateManager.stateMachine.state)

        deviceStateManager.stateMachine.transition(ConnectionEvent.Command.Disconnect)
        Assertions.assertEquals(DeviceState.DISCONNECTED, deviceStateManager.stateMachine.state)

        deviceStateManager.stateMachine.transition(ConnectionEvent.StateChange.Connecting)
        Assertions.assertEquals(DeviceState.CONNECTING, deviceStateManager.stateMachine.state)

        deviceStateManager.stateMachine.transition(ConnectionEvent.Command.Disconnect)
        Assertions.assertEquals(DeviceState.CONNECTING, deviceStateManager.stateMachine.state)
    }

    private fun <R : Any> createKClassInstance(kClass: KClass<R>): R? {
        if (kClass.isData) {
            val primaryConstr = kClass.primaryConstructor
            if (primaryConstr != null) {
                val parameters = primaryConstr.parameters
                if (parameters.isEmpty()) {
                    return primaryConstr.call()
                }
                val args = parameters.associateBy({ it },
                    { kParameter: KParameter ->
                        when (kParameter.type) {
                            Int::class.starProjectedType -> (0..100).random()
                            String::class.starProjectedType -> (0..100).random().toString()
                            else -> null
                        }
                    }
                )
                return primaryConstr.callBy(args)
            }
        }
        return kClass.objectInstance
    }

    private fun <T : Any> getSubClasses(kClass: KClass<T>): List<KClass<out T>> {
        val sealedSubclasses: List<KClass<out T>> = kClass.sealedSubclasses
        val resultList: MutableList<KClass<out T>> = mutableListOf()
        sealedSubclasses.forEach {
            if (it.isSealed) {
                resultList.addAll(getSubClasses(it))
            } else {
                resultList.add(it)
            }
        }
        return resultList
    }
}
