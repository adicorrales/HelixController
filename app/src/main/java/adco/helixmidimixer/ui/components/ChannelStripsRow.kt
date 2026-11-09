package adco.helixmidimixer.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import adco.helixmidimixer.data.model.ChannelState
import adco.helixmidimixer.data.model.HomeRoute
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.ui.theme.HelixBackground

@Composable
fun ChannelStripsRow(
    channelStates: List<ChannelState>,
    midiCcMapping: MidiCcMapping,
    selectedGroup: OutputGroup,
    routesExpanded: Map<OutputGroup, Boolean>,
    songChannelsExpanded: Map<OutputGroup, Boolean>,
    onVolumeChanged: (channelIndex: Int, newVolume: Int) -> Unit,
    onPanChanged: (channelIndex: Int, newPan: Int) -> Unit,
    onMuteToggled: (channelIndex: Int, isMuted: Boolean) -> Unit,
    onSoloToggled: (channelIndex: Int, isSoloed: Boolean) -> Unit,
    onHomeRouteChanged: (newRoute: HomeRoute) -> Unit,
    onOpenPanDialog: (channelIndex: Int) -> Unit,
    onChannelRenameRequested: (songIndex: Int) -> Unit,
    onToggleRoutesExpanded: (OutputGroup, Boolean) -> Unit,
    onToggleSongChannelsExpanded: (OutputGroup, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTabletOrWidePortrait = !isLandscape && (configuration.smallestScreenWidthDp >= 600 || configuration.screenWidthDp >= 600)
    val isExpanded = routesExpanded[selectedGroup] ?: false
    val isSongExpanded = songChannelsExpanded[selectedGroup] ?: true

    val renderStrip: @Composable (ChannelState, Modifier) -> Unit = { chState, stripModifier ->
        val chCc = midiCcMapping.getChannelCc(selectedGroup, chState.channelIndex)
        ChannelStrip(
            channelState = chState,
            volumeCc = chCc.volumeCc,
            panCc = chCc.panCc,
            muteCc = chCc.muteCc,
            soloCc = chCc.soloCc,
            onVolumeChanged = { newVol -> onVolumeChanged(chState.channelIndex, newVol) },
            onPanChanged = { newPan -> onPanChanged(chState.channelIndex, newPan) },
            onMuteToggled = { isMuted -> onMuteToggled(chState.channelIndex, isMuted) },
            onSoloToggled = { isSoloed -> onSoloToggled(chState.channelIndex, isSoloed) },
            onHomeRouteChanged = onHomeRouteChanged,
            onOpenPanDialog = { onOpenPanDialog(chState.channelIndex) },
            onChannelRenameRequested = {
                val songIdx = chState.channelIndex - 5 // 1..8
                if (songIdx in 1..8) {
                    onChannelRenameRequested(songIdx)
                }
            },
            isRoutesExpanded = isExpanded,
            onToggleRoutesExpanded = if (chState.channelIndex == 0) {
                { expanded -> onToggleRoutesExpanded(selectedGroup, expanded) }
            } else null,
            isSongChannelsExpanded = isSongExpanded,
            onToggleSongChannelsExpanded = if (chState.channelIndex == 5) {
                { expanded -> onToggleSongChannelsExpanded(selectedGroup, expanded) }
            } else null,
            modifier = stripModifier
        )
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(HelixBackground)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            channelStates.forEach { chState ->
                renderStrip(chState, Modifier.fillMaxHeight().weight(1f).padding(horizontal = 2.dp))
            }
        }
    } else if (isTabletOrWidePortrait) {
        val splitIndex = (channelStates.size + 1) / 2
        val firstTier = channelStates.take(splitIndex)
        val secondTier = channelStates.drop(splitIndex)

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HelixBackground)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                firstTier.forEach { chState ->
                    renderStrip(chState, Modifier.fillMaxHeight().weight(1f).padding(horizontal = 2.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                secondTier.forEach { chState ->
                    renderStrip(chState, Modifier.fillMaxHeight().weight(1f).padding(horizontal = 2.dp))
                }
            }
        }
    } else {
        val splitIndex = (channelStates.size + 1) / 2
        val firstTier = channelStates.take(splitIndex)
        val secondTier = channelStates.drop(splitIndex)

        val topScrollState = rememberScrollState()
        val bottomScrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HelixBackground)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .background(HelixBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(topScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    firstTier.forEach { chState ->
                        renderStrip(chState, Modifier.width(82.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(HelixBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(bottomScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    secondTier.forEach { chState ->
                        renderStrip(chState, Modifier.width(82.dp))
                    }
                }
            }
        }
    }
}
