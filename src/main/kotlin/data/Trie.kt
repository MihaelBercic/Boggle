package data

/**
 * Created by Unknown on Gist
 * on 30/01/2022 at 19:46
 * using IntelliJ IDEA
 */
class Trie {

    data class Node(var word: String? = null, val childNodes: MutableMap<Char, Node> = mutableMapOf()) {
        override fun toString(): String = "\"$word\"[$childNodes]"
    }

    val root = Node()

    fun insert(word: String) {
        var currentNode = root
        for (char in word) {
            if (currentNode.childNodes[char] == null) {
                currentNode.childNodes[char] = Node()
            }
            currentNode = currentNode.childNodes[char]!!
        }
        currentNode.word = word
    }

    fun search(word: String): Boolean {
        var currentNode = root
        for (char in word) {
            if (currentNode.childNodes[char] == null) {
                return false
            }
            currentNode = currentNode.childNodes[char]!!
        }
        return currentNode.word != null
    }

    fun startsWith(word: String): Boolean {
        var currentNode = root
        for (char in word) {
            if (currentNode.childNodes[char] == null) {
                return false
            }
            currentNode = currentNode.childNodes[char]!!
        }
        return true
    }

}

class ProperTrie() {

    val root = TrieNode()

    data class TrieNode(val string: String = "", val children: MutableMap<Char, TrieNode> = HashMap()) {

        operator fun get(index: Int) = string[index]

        operator fun get(char: Char) = children[char]

        operator fun set(char: Char, node: TrieNode) = children.set(char, node)

        override fun toString(): String = "\"$string\"[$children]"
    }

    tailrec fun insert(string: String, node: TrieNode = root): Boolean {
        if (string == node.string) return true
        val prefix = string.commonPrefixWith(node.string)
        val character = string[prefix.length]
        val nextNode = node.children.computeIfAbsent(character) { TrieNode(string) }
        val nextPrefix = nextNode.string.commonPrefixWith(string)
        if (nextPrefix.length < nextNode.string.length) {
            val nextCharacter = nextNode.string[nextPrefix.length]
            TrieNode(nextPrefix, hashMapOf(nextCharacter to nextNode)).apply {
                node[character] = this
                this[nextCharacter] = nextNode
                return insert(string, this)
            }
        }
        return insert(string, nextNode)
    }

    tailrec fun startsWith(string: String, node: TrieNode = root): Boolean {
        if (node.string.contains(string)) return true
        val prefix = node.string.commonPrefixWith(string)
        val character = string[prefix.length]
        val nextNode = node[character] ?: return false
        return startsWith(string, nextNode)
    }

    tailrec fun contains(string: String, node: TrieNode = root): Boolean {
        if (node.string == string) return true
        val prefix = node.string.commonPrefixWith(string)
        val character = string.getOrNull(prefix.length) ?: return false
        val nextNode = node[character] ?: return false
        return contains(string, nextNode)
    }

    override fun toString(): String = "$root"

}