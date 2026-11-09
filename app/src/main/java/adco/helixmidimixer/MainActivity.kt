package adco.helixmidimixer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import adco.helixmidimixer.data.repository.DataStoreRepository
import adco.helixmidimixer.data.repository.helixDataStore
import adco.helixmidimixer.midi.BleMidiManager
import adco.helixmidimixer.ui.screens.HelixStadiumMixerScreen
import adco.helixmidimixer.ui.theme.HelixBackground
import adco.helixmidimixer.ui.theme.HelixMidiMixerTheme
import adco.helixmidimixer.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = DataStoreRepository(applicationContext.helixDataStore)
                val bleManager = BleMidiManager(applicationContext)
                return MainViewModel(repo, bleManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
        setContent {
            HelixMidiMixerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HelixBackground
                ) {
                    HelixStadiumMixerScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun HelixStadiumMixerPreview() {
    HelixMidiMixerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HelixBackground
        ) {
            // Preview placeholder
        }
    }
}
