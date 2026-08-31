package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(private val context: Context) {

    companion object {
        private const val TAG = "MediaScanner"
    }

    suspend fun scanDeviceAudio(): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Filter for real music files only (at least 3 seconds long to ignore short ui sounds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 3000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawTitle = cursor.getString(titleColumn)
                    val rawArtist = cursor.getString(artistColumn)
                    val rawAlbum = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val title = if (rawTitle.isNullOrBlank()) {
                        path.substringAfterLast("/").substringBeforeLast(".")
                    } else rawTitle

                    val artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") {
                        "Unknown Artist"
                    } else rawArtist

                    val album = if (rawAlbum.isNullOrBlank() || rawAlbum == "<unknown>") {
                        "Unknown Album"
                    } else rawAlbum

                    val contentUri = ContentUris.withAppendedId(collection, id)

                    songsList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            path = path,
                            contentUri = contentUri,
                            size = size,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied scanning audio", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning audio", e)
        }

        songsList
    }

    fun groupIntoAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = albumId,
                    title = firstSong.album,
                    artist = firstSong.artist,
                    songCount = albumSongs.size,
                    songs = albumSongs
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    fun groupIntoArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist.lowercase() }
            .map { (_, artistSongs) ->
                val firstSong = artistSongs.first()
                val distinctAlbums = artistSongs.map { it.albumId }.distinct().size
                Artist(
                    name = firstSong.artist,
                    songCount = artistSongs.size,
                    albumCount = distinctAlbums,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
