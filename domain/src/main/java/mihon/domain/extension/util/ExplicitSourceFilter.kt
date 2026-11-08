package mihon.domain.extension.util

object ExplicitSourceFilter {
    private val explicitRegex = Regex(
        """hentai|porn|porno|coomer|kemono|luscious|fakku|doujindesu|manhwa18|manga18|erocool|\beromanga|\bxxx\b""",
        RegexOption.IGNORE_CASE,
    )

    fun isExplicit(name: String, pkgName: String? = null): Boolean {
        if (explicitRegex.containsMatchIn(name)) return true
        if (pkgName != null && explicitRegex.containsMatchIn(pkgName)) return true
        return false
    }
}
