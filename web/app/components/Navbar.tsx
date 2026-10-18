"use client";

import React, { useState } from "react";
import { useTheme } from "../ThemeContext";
import {
  Sun,
  Moon,
  Laptop,
  Copy,
  Scissors,
  HelpCircle,
  X,
  Layers,
} from "lucide-react";

interface NavbarProps {
  activeTab: "replicate" | "split";
  setActiveTab: (tab: "replicate" | "split") => void;
}

export function Navbar({ activeTab, setActiveTab }: NavbarProps) {
  const { theme, setTheme } = useTheme();
  const [showInfo, setShowInfo] = useState(false);

  return (
    <>
      <header className="sticky top-0 z-40 w-full bg-white/90 dark:bg-black/90 backdrop-blur-md border-b border-neutral-200 dark:border-neutral-800 transition-colors">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
          {/* Logo & Product Title */}
          <div className="flex items-center space-x-2.5">
            <div className="h-7 w-7 rounded-md bg-neutral-900 dark:bg-neutral-100 flex items-center justify-center text-white dark:text-neutral-900 shrink-0">
              <Layers className="h-4 w-4" />
            </div>
            <span className="font-semibold text-sm tracking-tight text-neutral-900 dark:text-neutral-100">
              MTX JarSync
            </span>
          </div>

          {/* Core Mode Tabs: Replicate vs Split */}
          <div className="flex items-center p-0.5 bg-neutral-100 dark:bg-neutral-900 rounded-lg border border-neutral-200/80 dark:border-neutral-800">
            <button
              onClick={() => setActiveTab("replicate")}
              className={`flex items-center space-x-1.5 px-3 py-1 rounded-md text-xs font-medium transition-all ${
                activeTab === "replicate"
                  ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs font-semibold"
                  : "text-neutral-600 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-200"
              }`}
            >
              <Copy className="h-3.5 w-3.5 shrink-0" />
              <span>Replicate</span>
            </button>
            <button
              onClick={() => setActiveTab("split")}
              className={`flex items-center space-x-1.5 px-3 py-1 rounded-md text-xs font-medium transition-all ${
                activeTab === "split"
                  ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs font-semibold"
                  : "text-neutral-600 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-200"
              }`}
            >
              <Scissors className="h-3.5 w-3.5 shrink-0" />
              <span>Split</span>
            </button>
          </div>

          {/* Theme Switcher & Info */}
          <div className="flex items-center space-x-1.5">
            <button
              onClick={() => setShowInfo(true)}
              className="p-1.5 text-neutral-500 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100 rounded-md transition"
              title="About"
            >
              <HelpCircle className="h-4 w-4" />
            </button>

            <div className="flex items-center p-0.5 bg-neutral-100 dark:bg-neutral-900 rounded-lg border border-neutral-200/80 dark:border-neutral-800">
              <button
                onClick={() => setTheme("light")}
                className={`p-1 rounded-md transition ${
                  theme === "light"
                    ? "bg-white text-neutral-900 shadow-xs font-medium"
                    : "text-neutral-500 hover:text-neutral-700 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title="Light"
              >
                <Sun className="h-3.5 w-3.5" />
              </button>
              <button
                onClick={() => setTheme("system")}
                className={`p-1 rounded-md transition ${
                  theme === "system"
                    ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs font-medium"
                    : "text-neutral-500 hover:text-neutral-700 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title="System Default"
              >
                <Laptop className="h-3.5 w-3.5" />
              </button>
              <button
                onClick={() => setTheme("dark")}
                className={`p-1 rounded-md transition ${
                  theme === "dark"
                    ? "bg-neutral-800 text-white shadow-xs font-medium"
                    : "text-neutral-500 hover:text-neutral-700 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title="Pure Dark"
              >
                <Moon className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Info Modal */}
      {showInfo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="relative w-full max-w-lg bg-white dark:bg-neutral-950 border border-neutral-200 dark:border-neutral-800 rounded-xl p-6 shadow-2xl">
            <button
              onClick={() => setShowInfo(false)}
              className="absolute top-4 right-4 p-1 rounded-md text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 transition"
            >
              <X className="h-4 w-4" />
            </button>

            <h3 className="text-base font-semibold text-neutral-900 dark:text-neutral-100 mb-1.5">
              J2ME Replication & Splitting Engine
            </h3>
            <p className="text-xs text-neutral-600 dark:text-neutral-400 leading-relaxed mb-4">
              100% in-browser JVM bytecode manipulation tool for retro Java MIDlet suites.
            </p>

            <div className="space-y-3 text-xs text-neutral-700 dark:text-neutral-300">
              <div className="p-3.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/60 border border-neutral-200/80 dark:border-neutral-800">
                <span className="font-semibold text-neutral-900 dark:text-neutral-100 block mb-1">
                  1. Replicate (Multi-Instance Cloning)
                </span>
                Takes a single J2ME JAR and clones it into 2 to 26 independent instances with isolated package namespaces (<code className="font-mono text-neutral-800 dark:text-neutral-200">root (a)</code>, <code className="font-mono text-neutral-800 dark:text-neutral-200">b/</code>, <code className="font-mono text-neutral-800 dark:text-neutral-200">c/</code>...), isolated RecordStore storage, cloned static initializers (<code className="font-mono text-neutral-800 dark:text-neutral-200">&lt;clinit&gt;</code>), and an in-game switcher overlay.
              </div>

              <div className="p-3.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/60 border border-neutral-200/80 dark:border-neutral-800">
                <span className="font-semibold text-neutral-900 dark:text-neutral-100 block mb-1">
                  2. Split (Deconstruction)
                </span>
                Extracts an existing multi-account or combined JAR back into standalone single-app J2ME JARs by stripping namespace prefixes and restoring individual manifests.
              </div>
            </div>

            <div className="mt-5 pt-3 border-t border-neutral-200 dark:border-neutral-800 flex justify-end">
              <button
                onClick={() => setShowInfo(false)}
                className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
