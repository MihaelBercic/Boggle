import data.*
import io.javalin.Javalin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.random.Random


private val activeGames = ConcurrentHashMap<Dictionary, Game>()
private val gameResults = ConcurrentHashMap<String, ResultsSet>()
private val queuedGames = ConcurrentHashMap<Dictionary, LinkedBlockingQueue<Game>>()

private const val gridSize = 4


private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

fun main() {
    Dictionary.values().forEach { dictionary ->
        println("Launching game computation for $dictionary")
        val queue = queuedGames.computeIfAbsent(dictionary) { LinkedBlockingQueue(10) }
        thread {
            while (true) {
                val grid = Grid(gridSize, dictionary)
                val found = grid.findWords()
                val setOfCharacters = "$grid".replace("\n", "").replace(" ", "")
                val game = Game(
                    setOfCharacters, found.map { it.sequence },
                    gameDuration = 90,
                    gatheringDuration = 5,
                    resultsDuration = 15,
                    words = found
                )
                queue.put(game)
                val longestWords = found.sortedByDescending { it.word.length }.map { it.word }.distinct()
                println(longestWords)
            }
        }
        setNewGame(dictionary)
    }

    val app = Javalin.create {
        it.showJavalinBanner = false
    }.start(6969)

    app.exception(Exception::class.java) { exception, ctx ->
        exception.printStackTrace()
    }

    val randomNames = listOf(
        "Franc", "Marija", "Jožef", "Ana", "Anton", "Frančiška", "Ivan", "Ivana", "Janez",
        "Jožefa", "Stanislav", "Terezija", "Alojz", "Angela", "Alojzij", "Antonija", "Jože",
        "Ljudmila", "Frančišek", "Alojzija", "Štefan", "Štefanija", "Milan", "Pavla", "Martin",
        "Kristina", "Ludvik", "Rozalija", "Rudolf", "Stanislava", "Viktor",
        "Vida", "Ciril", "Elizabeta", "Peter", "Katarina", "Leopold", "Olga", "Andrej", "Matilda",
        "Vladimir", "Ivanka", "Mihael", "Amalija", "Marjan", "Justina", "Miroslav",
        "Julijana", "Vincenc", "Zofija", "Albin", "Veronika", "Jakob", "Neža", "Pavel", "Helena",
        "Mirko", "Cecilija", "Dušan", "Albina", "Karel", "Emilija", "Vinko", "Milena", "Josip",
        "Danica", "Emil", "Marta", "Karol", "Karolina", "Marijan", "Vera", "Stanko",
        "Nada", "Edvard", "Roza", "Janko", "Dragica", "Slavko", "Marjeta", "Drago", "Anica", "Srečko",
        "Ema", "Valentin", "Jožica", "Ernest", "Zora", "Matija", "Viktorija", "Avgust",
        "Ida", "Boris", "Silva", "Avguštin", "Sonja", "Viljem", "Irena", "Aleksander",
        "Gizela", "Branko", "Gabrijela", "Ferdinand", "Danijela", "Ignac", "Martina",
        "Ladislav", "Pavlina", "Feliks", "Majda", "Vincencij", "Mihaela", "Marko", "Barbara",
        "Bogomir", "Slavica", "Jurij", "Lidija", "Maksimiljan", "Miroslava", "Adolf", "Hedvika",
        "Božidar", "Silvestra", "Rafael", "Magdalena", "Roman", "Milka", "Karl", "Jozefa",
        "France", "Leopoldina", "Silvester", "Bernarda", "Ignacij", "Darinka",
        "Jernej", "Cvetka", "Maksimilijan", "Rozina", "Franjo", "Doroteja", "Friderik", "Otilija", "Dominik", "Slavka", "Albert", "Hilda", "Maks", "Berta",
        "Stjepan", "Irma", "Konrad", "Hermina", "Henrik", "Lucija", "Miloš", "Valentina",
        "Nikolaj", "Valerija", "Stane", "Cirila", "Nikola", "Julija", "Bojan", "Zdenka", "Filip", "Ana Marija",
        "Zdravko", "Mira", "Danijel", "Marija Ana", "Danilo", "Zorka",
        "Dragotin", "Genovefa", "Bogdan", "Marijana", "Matevž", "Draga", "Vekoslav", "Tatjana", "Vid",
        "Marica", "Dragutin", "Regina", "Florijan", "Jolanda", "Julij", "Vilma", "Petar", "Daniela",
        "Rajko", "Jelka", "Herman", "Štefka", "Bernard", "Ljubica", "Bruno", "Jolanka"
    )

    app.get("/game/{language}") { context ->
        val dictionary = Dictionary.valueOf(context.pathParam("language").replaceFirstChar { it.uppercaseChar() })
        val game = activeGames[dictionary] ?: throw Exception("Game for $dictionary not found!")
        game.startIfNeeded(dictionary)
        (0..Random.nextInt(20)).map {
            // TODO random username generation
            // TODO points calculation
        }
        val json = json.encodeToString(game)
        context.header("content-type", "application/json; charset=utf-8")
        context.result(json)
    }

    app.post("/game/results/{id}") {
        val gameId = it.pathParam("id")
        val resultsSet = gameResults[gameId]
        it.header("content-type", "application/json; charset=utf-8")
        if (resultsSet != null) {
            val result = json.decodeFromString<GameResult>(it.body())
            resultsSet.add(result)
            return@post
        }
        it.status(404)
    }


    app.get("/game/results/{id}/{user}") {
        val gameId = it.pathParam("id")
        val userIdentifier = it.pathParam("user")
        it.header("content-type", "application/json; charset=utf-8")
        val gameResults = gameResults[gameId]
        if (gameResults != null) {
            val sorted = gameResults.getResults().sortedByDescending { it.points }
            val top = sorted.take(100).mapIndexed { index, gameResult -> gameResult.copy(position = index + 1) }
            val userResult = top.firstOrNull { it.identifier == userIdentifier }
            val response = json.encodeToString(GameResults(top, userResult))
            it.result(response)
            return@get
        }
        it.status(404)
    }

    // crawl()
    // return
}

