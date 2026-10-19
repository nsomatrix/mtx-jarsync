"use client";

import React, { useState } from "react";
import { Navbar } from "./components/Navbar";
import { ReplicateStudio } from "./components/ReplicateStudio";
import { SplitStudio } from "./components/SplitStudio";
import { useLanguage } from "../lib/i18n/LanguageContext";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"replicate" | "split">("replicate");
  const { t } = useLanguage();

  return (
    <div className="min-h-screen flex flex-col justify-between selection:bg-neutral-900 selection:text-white dark:selection:bg-neutral-100 dark:selection:text-black">
      <div>
        {/* Navigation Bar */}
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Content Container */}
        <main className="w-full max-w-5xl mx-auto px-3.5 sm:px-6 pt-7 sm:pt-9 pb-10 sm:pb-14 overflow-x-hidden">
          {/* Section Header */}
          <div className="mb-6 sm:mb-8">
            <h1 className="text-lg sm:text-2xl font-bold tracking-tight text-neutral-900 dark:text-neutral-100">
              {activeTab === "replicate" ? t("page_replicate_title") : t("page_split_title")}
            </h1>
            <p className="text-xs sm:text-sm text-neutral-500 dark:text-neutral-400 mt-1.5 max-w-2xl leading-relaxed">
              {activeTab === "replicate" ? t("page_replicate_desc") : t("page_split_desc")}
            </p>
          </div>

          {activeTab === "replicate" ? <ReplicateStudio /> : <SplitStudio />}
        </main>
      </div>

      {/* Unified Modern Footer */}
      <footer className="w-full border-t border-neutral-200/80 dark:border-neutral-900 bg-neutral-50/60 dark:bg-neutral-950/60 py-7 sm:py-8 mt-12 sm:mt-16 transition-colors">
        <div className="max-w-5xl mx-auto px-3.5 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-4 sm:gap-6">
          {/* 1. JarSync GitHub Link Badge */}
          <div className="flex items-center shrink-0">
            <a
              href="https://github.com/nsomatrix/mtx-jarsync"
              target="_blank"
              rel="noopener noreferrer"
              className="group inline-flex items-center gap-2.5 px-3.5 py-1.5 rounded-xl bg-white dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 hover:border-neutral-300 dark:hover:border-neutral-700 transition-all shadow-2xs hover:shadow-xs"
              title="MTX JarSync Repository"
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src="/logo.png"
                alt="JarSync Logo"
                className="h-5 w-auto object-contain select-none shrink-0"
              />
              <div className="h-3.5 w-px bg-neutral-200 dark:bg-neutral-800 shrink-0" />
              <svg
                className="h-4 w-4 fill-neutral-700 dark:fill-neutral-300 group-hover:fill-black dark:group-hover:fill-white transition-colors shrink-0"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
                />
              </svg>
              <span className="text-xs font-medium text-neutral-700 dark:text-neutral-300 group-hover:text-black dark:group-hover:text-white transition-colors">
                nsomatrix/mtx-jarsync
              </span>
            </a>
          </div>

          {/* 2 & 3. Matrix Logo + Powered by NSOMatrix */}
          <div className="flex items-center gap-3 shrink-0 text-center sm:text-right">
            <a
              href="https://github.com/nsomatrix"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-block transition-transform hover:scale-105 active:scale-95 shrink-0"
              title="NSOMatrix GitHub"
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src="/mtx.png"
                alt="NSOMatrix MTX Logo"
                className="h-8 sm:h-9 w-auto object-contain select-none"
              />
            </a>
            <div className="h-6 w-px bg-neutral-200 dark:bg-neutral-800 shrink-0" />
            <span className="text-xs text-neutral-600 dark:text-neutral-400">
              Powered by{" "}
              <a
                href="https://github.com/nsomatrix"
                target="_blank"
                rel="noopener noreferrer"
                className="font-semibold text-neutral-900 dark:text-neutral-100 hover:underline transition-colors"
              >
                NSOMatrix™
              </a>
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
