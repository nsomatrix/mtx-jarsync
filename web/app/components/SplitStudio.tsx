"use client";

import React, { useState, useRef } from "react";
import { JarProcessor, SplitResult } from "../../lib/engine/JarProcessor";
import {
  Upload,
  Scissors,
  Download,
  AlertCircle,
  Package,
  Layers,
  Archive,
  RefreshCw,
} from "lucide-react";
import JSZip from "jszip";

export function SplitStudio() {
  const [sourceFile, setSourceFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");
  const [splitResults, setSplitResults] = useState<SplitResult[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    if (!file.name.toLowerCase().endsWith(".jar")) {
      setErrorMessage("Please select a valid combined .jar file.");
      return;
    }

    setSourceFile(file);
    setSplitResults([]);
    setErrorMessage(null);
    setIsProcessing(true);
    setProgress(0);

    try {
      const results = await JarProcessor.splitJar(file, (pct, status) => {
        setProgress(pct);
        setStatusMessage(status);
      });
      setSplitResults(results);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(`Split failed: ${msg}`);
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
    a.download = `${sourceFile?.name.replace(/\.[^/.]+$/, "")}_extracted_games.zip`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const resetAll = () => {
    setSourceFile(null);
    setSplitResults([]);
    setErrorMessage(null);
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Hero Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 text-white p-6 sm:p-10 shadow-xl border border-indigo-500/20">
        <div className="relative z-10 max-w-3xl">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-cyan-500/20 text-cyan-300 text-xs font-semibold mb-4 border border-cyan-400/30">
            <Scissors className="h-3.5 w-3.5" />
            <span>Deconstruction & Extraction</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-extrabold tracking-tight">
            Split Combined JARs to Standalone Games
          </h1>
          <p className="mt-2 text-sm sm:text-base text-slate-300 leading-relaxed">
            Upload an existing multi-game J2ME JAR (such as ones built with MTX JarSync or Quyet tools).
            Our parser extracts sub-game metadata, cleans class namespace prefixes, and builds standalone playable JARs.
          </p>
        </div>
      </div>

      {/* Main Content Area */}
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
          className={`border-2 border-dashed rounded-3xl p-12 sm:p-16 text-center cursor-pointer transition-all duration-200 ${
            isDragging
              ? "border-cyan-500 bg-cyan-50/50 dark:bg-cyan-950/30 scale-[0.99]"
              : "border-slate-300 dark:border-slate-800 hover:border-cyan-400 dark:hover:border-cyan-600 bg-white/50 dark:bg-slate-900/40"
          }`}
        >
          <input
            type="file"
            ref={fileInputRef}
            accept=".jar"
            className="hidden"
            onChange={(e) => handleFile(e.target.files)}
          />
          <div className="mx-auto w-16 h-16 rounded-2xl bg-cyan-100 dark:bg-cyan-950/80 text-cyan-600 dark:text-cyan-400 flex items-center justify-center mb-4 shadow-sm">
            <Scissors className="h-8 w-8" />
          </div>
          <h3 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white">
            Select a Combined Multi-MIDlet JAR
          </h3>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-2 max-w-md mx-auto">
            Drag and drop or browse for a combined archive (e.g. <code className="text-indigo-600 dark:text-cyan-400">nsx-3.jar</code>).
            We will deconstruct each title into individual standalone JARs.
          </p>
          <button
            type="button"
            className="mt-6 px-5 py-2.5 rounded-xl text-xs font-semibold bg-cyan-600 hover:bg-cyan-500 text-white transition shadow-sm"
          >
            Browse JAR File
          </button>
        </div>
      )}

      {/* Processing Indicator */}
      {isProcessing && (
        <div className="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-center space-y-4">
          <RefreshCw className="h-8 w-8 text-cyan-500 animate-spin mx-auto" />
          <div>
            <h4 className="font-bold text-slate-900 dark:text-white text-base">
              {statusMessage || "Splitting games..."}
            </h4>
            <p className="text-xs text-slate-500 mt-1">{progress}% Completed</p>
          </div>
          <div className="w-full max-w-md mx-auto h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-cyan-500 to-indigo-500 transition-all duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      )}

      {/* Error Message */}
      {errorMessage && (
        <div className="p-4 rounded-2xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900/60 text-red-700 dark:text-red-300 flex items-start space-x-3 text-sm">
          <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Split Failed</p>
            <p className="text-xs opacity-90">{errorMessage}</p>
            <button
              onClick={resetAll}
              className="mt-3 px-3 py-1 text-xs font-semibold rounded-lg bg-red-100 dark:bg-red-900/80 text-red-800 dark:text-red-200 hover:bg-red-200 transition"
            >
              Try Another File
            </button>
          </div>
        </div>
      )}

      {/* Results View */}
      {splitResults.length > 0 && (
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
            <div>
              <span className="text-xs font-bold text-cyan-600 dark:text-cyan-400 uppercase tracking-wider">
                Deconstruction Complete
              </span>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                Extracted {splitResults.length} Standalone Games
              </h3>
              <p className="text-xs text-slate-500">
                Source: {sourceFile?.name} ({(sourceFile?.size ? sourceFile.size / 1024 : 0).toFixed(1)} KB)
              </p>
            </div>

            <div className="flex items-center space-x-3 w-full sm:w-auto">
              <button
                onClick={resetAll}
                className="px-4 py-2.5 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 transition"
              >
                Split Another
              </button>
              <button
                onClick={handleDownloadAllZip}
                className="flex-1 sm:flex-none px-5 py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 text-white shadow-md shadow-cyan-500/20 flex items-center justify-center space-x-2 transition"
              >
                <Archive className="h-4 w-4" />
                <span>Download All (.ZIP)</span>
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {splitResults.map((item, idx) => (
              <div
                key={idx}
                className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between hover:border-cyan-300 dark:hover:border-cyan-700 transition"
              >
                <div className="flex items-start space-x-3 mb-4">
                  <div className="h-10 w-10 shrink-0 rounded-xl bg-cyan-100/70 dark:bg-cyan-950/60 border border-cyan-200 dark:border-cyan-800/60 flex items-center justify-center text-cyan-600 dark:text-cyan-400">
                    <Package className="h-5 w-5" />
                  </div>
                  <div className="min-w-0">
                    <h4 className="font-bold text-sm text-slate-900 dark:text-white truncate">
                      {item.metadata.name}
                    </h4>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      {(item.blob.size / 1024).toFixed(1)} KB • Standalone JAR
                    </p>
                  </div>
                </div>

                <button
                  onClick={() => handleDownloadSingle(item)}
                  className="w-full py-2 px-3 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 flex items-center justify-center space-x-2 transition"
                >
                  <Download className="h-3.5 w-3.5" />
                  <span>Download .JAR</span>
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
