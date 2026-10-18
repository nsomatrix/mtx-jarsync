"use client";

import React, { useState } from "react";
import { Navbar } from "./components/Navbar";
import { MergeStudio } from "./components/MergeStudio";
import { SplitStudio } from "./components/SplitStudio";
import { Shield, Sparkles, Terminal, Code2 } from "lucide-react";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"merge" | "split">("merge");

  return (
    <div className="min-h-screen flex flex-col justify-between selection:bg-indigo-500 selection:text-white">
      <div>
        {/* Navigation Bar */}
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Main App Container */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10">
          {activeTab === "merge" ? <MergeStudio /> : <SplitStudio />}
        </main>
      </div>

      {/* Footer */}
      <footer className="w-full border-t border-slate-200/80 dark:border-slate-800/80 py-8 bg-white/40 dark:bg-slate-950/40 backdrop-blur-sm mt-16 transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500 dark:text-slate-400">
          <div className="flex items-center space-x-2">
            <div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-semibold text-slate-700 dark:text-slate-300">
              100% In-Browser Execution
            </span>
            <span>• No files uploaded to any server</span>
          </div>

          <div className="flex items-center space-x-6">
            <span className="flex items-center space-x-1.5">
              <Shield className="h-3.5 w-3.5 text-indigo-500" />
              <span>CLDC 1.1 / MIDP 2.0 Bytecode Standard</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <Code2 className="h-3.5 w-3.5 text-cyan-500" />
              <span>MTX JarSync Studio</span>
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
