package adco.helixmidimixer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(HelixBackground)
            .horizontalScroll(scrollState)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                }
            )
        }
    }
}
