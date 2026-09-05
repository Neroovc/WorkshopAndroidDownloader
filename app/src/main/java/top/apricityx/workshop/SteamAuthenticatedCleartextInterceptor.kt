package top.apricityx.workshop

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class SteamAuthenticatedCleartextInterceptor(
    private val hasAuthenticatedSteamSession: () -> Boolean,
    private val allowAuthenticatedCleartextHttpProvider: () -> Boolean,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (
            !request.url.isHttps &&
            hasAuthenticatedSteamSession() &&
            request.url.host.isSteamWebAuthenticatedTrafficDomain() &&
            !allowAuthenticatedCleartextHttpProvider()
        ) {
            throw SteamAuthenticatedCleartextBlockedException(request.url.host)
        }
        return chain.proceed(request)
    }
}

class SteamAuthenticatedCleartextBlockedException(
    host: String,
) : IOException(
    "Current settings block authenticated cleartext HTTP requests to $host. Enable it in settings and try again.",
)

internal fun String.isSteamWebAuthenticatedTrafficDomain(): Boolean {
    val host = lowercase()
    return host.matchesDomainSuffix("steamcommunity.com") ||
        host.matchesDomainSuffix("steampowered.com") ||
        host.matchesDomainSuffix("steamcontent.com")
}

private fun String.matchesDomainSuffix(suffix: String): Boolean =
    this == suffix || endsWith(".$suffix")
