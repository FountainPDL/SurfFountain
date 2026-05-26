import { useState } from "react";

export default function App() {
  const [copied, setCopied] = useState(false);
  const workflow = `name: Android Build

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - uses: android-actions/setup-android@v3
      - uses: gradle/gradle-build-action@v2
        with:
          arguments: assembleDebug`;

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-green-50 text-zinc-900 selection:bg-purple-200">
      <header className="sticky top-0 z-30 backdrop-blur-xl bg-white/70 border-b border-purple-100">
        <div className="mx-auto max-w-6xl px-6 py-4 flex items-center gap-3">
          <img src="/icon.png" alt="Surf Fountain" className="h-9 w-9 rounded-2xl shadow-md ring-2 ring-purple-200" />
          <div>
            <h1 className="text-xl font-bold tracking-tight text-purple-700">Surf Fountain</h1>
            <p className="text-xs text-zinc-500 -mt-1">Pure Java Android Browser • GitHub APK Builder</p>
          </div>
          <span className="ml-auto text-[11px] font-medium px-2.5 py-1 rounded-full bg-purple-600 text-white">PDL AI</span>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-10">
        <section className="grid md:grid-cols-[1.15fr_0.85fr] gap-10 items-center">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-purple-100 text-purple-700 text-xs font-medium">
              <span className="h-2 w-2 rounded-full bg-green-500 animate-pulse" />
              GitHub Actions ready • Termux friendly
            </div>
            <h2 className="mt-4 text-4xl md:text-5xl font-extrabold leading-[1.1] tracking-tight">
              <span className="text-purple-700">Surf</span> <span className="text-zinc-900">Fountain</span>
              <br />
              <span className="text-2xl font-semibold text-zinc-700">Android browser in pure Java</span>
            </h2>
            <p className="mt-4 text-zinc-600 max-w-xl">
              Built for your phone workflow. Push to GitHub and get an APK—no local Android Studio needed.
              All features from your spec, with purple theme, iframe extraction, video download, and <b>PDL AI</b>.
            </p>

            <div className="mt-6 flex flex-wrap gap-3">
              <a href="https://github.com/new" target="_blank" className="px-5 py-2.5 rounded-2xl bg-purple-600 text-white font-medium shadow-lg shadow-purple-200 hover:brightness-105 active:scale-[0.98] transition">
                Create GitHub Repo
              </a>
              <button
                onClick={() => { navigator.clipboard.writeText(workflow); setCopied(true); setTimeout(()=>setCopied(false),1600); }}
                className="px-5 py-2.5 rounded-2xl bg-white border border-purple-200 font-medium hover:bg-purple-50"
              >
                {copied ? "Copied workflow!" : "Copy Actions workflow"}
              </button>
            </div>

            <div className="mt-8 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              {[
                {k:"Primary",v:"#7C3AED",c:"bg-purple-600"},
                {k:"Secondary",v:"#10B981",c:"bg-emerald-500"},
                {k:"Accent",v:"#EF4444",c:"bg-red-500"},
                {k:"AI",v:"PDL AI",c:"bg-purple-700 text-white"}
              ].map(x=>(
                <div key={x.k} className="rounded-2xl border border-zinc-200 bg-white p-3">
                  <div className={`h-8 w-full rounded-xl ${x.c} flex items-center justify-center text-[10px] text-white font-semibold`}>{x.v}</div>
                  <div className="mt-1 font-medium">{x.k}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="relative">
            <div className="absolute -inset-6 bg-gradient-to-tr from-purple-200 via-emerald-100 to-red-100 rounded-[2.5rem] blur-2xl opacity-60" />
            <div className="relative rounded-[2.2rem] border border-purple-200 bg-white shadow-2xl overflow-hidden">
              <div className="px-5 py-3 flex items-center gap-2 bg-purple-600 text-white">
                <span className="h-2.5 w-2.5 rounded-full bg-emerald-300" />
                <span className="h-2.5 w-2.5 rounded-full bg-red-300" />
                <span className="h-2.5 w-2.5 rounded-full bg-white/80" />
                <div className="ml-2 flex-1 text-center text-[13px] font-medium tracking-wide">surffountain.app</div>
              </div>
              <div className="p-4 space-y-3">
                <div className="flex items-center gap-2">
                  <div className="flex gap-1">
                    <div className="h-7 w-7 rounded-xl bg-zinc-100 grid place-items-center">←</div>
                    <div className="h-7 w-7 rounded-xl bg-zinc-100 grid place-items-center">→</div>
                  </div>
                  <div className="flex-1 h-9 rounded-2xl bg-zinc-100 px-3 flex items-center text-sm text-zinc-600">Search or URL</div>
                  <div className="h-9 px-3 rounded-2xl bg-emerald-500 text-white grid place-items-center text-sm font-medium">Go</div>
                </div>
                <div className="h-44 rounded-2xl bg-gradient-to-br from-purple-100 via-white to-emerald-50 border border-purple-100 grid place-items-center">
                  <div className="text-center">
                    <div className="text-lg font-bold text-purple-700">PDL AI</div>
                    <div className="text-xs text-zinc-500">Ask anything • Extract iFrames • Download video</div>
                  </div>
                </div>
                <div className="grid grid-cols-3 gap-2 text-[11px]">
                  {["iFrame extract","Video download","Desktop mode","Extensions","No wallet","Java only"].map(t=>(
                    <div key={t} className="rounded-xl bg-zinc-50 border border-zinc-200 py-2 text-center">{t}</div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="mt-14 rounded-[2rem] border border-purple-200 bg-white shadow-xl p-6 md:p-8">
          <h3 className="text-xl font-bold text-zinc-900">What I created for you</h3>
          <p className="text-sm text-zinc-600 mt-1">All files are in this project. Push everything to a new GitHub repo — Actions will build the APK.</p>

          <div className="mt-5 grid md:grid-cols-2 gap-5 text-sm">
            <div className="rounded-2xl bg-purple-50 border border-purple-200 p-4">
              <h4 className="font-semibold text-purple-800">Android app (pure Java)</h4>
              <ul className="mt-2 list-disc list-inside space-y-1 text-zinc-700">
                <li><code>app/src/main/java/com/surffountain/browser/MainActivity.java</code> — WebView, PDL AI, iframe extraction, video download</li>
                <li><code>app/src/main/res/layout/activity_main.xml</code> — Purple UI with green/red accents</li>
                <li><code>app/src/main/AndroidManifest.xml</code> — Permissions & intents</li>
                <li><code>app/build.gradle</code> — AndroidX, WebKit, Material</li>
              </ul>
            </div>
            <div className="rounded-2xl bg-emerald-50 border border-emerald-200 p-4">
              <h4 className="font-semibold text-emerald-800">GitHub build (Termux friendly)</h4>
              <ul className="mt-2 list-disc list-inside space-y-1 text-zinc-700">
                <li><code>.github/workflows/android.yml</code> — compiles <code>app-debug.apk</code></li>
                <li><code>gradle/wrapper/gradle-wrapper.properties</code> — Gradle 8.4</li>
                <li><code>settings.gradle</code>, <code>build.gradle</code> — project configs</li>
                <li>No local setup. Just push.</li>
              </ul>
            </div>
          </div>

          <div className="mt-6 rounded-2xl bg-zinc-950 text-zinc-100 p-4 text-[13px] overflow-auto">
            <div className="font-mono">
              # In Termux/phone:
              git init
              git add .
              git commit -m "Surf Fountain Android browser"
              git branch -M main
              git remote add origin &lt;YOUR_GITHUB_REPO_URL&gt;
              git push -u origin main
              # → GitHub Actions builds APK automatically
            </div>
          </div>
        </section>

        <section className="mt-12">
          <h3 className="text-lg font-bold">Features included (ALL)</h3>
          <div className="mt-3 grid sm:grid-cols-2 lg:grid-cols-3 gap-3 text-sm">
            {[
              "Full WebView browser with navigation",
              "Address bar with search fallback",
              "Tabs via intent & \"New Tab\"",
              "PDL AI floating action button",
              "iFrame URL extraction + share",
              "Video & file download (DownloadManager)",
              "Desktop site toggle",
              "Dark mode auto",
              "File chooser support",
              "Cookies & third-party enabled",
              "Extensions via JS injection bridge",
              "Purple #7C3AED theme, green/red accents",
              "No wallet, no rewards",
            ].map(f=>(
              <div key={f} className="rounded-2xl border border-zinc-200 bg-white p-3 flex items-start gap-2">
                <span className="mt-0.5 h-2.5 w-2.5 rounded-full bg-purple-600 inline-block" />
                <span>{f}</span>
              </div>
            ))}
          </div>
          <p className="mt-4 text-xs text-zinc-500">Icon: <code>/public/icon.png</code> • Update it in Android <code>mipmap</code> after first build if you want higher-res assets.</p>
        </section>
      </main>

      <footer className="mt-16 border-t border-purple-100">
        <div className="mx-auto max-w-6xl px-6 py-8 text-xs text-zinc-500 flex flex-wrap items-center gap-3">
          <span>© 2026 Surf Fountain</span>
          <span className="opacity-60">•</span>
          <span>Pure Java • GitHub APK • PDL AI</span>
        </div>
      </footer>
    </div>
  );
}