package adco.helixmidimixer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.data.model.ChannelColorCategory
import adco.helixmidimixer.data.model.ChannelState
import adco.helixmidimixer.data.model.ChannelType
import adco.helixmidimixer.data.model.HomeRoute
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixBlueBorder
import adco.helixmidimixer.ui.theme.HelixBlueHeader
import adco.helixmidimixer.ui.theme.HelixBluePrimary
import adco.helixmidimixer.ui.theme.HelixBlueTint
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenBorder
import adco.helixmidimixer.ui.theme.HelixGreenHeader
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixGreenTint
import adco.helixmidimixer.ui.theme.HelixMidiMixerTheme
import adco.helixmidimixer.ui.theme.HelixMuteActive
import adco.helixmidimixer.ui.theme.HelixMuteBorder
import adco.helixmidimixer.ui.theme.HelixMuteInactive
import adco.helixmidimixer.ui.theme.HelixSilverBorder
import adco.helixmidimixer.ui.theme.HelixSilverHeader
import adco.helixmidimixer.ui.theme.HelixSilverPrimary
import adco.helixmidimixer.ui.theme.HelixSilverTint
import adco.helixmidimixer.ui.theme.HelixSoloActive
import adco.helixmidimixer.ui.theme.HelixSoloBorder
import adco.helixmidimixer.ui.theme.HelixSoloInactive
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun ChannelStrip(
    channelState: ChannelState,
    volumeCc: Int,
    panCc: Int,
    muteCc: Int,
    soloCc: Int,
    onVolumeChanged: (Int) -> Unit,
    onPanChanged: (Int) -> Unit,
    onMuteToggled: (Boolean) -> Unit,
    onSoloToggled: (Boolean) -> Unit,
    onHomeRouteChanged: (HomeRoute) -> Unit,
    onOpenPanDialog: () -> Unit,
    onChannelRenameRequested: () -> Unit,
    isRoutesExpanded: Boolean = false,
    onToggleRoutesExpanded: ((Boolean) -> Unit)? = null,
    isSongChannelsExpanded: Boolean = true,
    onToggleSongChannelsExpanded: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Determine category styling
    val (bgTint, headerBg, borderTint, accentColor) = when (channelState.config.colorCategory) {
        ChannelColorCategory.LIGHT_GREEN -> Quadruple(
            HelixGreenTint, HelixGreenHeader, HelixGreenBorder, HelixGreenPrimary
        )
        ChannelColorCategory.LIGHT_SILVER -> Quadruple(
            HelixSilverTint, HelixSilverHeader, HelixSilverBorder, HelixSilverPrimary
        )
        ChannelColorCategory.LIGHT_BLUE -> Quadruple(
            HelixBlueTint, HelixBlueHeader, HelixBlueBorder, HelixBluePrimary
        )
    }

    // Format Pan text display
    val panText = remember(channelState.pan) {
        when {
            channelState.pan == 64 -> "Center"
            channelState.pan < 64 -> "L ${64 - channelState.pan}"
            else -> "R ${channelState.pan - 64}"
        }
    }

    Surface(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderTint, RoundedCornerShape(8.dp)),
        color = bgTint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Channel Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(headerBg),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = getChannelHeaderTag(channelState),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = HelixTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if ((channelState.channelIndex == 0 && onToggleRoutesExpanded != null) ||
                        (channelState.channelIndex == 5 && onToggleSongChannelsExpanded != null)) {
                        val isExp = if (channelState.channelIndex == 0) isRoutesExpanded else isSongChannelsExpanded
                        val toggleAct = if (channelState.channelIndex == 0) onToggleRoutesExpanded else onToggleSongChannelsExpanded
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(HelixBadgeBg)
                                .clickable { toggleAct?.invoke(!isExp) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isExp) "-" else "+",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Pan / Balance Control Button Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HelixBadgeBg)
                    .border(0.5.dp, HelixCardBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onOpenPanDialog)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = HelixTextSecondary
                        )
                    )
                    Text(
                        text = panText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Mute (M) and Solo (S) Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mute (M) Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (channelState.isMuted) HelixMuteActive else HelixMuteInactive)
                        .border(
                            width = 1.dp,
                            color = if (channelState.isMuted) HelixMuteActive else HelixMuteBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onMuteToggled(!channelState.isMuted) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (channelState.isMuted) Color.White else HelixMuteBorder
                        )
                    )
                }

                // Solo (S) Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (channelState.isSoloed) HelixSoloActive else HelixSoloInactive)
                        .border(
                            width = 1.dp,
                            color = if (channelState.isSoloed) HelixSoloActive else HelixSoloBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSoloToggled(!channelState.isSoloed) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (channelState.isSoloed) Color(0xFF121418) else HelixSoloBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Vertical Volume Fader
            VerticalFader(
                value = channelState.volume,
                volumeCc = volumeCc,
                onValueChange = onVolumeChanged,
                accentColor = accentColor,
                isMuted = channelState.isMuted,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Bottom Area: Icon & Channel Name / Route Selector
            BottomChannelArea(
                channelState = channelState,
                accentColor = accentColor,
                onHomeRouteChanged = onHomeRouteChanged,
                onChannelRenameRequested = onChannelRenameRequested
            )
        }
    }
}

