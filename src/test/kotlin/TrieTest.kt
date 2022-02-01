import data.trie.Trie
import org.junit.jupiter.api.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by mihael
 * on 31/01/2022 at 12:33
 * using IntelliJ IDEA
 */
class TrieTest {

    @Test
    fun insertion() {
        Trie().apply {
            assertTrue { insert("timothy") }
            assertTrue { insert("ti") }
            assertTrue { insert("tim") }
        }
    }

    @Test
    fun startsWithTest() {
        Trie().apply {
            insert("timothy")
            insert("ti")
            insert("tim")

            assertTrue { startsWith("tim") }
            assertTrue { startsWith("timothy") }
            assertTrue { startsWith("ti") }
            assertTrue { startsWith("t") }

            assertFalse { startsWith("tii") }
            assertFalse { startsWith("hi") }
        }
    }

    @Test
    fun contains() {
        Trie().apply {
            insert("timothy")
            insert("ti")
            insert("tim")

            assertTrue { contains("tim") }
            assertTrue { contains("ti") }
            assertTrue { contains("timothy") }

            assertFalse { contains("t") }
            assertFalse { contains("timo") }
            assertFalse { contains("timothyo") }
        }
    }
}