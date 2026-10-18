"use client";

import React, { useState, useRef } from "react";
import { JarMetadata, JarProcessor, MergeOptions } from "../../lib/engine/JarProcessor";
import {
  Upload,
  Layers,
  Settings2,
  Trash2,
  GripVertical,
  CheckCircle2,
  Download,
  AlertCircle,
  Sparkles,
  ArrowRight,
  Package,
} from "lucide-react";

interface LoadedGame {
  id: string;
  file: File;
  metadata: JarMetadata;
}

export function MergeStudio() {
  const [games, setGames] = useState<LoadedGame[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");
  const [mergedBlob, setMergedBlob] = useState<Blob | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Merge Config Options
  const [options, setOptions] = useState<MergeOptions>({
    appName: "MTX Combined Collection",
    displayName: "MTX Multi Midlet",
    author: "MTX JarSync",
    featureScreenshot: false,
    featureList: true,
    featureMainPageList: false,
    featureNoExit: true,
  });

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFiles = async (fileList: FileList | null) => {
    if (!fileList || fileList.length === 0) return;
    setErrorMessage(null);
    setMergedBlob(null);

    const newGames: LoadedGame[] = [];
    for (let i = 0; i < fileList.length; i++) {
      const file = fileList[i];
      if (!file.name.toLowerCase().endsWith(".jar")) {
        continue;
      }
      try {
        const { metadata } = await JarProcessor.inspectJar(file);
        newGames.push({
          id: `${file.name}-${Date.now()}-${i}`,
          file,
          metadata,
        });
      } catch (err: unknown) {
        console.error("Failed to inspect jar", file.name, err);
      }
    }

    if (newGames.length === 0) {
      setErrorMessage("Please select valid J2ME .jar archive files.");
      return;
    }

    setGames((prev) => {
      const combined = [...prev, ...newGames];
      if (prev.length === 0 && combined.length > 0) {
        setOptions((opt) => ({
          ...opt,
          appName: combined[0].metadata.name + " Pack",
          displayName: combined[0].metadata.name + " Multi",
        }));
      }
      return combined;
    });
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    handleFiles(e.dataTransfer.files);
  };

  const removeGame = (index: number) => {
    setGames((prev) => prev.filter((_, i) => i !== index));
    setMergedBlob(null);
  };

  const moveGame = (from: number, to: number) => {
    if (to < 0 || to >= games.length) return;
    setGames((prev) => {
      const copy = [...prev];
      const item = copy.splice(from, 1)[0];
      copy.splice(to, 0, item);
      return copy;
    });
    setMergedBlob(null);
  };

  const handleStartMerge = async () => {
    if (games.length === 0) return;
    setIsProcessing(true);
    setProgress(0);
    setMergedBlob(null);
    setErrorMessage(null);

    try {
      const files = games.map((g) => g.file);
      const blob = await JarProcessor.mergeJars(files, options, (pct, status) => {
        setProgress(pct);
        setStatusMessage(status);
      });
      setMergedBlob(blob);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(`Merge failed: ${msg}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDownload = () => {
    if (!mergedBlob) return;
    const url = URL.createObjectURL(mergedBlob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${options.appName.replace(/[^a-zA-Z0-9_\-]/g, "_")}.jar`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Hero Banner / Instructions */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-900 via-indigo-950 to-slate-950 text-white p-6 sm:p-10 shadow-xl border border-indigo-500/20">
        <div className="absolute -right-16 -top-16 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -left-16 -bottom-16 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 max-w-3xl">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-indigo-500/20 text-cyan-300 text-xs font-semibold mb-4 border border-indigo-400/30">
            <Sparkles className="h-3.5 w-3.5" />
            <span>Zero-Backend Next.js Engine</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-extrabold tracking-tight">
            Merge J2ME Games with Complete Isolation
          </h1>
          <p className="mt-2 text-sm sm:text-base text-slate-300 leading-relaxed">
            Combine multiple Java MIDlet games into a single master JAR. Features automatic
            namespace prefixing, static initializer cloning, and an in-game switcher compatible with MicroEmulator and Nokia devices.
          </p>
        </div>
      </div>

      {/* Main Grid: Upload & Games on Left, Config & Actions on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Column (8 cols): File Dropzone & App List */}
        <div className="lg:col-span-7 xl:col-span-8 space-y-6">
          {/* Dropzone */}
          <div
            onDragOver={(e) => {
              e.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-3xl p-8 sm:p-12 text-center cursor-pointer transition-all duration-200 ${
              isDragging
                ? "border-indigo-500 bg-indigo-50/50 dark:bg-indigo-950/30 scale-[0.99]"
                : "border-slate-300 dark:border-slate-800 hover:border-indigo-400 dark:hover:border-indigo-600 bg-white/50 dark:bg-slate-900/40"
            }`}
          >
            <input
              type="file"
              ref={fileInputRef}
              multiple
              accept=".jar"
              className="hidden"
              onChange={(e) => handleFiles(e.target.files)}
            />
            <div className="mx-auto w-14 h-14 rounded-2xl bg-indigo-100 dark:bg-indigo-950/80 text-indigo-600 dark:text-cyan-400 flex items-center justify-center mb-4 shadow-sm">
              <Upload className="h-7 w-7" />
            </div>
            <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-white">
              Drag & Drop your .JAR games here
            </h3>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1 max-w-sm mx-auto">
              Select 2 to 25+ J2ME .jar applications. All bytecode parsing happens client-side.
            </p>
            <button
              type="button"
              className="mt-4 px-4 py-2 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition"
            >
              Browse Files
            </button>
          </div>

          {/* Error Message */}
          {errorMessage && (
            <div className="p-4 rounded-2xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900/60 text-red-700 dark:text-red-300 flex items-start space-x-3 text-sm">
              <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Operation Error</p>
                <p className="text-xs opacity-90">{errorMessage}</p>
              </div>
            </div>
          )}

          {/* Game List */}
          {games.length > 0 && (
            <div className="space-y-3">
              <div className="flex items-center justify-between px-1">
                <div className="flex items-center space-x-2">
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-600 dark:text-slate-400">
                    Included Games ({games.length})
                  </h3>
                  <span className="text-xs text-slate-400">
                    (Namespace: a, b, c, etc.)
                  </span>
                </div>
                <button
                  onClick={() => {
                    setGames([]);
                    setMergedBlob(null);
                  }}
                  className="text-xs text-red-500 hover:text-red-600 dark:text-red-400 font-medium"
                >
                  Clear All
                </button>
              </div>

              <div className="space-y-2">
                {games.map((game, idx) => {
                  const prefixLetter = idx === 0 ? "root (a)" : String.fromCharCode(97 + idx);
                  return (
                    <div
                      key={game.id}
                      className="group flex items-center justify-between p-3.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800/80 shadow-xs hover:border-indigo-300 dark:hover:border-indigo-700 transition"
                    >
                      <div className="flex items-center space-x-3 min-w-0">
                        {/* Drag Handle & Index */}
                        <div className="flex items-center space-x-1.5 text-slate-400">
                          <GripVertical className="h-4 w-4" />
                          <span className="text-xs font-mono font-bold w-4 text-center">
                            {idx + 1}
                          </span>
                        </div>

                        {/* Icon */}
                        <div className="h-10 w-10 shrink-0 rounded-xl bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 flex items-center justify-center overflow-hidden p-1">
                          {game.metadata.iconDataUrl ? (
                            /* eslint-disable-next-line @next/next/no-img-element */
                            <img
                              src={game.metadata.iconDataUrl}
                              alt={game.metadata.name}
                              className="h-full w-full object-contain"
                            />
                          ) : (
                            <Package className="h-5 w-5 text-slate-400" />
                          )}
                        </div>

                        {/* Metadata Details */}
                        <div className="min-w-0">
                          <div className="flex items-center space-x-2">
                            <h4 className="text-sm font-bold text-slate-900 dark:text-slate-100 truncate">
                              {game.metadata.name}
                            </h4>
                            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 font-semibold">
                              prefix: {prefixLetter}
                            </span>
                          </div>
                          <p className="text-xs text-slate-400 dark:text-slate-500 truncate">
                            {game.metadata.vendor} • {(game.metadata.fileSize / 1024).toFixed(1)} KB
                          </p>
                        </div>
                      </div>

                      {/* Reorder and Delete Actions */}
                      <div className="flex items-center space-x-1 opacity-80 group-hover:opacity-100 transition">
                        <button
                          disabled={idx === 0}
                          onClick={() => moveGame(idx, idx - 1)}
                          className="p-1.5 text-xs text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 disabled:opacity-20"
                          title="Move Up"
                        >
                          ▲
                        </button>
                        <button
                          disabled={idx === games.length - 1}
                          onClick={() => moveGame(idx, idx + 1)}
                          className="p-1.5 text-xs text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 disabled:opacity-20"
                          title="Move Down"
                        >
                          ▼
                        </button>
                        <button
                          onClick={() => removeGame(idx)}
                          className="p-1.5 text-slate-400 hover:text-red-500 dark:hover:text-red-400 transition"
                          title="Remove Game"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        {/* Right Column (4-5 cols): Options & Execution Panel */}
        <div className="lg:col-span-5 xl:col-span-4 space-y-6">
          <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800/80 shadow-sm space-y-5">
            <div className="flex items-center space-x-2 text-slate-900 dark:text-white font-bold text-base pb-3 border-b border-slate-100 dark:border-slate-800">
              <Settings2 className="h-5 w-5 text-indigo-600 dark:text-cyan-400" />
              <span>Packaging Settings</span>
            </div>

            {/* Inputs */}
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                  Collection Name (MIDlet-Name)
                </label>
                <input
                  type="text"
                  value={options.appName}
                  onChange={(e) => setOptions({ ...options, appName: e.target.value })}
                  className="w-full px-3.5 py-2 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                  Display Title (MIDlet-1)
                </label>
                <input
                  type="text"
                  value={options.displayName}
                  onChange={(e) => setOptions({ ...options, displayName: e.target.value })}
                  className="w-full px-3.5 py-2 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                  Vendor / Author
                </label>
                <input
                  type="text"
                  value={options.author}
                  onChange={(e) => setOptions({ ...options, author: e.target.value })}
                  className="w-full px-3.5 py-2 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            </div>

            {/* Toggles */}
            <div className="pt-2 space-y-3">
              <label className="flex items-center justify-between cursor-pointer">
                <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                  Nokia No-Exit Hook
                </span>
                <input
                  type="checkbox"
                  checked={options.featureNoExit}
                  onChange={(e) => setOptions({ ...options, featureNoExit: e.target.checked })}
                  className="h-4 w-4 text-indigo-600 rounded-sm border-slate-300 focus:ring-indigo-500"
                />
              </label>

              <label className="flex items-center justify-between cursor-pointer">
                <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                  Enable In-Game Color List
                </span>
                <input
                  type="checkbox"
                  checked={options.featureList}
                  onChange={(e) => setOptions({ ...options, featureList: e.target.checked })}
                  className="h-4 w-4 text-indigo-600 rounded-sm border-slate-300 focus:ring-indigo-500"
                />
              </label>

              <label className="flex items-center justify-between cursor-pointer">
                <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                  Screenshot Overlay Support
                </span>
                <input
                  type="checkbox"
                  checked={options.featureScreenshot}
                  onChange={(e) => setOptions({ ...options, featureScreenshot: e.target.checked })}
                  className="h-4 w-4 text-indigo-600 rounded-sm border-slate-300 focus:ring-indigo-500"
                />
              </label>
            </div>

            {/* Progress Bar (when active) */}
            {isProcessing && (
              <div className="space-y-2 pt-2">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-indigo-600 dark:text-cyan-400 animate-pulse">
                    {statusMessage || "Processing..."}
                  </span>
                  <span className="text-slate-500">{progress}%</span>
                </div>
                <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-indigo-500 to-cyan-400 transition-all duration-300"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </div>
            )}

            {/* Primary Action Button */}
            {!mergedBlob ? (
              <button
                disabled={games.length < 2 || isProcessing}
                onClick={handleStartMerge}
                className={`w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center space-x-2 transition shadow-lg ${
                  games.length >= 2 && !isProcessing
                    ? "bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white shadow-indigo-500/25 active:scale-[0.98]"
                    : "bg-slate-200 dark:bg-slate-800 text-slate-400 dark:text-slate-600 cursor-not-allowed"
                }`}
              >
                <Layers className="h-4 w-4" />
                <span>
                  {games.length < 2
                    ? "Add at least 2 games"
                    : isProcessing
                    ? "Rewriting Bytecode..."
                    : `Merge ${games.length} Games into 1 JAR`}
                </span>
                {games.length >= 2 && !isProcessing && <ArrowRight className="h-4 w-4" />}
              </button>
            ) : (
              <div className="space-y-2">
                <button
                  onClick={handleDownload}
                  className="w-full py-3.5 px-4 rounded-2xl font-bold text-sm bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white shadow-lg shadow-emerald-500/25 active:scale-[0.98] flex items-center justify-center space-x-2 transition"
                >
                  <Download className="h-4 w-4" />
                  <span>Download Merged JAR ({(mergedBlob.size / 1024).toFixed(1)} KB)</span>
                </button>
                <div className="flex items-center justify-center space-x-1.5 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
                  <CheckCircle2 className="h-4 w-4" />
                  <span>Ready for MicroEmulator & Nokia Phones</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
