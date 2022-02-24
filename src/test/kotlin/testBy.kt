import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

interface IA {
    fun print()
}

class ImplA() : IA {
    override fun print() {
        println("hello")
    }
}

open class ImplB(val ia: IA) : IA by ia

class ImplB2(ia: IA): ImplB(ia) {

    override fun print() {
        print("*****")
        super.print()
    }
}

@Tag("unitTest")
internal class TestBy{
    @Test
    internal fun test_test_createBy() {
        val a:IA = ImplB2(ImplA())
        a.print()
        (a as ImplB).ia.print()
        a.print()
    }
}
