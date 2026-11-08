# Project Plan

Helix Midi Mixer: Aplicación Android para teléfonos y tablets (UI adaptativa/responsive) que sirve como controlador MIDI vía Bluetooth (CME WIDI Master) para la Matrix Mixer de Line 6 Helix Stadium.

Funcionalidades clave:
1. 3 Matrices/Grupos de salida (1/4" Outputs, XLR Outputs, Phones Outputs) con 3 botones de selección con icono en la barra superior.
2. Replica exacta de la GUI de Line 6 Helix Stadium Matrix UI (modo oscuro, faders estilizados, botones Mute/Solo M/S, etc.).
3. 11 Canales siempre visibles por cada grupo de salida:
   - Canal 1: Home (Verde claro) con selector desplegable para 4 rutas (1A, 1B, 2A, 2B).
   - Canal 2: Volumen general Song (Gris claro / Plata).
   - Canales 3 a 10: 8 canales individuales de Song (Gris claro / Plata) con nombres editables guardados en configuración.
   - Canal 11: Audio Bluetooth (Azul claro).
4. Componentes verticales de cada canal (de arriba a abajo):
   - Balance/Pan (Left-Right) slider/desplegable.
   - Botones Mute (M) y Solo (S).
   - Slider / Fader de volumen vertical.
   - Nombre fijo con icono (Home, Song, BT) y editable para canales Song.
5. Control MIDI mediante Bluetooth LE (CME WIDI Master):
   - Mapeo por defecto de MIDI CC (volumen, pan, mute, solo) con pantalla/opción para personalizar CCs.
   - Botón accesible para buscar y conectar a WIDI Master Bluetooth MIDI.
   - Valores iniciales por defecto (volumen 0, pan Center); preservación de estados Mute y Solo entre sesiones (persistencia).
6. Adaptabilidad de pantalla (Phones & Tablets):
   - Scroll horizontal si no entran los 11 canales en pantalla manteniendo el ancho de canal.
   - En orientación Portrait (vertical), los canales se dividen en dos niveles/filas verticales.

## Project Brief

# Project Brief: Helix Midi Mixer

## Features
- **Matrix Output Group Management**: Top bar selection for 3 output groups (1/4" Outputs, XLR Outputs, Phones Outputs) with a Line 6 Helix Stadium Matrix styled dark mode interface.
- **11-Channel Audio Strip Control**: 11 channel strips per output group (Home with 4-route 1A/1B/2A/2B selector, Song Master Volume, 8 customizable Song channels with editable labels, and BT Audio), each with Pan/Balance, Mute/Solo buttons, and vertical volume faders.
- **Bluetooth LE MIDI Integration**: Wireless BLE MIDI connectivity designed for CME WIDI Master, featuring pre-mapped and customizable MIDI CC controls (Volume, Pan, Mute, Solo).
- **Adaptive UI (Phones & Tablets)**: Responsive UI adapting seamlessly across form factors, utilizing a two-tier layout in portrait mode and horizontal scrolling for full 11-channel access.
- **Session State & Config Persistence**: Persistent storage of custom channel names, Mute/Solo toggle states, and MIDI CC mapping configurations between sessions.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation 3 (State-driven)
- **Adaptive Strategy**: Compose Material Adaptive (`androidx.compose.material3.adaptive`)
- **Async & Concurrency**: Kotlin Coroutines & Flow
- **Connectivity**: Android Bluetooth LE (MIDI over BLE)
- **Persistence**: Jetpack DataStore (Preferences DataStore for channel names, states, and MIDI CC maps)

## Implementation Steps
**Total Duration:** 41m 1s

### Task_1_Core_Models_DataStore_And_MidiEngine: Setup data models for 3 Output Groups, 11 Channels per group, channel color coding, MIDI CC mappings, DataStore repository for state persistence (channel names, Mute/Solo, CC maps), and BLE MIDI manager for CME WIDI Master connectivity.
- **Status:** COMPLETED
- **Updates:** Implemented OutputGroup, ChannelType, HomeRoute, ChannelState, MidiCcMapping, DataStoreRepository for Mute/Solo & channel names persistence, and BleMidiManager for CME WIDI Master Bluetooth LE MIDI communication. All unit tests passed and build succeeded.
- **Acceptance Criteria:**
  - Data models created for Output Groups, 11 Channels, and MIDI CC mappings
  - DataStore persistence implemented for channel names, Mute/Solo states, and CC maps
  - BLE MIDI connection manager implemented for sending and receiving CC messages
  - build pass
- **Duration:** 10m 12s

### Task_2_Helix_Stadium_Mixer_UI_And_Channel_Strips: Develop the Line 6 Helix Stadium Matrix dark styled Compose UI including Top Bar output group selector (1/4 Outputs, XLR Outputs, Phones Outputs), Bluetooth MIDI connection button, 11 vertical channel strips (Pan slider/popup, Mute & Solo buttons, Volume slider, Channel name/icon), color coding (Home: light green, Song: light silver, BT: light blue), and MIDI CC customization dialog.
- **Status:** COMPLETED
- **Updates:** Implemented Line 6 Helix Stadium Matrix UI theme, MatrixTopBar with Output Group selector & BLE MIDI status button, 11 vertical channel strips with volume faders, Pan popup dialog, Mute & Solo buttons, 4-route selector, editable channel names, BluetoothMidiDialog, and MidiCcCustomizationDialog. Gradle build and unit tests passed.
- **Acceptance Criteria:**
  - Top bar with Output Group selector and BLE MIDI status button rendered
  - 11 channel strips rendered with dark Helix Matrix UI design and proper color highlights
  - Volume fader, Pan popup/slider, Mute, Solo, 4-route selector, and MIDI CC editing dialog functional
  - build pass
- **Duration:** 6m 31s

### Task_3_Adaptive_Layout_And_State_Wiring: Integrate Jetpack Compose UI with ViewModel, DataStore, and BLE MIDI manager. Implement adaptive layouts (horizontal scroll for landscape/tablets, two-tier vertical breakdown for portrait mode).
- **Status:** COMPLETED
- **Updates:** Adaptive layouts (horizontal scroll for landscape/tablets, two-tier vertical breakdown for portrait mode) implemented. Bidirectional state & MIDI wiring completed in MainViewModel with DataStore persistence and BleMidiManager integration. All 12 unit tests and debug assemble passed successfully.
- **Acceptance Criteria:**
  - ViewModel syncs UI controls with BLE MIDI output and DataStore persistence
  - Adaptive layout correctly switches between portrait two-tier layout and landscape/tablet horizontal scroll
  - Custom channel names and Mute/Solo states persist across app restarts
  - build pass
- **Duration:** 15m 27s

### Task_4_Run_And_Verify: Build, run, and verify the Helix Midi Mixer application. Critic agent must verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Critic agent deployed and verified the app successfully on emulator. App is stable with zero crashes, all 3 output groups, 11 channel strips, adaptive layouts (horizontal scroll & two-tier portrait), persistent Mute/Solo states, custom channel names, and MIDI CC customization fully verified.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - All project requirements verified: BLE MIDI integration, 3 Output Groups, 11 Channels, persistence, dark Helix matrix styling, and adaptive layout
- **Duration:** 3m 30s

### Task_5_Channel_Cc_Mappings_And_Ui_Refinements: Update Home channel defaults ('All paths') and route selectors (1A, 1B, 2A, 2B) with correct Line 6 Helix Stadium MIDI CC mappings (Home All paths, 1A, 1B, 2A, 2B, BT audio Vol CC28, Pan CC51, Mute CC74). Remove CC command badges from channel strips. Fix Pan slider behavior (0=Left, 64=Center, 127=Right) and prevent sliding feedback issues. Ensure Volume (0-127), Pan (0-127), Mute/Solo (toggle >=64), and Clear Solo CC logic adhere strictly to specs.
- **Status:** COMPLETED
- **Updates:** Updated Home channel defaults ('All paths') and route selectors (1A, 1B, 2A, 2B) with correct Line 6 Helix Stadium MIDI CC mappings. Removed CC command badges from main interface channel strips. Fixed Pan slider mapping (0=Left, 64=Center, 127=Right) and prevented feedback loops. Configured Mute/Solo toggle on >=64 and Clear Solo support. Unit tests and gradle build passed.
- **Acceptance Criteria:**
  - Home channel defaults to 'All paths' and route selectors (1A, 1B, 2A, 2B) send correct Line 6 Helix Stadium MIDI CC mappings
  - CC command badges removed from main interface channel strips
  - Pan slider behavior correctly mapped to 0 (Left), 64 (Center), 127 (Right) with feedback prevention
  - Volume, Mute/Solo (toggle >=64), and Clear Solo CC logic adhere strictly to specs
  - build pass
- **Duration:** 2m 58s

### Task_6_Window_Insets_Layout_Refinements_And_Verification: Fix top status bar window insets overlap for top text and buttons. Expand channels to fill screen horizontally in landscape mode. Split mixer into top half and bottom half in portrait mode. Run and verify application stability, layout correctness, and MIDI communication.
- **Status:** COMPLETED
- **Updates:** Successfully applied window insets padding to avoid status bar overlap. Implemented landscape horizontal expansion layout and portrait half-screen split layout (top half and bottom half). Removed CC badges from main channel strips. Verified stability, unit tests (12/12 passed), and emulator QA testing.
- **Acceptance Criteria:**
  - Top status bar window insets padding correctly applied to prevent overlap
  - Landscape mode expands channel strips to fill available screen width horizontally
  - Portrait mode splits mixer cleanly into top half and bottom half
  - make sure all existing tests pass
  - build pass
  - app does not crash
- **Duration:** 2m 23s

