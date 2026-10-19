"use client";

import React, { useState, useRef } from "react";
import { JarMetadata, JarProcessor, SplitResult } from "../../lib/engine/JarProcessor";
import { useLanguage } from "../../lib/i18n/LanguageContext";
import {
  Scissors,
  Download,
  AlertCircle,
  Package,
  Loader2,
  CheckSquare,
  Square,
  Layers,
  Zap,
  RotateCcw,
  RefreshCw,
  Upload,
  Check,
  Settings2,
} from "lucide-react";

export function SplitStudio() {
  const { language, t } = useLanguage();
  const [sourceFile, setSourceFile] = useState<File | null>(null);
  const [metadata, setMetadata] = useState<JarMetadata | null>(null);
  const [customIcon, setCustomIcon] = useState<{ blob: Blob; url: string; bytes: Uint8Array } | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isMerging, setIsMerging] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");
  const [splitResults, setSplitResults] = useState<SplitResult[]>([]);
  const [selectedIndices, setSelectedIndices] = useState<number[]>([]);
  const [instanceNames, setInstanceNames] = useState<Record<number, string>>({});
  const [mergeOptions, setMergeOptions] = useState({
    appName: "",
    displayName: "",
    author: "",
  });
  const [mergedBlob, setMergedBlob] = useState<Blob | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const iconInputRef = useRef<HTMLInputElement>(null);

  const handleCustomIcon = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    if (!file.type.startsWith("image/")) {
      setErrorMessage(t("err_select_image"));
      return;
    }

    try {
      const bytes = new Uint8Array(await file.arrayBuffer());
      const url = URL.createObjectURL(file);
      setCustomIcon({ blob: file, url, bytes });
      if (mergedBlob) setMergedBlob(null);
    } catch {
      setErrorMessage(t("err_read_image"));
    }
  };

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
    setMergedBlob(null);
    setCustomIcon(null);
    setIsProcessing(true);
    setProgress(0);

    try {
      const { metadata: meta } = await JarProcessor.inspectJar(file);
      setMetadata(meta);

      const isViet = language === "vi";
      const defaultBase = meta.name || file.name.replace(/\.[^/.]+$/, "");
      setMergeOptions({
        appName: isViet ? `${defaultBase} Ghép` : `${defaultBase} Merged`,
        displayName: isViet ? `${defaultBase} Ghép` : `${defaultBase} Merged`,
        author: meta.vendor || "MTX JarSync",
      });

      const results = await JarProcessor.splitJar(file, (pct, status) => {
        setProgress(pct);
        setStatusMessage(status);
      }, { language });
      setSplitResults(results);
      setSelectedIndices(results.map((_, i) => i));

      const initialNames: Record<number, string> = {};
      results.forEach((r, idx) => {
        initialNames[idx] = r.metadata.name || `${defaultBase} ${idx + 1}`;
      });
      setInstanceNames(initialNames);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(t("err_split_failed", { msg }));
    } finally {
      setIsProcessing(false);
    }
  };

  const resetDefaults = () => {
    if (!sourceFile || !metadata) return;
    const isViet = language === "vi";
    const defaultBase = metadata.name || sourceFile.name.replace(/\.[^/.]+$/, "");
    setMergeOptions({
      appName: isViet ? `${defaultBase} Ghép` : `${defaultBase} Merged`,
      displayName: isViet ? `${defaultBase} Ghép` : `${defaultBase} Merged`,
      author: metadata.vendor || "MTX JarSync",
    });
    const initialNames: Record<number, string> = {};
    splitResults.forEach((r, idx) => {
      initialNames[idx] = r.metadata.name || `${defaultBase} ${idx + 1}`;
    });
    setInstanceNames(initialNames);
    setCustomIcon(null);
    setMergedBlob(null);
  };

  const handleDownloadSingle = (res: SplitResult) => {
    const jarBlob = new Blob([res.blob], { type: "application/java-archive" });
    const url = URL.createObjectURL(jarBlob);
    const a = document.createElement("a");
    a.href = url;
    const cleanName = res.fileName.endsWith(".jar") ? res.fileName : `${res.fileName}.jar`;
    a.download = cleanName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };


  const handleToggleSelect = (idx: number) => {
    setSelectedIndices((prev) =>
      prev.includes(idx) ? prev.filter((i) => i !== idx) : [...prev, idx]
    );
    if (mergedBlob) setMergedBlob(null);
  };

  const handleSelectAll = () => {
    if (selectedIndices.length === splitResults.length) {
      setSelectedIndices([]);
    } else {
      setSelectedIndices(splitResults.map((_, i) => i));
    }
    if (mergedBlob) setMergedBlob(null);
  };

  const handleMergeSelected = async () => {
    if (!sourceFile || selectedIndices.length === 0) return;
    setIsMerging(true);
    setErrorMessage(null);
    setMergedBlob(null);
    try {
      const selectedNames = selectedIndices.map(
        (idx) => instanceNames[idx] || splitResults[idx]?.metadata.name || `Game ${idx + 1}`
      );

      const defaultBase = metadata?.name || sourceFile.name.replace(/\.[^/.]+$/, "");
      const finalAppName = mergeOptions.appName.trim() || `${defaultBase} Merged`;

      const blob = await JarProcessor.mergeSubApps(
        sourceFile,
        selectedIndices,
        {
          appName: finalAppName,
          displayName: mergeOptions.displayName.trim() || finalAppName,
          author: mergeOptions.author.trim() || metadata?.vendor || "MTX JarSync",
          language,
          instanceNames: selectedNames,
          customIcon: customIcon?.bytes,
        },
        (pct, status) => {
          setProgress(pct);
          setStatusMessage(status);
        }
      );
      setMergedBlob(blob);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(t("err_merge_failed", { msg }));
    } finally {
      setIsMerging(false);
    }
  };

  const handleDownloadMerged = () => {
    if (!mergedBlob) return;
    const jarBlob = new Blob([mergedBlob], { type: "application/java-archive" });
    const url = URL.createObjectURL(jarBlob);
    const a = document.createElement("a");
    a.href = url;
    const baseName = mergeOptions.appName.trim() || "merged_package";
    const cleanName = baseName.replace(/[^a-zA-Z0-9_\-\u00C0-\u024F\u1EA0-\u1EF9]/g, "_");
    a.download = cleanName.endsWith(".jar") ? cleanName : `${cleanName}.jar`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const resetAll = () => {
    setSourceFile(null);
    setMetadata(null);
    setCustomIcon(null);
    setMergedBlob(null);
    setSplitResults([]);
    setSelectedIndices([]);
    setInstanceNames({});
    setErrorMessage(null);
    setIsProcessing(false);
    setIsMerging(false);
    setProgress(0);
    setStatusMessage("");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    if (iconInputRef.current) {
      iconInputRef.current.value = "";
    }
  };

  return (
    <div className="flex-1 flex flex-col space-y-6">
      {/* Hidden custom icon input */}
      <input
        type="file"
        ref={iconInputRef}
        accept="image/png,image/jpeg,image/webp,image/gif"
        className="hidden"
        onChange={(e) => handleCustomIcon(e.target.files)}
      />

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
          className={`flex-1 min-h-[300px] sm:min-h-[380px] flex flex-col items-center justify-center border border-dashed rounded-2xl p-6 sm:p-14 text-center cursor-pointer transition-all duration-200 ${
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
          <div className="flex flex-col items-center justify-center max-w-md mx-auto my-auto py-2">
            <div className="w-13 h-13 sm:w-14 sm:h-14 rounded-2xl bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 text-neutral-800 dark:text-neutral-200 flex items-center justify-center mb-4 shadow-2xs">
              <Scissors className="h-6 w-6" />
            </div>
            <h3 className="text-sm sm:text-base font-semibold text-neutral-900 dark:text-neutral-100">
              {t("split_drop_title")}
            </h3>
            <p className="text-xs sm:text-sm text-neutral-600 dark:text-neutral-400 mt-1.5 max-w-sm mx-auto leading-relaxed">
              {t("split_drop_subtitle")}
            </p>
            <button
              type="button"
              className="mt-5 px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95 shadow-xs"
            >
              {t("browse_file")}
            </button>
          </div>
        </div>
      )}

      {/* Processing State */}
      {isProcessing && (
        <div className="surface-card p-6 rounded-xl text-center space-y-3 animate-in fade-in duration-200">
          <Loader2 className="h-5 w-5 text-neutral-700 dark:text-neutral-300 animate-spin mx-auto" />
          <div>
            <span className="text-xs font-semibold text-neutral-900 dark:text-neutral-100 block">
              {statusMessage || t("status_splitting")}
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
              className="mt-2 text-[11px] font-semibold underline text-neutral-700 dark:text-neutral-300 flex items-center space-x-1"
            >
              <RotateCcw className="h-3 w-3 inline" />
              <span>{t("error_choose_another")}</span>
            </button>
          </div>
        </div>
      )}

      {/* Loaded View with Package Inspection & Results */}
      {sourceFile && metadata && !isProcessing && (
        <div className="space-y-4 animate-in fade-in duration-200">
          {/* 1. Package Inspection Card (Unified with Replicate) */}
          <div className="surface-card p-3.5 sm:p-4 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div className="flex items-center space-x-3 min-w-0 w-full sm:w-auto">
              <div
                onClick={() => iconInputRef.current?.click()}
                className="relative group cursor-pointer shrink-0"
                title={t("custom_icon_desc")}
              >
                <div className="h-10 w-10 sm:h-11 sm:w-11 rounded-lg bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center overflow-hidden p-1 transition group-hover:border-neutral-400 dark:group-hover:border-neutral-600">
                  {customIcon ? (
                    /* eslint-disable-next-line @next/next/no-img-element */
                    <img
                      src={customIcon.url}
                      alt="Custom Icon"
                      className="h-full w-full object-contain"
                    />
                  ) : metadata.iconDataUrl ? (
                    /* eslint-disable-next-line @next/next/no-img-element */
                    <img
                      src={metadata.iconDataUrl}
                      alt={metadata.name}
                      className="h-full w-full object-contain"
                    />
                  ) : (
                    <Package className="h-5 w-5 text-neutral-400" />
                  )}
                </div>
                <div className="absolute inset-0 bg-black/50 rounded-lg opacity-0 group-hover:opacity-100 flex items-center justify-center transition text-white">
                  <Upload className="h-4 w-4" />
                </div>
              </div>

              <div className="min-w-0 flex-1">
                <div className="flex items-center space-x-2">
                  <h4 className="text-xs sm:text-sm font-semibold text-neutral-900 dark:text-neutral-100 truncate">
                    {metadata.name}
                  </h4>
                  {customIcon && (
                    <span className="text-[10px] font-mono px-1.5 py-0.5 rounded-sm bg-neutral-100 dark:bg-neutral-900 text-neutral-600 dark:text-neutral-300 border border-neutral-200/80 dark:border-neutral-800 shrink-0">
                      {language === "vi" ? "Icon tùy chỉnh" : "Custom Icon"}
                    </span>
                  )}
                </div>
                <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-neutral-500 dark:text-neutral-400 mt-0.5">
                  <span className="truncate">
                    {metadata.vendor} • {(metadata.fileSize / 1024).toFixed(1)} KB • {splitResults.length} {t("instances")}
                  </span>
                  <span>•</span>
                  {customIcon ? (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setCustomIcon(null);
                        if (mergedBlob) setMergedBlob(null);
                      }}
                      className="text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200 underline text-[11px]"
                    >
                      {t("remove_icon")}
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        iconInputRef.current?.click();
                      }}
                      className="text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200 underline text-[11px]"
                    >
                      {t("upload_icon")}
                    </button>
                  )}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-1.5 self-end sm:self-auto shrink-0">
              <button
                type="button"
                onClick={resetDefaults}
                className="inline-flex items-center gap-1 text-xs font-medium text-neutral-500 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100 transition px-2 py-1 rounded-md hover:bg-neutral-100 dark:hover:bg-neutral-900"
                title={t("btn_reset_defaults")}
              >
                <RotateCcw className="h-3 w-3 shrink-0" />
                <span>{t("btn_reset_defaults")}</span>
              </button>
              <div className="h-3 w-px bg-neutral-200 dark:bg-neutral-800" />
              <button
                type="button"
                onClick={resetAll}
                className="text-xs font-medium text-neutral-500 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100 transition px-2 py-1 rounded-md hover:bg-neutral-100 dark:hover:bg-neutral-900 shrink-0"
              >
                {t("change_file")}
              </button>
            </div>
          </div>

          {/* 2. Merge Configuration Card (When 2 or more sub-apps are selected) */}
          {selectedIndices.length >= 2 && (
            <div className="surface-card p-4 sm:p-5 rounded-xl space-y-4 border border-neutral-300 dark:border-neutral-800">
              <div className="flex items-center justify-between border-b border-neutral-200/80 dark:border-neutral-800 pb-2.5">
                <div className="flex items-center space-x-2">
                  <Settings2 className="h-4 w-4 text-neutral-700 dark:text-neutral-300" />
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-neutral-800 dark:text-neutral-200">
                    {t("merge_config_title")}
                  </h4>
                </div>
                <span className="text-[11px] font-mono px-2 py-0.5 rounded-md bg-neutral-100 dark:bg-neutral-900 text-neutral-600 dark:text-neutral-400 border border-neutral-200/80 dark:border-neutral-800">
                  {selectedIndices.length} {t("instances")}
                </span>
              </div>

              {/* Package and Menu Names */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="text-[11px] font-medium text-neutral-600 dark:text-neutral-400 block mb-1">
                    {t("app_name_label")}
                  </label>
                  <input
                    type="text"
                    value={mergeOptions.appName}
                    onChange={(e) => {
                      setMergeOptions({ ...mergeOptions, appName: e.target.value });
                      if (mergedBlob) setMergedBlob(null);
                    }}
                    placeholder="Merged Game Suite"
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400"
                  />
                </div>
                <div>
                  <label className="text-[11px] font-medium text-neutral-600 dark:text-neutral-400 block mb-1">
                    {t("display_name_label")}
                  </label>
                  <input
                    type="text"
                    value={mergeOptions.displayName}
                    onChange={(e) => {
                      setMergeOptions({ ...mergeOptions, displayName: e.target.value });
                      if (mergedBlob) setMergedBlob(null);
                    }}
                    placeholder="Switcher Menu Name"
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400"
                  />
                </div>
                <div>
                  <label className="text-[11px] font-medium text-neutral-600 dark:text-neutral-400 block mb-1">
                    {t("author_label")}
                  </label>
                  <input
                    type="text"
                    value={mergeOptions.author}
                    onChange={(e) => {
                      setMergeOptions({ ...mergeOptions, author: e.target.value });
                      if (mergedBlob) setMergedBlob(null);
                    }}
                    placeholder="Author / Vendor"
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400"
                  />
                </div>
              </div>

              {/* Custom Instance Names for each selected sub-app */}
              <div className="space-y-2 pt-1 border-t border-neutral-200/60 dark:border-neutral-800/80">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300">
                    {t("merge_custom_names_label")}
                  </label>
                  <span className="text-[10px] text-neutral-400 dark:text-neutral-500">
                    {language === "vi" ? "Tên hiển thị trong game khi chuyển đổi" : "Names shown in switcher menu"}
                  </span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 max-h-56 overflow-y-auto pr-1">
                  {selectedIndices.map((idx, order) => {
                    const item = splitResults[idx];
                    return (
                      <div
                        key={idx}
                        className="flex items-center space-x-2 p-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/60 border border-neutral-200/80 dark:border-neutral-800"
                      >
                        <span className="text-[11px] font-mono font-bold text-neutral-500 w-6 text-center shrink-0">
                          #{order + 1}
                        </span>
                        <input
                          type="text"
                          value={instanceNames[idx] || ""}
                          onChange={(e) => {
                            const val = e.target.value;
                            setInstanceNames((prev) => ({ ...prev, [idx]: val }));
                            if (mergedBlob) setMergedBlob(null);
                          }}
                          placeholder={item?.metadata.name || `Sub-app ${order + 1}`}
                          className="flex-1 text-xs px-2 py-1 rounded-md bg-white dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400"
                        />
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Merge Action / Progress / Completed Block */}
              <div className="pt-2 border-t border-neutral-200/60 dark:border-neutral-800/80">
                {isMerging && (
                  <div className="space-y-2 mb-3">
                    <div className="flex justify-between text-xs font-mono">
                      <span className="text-neutral-600 dark:text-neutral-400 truncate mr-2">
                        {statusMessage || t("status_merging")}
                      </span>
                      <span className="font-bold text-neutral-900 dark:text-neutral-100">{progress}%</span>
                    </div>
                    <div className="w-full h-1.5 rounded-full bg-neutral-200 dark:bg-neutral-800 overflow-hidden">
                      <div
                        className="h-full bg-neutral-900 dark:bg-neutral-100 transition-all duration-200"
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                  </div>
                )}

                {!mergedBlob ? (
                  <button
                    disabled={isMerging}
                    onClick={handleMergeSelected}
                    className={`w-full py-2.5 px-3 rounded-lg font-semibold text-xs flex items-center justify-center space-x-2 transition ${
                      !isMerging
                        ? "bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 active:scale-[0.99] shadow-xs"
                        : "bg-neutral-200 dark:bg-neutral-800 text-neutral-400 dark:text-neutral-500 cursor-not-allowed"
                    }`}
                  >
                    {isMerging ? (
                      <Loader2 className="h-3.5 w-3.5 animate-spin shrink-0" />
                    ) : (
                      <Layers className="h-3.5 w-3.5 shrink-0" />
                    )}
                    <span>
                      {isMerging ? t("status_merging") : `${t("btn_start_merge")} (${selectedIndices.length})`}
                    </span>
                  </button>
                ) : (
                  <div className="space-y-2.5">
                    <button
                      onClick={handleDownloadMerged}
                      className="w-full py-2.5 px-3 sm:px-4 rounded-lg font-semibold text-xs bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 active:scale-[0.99] flex items-center justify-center space-x-2 transition shadow-xs"
                    >
                      <Download className="h-3.5 w-3.5 shrink-0" />
                      <span className="truncate">{t("btn_download_merged")} ({(mergedBlob.size / 1024).toFixed(1)} KB)</span>
                    </button>

                    <div className="grid grid-cols-2 gap-2">
                      <button
                        type="button"
                        onClick={() => setMergedBlob(null)}
                        className="w-full py-2 px-2.5 rounded-lg text-xs font-medium border border-neutral-200/80 dark:border-neutral-800 text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-900 flex items-center justify-center space-x-1.5 transition active:scale-95 shadow-2xs"
                        title={t("btn_reconfigure")}
                      >
                        <RotateCcw className="h-3.5 w-3.5 shrink-0" />
                        <span className="truncate">{t("btn_reconfigure")}</span>
                      </button>
                      <button
                        type="button"
                        onClick={resetAll}
                        className="w-full py-2 px-2.5 rounded-lg text-xs font-medium border border-neutral-200/80 dark:border-neutral-800 text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-900 flex items-center justify-center space-x-1.5 transition active:scale-95 shadow-2xs"
                        title={t("split_another")}
                      >
                        <RefreshCw className="h-3.5 w-3.5 shrink-0" />
                        <span className="truncate">{t("split_another")}</span>
                      </button>
                    </div>

                    <div className="flex items-center justify-center space-x-1.5 text-xs text-neutral-600 dark:text-neutral-400 pt-0.5">
                      <Check className="h-3.5 w-3.5 shrink-0 text-emerald-600 dark:text-emerald-400" />
                      <span className="truncate">{t("merge_result_title")}</span>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 3. Quick Selection Bar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs px-1 text-neutral-500 dark:text-neutral-400 pt-1">
            <div className="flex items-center gap-3">
              <button
                onClick={handleSelectAll}
                className="flex items-center space-x-1.5 hover:text-neutral-900 dark:hover:text-white font-medium transition py-0.5"
              >
                {selectedIndices.length === splitResults.length ? (
                  <CheckSquare className="h-3.5 w-3.5 text-neutral-900 dark:text-neutral-100 shrink-0" />
                ) : (
                  <Square className="h-3.5 w-3.5 shrink-0" />
                )}
                <span>
                  {selectedIndices.length === splitResults.length ? t("deselect_all") : t("select_all")}{" "}
                  {t("selected_count", { selected: selectedIndices.length, total: splitResults.length })}
                </span>
              </button>

            </div>
            <span className="text-[11px] font-mono text-neutral-400 dark:text-neutral-500">
              {t("direct_launch_hint")}
            </span>
          </div>

          {/* 4. Sub-Applications Grid */}
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
                          {instanceNames[idx] || item.metadata.name}
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
