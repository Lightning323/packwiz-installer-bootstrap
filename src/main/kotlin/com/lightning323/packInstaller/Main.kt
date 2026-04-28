package lightning323.packInstaller.com.lightning323.packInstaller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data structures matching your TOML format.
 * Using Kebab-Case strategy handles 'pack-format' and 'hash-format' automatically.
 */
data class PackConfig(
    val name: String = "",
    val author: String = "",
    val version: String = "",
    val packFormat: String = "",
    val index: IndexSection? = null,
    val versions: Map<String, String>? = null
)

data class IndexSection(
    val file: String,
    val hashFormat: String,
    val hash: String
)

/**
 * Helper to get HTTP response as String
 */
fun fetchString(url: String): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    return connection.inputStream.use { stream ->
        stream.bufferedReader().readText()
    }
}

/**
 * Helper to get HTTP response as Binary
 */
fun fetchBinary(url: String): ByteArray {
    val connection = URL(url).openConnection() as HttpURLConnection
    return connection.inputStream.use { it.readBytes() }
}

fun main() {
    val packTomlURL = "https://raw.githubusercontent.com/Lightning323/MC-Terranova/refs/heads/main/pack/pack.toml"

    val mapper = TomlMapper().apply {
        registerKotlinModule()
        // Crucial for mapping 'pack-format' -> 'packFormat'
        propertyNamingStrategy = PropertyNamingStrategies.KEBAB_CASE
    }

    try {
        val result = fetchString(packTomlURL)

        // Deserialize into our object model
        val config = mapper.readValue(result, PackConfig::class.java)

        println("--- Pack Info ---")
        println("Name: ${config.name}")
        println("Minecraft Version: ${config.versions?.get("minecraft")}")

        config.index?.let { index ->
            println("\n--- Index ---")
            println("File: ${index.file}")
            println("Hash: ${index.hash}")
        }

    } catch (e: Exception) {
        println("Failed to parse TOML: ${e.message}")
    }
}