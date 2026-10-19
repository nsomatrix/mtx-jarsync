"use client";

import React, { useState, useRef } from "react";
import { JarProcessor, SplitResult } from "../../lib/engine/JarProcessor";
import { useLanguage } from "../../lib/i18n/LanguageContext";
import {
  Scissors,
  Download,
  AlertCircle,
  Package,
  Archive,
  Loader2,
  CheckSquare,
  Square,
  Layers,
  Zap,
} from "lucide-react";
import JSZip from "jszip";

export function SplitStudio() {
  const { language, t } = useLanguage();
  const [sourceFile, setSourceFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isMerging, setIsMerging] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");
  const [splitResults, setSplitResults] = useState<SplitResult[]>([]);
  const [selectedIndices, setSelectedIndices] = useState<number[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    if (!file.name.toLowerCase().endsWith(".jar")) {
      setErrorMessage(t("err_select_jar"));
      return;
    }

    setSourceFile(file);
    setSplitResults([]);
    setSelectedIndices([]);
    setErrorMessage(null);
    setIsProcessing(true);
    setProgress(0);

    try {
      const results = await JarProcessor.splitJar(file, (pct, status) => {
        setProgress(pct);
        setStatusMessage(status);
      }, { language });
      setSplitResults(results);
      setSelectedIndices(results.map((_, i) => i));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(t("err_split_failed", { msg }));
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDownloadSingle = (res: SplitResult) => {
    const url = URL.createObjectURL(res.blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = res.fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleDownloadAllZip = async () => {
    if (splitResults.length === 0) return;
    const zip = new JSZip();
    for (const res of splitResults) {
      zip.file(res.fileName, res.blob);
    }
    const archiveBlob = await zip.generateAsync({ type: "blob" });
    const url = URL.createObjectURL(archiveBlob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${sourceFile?.name.replace(/\.[^/.]+$/, "")}_extracted_standalone_jars.zip`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleToggleSelect = (idx: number) => {
    setSelectedIndices((prev) =>
      prev.includes(idx) ? prev.filter((i) => i !== idx) : [...prev, idx]
    );
  };

  const handleSelectAll = () => {
    if (selectedIndices.length === splitResults.length) {
      setSelectedIndices([]);
    } else {
      setSelectedIndices(splitResults.map((_, i) => i));
    }
  };

  const handleMergeSelected = async () => {
    if (!sourceFile || selectedIndices.length === 0) return;
    setIsMerging(true);
    setErrorMessage(null);
    try {
      const isViet = language === "vi";
      const baseName = sourceFile.name.replace(/\.[^/.]+$/, "");
      const appName = isViet
        ? `${baseName} (${selectedIndices.length}-trong-1)`
        : `${baseName} (${selectedIndices.length}-in-1)`;

      const mergedBlob = await JarProcessor.mergeSubApps(
        sourceFile,
        selectedIndices,
        {
          appName,
          displayName: appName,
          language,
        },
        (pct, status) => {
          setProgress(pct);
          setStatusMessage(status);
        }
      );
      const url = URL.createObjectURL(mergedBlob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${baseName}_merged_${selectedIndices.length}in1.jar`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(t("err_merge_failed", { msg }));
    } finally {
      setIsMerging(false);
    }
  };

  const resetAll = () => {
    setSourceFile(null);
    setSplitResults([]);
    setSelectedIndices([]);
    setErrorMessage(null);
  };

  return (
    <div className="space-y-6">
      {/* Upload Dropzone */}
      {!sourceFile && (
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setIsDragging(false);
            handleFile(e.dataTransfer.files);
          }}
          onClick={() => fileInputRef.current?.click()}
          className={`border border-dashed rounded-xl p-6 sm:p-14 text-center cursor-pointer transition-all duration-200 ${
            isDragging
              ? "border-neutral-500 bg-neutral-100/80 dark:bg-neutral-900"
              : "border-neutral-300 dark:border-neutral-800 hover:border-neutral-400 dark:hover:border-neutral-700 bg-white dark:bg-neutral-950/50 shadow-xs"
          }`}
        >
          <input
            type="file"
            ref={fileInputRef}
            accept=".jar"
            className="hidden"
            onChange={(e) => handleFile(e.target.files)}
          />
          <div className="mx-auto w-12 h-12 rounded-lg bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 text-neutral-800 dark:text-neutral-200 flex items-center justify-center mb-3">
            <Scissors className="h-5 w-5" />
          </div>
          <h3 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100">
            {t("split_drop_title")}
          </h3>
          <p className="text-xs text-neutral-600 dark:text-neutral-400 mt-1 max-w-sm mx-auto leading-normal">
            {t("split_drop_subtitle")}
          </p>
          <button
            type="button"
            className="mt-4 px-4 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95 shadow-xs"
          >
            {t("browse_file")}
          </button>
        </div>
      )}

      {/* Progress state */}
      {(isProcessing || isMerging) && (
        <div className="surface-card p-6 rounded-xl text-center space-y-3 animate-in fade-in duration-200">
          <Loader2 className="h-5 w-5 text-neutral-700 dark:text-neutral-300 animate-spin mx-auto" />
          <div>
            <span className="text-xs font-semibold text-neutral-900 dark:text-neutral-100 block">
              {statusMessage || (isMerging ? t("status_merging") : t("status_splitting"))}
            </span>
            <span className="text-[11px] text-neutral-500 dark:text-neutral-400 font-mono">{progress}%</span>
          </div>
          <div className="w-full max-w-xs mx-auto h-1.5 rounded-full bg-neutral-200 dark:bg-neutral-800 overflow-hidden">
            <div
              className="h-full bg-neutral-900 dark:bg-neutral-100 transition-all duration-200"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      )}

      {/* Error Message */}
      {errorMessage && (
        <div className="p-3.5 rounded-lg bg-red-50/80 dark:bg-neutral-900 border border-red-200 dark:border-red-900/50 text-red-900 dark:text-red-300 flex items-start space-x-2.5 text-xs animate-in fade-in duration-200">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-red-600 dark:text-red-400" />
          <div className="flex-1">
            <span className="font-semibold block text-red-950 dark:text-red-200">{t("error_prefix")}</span>
            <p className="opacity-90">{errorMessage}</p>
            <button
              onClick={resetAll}
              className="mt-2 text-[11px] font-semibold underline text-neutral-700 dark:text-neutral-300"
            >
              {t("error_choose_another")}
            </button>
          </div>
        </div>
      )}

      {/* Results View */}
      {splitResults.length > 0 && (
        <div className="space-y-4 animate-in fade-in duration-200">
          {/* Action Header Card */}
          <div className="surface-card p-4 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <span className="text-[11px] font-mono text-neutral-500 dark:text-neutral-400 uppercase tracking-wider block">
                {sourceFile?.name} ({(sourceFile?.size ? sourceFile.size / 1024 : 0).toFixed(1)} KB)
              </span>
              <h4 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100 mt-0.5">
                {t("split_extracted_title", { count: splitResults.length })}
              </h4>
              <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-0.5">
                {t("split_extracted_desc")}
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
              <button
                onClick={resetAll}
                className="px-3 py-1.5 rounded-lg text-xs font-medium text-neutral-600 dark:text-neutral-400 hover:text-neutral-900 dark:hover:text-white transition hover:bg-neutral-100 dark:hover:bg-neutral-900 shrink-0"
              >
                {t("split_another")}
              </button>

              {selectedIndices.length >= 2 && (
                <button
                  onClick={handleMergeSelected}
                  disabled={isMerging}
                  className="px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 flex items-center justify-center space-x-1.5 transition active:scale-95 shadow-xs"
                >
                  <Layers className="h-3.5 w-3.5 shrink-0" />
                  <span className="truncate">{t("btn_merge", { count: selectedIndices.length })}</span>
                </button>
              )}

              <button
                onClick={handleDownloadAllZip}
                className="px-3.5 py-1.5 rounded-lg text-xs font-medium text-neutral-700 dark:text-neutral-300 bg-neutral-100 dark:bg-neutral-900 hover:bg-neutral-200/80 dark:hover:bg-neutral-800 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center space-x-1.5 transition active:scale-95 shadow-xs"
              >
                <Archive className="h-3.5 w-3.5 shrink-0" />
                <span className="truncate">{t("btn_download_all_zip")}</span>
              </button>
            </div>
          </div>

          {/* Quick Selection Bar */}
          <div className="flex items-center justify-between text-xs px-1 text-neutral-500 dark:text-neutral-400">
            <button
              onClick={handleSelectAll}
              className="flex items-center space-x-1.5 hover:text-neutral-900 dark:hover:text-white font-medium transition"
            >
              {selectedIndices.length === splitResults.length ? (
                <CheckSquare className="h-3.5 w-3.5 text-neutral-900 dark:text-neutral-100" />
              ) : (
                <Square className="h-3.5 w-3.5" />
              )}
              <span>
                {selectedIndices.length === splitResults.length ? t("deselect_all") : t("select_all")}{" "}
                {t("selected_count", { selected: selectedIndices.length, total: splitResults.length })}
              </span>
            </button>
            <span className="text-[11px] font-mono">
              {t("direct_launch_hint")}
            </span>
          </div>

          {/* Cards Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {splitResults.map((item, idx) => {
              const isSelected = selectedIndices.includes(idx);
              return (
                <div
                  key={idx}
                  className={`surface-card p-3.5 rounded-xl flex flex-col justify-between space-y-3 border transition ${
                    isSelected
                      ? "border-neutral-400 dark:border-neutral-600 bg-neutral-50/50 dark:bg-neutral-900/40"
                      : "border-neutral-200/80 dark:border-neutral-800"
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center space-x-2.5 min-w-0">
                      <div className="h-8 w-8 shrink-0 rounded-lg bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center text-neutral-600 dark:text-neutral-400">
                        <Package className="h-4 w-4" />
                      </div>
                      <div className="min-w-0">
                        <h5 className="text-xs font-semibold text-neutral-900 dark:text-neutral-100 truncate">
                          {item.metadata.name}
                        </h5>
                        <div className="flex items-center space-x-1.5 mt-0.5">
                          <span className="text-[11px] text-neutral-500 dark:text-neutral-400 font-mono">
                            {(item.blob.size / 1024).toFixed(1)} KB
                          </span>
                          <span className="text-[10px] text-neutral-400 dark:text-neutral-600">•</span>
                          <span className="inline-flex items-center space-x-0.5 text-[10px] font-medium text-emerald-700 dark:text-emerald-400">
                            <Zap className="h-2.5 w-2.5 shrink-0" />
                            <span>{t("direct_launch_badge")}</span>
                          </span>
                        </div>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => handleToggleSelect(idx)}
                      className="p-1 text-neutral-400 hover:text-neutral-900 dark:hover:text-neutral-100 transition shrink-0"
                      title={isSelected ? t("tooltip_deselect_merge") : t("tooltip_select_merge")}
                    >
                      {isSelected ? (
                        <CheckSquare className="h-4 w-4 text-neutral-900 dark:text-neutral-100" />
                      ) : (
                        <Square className="h-4 w-4" />
                      )}
                    </button>
                  </div>

                  <button
                    onClick={() => handleDownloadSingle(item)}
                    className="w-full py-1.5 px-2.5 rounded-lg text-xs font-medium bg-neutral-100 dark:bg-neutral-900 hover:bg-neutral-200/80 dark:hover:bg-neutral-800 text-neutral-800 dark:text-neutral-200 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center space-x-1.5 transition active:scale-95"
                  >
                    <Download className="h-3.5 w-3.5" />
                    <span>{t("btn_download_standalone")}</span>
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
