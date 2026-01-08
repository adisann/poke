# Pokemon Game - Kelompok 5

## Description
This is a LibGDX-based Pokemon game clone. 

## Requirements
- Java 17 or higher (Eclipse Adoptium / Temurin recommended)

## Download & Play (Google Drive)
**[CLICK HERE TO DOWNLOAD GAME ANDROID/PC](https://drive.google.com/file/d/1fo_iGFcqVZ5bFS-BM8wSP_7EQUYcL7ZI/view?usp=sharing)**

Cara install & main:
1. Download file `pokemon-1.0.0.jar` dari link di atas.
2. Pastikan Java 17 sudah terinstall.
3. Double click file `.jar` nya.
4. Enjoy!

## How to Run

### Method 1: Using the Runnable JAR
**Prerequisite:** You MUST have Java 17 installed. Check by running `java -version`. If it says "1.8", you need to update Java.

1. **If running from the project root:**
   ```
   java -jar lwjgl3/build/libs/pokemon-1.0.0.jar
   ```

2. **If you navigate to the folder:**
   ```
   cd lwjgl3/build/libs
   java -jar pokemon-1.0.0.jar
   ```

**Troubleshooting:**
- If you see `UnsupportedClassVersionError`: You are using Java 8. Install Java 17+.
- If you see `Unable to access jarfile`: Check the path. Are you in the right folder?

### Method 2: Building from Source (Recommended if you have Gradle problems)
1. Open a terminal in the project root.
2. Run:
   ```
   ./gradlew.bat lwjgl3:run
   ```
   (This uses Gradle's internal Java 17, so it works even if your system Java is old).

## Controls
- **Arrow Keys**: Move
- **X**: Interact / Select
- **Z**: Cancel / Back
- **Shift (Hold)**: Run
- **F5**: Quicksave
- **F8**: Quickload
- **ESC**: Open Debug/Menu (if implemented)

## Known Issues
- **Lab Entry**: You cannot enter Professor Birch's lab because the map file for the interior is missing from the asset pack.
- **Map Transitions**: Some map transitions might cause the player collision to behave interactively. If you get stuck, try restarting or loading a save (F8) if you saved earlier (F5).

## Packaging for Distribution

### Creating a Standalone JAR
To create a single JAR file that can be sent to friends (requires them to have Java installed):
1. Run `./gradlew.bat lwjgl3:jar`
2. The output file will be in `lwjgl3/build/libs/pokemon-1.0.0.jar`.

### Creating a Windows EXE (Advanced)
Since the built-in `construo` plugin has issues with newer Java versions, you can use **Launch4j** to wrap the JAR into an EXE.

1. Download **Launch4j** (http://launch4j.sourceforge.net/).
2. Open Launch4j.
3. **Basic**:
   - Output file: `Pokemon.exe`
   - Jar: `pokemon-1.0.0.jar`
   - Icon: `graphics/pokeball_icon.ico` (convert png to ico if needed)
4. **JRE**:
   - Min JRE version: `17`
5. Click **Build wrapper**.
6. Distribute the `Pokemon.exe` along with the `pokemon-1.0.0.jar` (Launch4j usually wraps it or references it).

Alternatively, use `jpackage` (included in JDK 14+):
```bash
jpackage --input lwjgl3/build/libs --name Pokemon --main-jar pokemon-1.0.0.jar --main-class com.github.adisann.pokemon.lwjgl3.Lwjgl3Launcher --type app-image
```
This will create a `Pokemon` folder with a standalone executable that includes its own Java runtime.
