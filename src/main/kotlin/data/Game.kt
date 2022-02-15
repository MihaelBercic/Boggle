package data

import asHex
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import setNewGame
import sha256
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule

/**
 * Created by mihael
 * on 11/02/2022 at 17:50
 * using IntelliJ IDEA
 */

@Serializable
data class Game(
    val grid: String,
    val data: List<List<Int>>,
    val identifier: String = sha256(data.joinToString("") { it.joinToString("") }).asHex,
    val gameDuration: Long,
    val resultsDuration: Long,
    val gatheringDuration: Long,
    private var started: String = ""
) {

    fun startIfNeeded(dictionary: Dictionary) {
        println("Game: $identifier ... $isStarted")
        if (!isStarted.getAndSet(true)) {
            println("Game started!")
            started = Instant.now().toString()
            Timer().schedule((gameDuration + resultsDuration + gatheringDuration) * 1000 - 500) {
                setNewGame(dictionary)
            }
        }
    }

    @Transient
    private val isStarted = AtomicBoolean(false)
}

@Serializable
data class GameResult(
    val identifier: String?,
    val username: String,
    val longestWord: String?,
    val position: Int?,
    val points: Int
)

@Serializable
data class GameResults(
    val results: Collection<GameResult>,
    val user: GameResult?
)