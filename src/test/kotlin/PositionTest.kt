import data.Position
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Created by mihael
 * on 30/01/2022 at 15:17
 * using IntelliJ IDEA
 */
class PositionTest {

    @Test
    fun equality(){
        val array = Array(4) {0}
        val positionA = Position(0,0, array.indices)
        val positionB = Position(0,0, array.indices)
        assertEquals(positionA, positionB)
    }
}