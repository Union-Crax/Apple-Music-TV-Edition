package com.apple.music.tv.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit binding for Apple's public **iTunes Search API**.
 *
 * This endpoint requires no API key or developer token, which lets the app pull in
 * real Apple Music catalogue metadata, high-resolution artwork, and — crucially —
 * playable 30-second AAC preview streams that ExoPlayer renders as real audio.
 *
 * Docs: https://performance-partners.apple.com/search-api
 */
interface AppleMusicApi {

    /**
     * Full-text catalogue search.
     *
     * @param term   The user's search text (artist, song, or album).
     * @param media  Restrict to music.
     * @param entity Restrict to individual songs (so every result has a preview).
     * @param limit  Maximum number of results (Apple caps this at 200).
     * @param country Two-letter storefront code.
     */
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "US",
    ): ItunesResponse

    /**
     * Looks up specific catalogue items by their iTunes IDs (comma-separated).
     * Used to resolve chart entries into full song rows with preview streams.
     */
    @GET("lookup")
    suspend fun lookup(
        @Query("id") ids: String,
        @Query("entity") entity: String = "song",
        @Query("country") country: String = "US",
    ): ItunesResponse

    companion object {
        const val BASE_URL = "https://itunes.apple.com/"
    }
}
