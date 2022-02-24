import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

open class Event<out T : Any>(val source: String, val value: T)

open class StringEvent(source: String, value: String) : Event<String>(source, value) {
    override fun toString(): String {
        return "${javaClass.simpleName}: source = $source, value = $value"
    }
}

class DeviceEvent(source: String, value: String, val deviceId: String) : StringEvent(source, value) {
    override fun toString(): String {
        return "${javaClass.simpleName}: source = $source, value = $value, id=$deviceId"
    }
}

class ReaderDevice(val readerId: String) {
    private val publishSubject: PublishSubject<StringEvent> = PublishSubject.create()

    fun read(str: String) {
        publishSubject.onNext(StringEvent(javaClass.simpleName, "$readerId read: $str"))
    }

    fun observable(): Observable<StringEvent> = publishSubject

}

object ReaderRegistry {
    private val publishSubject: PublishSubject<DeviceEvent> = PublishSubject.create()

    fun observable(): Observable<DeviceEvent> = publishSubject

    private val devices = mutableListOf<ReaderDevice>()

    fun device(id: String): ReaderDevice? {
        return devices.firstOrNull { it.readerId == id }
    }

    fun addDevice(id: String): ReaderDevice {
        val device = ReaderDevice(id)
        devices.add(device)
        publishSubject.onNext(DeviceEvent(javaClass.simpleName, "device add", id))
        return device
    }

    fun removeDevice(device: ReaderDevice) {
        if (devices.remove(device)) {
            publishSubject.onNext(DeviceEvent(javaClass.simpleName, "device remove", device.readerId))
        }
    }

    fun removeDevice(deviceId: String) {
        if (devices.removeIf { it.readerId == deviceId }) {
            publishSubject.onNext(DeviceEvent(javaClass.simpleName, "device remove", deviceId))
        }
    }
}

object EventBus {

    private val disposable = mutableMapOf<String, Disposable>()

    fun print() {
        printOut(javaClass.simpleName)
    }


    init {
        ReaderRegistry.observable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .subscribeBy(
                onNext = { event ->
                    printOut("${javaClass.simpleName}: $event")
                    when (event.value) {
                        "device add" -> {
                            val device = ReaderRegistry.device(event.deviceId)
                            device?.let {
                                val disp = it.observable()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.computation())
                                    .subscribeBy(
                                        onNext = { event ->
                                            printOut("${javaClass.simpleName}: $event")
                                        },
                                        onError = { printOut("error") },
                                        onComplete = { printOut("Subscription completed") }
                                    )
                                disposable.put(event.deviceId, disp)
                            }
                        }
                        else -> {
                            val disp: Disposable? = disposable[event.deviceId]
                            disp?.dispose()
                            disposable.remove(event.deviceId)
                        }
                    }
                },
                onError = { printOut("error") },
                onComplete = { printOut("Subscription completed") }
            )

    }
}

fun printOut(message: Any?) {
    println("${Thread.currentThread().name} $message")
}
