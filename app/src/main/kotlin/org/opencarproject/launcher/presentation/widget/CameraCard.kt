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
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.opencarproject.launcher.R
import org.opencarproject.launcher.core.pure.CameraApps
import org.opencarproject.launcher.presentation.common.rememberAppIcon

/**
 * Cameras module: one tap opens the head unit's camera app — the 360 around-view monitor
 * when the unit has one ([CameraApps]), otherwise a DVR / reverse / plain camera app.
 * Without any camera app the card explains itself.
 */
@Composable
fun CameraCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val camera = remember(context) { context.bestCameraApp() }
    Box(
        modifier
            .fillMaxSize()
            .clickable(enabled = camera != null, onClickLabel = stringResource(R.string.camera_open)) {
                camera?.let { context.openRadioApp(it.packageName) }
            },
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (camera == null) {
                Icon(
                    imageVector = Icons.Rounded.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    text = stringResource(R.string.camera_none),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val icon = rememberAppIcon(camera.packageName, withoutBackground = true)
                if (icon != null) {
                    Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = camera.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.camera_open),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** The installed app that looks most like the head unit's camera/AVM viewer, or null. */
internal fun Context.bestCameraApp(): RadioApp? {
    val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val apps = packageManager.queryIntentActivities(main, 0)
        .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
        .distinctBy { it.first }
    val pkg = CameraApps.pick(apps) ?: return null
    return RadioApp(pkg, apps.first { it.first == pkg }.second)
}
