package adco.helixmidimixer

import android.content.Context
import android.content.ContextWrapper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.data.repository.DataStoreRepository
import adco.helixmidimixer.midi.BleMidiManager
import adco.helixmidimixer.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

data class SentCc(val channel: Int, val ccNumber: Int, val value: Int)

private class FakeContext : ContextWrapper(null) {
    override fun getSystemService(name: String): Any? = null
}

class TestBleMidiManager(context: Context) : BleMidiManager(context) {
    val sentCcs = mutableListOf<SentCc>()

    override fun sendControlChange(channel: Int, ccNumber: Int, value: Int): Boolean {
        sentCcs.add(SentCc(channel, ccNumber, value))
        return true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var repository: DataStoreRepository
    private lateinit var bleMidiManager: TestBleMidiManager
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val testFile = tmpFolder.newFile("test_main_vm.preferences_pb")
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        repository = DataStoreRepository(dataStore)
        bleMidiManager = TestBleMidiManager(FakeContext())
        viewModel = MainViewModel(repository, bleMidiManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetActiveMatrixMidiChannelDefaults() {
        // Default for OUTPUT_1_4 is 3
        assertEquals(3, viewModel.getActiveMatrixMidiChannel(OutputGroup.OUTPUT_1_4))
        // Default for XLR is 4
        assertEquals(4, viewModel.getActiveMatrixMidiChannel(OutputGroup.XLR))
        // Default for PHONES is 5
        assertEquals(5, viewModel.getActiveMatrixMidiChannel(OutputGroup.PHONES))
    }

    @Test
    fun testVolumeChangeSendsLayerMidiChannelAndCorrectCc() = runTest(testDispatcher) {
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        bleMidiManager.sentCcs.clear()

        // Volume for channelIndex 0 (Home) -> volumeCc = 11
        viewModel.onVolumeChange(channelIndex = 0, volume = 120)

        assertEquals(1, bleMidiManager.sentCcs.size)
        val sent = bleMidiManager.sentCcs[0]
        // Layer MIDI Channel for OUTPUT_1_4 should be 3 (NOT 0/channelIndex)
        assertEquals(3, sent.channel)
        assertEquals(11, sent.ccNumber)
        assertEquals(120, sent.value)
    }

    @Test
    fun testPanChangeSendsLayerMidiChannelAndCorrectCc() = runTest(testDispatcher) {
        viewModel.selectOutputGroup(OutputGroup.PHONES)
        bleMidiManager.sentCcs.clear()

        // Pan for channelIndex 0 (Home) on PHONES -> panCc = 34, Layer MIDI Channel = 5
        viewModel.onPanChange(channelIndex = 0, pan = 90)

        assertEquals(1, bleMidiManager.sentCcs.size)
        val sent = bleMidiManager.sentCcs[0]
        assertEquals(5, sent.channel)
        assertEquals(34, sent.ccNumber)
        assertEquals(90, sent.value)
    }

    @Test
    fun testToggleMuteSendsLayerMidiChannelAndCorrectCc() = runTest(testDispatcher) {
        viewModel.selectOutputGroup(OutputGroup.XLR)
        bleMidiManager.sentCcs.clear()

        // Mute for channelIndex 1 (Path 1A) on XLR -> muteCc = 58, Layer MIDI Channel = 4
        viewModel.toggleMute(channelIndex = 1, isMuted = true)

        assertEquals(1, bleMidiManager.sentCcs.size)
        val sent = bleMidiManager.sentCcs[0]
        assertEquals(4, sent.channel)
        assertEquals(58, sent.ccNumber)
        assertEquals(127, sent.value)
    }

    @Test
    fun testToggleSoloSendsLayerMidiChannelAndCorrectCc() = runTest(testDispatcher) {
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        bleMidiManager.sentCcs.clear()

        // Solo for channelIndex 6 (CH 1) on OUTPUT_1_4 -> soloCc = 86, Layer MIDI Channel = 3
        viewModel.toggleSolo(channelIndex = 6, isSoloed = true)

        assertEquals(1, bleMidiManager.sentCcs.size)
        val sent = bleMidiManager.sentCcs[0]
        assertEquals(3, sent.channel)
        assertEquals(86, sent.ccNumber)
        assertEquals(127, sent.value)
    }

    @Test
    fun testIncomingMuteCcSetsExplicitBooleanState() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.channelStates.collect {} }
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        // Mute CC for channelIndex 0 on OUTPUT_1_4 is CC 57
        val muteCc = 57

        // 1. Send CC 57 with value 127 (Muted)
        bleMidiManager.emitIncomingCc(muteCc, 127)
        var ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertTrue(ch0State?.isMuted == true)

        // 2. Send CC 57 with value 0 (Unmuted)
        bleMidiManager.emitIncomingCc(muteCc, 0)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertFalse(ch0State?.isMuted == true)

        // 3. Send CC 57 with value 127 again (Muted) -> must explicitly set to true, not toggle back to false
        bleMidiManager.emitIncomingCc(muteCc, 127)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertTrue(ch0State?.isMuted == true)
    }

    @Test
    fun testIncomingSoloCcSetsExplicitBooleanState() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.channelStates.collect {} }
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        // Solo CC for channelIndex 0 on OUTPUT_1_4 is CC 80
        val soloCc = 80

        // 1. Send CC 80 with value 127 (Soloed)
        bleMidiManager.emitIncomingCc(soloCc, 127)
        var ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertTrue(ch0State?.isSoloed == true)

        // 2. Send CC 80 with value 0 (Unsoloed)
        bleMidiManager.emitIncomingCc(soloCc, 0)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertFalse(ch0State?.isSoloed == true)

        // 3. Send CC 80 with value 127 again -> must explicitly set to true
        bleMidiManager.emitIncomingCc(soloCc, 127)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertTrue(ch0State?.isSoloed == true)
    }

    @Test
    fun testIncomingVolumeCcSetsCoercedVolume() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.channelStates.collect {} }
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        // Volume CC for channelIndex 0 on OUTPUT_1_4 is CC 11
        val volumeCc = 11

        bleMidiManager.emitIncomingCc(volumeCc, 85)
        var ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertEquals(85, ch0State?.volume)

        // Out of bounds value -> should coerce to 127
        bleMidiManager.emitIncomingCc(volumeCc, 150)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertEquals(127, ch0State?.volume)
    }

    @Test
    fun testIncomingPanCcSetsCoercedPan() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.channelStates.collect {} }
        viewModel.selectOutputGroup(OutputGroup.OUTPUT_1_4)
        // Pan CC for channelIndex 0 on OUTPUT_1_4 is CC 34
        val panCc = 34

        bleMidiManager.emitIncomingCc(panCc, 20)
        var ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertEquals(20, ch0State?.pan)

        // Out of bounds value -> should coerce to 0
        bleMidiManager.emitIncomingCc(panCc, -10)
        ch0State = viewModel.channelStates.value.find { it.channelIndex == 0 }
        assertEquals(0, ch0State?.pan)
    }
}
