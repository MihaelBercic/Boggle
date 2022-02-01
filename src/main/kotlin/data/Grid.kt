package data

/**
 * Created by mihael
 * on 30/01/2022 at 15:24
 * using IntelliJ IDEA
 */
class Grid(private val size: Int, private val allWords: Set<String>) {

    val trie = Trie().apply {
        allWords.forEach { insert(it) }
    }
    // val alphabet = arrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    val alphabet = arrayOf('A', 'B', 'C', 'Č', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'Š', 'T', 'U', 'V', 'Z', 'Ž')

    val cells = Array(4) { arrayOfNulls<Char>(4) }
    val positions = cells.mapIndexed { index, chars -> chars.indices.map { Position(it, index) } }
    val neighbours = positions.flatten().associateWith { position ->
        (-1..1).flatMap { xDiff ->
            (-1..1).map { yDiff ->
                Position(position.x + xDiff, position.y + yDiff)
            }
        }.filter { it != position && it.x in cells.indices && it.y in cells.indices }
    }

    val randomEmptyCell get() = positions.flatMap { it.filter { cells[it.y][it.x] == null } }.randomOrNull()

    fun findWords(): List<String> {
        val start = System.currentTimeMillis()
        val allWords = positions.flatten().flatMap { position -> findWord(listOf(position)) }
        calculatePoints(allWords)
        println("Search took us ${System.currentTimeMillis() - start}ms")
        return emptyList()
    }

    private fun reset() {
        for (y in cells.indices) {
            for (x in cells.indices)
                cells[y][x] = null
        }
    }

    // TODO improve
    fun populate(groupedWords: Map<Int, List<String>>) {
        reset()
        (10 downTo 3).forEach { length ->
            for (attempt in 0 until 10000) {
                val word = groupedWords[length]?.randomOrNull() ?: return@forEach
                grow(word)
            }
        }
        cells.forEachIndexed { y, row ->
            row.forEachIndexed { x, it ->
                if (it == null) cells[y][x] = alphabet.random()
            }
        }
    }


    private fun findWord(path: List<Position> = emptyList()): Set<String> {
        val set = mutableSetOf<String>()
        val currentPosition = path.lastOrNull() ?: return set
        val wordSoFar = path.joinToString("") { "${cells[it.y][it.x]}" }
        if (!trie.startsWith(wordSoFar)) return set
        if (trie.search(wordSoFar)) {
            println("Found $wordSoFar")
            set.add(wordSoFar)
        }
        val reachable = neighbours[currentPosition]!!.filter { !path.contains(it) }
        return if (wordSoFar.length < 15) set.plus(reachable.flatMap { findWord(path.plus(it)) })
        else emptySet()
    }

    private fun grow(word: String, avoid: MutableList<Position> = mutableListOf(), path: MutableList<Position> = mutableListOf()): Boolean {
        if (path.size == word.length) {
            println("Placed $word")
            return true
        }
        val currentIndex = path.size
        val currentPosition = path.lastOrNull() ?: randomEmptyCell ?: return false
        val reachable = neighbours[currentPosition]!!.filter { position ->
            cells[position.y][position.x] == null && !avoid.contains(position) && !path.contains(position)
        }
        if (reachable.isEmpty()) {
            path.removeLastOrNull() ?: return false
            avoid.add(currentPosition)
            cells[currentPosition.y][currentPosition.x] = null
            return grow(word, avoid, path)
        }
        val chosen = reachable.random()
        path.add(chosen)
        cells[chosen.y][chosen.x] = word[currentIndex]
        return grow(word, avoid, path)
    }

    fun calculatePoints(words: Collection<String>) {
        val totalPoints = words.fold(0) { previous, word ->
            previous + when (word.length) {
                3, 4 -> 1
                5 -> 2
                6 -> 3
                7 -> 4
                else -> 11
            }
        }
        println("Total points: $totalPoints with ${words.size} words.")

    }

    override fun toString(): String = cells.joinToString("\n") {
        it.joinToString(" ")
    }

}