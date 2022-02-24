
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun main(args: Array<String>) {
    runBlocking {
        launch {
            for (i in 0..5) {
                delay(400)
                println(i)
            }
        }
        launch {
            for (i in 0..5) {
                delay(200)
                println("* $i")
            }
        }
        println("hello")
    }
/*
    EventBus.print()
    val device = ReaderRegistry.addDevice("reader1")
    sleep(0.3)
    device.read("bla bla")
    sleep(0.3)
    ReaderRegistry.removeDevice(device)
    sleep(0.3)
    device.read("bla2 bla2")
    sleep(0.3)
*/


/*
    val source: Observable<Int> = (1..10).toObservable()
    val disposable:Disposable = source
        .map { it * 100 }
        .doOnNext { println("emitting ${it} on thread ${Thread.currentThread().name}") }
        .doAfterNext{ println("after emitting ${it} on thread ${Thread.currentThread().name}") }
        .subscribeOn(Schedulers.io())
        .map { System.out.println("second map ${it} on thread ${Thread.currentThread().name}")
            it + 10 }
        .observeOn(Schedulers.computation())
        .subscribe { System.out.println("recieved ${it} on thread ${Thread.currentThread().name}") }
    sleep(2.5)
    disposable.dispose()
*/
    sleep(2.0)
}

private fun sleep(sec: Double) {
    try {
        Thread.sleep((sec * 1000.0).toLong())
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}