fun setNewGame(dictionary: Dictionary) {
    println("Setting new game for $dictionary!")
    val newGame = queuedGames[dictionary]!!.take()
    activeGames[dictionary] = newGame
    gameResults[newGame.identifier] = ResultsSet()
}

/** Queries database of [bos.zrc-saz.si](http://bos.zrc-sazu.si) and stores all paginated words. */
fun crawl() {
    val regex = "<font size=\"\\+1\"><b>(.*?)<\\/b><\\/font>".toRegex()
    val replacedWith = mapOf(
        'á' to 'a',
        'é' to 'e',
        'í' to 'i',
        'ó' to 'o',
        'ú' to 'u',
        'ŕ' to 'r',
        'à' to 'a',
        'è' to 'e',
        'ì' to 'i',
        'ò' to 'o',
        'ù' to 'u',

        'ô' to 'o',
        'ê' to 'e'
    )
    val wordsFile = File("users/mihael/Desktop/slovenske.txt")
    val client = HttpClient.newHttpClient()
    wordsFile.createNewFile()
    wordsFile.writer().apply {
        write("")
        flush()
    }
    var page = 1
    while (true) {
        val url = "http://bos.zrc-sazu.si/cgi_new/neva.exe?name=ssbsj&expression=be%25%3E2%20in%20be%25%3C11%20in%20bv%21%3Amed%20in%20vi%3AS&hs=$page"
        println("Visiting page $page at \t$url.")
        val request = HttpRequest.newBuilder(URI(url)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val html = response.body()
        val groups = regex.findAll(html)
        val words = groups.map { it.groupValues[1].lowercase().map { replacedWith[it] ?: it }.joinToString("") }.filter { !it.contains(" ") && !it.contains(".") }.toSet()
        val count = words.size
        if (count == 0) return
        wordsFile.appendText(words.joinToString("\n") + "\n")
        println("Retrieved[$count] ${words.joinToString(",")}")
        page += count
    }
}


private val digits = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
    'E', 'F'
)

/** Taken from DigestUtils dependency.
 *
 * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
 * The returned array will be double the length of the passed array, as it takes two characters to represent any given byte.
 * */
val ByteArray.asHex
    get() :String {
        val l = size
        val out = CharArray(l shl 1)
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = digits[0xF0 and this[i].toInt() ushr 4]
            out[j++] = digits[0x0F and this[i].toInt()]
            i++
        }
        return out.joinToString("")
    }

/** Digests [data] using SHA-256 hashing algorithm. */
fun sha256(data: ByteArray): ByteArray {
    return MessageDigest.getInstance("SHA-256").let {
        it.update(data)
        it.digest()
    }
}

/** Digests [data] using SHA-256 hashing algorithm. */
fun sha256(data: String) = sha256(data.encodeToByteArray())

class ResultsSet {

    private val backing = mutableSetOf<GameResult>()
    private val lock = ReentrantLock(true)

    fun add(result: GameResult) {
        lock.withLock {
            backing.add(result)
        }
    }

    fun getResults(): List<GameResult> {
        return lock.withLock { backing.toList() }
    }
}