@Composable
private fun BottomChannelArea(
    channelState: ChannelState,
    accentColor: Color,
    onHomeRouteChanged: (HomeRoute) -> Unit,
    onChannelRenameRequested: () -> Unit
) {
    val icon: ImageVector = when (channelState.config.type) {
        ChannelType.HOME -> Icons.Rounded.Home
        ChannelType.ROUTE -> Icons.Rounded.Home
        ChannelType.SONG_MASTER -> Icons.Rounded.LibraryMusic
        ChannelType.SONG_CHANNEL -> Icons.Rounded.Audiotrack
        ChannelType.CLICK -> Icons.Rounded.Audiotrack
        ChannelType.COUNT_IN -> Icons.Rounded.Audiotrack
        ChannelType.USB_IN -> Icons.Rounded.Cable
        ChannelType.BT_IN -> Icons.Rounded.Bluetooth
        ChannelType.AUX_IN -> Icons.Rounded.Cable
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(HelixBadgeBg)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = channelState.effectiveName,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        when (channelState.config.type) {
            ChannelType.HOME, ChannelType.ROUTE, ChannelType.SONG_MASTER, ChannelType.CLICK, ChannelType.COUNT_IN, ChannelType.USB_IN, ChannelType.BT_IN, ChannelType.AUX_IN -> {
                Text(
                    text = channelState.effectiveName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HelixTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
            ChannelType.SONG_CHANNEL -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChannelRenameRequested)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = channelState.effectiveName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HelixTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Name",
                        tint = HelixTextSecondary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

private fun getChannelHeaderTag(channelState: ChannelState): String {
    return when (channelState.config.type) {
        ChannelType.HOME -> "HOME"
        ChannelType.ROUTE -> when (channelState.channelIndex) {
            1 -> "1A"
            2 -> "1B"
            3 -> "2A"
            4 -> "2B"
            else -> channelState.config.defaultName.uppercase()
        }
        ChannelType.SONG_MASTER -> "MASTER"
        ChannelType.SONG_CHANNEL -> "CH ${channelState.channelIndex - 5}"
        ChannelType.CLICK -> "CLICK"
        ChannelType.COUNT_IN -> "COUNT IN"
        ChannelType.USB_IN -> "USB IN"
        ChannelType.BT_IN -> "BT IN"
        ChannelType.AUX_IN -> "AUX IN"
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true, heightDp = 450)
@Composable
fun ChannelStripPreview() {
    HelixMidiMixerTheme {
        Box(
            modifier = Modifier
                .background(HelixBadgeBg)
                .padding(16.dp)
        ) {
            ChannelStrip(
                channelState = ChannelState(
                    group = OutputGroup.OUTPUT_1_4,
                    channelIndex = 6,
                    customName = "Guitar 1",
                    volume = 108,
                    pan = 64,
                    isMuted = false,
                    isSoloed = false
                ),
                volumeCc = 17,
                panCc = 40,
                muteCc = 63,
                soloCc = 86,
                onVolumeChanged = {},
                onPanChanged = {},
                onMuteToggled = {},
                onSoloToggled = {},
                onHomeRouteChanged = {},
                onOpenPanDialog = {},
                onChannelRenameRequested = {}
            )
        }
    }
}
