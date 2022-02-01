package data

import data.trie.Trie

/**
 * Created by mihael
 * on 30/01/2022 at 15:24
 * using IntelliJ IDEA
 */
class Grid(private val size: Int, private val dictionary: Dictionary) {

    private val words = dictionary.loadWords()
    private val groupedWords = words.groupBy { it.length }
    private val trie = Trie(words)

    private val cells = Array(size) { arrayOfNulls<Char>(size) }
    private val positions = cells.mapIndexed { index, chars -> chars.indices.map { Position(it, index) } }.flatten()
    private val randomEmptyCell get() = positions.filter { position -> cells[position.y][position.x] == null }.randomOrNull()

    private val neighbours = positions.associateWith { position ->
        (-1..1).flatMap { xDiff ->
            (-1..1).map { yDiff ->
                Position(position.x + xDiff, position.y + yDiff)
            }
        }.filter { it != position && it.x in cells.indices && it.y in cells.indices }
    }

    init {
        populate()
    }


    fun findWords(): HashSet<String> {
        return positions.flatMap { position -> findWord(listOf(position)) }.toHashSet()
    }

    private fun reset() {
        for (y in cells.indices) {
            for (x in cells.indices)
                cells[y][x] = null
        }
    }

    fun populate() {
        reset()
        (10 downTo 5).forEach { length ->
            for (attempt in 0 until 1000) {
                val word = groupedWords[length]?.randomOrNull() ?: return@forEach
                try {
                    if (grow(word)) return@forEach
                } catch (e:Exception){
                    println(this)
                    e.printStackTrace()
                }
            }
        }
        cells.forEachIndexed { y, row ->
            row.forEachIndexed { x, it ->
                if (it == null) cells[y][x] = dictionary.alphabet.characters.random()
            }
        }
    }


    private fun findWord(path: List<Position> = emptyList()): Set<String> {
        val set = mutableSetOf<String>()
        val currentPosition = path.lastOrNull() ?: return set
        val wordSoFar = path.joinToString("") { "${cells[it.y][it.x]}" }
        if (!trie.startsWith(wordSoFar)) return set
        if (words.contains(wordSoFar)) set.add(wordSoFar)

        val reachable = neighbours[currentPosition]!!.filter { !path.contains(it) }
        return if (wordSoFar.length < 11) set.plus(reachable.flatMap { findWord(path.plus(it)) })
        else emptySet()
    }

    private tailrec fun grow(word: String, avoid: MutableList<Position> = mutableListOf(), path: MutableList<Position> = mutableListOf(), shared: HashSet<Position> = hashSetOf()): Boolean {
        if (path.size == word.length) return true
        val currentIndex = path.size
        val currentPosition = path.lastOrNull() ?: randomEmptyCell ?: return false
        val currentCharacter = word[currentIndex]
        val reachable = neighbours[currentPosition]!!.filter { position -> !avoid.contains(position) && !path.contains(position) }
        val existing = reachable.firstOrNull { cells[it.y][it.x] == currentCharacter }
        val chosen = existing ?: reachable.filter { cells[it.y][it.x] == null }.randomOrNull()

        if (chosen == null) {
            path.removeLastOrNull() ?: return false
            avoid.add(currentPosition)
            if (!shared.remove(currentPosition)) cells[currentPosition.y][currentPosition.x] = null
            return grow(word, avoid, path, shared)
        }
        if (chosen == existing) shared.add(existing)
        path.add(chosen)
        cells[chosen.y][chosen.x] = currentCharacter
        return grow(word, avoid, path, shared)
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
        it.joinToString(" ").replace("null", " ")
    }

}