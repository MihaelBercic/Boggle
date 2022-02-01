package data

/**
 * Created by mihael
 * on 01/02/2022 at 12:20
 * using IntelliJ IDEA
 */
enum class Dictionary(val alphabet: Alphabet, private val dictionaryFile: String) {

    Slovenian(Alphabet.Slovenian, "slovenian.txt"),
    English(Alphabet.English, "english.txt"),


    ;

    fun loadWords(): HashSet<String> {
        val file = object {}.javaClass.getResource("/$dictionaryFile")?.readText() ?: throw Exception("No such file $dictionaryFile.")
        return file.split("\n")
            .asSequence()
            .map(String::trim)
            .map(String::uppercase)
            .filter { it.length in 3..10 }
            .toHashSet()
    }
}