"use client";

import React, { useState } from "react";
import { useTheme } from "../ThemeContext";
import { useLanguage } from "../../lib/i18n/LanguageContext";
import {
  Sun,
  Moon,
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
  const { language, setLanguage, t } = useLanguage();
  const [showInfo, setShowInfo] = useState(false);

  return (
    <>
      <header className="sticky top-0 z-40 w-full bg-white/90 dark:bg-black/90 backdrop-blur-md border-b border-neutral-200 dark:border-neutral-800 transition-colors">
        <div className="max-w-5xl mx-auto px-3.5 sm:px-6 h-14 flex items-center justify-between gap-2">
          {/* Logo & Product Title */}
          <div className="flex items-center space-x-2 shrink-0">
            <div className="h-7 w-7 rounded-md bg-neutral-900 dark:bg-neutral-100 flex items-center justify-center text-white dark:text-neutral-900 shrink-0">
              <Layers className="h-4 w-4" />
            </div>
            <span className="font-semibold text-sm tracking-tight text-neutral-900 dark:text-neutral-100 hidden sm:inline">
              {t("app_title")}
            </span>
          </div>

          {/* Core Mode Tabs: Replicate vs Split */}
          <div className="flex items-center p-0.5 bg-neutral-100 dark:bg-neutral-900 rounded-lg border border-neutral-200/80 dark:border-neutral-800">
            <button
              onClick={() => setActiveTab("replicate")}
              className={`flex items-center space-x-1.5 px-2.5 sm:px-3 py-1 rounded-md text-xs font-medium transition-all ${
                activeTab === "replicate"
                  ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs font-semibold"
                  : "text-neutral-600 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-200"
              }`}
            >
              <Copy className="h-3.5 w-3.5 shrink-0" />
              <span>{t("tab_replicate")}</span>
            </button>
            <button
              onClick={() => setActiveTab("split")}
              className={`flex items-center space-x-1.5 px-2.5 sm:px-3 py-1 rounded-md text-xs font-medium transition-all ${
                activeTab === "split"
                  ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs font-semibold"
                  : "text-neutral-600 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-200"
              }`}
            >
              <Scissors className="h-3.5 w-3.5 shrink-0" />
              <span>{t("tab_split")}</span>
            </button>
          </div>

          {/* Controls: Language, Theme & Info */}
          <div className="flex items-center space-x-1.5 shrink-0">
            {/* Language Switcher */}
            <div className="flex items-center p-0.5 bg-neutral-100 dark:bg-neutral-900 rounded-lg border border-neutral-200/80 dark:border-neutral-800 text-[11px] font-semibold">
              <button
                onClick={() => setLanguage("en")}
                className={`px-2 py-1 rounded-md transition ${
                  language === "en"
                    ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs"
                    : "text-neutral-500 hover:text-neutral-800 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title="English"
              >
                EN
              </button>
              <button
                onClick={() => setLanguage("vi")}
                className={`px-2 py-1 rounded-md transition ${
                  language === "vi"
                    ? "bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 shadow-xs"
                    : "text-neutral-500 hover:text-neutral-800 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title="Tiếng Việt"
              >
                VI
              </button>
            </div>

            {/* Dark / Light Toggle */}
            <div className="flex items-center p-0.5 bg-neutral-100 dark:bg-neutral-900 rounded-lg border border-neutral-200/80 dark:border-neutral-800">
              <button
                onClick={() => setTheme("dark")}
                className={`p-1.5 rounded-md transition ${
                  theme === "dark"
                    ? "bg-neutral-800 text-white shadow-xs"
                    : "text-neutral-500 hover:text-neutral-800 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title={t("theme_dark")}
                aria-label={t("theme_dark")}
              >
                <Moon className="h-3.5 w-3.5" />
              </button>
              <button
                onClick={() => setTheme("light")}
                className={`p-1.5 rounded-md transition ${
                  theme === "light"
                    ? "bg-white text-neutral-900 shadow-xs"
                    : "text-neutral-500 hover:text-neutral-800 dark:text-neutral-400 dark:hover:text-neutral-200"
                }`}
                title={t("theme_light")}
                aria-label={t("theme_light")}
              >
                <Sun className="h-3.5 w-3.5" />
              </button>
            </div>

            <button
              onClick={() => setShowInfo(true)}
              className="p-1.5 text-neutral-500 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100 rounded-md transition"
              title={t("info_btn_title")}
            >
              <HelpCircle className="h-4 w-4" />
            </button>
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
              {t("modal_title")}
            </h3>
            <p className="text-xs text-neutral-600 dark:text-neutral-400 leading-relaxed mb-4">
              {t("modal_subtitle")}
            </p>

            <div className="space-y-3 text-xs text-neutral-700 dark:text-neutral-300">
              <div className="p-3.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/60 border border-neutral-200/80 dark:border-neutral-800">
                <span className="font-semibold text-neutral-900 dark:text-neutral-100 block mb-1">
                  {t("modal_step1_title")}
                </span>
                {t("modal_step1_desc")}
              </div>

              <div className="p-3.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/60 border border-neutral-200/80 dark:border-neutral-800">
                <span className="font-semibold text-neutral-900 dark:text-neutral-100 block mb-1">
                  {t("modal_step2_title")}
                </span>
                {t("modal_step2_desc")}
              </div>
            </div>

            <div className="mt-5 pt-3 border-t border-neutral-200 dark:border-neutral-800 flex justify-end">
              <button
                onClick={() => setShowInfo(false)}
                className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95"
              >
                {t("close")}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
