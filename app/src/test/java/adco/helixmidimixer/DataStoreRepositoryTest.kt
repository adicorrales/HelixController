package adco.helixmidimixer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import adco.helixmidimixer.data.model.HomeRoute
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
        var names = repository.getSongChannelNames().first()
        assertTrue(names.isEmpty())

        repository.saveSongChannelName(1, "Guitar Lead")
        repository.saveSongChannelName(3, "Bass Synth")

        names = repository.getSongChannelNames().first()
        assertEquals(2, names.size)
        assertEquals("Guitar Lead", names[1])
        assertEquals("Bass Synth", names[3])
    }

    @Test
    fun testMuteAndSoloPersistenceForHighChannels() = runTest(testDispatcher) {
        var mutes = repository.getMuteStates().first()
        assertFalse(mutes["${OutputGroup.XLR.name}_15"] ?: true)

        repository.saveMuteState(OutputGroup.XLR, 15, true)
        mutes = repository.getMuteStates().first()
        assertTrue(mutes["${OutputGroup.XLR.name}_15"] ?: false)

        repository.saveSoloState(OutputGroup.PHONES, 18, true)
        val solos = repository.getSoloStates().first()
        assertTrue(solos["${OutputGroup.PHONES.name}_18"] ?: false)
    }

    @Test
    fun testChannelVisibilityPersistence() = runTest(testDispatcher) {
        var visibilities = repository.getChannelVisibilities().first()
        assertTrue(visibilities["${OutputGroup.OUTPUT_1_4.name}_6"] ?: false)

        repository.saveChannelVisibility(OutputGroup.OUTPUT_1_4, 6, false)
        visibilities = repository.getChannelVisibilities().first()
        assertFalse(visibilities["${OutputGroup.OUTPUT_1_4.name}_6"] ?: true)
    }

    @Test
    fun testHomeRoutePersistence() = runTest(testDispatcher) {
        var routes = repository.getHomeRoutes().first()
        assertEquals(HomeRoute.ROUTE_1A, routes[OutputGroup.OUTPUT_1_4])

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

    @Test
    fun testTransportMidiChannelPersistence() = runTest(testDispatcher) {
        val defaultCh = repository.getTransportMidiChannel().first()
        assertEquals(1, defaultCh)

        repository.saveTransportMidiChannel(5)
        val savedCh = repository.getTransportMidiChannel().first()
        assertEquals(5, savedCh)
    }

    @Test
    fun testMatrixMidiChannelsPersistence() = runTest(testDispatcher) {
        val defaultChannels = repository.getMatrixMidiChannels().first()
        assertEquals(3, defaultChannels[OutputGroup.OUTPUT_1_4])
        assertEquals(4, defaultChannels[OutputGroup.XLR])
        assertEquals(5, defaultChannels[OutputGroup.PHONES])

        repository.saveMatrixMidiChannel(OutputGroup.OUTPUT_1_4, 1)
        repository.saveMatrixMidiChannel(OutputGroup.XLR, 10)
        repository.saveMatrixMidiChannel(OutputGroup.PHONES, 16)

        val updatedChannels = repository.getMatrixMidiChannels().first()
        assertEquals(1, updatedChannels[OutputGroup.OUTPUT_1_4])
        assertEquals(10, updatedChannels[OutputGroup.XLR])
        assertEquals(16, updatedChannels[OutputGroup.PHONES])
    }

    @Test
    fun testSongPlayModePersistence() = runTest(testDispatcher) {
        val defaultMode = repository.getSongPlayMode().first()
        assertEquals(adco.helixmidimixer.data.model.SongPlayMode.PLAY, defaultMode)

        repository.saveSongPlayMode(adco.helixmidimixer.data.model.SongPlayMode.LOAD_PRESET)
        val savedMode = repository.getSongPlayMode().first()
        assertEquals(adco.helixmidimixer.data.model.SongPlayMode.LOAD_PRESET, savedMode)
    }

    @Test
    fun testVolumeAndPanPersistence() = runTest(testDispatcher) {
        val defaultVols = repository.getVolumeStates().first()
        assertEquals(108, defaultVols["${OutputGroup.OUTPUT_1_4.name}_0"])

        repository.saveVolumeState(OutputGroup.OUTPUT_1_4, 0, 120)
        val savedVols = repository.getVolumeStates().first()
        assertEquals(120, savedVols["${OutputGroup.OUTPUT_1_4.name}_0"])

        val defaultPans = repository.getPanStates().first()
        assertEquals(64, defaultPans["${OutputGroup.OUTPUT_1_4.name}_0"])

        repository.savePanState(OutputGroup.OUTPUT_1_4, 0, 32)
        val savedPans = repository.getPanStates().first()
        assertEquals(32, savedPans["${OutputGroup.OUTPUT_1_4.name}_0"])
    }

    @Test
    fun testSelectedGroupPersistence() = runTest(testDispatcher) {
        val defaultGroup = repository.getSelectedGroup().first()
        assertEquals(OutputGroup.OUTPUT_1_4, defaultGroup)

        repository.saveSelectedGroup(OutputGroup.PHONES)
        val savedGroup = repository.getSelectedGroup().first()
        assertEquals(OutputGroup.PHONES, savedGroup)
    }
}
