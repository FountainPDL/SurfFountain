# Surf Fountain Browser

Pure Java Android browser built for GitHub Actions APK compilation.

## Features
- Full WebView browser with tabs support
- **PDL AI** assistant (floating button)
- iFrame URL extraction tool
- Video & file download via DownloadManager
- Desktop site toggle
- Extensions support via JavaScript injection
- Purple (#7C3AED) primary, Green (#10B981) secondary, Red (#EF4444) accent
- No wallet or rewards

## Build on GitHub (Termux friendly)
1. Push this repo to GitHub
2. GitHub Actions will compile `app-debug.apk`
3. Download artifact from Actions tab

## Local build (if needed)
```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`