"use client";

import React, { useState } from "react";
import { useTheme } from "../ThemeContext";
import {
  Sun,
  Moon,
  Laptop,
  Layers,
  Scissors,
  HelpCircle,
  X,
  Cpu,
  ShieldCheck,
  Zap,
} from "lucide-react";

interface NavbarProps {
  activeTab: "merge" | "split";
  setActiveTab: (tab: "merge" | "split") => void;
}

export function Navbar({ activeTab, setActiveTab }: NavbarProps) {
  const { theme, setTheme } = useTheme();
  const [showInfoModal, setShowInfoModal] = useState(false);

  return (
    <>
      <header className="sticky top-0 z-40 w-full glass-panel border-b border-slate-200/80 dark:border-slate-800/80 transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Logo & Brand */}
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-0.5 shadow-lg shadow-indigo-500/25 flex items-center justify-center">
              <div className="h-full w-full bg-slate-900 rounded-[10px] flex items-center justify-center">
                <Cpu className="h-5 w-5 text-cyan-400" />
              </div>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-lg tracking-tight bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-800 dark:from-white dark:via-slate-100 dark:to-slate-300 bg-clip-text text-transparent">
                  MTX JarSync
                </span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-indigo-100 dark:bg-indigo-950/80 text-indigo-700 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800/60">
                  Client-Side SaaS
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 hidden sm:block">
                Zero-Backend J2ME Multi-App Studio
              </p>
            </div>
          </div>

          {/* Mode Tabs */}
          <div className="flex items-center p-1 bg-slate-100/90 dark:bg-slate-900/90 rounded-xl border border-slate-200 dark:border-slate-800">
            <button
              onClick={() => setActiveTab("merge")}
              className={`flex items-center space-x-2 px-3 sm:px-4 py-1.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "merge"
                  ? "bg-white dark:bg-slate-800 text-indigo-600 dark:text-cyan-400 shadow-sm"
                  : "text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200"
              }`}
            >
              <Layers className="h-4 w-4" />
              <span>Merge</span>
            </button>
            <button
              onClick={() => setActiveTab("split")}
              className={`flex items-center space-x-2 px-3 sm:px-4 py-1.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "split"
                  ? "bg-white dark:bg-slate-800 text-indigo-600 dark:text-cyan-400 shadow-sm"
                  : "text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200"
              }`}
            >
              <Scissors className="h-4 w-4" />
              <span>Split</span>
            </button>
          </div>

          {/* Action Tools & Theme Selector */}
          <div className="flex items-center space-x-2">
            {/* Info / Architecture */}
            <button
              onClick={() => setShowInfoModal(true)}
              title="How it works"
              className="p-2 text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800/60 transition"
            >
              <HelpCircle className="h-5 w-5" />
            </button>

            {/* Theme Toggle (Light / Dark / System) */}
            <div className="flex items-center p-1 bg-slate-100 dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800">
              <button
                onClick={() => setTheme("light")}
                title="Light Theme"
                className={`p-1.5 rounded-md transition ${
                  theme === "light"
                    ? "bg-white text-amber-500 shadow-xs"
                    : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                }`}
              >
                <Sun className="h-4 w-4" />
              </button>
              <button
                onClick={() => setTheme("system")}
                title="System Default Theme"
                className={`p-1.5 rounded-md transition ${
                  theme === "system"
                    ? "bg-white dark:bg-slate-800 text-indigo-500 shadow-xs"
                    : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                }`}
              >
                <Laptop className="h-4 w-4" />
              </button>
              <button
                onClick={() => setTheme("dark")}
                title="Dark Theme"
                className={`p-1.5 rounded-md transition ${
                  theme === "dark"
                    ? "bg-slate-800 text-cyan-400 shadow-xs"
                    : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                }`}
              >
                <Moon className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Info / Architecture Modal */}
      {showInfoModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="relative w-full max-w-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 sm:p-8 shadow-2xl overflow-hidden">
            <button
              onClick={() => setShowInfoModal(false)}
              className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="flex items-center space-x-3 mb-5">
              <div className="h-10 w-10 rounded-xl bg-indigo-500/10 dark:bg-indigo-500/20 text-indigo-600 dark:text-cyan-400 flex items-center justify-center">
                <Zap className="h-6 w-6" />
              </div>
              <div>
                <h3 className="text-xl font-bold tracking-tight text-slate-900 dark:text-white">
                  Client-Side Architecture
                </h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  100% Private, Zero Server Uploads, Pure TypeScript JVM Bytecode Rewriter
                </p>
              </div>
            </div>

            <div className="space-y-4 text-sm text-slate-600 dark:text-slate-300">
              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-800 space-y-2">
                <div className="flex items-center space-x-2 text-indigo-600 dark:text-indigo-400 font-semibold text-xs uppercase tracking-wider">
                  <ShieldCheck className="h-4 w-4" />
                  <span>Privacy First Guarantee</span>
                </div>
                <p className="text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                  All bytecode manipulations, class constant pool rewrites, and archive compression happen
                  directly inside your web browser’s WebAssembly / V8 JavaScript engine. Your game ROMs and JARs never touch an external server.
                </p>
              </div>

              <div>
                <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-1">
                  How Jar Merging Works
                </h4>
                <ul className="list-disc pl-5 space-y-1 text-xs text-slate-500 dark:text-slate-400">
                  <li>Assigns distinct namespace prefixes (<code className="text-indigo-600 dark:text-cyan-400">b/</code>, <code className="text-indigo-600 dark:text-cyan-400">c/</code>... <code className="text-indigo-600 dark:text-cyan-400">z/</code>) to each sub-game to prevent class collision.</li>
                  <li>Patches all class files, rewriting <code className="text-slate-700 dark:text-slate-300">javax.microedition.*</code> imports to custom runtime wrappers.</li>
                  <li>Clones static initializers (<code className="text-slate-700 dark:text-slate-300">&lt;clinit&gt;</code>) and generates synthetic <code className="text-indigo-600 dark:text-cyan-400">Static.class</code> state routers.</li>
                  <li>Bakes an interactive switcher overlay so you can switch games in-game or run on real Nokia devices and MicroEmulator.</li>
                </ul>
              </div>

              <div>
                <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-1">
                  How Jar Splitting Works
                </h4>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Scans master manifests for multi-game index records, decodes sub-app manifests from embedded <code className="text-slate-700 dark:text-slate-300">.ini</code> entries, strips package prefixes, and re-bundles standalone playable JARs.
                </p>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-end">
              <button
                onClick={() => setShowInfoModal(false)}
                className="px-5 py-2 rounded-xl text-sm font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition shadow-sm"
              >
                Got it
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
