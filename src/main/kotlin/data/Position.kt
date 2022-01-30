package data

/**
 * Created by mihael
 * on 30/01/2022 at 15:15
 * using IntelliJ IDEA
 */
data class Position(val x: Int, val y: Int, val indices: IntRange) {

    val surrounding
        get() = (-1..1).map { xDiff ->
            (-1..1).map { yDiff ->
                Position(x + xDiff, y + yDiff, indices)
            }
        }.flatten().filter { it != this && it.x in indices && it.y in indices }

}