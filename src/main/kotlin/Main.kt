import data.Dictionary
import data.Grid
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


fun main() {
    val start = System.currentTimeMillis()
    val grid = Grid(4, Dictionary.Slovenian)
    val found = grid.findWords()
    grid.calculatePoints(found)

    found.groupBy { it.length }.toSortedMap().forEach { (length, words) ->
        println("$length => $words")
    }
    println(grid)
    println("$grid".replace("\n", "").replace(" ", ""))
    println("Took us: ${System.currentTimeMillis() - start}ms")

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