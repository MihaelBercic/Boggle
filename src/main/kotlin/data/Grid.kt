package data

import kotlinx.coroutines.coroutineScope

/**
 * Created by mihael
 * on 30/01/2022 at 15:24
 * using IntelliJ IDEA
 */
class Grid(private val size: Int, private val allWords: Set<String>) {

    val englishAlphabet = arrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    val alphabet = arrayOf('A', 'B', 'C', 'Č', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'Š', 'T', 'U', 'V', 'Z', 'Ž')

    val cells = Array(4) { arrayOfNulls<Char>(4) }
    val positions = cells.mapIndexed { index, chars -> chars.indices.map { Position(it, index, chars.indices) } }

    val randomEmptyCell get() = positions.flatMap { it.filter { cells[it.y][it.x] == null } }.randomOrNull()

    fun findWords(): List<String> {
        return emptyList() // TODO
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


    private suspend fun findWord(position: Position, path: List<Position> = emptyList()): Any = coroutineScope {
        val wordSoFar = path.plus(position).joinToString("") { "${cells[it.y][it.x]}" }
        val isWord = allWords.contains(wordSoFar)
        val reachable = position.surrounding.filter { !path.contains(it) }
        val words = mutableSetOf<String>()
        if (isWord) words.add(wordSoFar)

        TODO("Find word has not been implemented yet.")
    }

    /**
     * Attempts to grow a certain word in the grid.
     * @return true if the word was successfully added and false if not.
     * */
    private fun grow(word: String, avoid: MutableList<Position> = mutableListOf(), path: MutableList<Position> = mutableListOf()): Boolean {
        if (path.size == word.length) {
            println("Placed $word")
            return true
        }
        val currentIndex = path.size
        val currentPosition = path.lastOrNull() ?: randomEmptyCell ?: return false
        val reachable = currentPosition.surrounding.filter { position ->
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