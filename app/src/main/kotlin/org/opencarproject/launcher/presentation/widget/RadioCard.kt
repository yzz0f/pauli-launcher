package org.opencarproject.launcher.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.opencarproject.launcher.R
import org.opencarproject.launcher.core.pure.RadioApps
import org.opencarproject.launcher.data.source.MediaSessionSource
import org.opencarproject.launcher.presentation.common.AudioBars
import org.opencarproject.launcher.presentation.common.rememberAppIcon

/**
 * FM radio module: opens the head unit's radio app and, when that app publishes a MediaSession,
 * shows what is on air with a play/pause control. The app is detected among the installed
 * launchable apps ([RadioApps]); without one the card explains itself.
 */
@Composable
fun RadioCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val radio = remember(context) { context.bestRadioApp() }
    if (radio == null) {
        Column(
            modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Radio,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(34.dp),
            )
            Text(
                text = stringResource(R.string.radio_none),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    DisposableEffect(context) {
        MediaSessionSource.start(context)
        onDispose { MediaSessionSource.stop() }
    }
    val nowPlaying by MediaSessionSource.nowPlaying.collectAsStateWithLifecycle()
    val onAir = nowPlaying?.takeIf { it.packageName == radio.packageName }

    Box(
        modifier
            .fillMaxSize()
            .clickable(onClickLabel = stringResource(R.string.radio_open)) { context.openRadioApp(radio.packageName) },
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val icon = rememberAppIcon(radio.packageName, withoutBackground = true)
                if (icon != null) {
                    Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = radio.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onAir != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    if (onAir.isPlaying) {
                        AudioBars(
                            colors = List(4) { MaterialTheme.colorScheme.primary },
                            modifier = Modifier.size(width = 18.dp, height = 16.dp),
                        )
                    }
                    Text(
                        text = listOf(onAir.title, onAir.artist).filter { it.isNotBlank() }.joinToString(" — "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FilledTonalIconButton(
                        onClick = {
                            val controls = MediaSessionSource.controller?.transportControls
                            if (onAir.isPlaying) controls?.pause() else MediaSessionSource.play(context)
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (onAir.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(if (onAir.isPlaying) R.string.media_pause else R.string.media_play),
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.radio_open),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

internal data class RadioApp(val packageName: String, val label: String)

/** The installed app that looks most like the head unit's radio, or null. */
internal fun Context.bestRadioApp(): RadioApp? {
    val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val apps = packageManager.queryIntentActivities(main, 0)
        .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
        .distinctBy { it.first }
    val pkg = RadioApps.pick(apps) ?: return null
    return RadioApp(pkg, apps.first { it.first == pkg }.second)
}

/** Opens the radio app in its own task. */
internal fun Context.openRadioApp(packageName: String) {
    packageManager.getLaunchIntentForPackage(packageName)?.let {
        runCatching { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
