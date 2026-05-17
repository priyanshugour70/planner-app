## 🛠️ Step 1: Generate Native Projects (Android & iOS)

Run this single command in your `planner-app` directory to generate the native folders:
```bash
npx expo prebuild --clean
```

This will create two new directories in your project root:
*   📁 `/android` — A full native Android Studio Gradle project.
*   📁 `/ios` — A full native Xcode workspace project (requires macOS).

---

## 🤖 Step 2: Build Android Manually

You can compile your Android app using the **Gradle Command Line** or visually in **Android Studio**.

### Option A: Via Command Line (Fastest)
1. Navigate into the native Android folder:
   ```bash
   cd android
   ```
2. Compile a debug build (perfect for local testing):
   ```bash
   ./gradlew assembleDebug
   ```
3. Compile a signed release build:
   ```bash
   ./gradlew assembleRelease
   ```
   *Your compiled APK will be exported directly to:*  
   `android/app/build/outputs/apk/release/app-release.apk`

### Option B: Via Android Studio (Best for Debugging)
1. Open **Android Studio**.
2. Click **Open** and select the `/Users/priyanshugour/Desktop/per-pro/planner-done/planner-app/android` folder.
3. Wait for Gradle to finish indexing and downloading dependencies.
4. **Run on Device/Simulator**: Click the green **Play** button at the top.
5. **Generate signed package**: Go to **Build** > **Generate Signed Bundle / APK...**, select your keystore, and build your release bundle!

---

## 🍎 Step 3: Build iOS Manually (Requires a Mac)

Since you are running macOS, you can build the iOS app manually using **Xcode**.

### Step-by-Step iOS Build:
1. **Install iOS Native Dependencies (CocoaPods)**:
   Navigate to the iOS folder and run install:
   ```bash
   cd ios && pod install
   ```
2. **Open the Project in Xcode**:
   Always open the Xcode Workspace file (`.xcworkspace`), not the raw project file:
   ```bash
   open Planner.xcworkspace
   ```
3. **Configure Code Signing**:
   * In Xcode, click on the **Planner** project root on the left side-panel.
   * Go to the **Signing & Capabilities** tab.
   * Select your developer account/team under **Team**. Xcode will automatically manage your provisioning profiles and certificates!
4. **Compile and Run**:
   * **Simulators / Local testing**: Select your target iPhone simulator/device from the top menu, then press the green **Play/Run** button (or `Cmd + R`).
   * **Production Archive**: Set the device target to **Any iOS Device (arm64)**, then click **Product** > **Archive** from the top menu. This compiles and packages the `.ipa` file ready for TestFlight or the App Store!

