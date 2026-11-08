package adco.helixmidimixer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import adco.helixmidimixer.data.model.HomeRoute
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.data.repository.DataStoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreRepository

    @Before
    fun setup() {
        val testFile = tmpFolder.newFile("test_settings.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        repository = DataStoreRepository(dataStore)
    }

    @Test
    fun testSongChannelNamesPersistence() = runTest(testDispatcher) {
        // Initial names should be empty map
        var names = repository.getSongChannelNames().first()
        assertTrue(names.isEmpty())

        // Save song channel 1 and song channel 3 names
        repository.saveSongChannelName(1, "Guitar Lead")
        repository.saveSongChannelName(3, "Bass Synth")

        names = repository.getSongChannelNames().first()
        assertEquals(2, names.size)
        assertEquals("Guitar Lead", names[1])
        assertEquals("Bass Synth", names[3])
    }

    @Test
    fun testMuteAndSoloPersistence() = runTest(testDispatcher) {
        // Test initial mute states
        var mutes = repository.getMuteStates().first()
        assertFalse(mutes["${OutputGroup.XLR.name}_2"] ?: true)

        // Save mute state
        repository.saveMuteState(OutputGroup.XLR, 2, true)
        mutes = repository.getMuteStates().first()
        assertTrue(mutes["${OutputGroup.XLR.name}_2"] ?: false)

        // Save solo state
        repository.saveSoloState(OutputGroup.PHONES, 0, true)
        val solos = repository.getSoloStates().first()
        assertTrue(solos["${OutputGroup.PHONES.name}_0"] ?: false)
    }

    @Test
    fun testHomeRoutePersistence() = runTest(testDispatcher) {
        // Initial route is 1A
        var routes = repository.getHomeRoutes().first()
        assertEquals(HomeRoute.ROUTE_1A, routes[OutputGroup.OUTPUT_1_4])

        // Save route 2B
        repository.saveHomeRoute(OutputGroup.OUTPUT_1_4, HomeRoute.ROUTE_2B)
        routes = repository.getHomeRoutes().first()
        assertEquals(HomeRoute.ROUTE_2B, routes[OutputGroup.OUTPUT_1_4])
    }

    @Test
    fun testMidiCcMappingPersistence() = runTest(testDispatcher) {
        val defaultMapping = repository.getMidiCcMapping().first()
        assertEquals(11, defaultMapping.getChannelCc(OutputGroup.OUTPUT_1_4, 0).volumeCc)

        val updatedMapping = defaultMapping.updateChannelCc(
            OutputGroup.OUTPUT_1_4,
            0,
            adco.helixmidimixer.data.model.ChannelCc(volumeCc = 50, panCc = 51, muteCc = 52, soloCc = 53)
        )
        repository.saveMidiCcMapping(updatedMapping)

        val loadedMapping = repository.getMidiCcMapping().first()
        val ch0Cc = loadedMapping.getChannelCc(OutputGroup.OUTPUT_1_4, 0)
        assertEquals(50, ch0Cc.volumeCc)
        assertEquals(51, ch0Cc.panCc)
        assertEquals(52, ch0Cc.muteCc)
        assertEquals(53, ch0Cc.soloCc)
    }
}
