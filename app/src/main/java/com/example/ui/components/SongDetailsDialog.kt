package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SongDetailsDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateAddedString = if (song.dateAdded > 0) {
        dateFormat.format(Date(song.dateAdded * 1000L))
    } else "Unknown"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Song Details",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow("Title", song.title)
                DetailRow("Artist", song.artist)
                DetailRow("Album", song.album)
                DetailRow("Duration", song.formattedDuration)
                DetailRow("File Size", song.formattedSize)
                DetailRow("Date Added", dateAddedString)
                DetailRow("Play Count", "${song.playCount} times")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("File Path", song.path)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
