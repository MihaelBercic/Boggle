package data.trie

/**
 * Created by mihael
 * on 01/02/2022 at 01:09
 * using IntelliJ IDEA
 */
data class TrieNode(val string: String = "", val children: MutableMap<Char, TrieNode> = HashMap()) {

    operator fun get(index: Int) = string[index]

    operator fun get(char: Char) = children[char]

    operator fun set(char: Char, node: TrieNode) = children.set(char, node)

    override fun toString(): String = "\"$string\"[$children]"
}