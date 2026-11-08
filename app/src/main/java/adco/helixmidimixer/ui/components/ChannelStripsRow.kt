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
    onVolumeChanged: (channelIndex: Int, newVolume: Int) -> Unit,
    onPanChanged: (channelIndex: Int, newPan: Int) -> Unit,
    onMuteToggled: (channelIndex: Int, isMuted: Boolean) -> Unit,
    onSoloToggled: (channelIndex: Int, isSoloed: Boolean) -> Unit,
    onHomeRouteChanged: (newRoute: HomeRoute) -> Unit,
    onOpenPanDialog: (channelIndex: Int) -> Unit,
    onChannelRenameRequested: (songIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape Mode: expand and fill available screen width horizontally using proportional weighting / SpaceEvenly / fillMaxWidth
        Row(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(HelixBackground)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            channelStates.forEach { chState ->
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
                        val songIdx = chState.channelIndex - 1 // 1..8
                        if (songIdx in 1..8) {
                            onChannelRenameRequested(songIdx)
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        }
    } else {
        // Portrait Mode: Half-Screen Split Layout (Two equal vertical halves for Tier 1 and Tier 2)
        val firstTier = channelStates.take(8)
        val secondTier = channelStates.drop(8)

        val topScrollState = rememberScrollState()
        val bottomScrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HelixBackground)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Half (Tier 1)
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
                                val songIdx = chState.channelIndex - 1 // 1..8
                                if (songIdx in 1..8) {
                                    onChannelRenameRequested(songIdx)
                                }
                            },
                            modifier = Modifier.width(82.dp)
                        )
                    }
                }
            }

            // Bottom Half (Tier 2)
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
                                val songIdx = chState.channelIndex - 1 // 1..8
                                if (songIdx in 1..8) {
                                    onChannelRenameRequested(songIdx)
                                }
                            },
                            modifier = Modifier.width(82.dp)
                        )
                    }
                }
            }
        }
    }
}
