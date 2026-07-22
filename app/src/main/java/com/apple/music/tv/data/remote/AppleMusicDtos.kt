package com.apple.music.tv.data.remote

import com.apple.music.tv.data.model.Track
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw JSON wrapper returned by the public Apple iTunes Search / Lookup API
 * (`https://itunes.apple.com/search` and `/lookup`).
 *
 * Only the fields the app actually consumes are declared; the shared
 * [kotlinx.serialization.json.Json] instance is configured to ignore everything else.
 */
@Serializable
data class ItunesResponse(
    val resultCount: Int = 0,
    val results: List<ItunesTrackDto> = emptyList(),
)

/** A single catalogue entry (song) from the iTunes API. */
@Serializable
data class ItunesTrackDto(
    val wrapperType: String? = null,
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null,
    val trackTimeMillis: Long? = null,
    val primaryGenreName: String? = null,
) {
    /** Whether this entry is a real, playable song row (vs. an album/artist wrapper). */
    fun isPlayableSong(): Boolean =
        wrapperType == "track" &&
            trackId != null &&
            !trackName.isNullOrBlank() &&
            !previewUrl.isNullOrBlank()

    /**
     * Maps this DTO to the app's domain [Track].
     *
     * The 100×100 artwork URL is upgraded to a crisp 600×600 image by rewriting the
     * size segment Apple embeds in the path (e.g. `100x100bb.jpg` → `600x600bb.jpg`).
     */
    fun toTrack(): Track = Track(
        id = trackId.toString(),
        title = trackName.orEmpty(),
        artist = artistName.orEmpty().ifBlank { "Unknown Artist" },
        album = collectionName.orEmpty().ifBlank { trackName.orEmpty() },
        artworkUrl = (artworkUrl100 ?: "").upscaleArtwork(),
        durationMs = trackTimeMillis ?: 30_000L,
        previewUrl = previewUrl,
        genre = primaryGenreName,
    )
}

/** Rewrites an iTunes `100x100` artwork URL to a higher resolution variant. */
internal fun String.upscaleArtwork(size: Int = 600): String =
    replace("100x100bb", "${size}x${size}bb")
        .replace("100x100", "${size}x${size}")
