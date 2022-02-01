package data

import data.trie.TrieNode

/**
 * Created by Unknown on Gist
 * on 30/01/2022 at 19:46
 * using IntelliJ IDEA
 */
class ProperTrie() {

    private val root = TrieNode()

    /** Returns true if the insertion of [string] was successful.*/
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

    /** Returns true if there are words that start with [string] prefix in the structure.*/
    tailrec fun startsWith(string: String, node: TrieNode = root): Boolean {
        if (node.string.contains(string)) return true
        val prefix = node.string.commonPrefixWith(string)
        val character = string[prefix.length]
        val nextNode = node[character] ?: return false
        return startsWith(string, nextNode)
    }

    /** Returns true if [string] exists in the structure.*/
    tailrec fun contains(string: String, node: TrieNode = root): Boolean {
        if (node.string == string) return true
        val prefix = node.string.commonPrefixWith(string)
        val character = string.getOrNull(prefix.length) ?: return false
        val nextNode = node[character] ?: return false
        return contains(string, nextNode)
    }

    override fun toString(): String = "$root"

}