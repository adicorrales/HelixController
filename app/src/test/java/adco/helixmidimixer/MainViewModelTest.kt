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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
}
