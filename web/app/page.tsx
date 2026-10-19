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
        <main className="w-full max-w-5xl mx-auto px-3.5 sm:px-6 py-5 sm:py-8 overflow-x-hidden">
          {/* Section Header */}
          <div className="mb-5 sm:mb-6">
            <h1 className="text-base sm:text-xl font-bold tracking-tight text-neutral-900 dark:text-neutral-100">
              {activeTab === "replicate" ? t("page_replicate_title") : t("page_split_title")}
            </h1>
            <p className="text-xs text-neutral-600 dark:text-neutral-400 mt-1 max-w-2xl leading-relaxed">
              {activeTab === "replicate" ? t("page_replicate_desc") : t("page_split_desc")}
            </p>
          </div>

          {activeTab === "replicate" ? <ReplicateStudio /> : <SplitStudio />}
        </main>
      </div>

      {/* Footer */}
      <footer className="w-full border-t border-neutral-200/80 dark:border-neutral-900 py-6 mt-12 sm:mt-16 transition-colors">
        <div className="max-w-5xl mx-auto px-3.5 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-2.5 text-xs text-neutral-500 dark:text-neutral-400 text-center sm:text-left">
          <div>
            <span>{t("footer_text")}</span>
          </div>
          <div className="font-medium text-neutral-600 dark:text-neutral-300">
            <span>{t("footer_brand")}</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
