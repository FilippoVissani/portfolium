package io.github.filippovissani.portfolium.controller.pdf

public data class PdfReport(
    val content: ByteArray,
    val fileName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfReport

        if (!content.contentEquals(other.content)) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}
