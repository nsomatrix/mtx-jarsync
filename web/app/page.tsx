"use client";

import React, { useState } from "react";
import { Navbar } from "./components/Navbar";
import { ReplicateStudio } from "./components/ReplicateStudio";
import { SplitStudio } from "./components/SplitStudio";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"replicate" | "split">("replicate");

  return (
    <div className="min-h-screen flex flex-col justify-between">
      <div>
        {/* Navigation Bar */}
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Content Container */}
        <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
          {/* Section Header */}
          <div className="mb-6">
            <h1 className="text-xl font-bold tracking-tight text-neutral-900 dark:text-neutral-100">
              {activeTab === "replicate" ? "Replicate J2ME Application" : "Split Multi-Game Archive"}
            </h1>
            <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
              {activeTab === "replicate"
                ? "Replicate a single JAR into 2 to 26 isolated instances with bytecode isolation, static cloning, and an in-game switcher."
                : "Deconstruct multi-session or combined JARs back into individual standalone playable archives."}
            </p>
          </div>

          {activeTab === "replicate" ? <ReplicateStudio /> : <SplitStudio />}
        </main>
      </div>

      {/* Footer */}
      <footer className="w-full border-t border-neutral-200 dark:border-neutral-900 py-6 mt-16 transition-colors">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-neutral-400 dark:text-neutral-500">
          <div>
            <span>100% In-Browser Execution • Pure TypeScript JVM Rewriter</span>
          </div>
          <div>
            <span>MTX JarSync</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
