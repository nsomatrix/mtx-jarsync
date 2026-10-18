"use client";

import React, { useState, useRef } from "react";
import { JarProcessor, SplitResult } from "../../lib/engine/JarProcessor";
import {
  Scissors,
  Download,
  AlertCircle,
  Package,
  Archive,
  Loader2,
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
      setErrorMessage("Please select a valid .jar file.");
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
    a.download = `${sourceFile?.name.replace(/\.[^/.]+$/, "")}_extracted_jars.zip`;
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
          className={`border border-dashed rounded-xl p-8 sm:p-14 text-center cursor-pointer transition-all duration-200 ${
            isDragging
              ? "border-neutral-500 bg-neutral-100/80 dark:bg-neutral-900"
              : "border-neutral-300 dark:border-neutral-800 hover:border-neutral-400 dark:hover:border-neutral-700 bg-white/70 dark:bg-neutral-950/50"
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
            Select a multi-game or replicated JAR to split
          </h3>
          <p className="text-xs text-neutral-600 dark:text-neutral-400 mt-1 max-w-sm mx-auto leading-normal">
            Extracts embedded instances, cleans package prefixes, and yields independent standalone JARs.
          </p>
          <button
            type="button"
            className="mt-4 px-4 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95 shadow-xs"
          >
            Browse File
          </button>
        </div>
      )}

      {/* Progress state */}
      {isProcessing && (
        <div className="surface-card p-6 rounded-xl text-center space-y-3 animate-in fade-in duration-200">
          <Loader2 className="h-5 w-5 text-neutral-700 dark:text-neutral-300 animate-spin mx-auto" />
          <div>
            <span className="text-xs font-semibold text-neutral-900 dark:text-neutral-100 block">
              {statusMessage || "Splitting games..."}
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
            <span className="font-semibold block text-red-950 dark:text-red-200">Split Error</span>
            <p className="opacity-90">{errorMessage}</p>
            <button
              onClick={resetAll}
              className="mt-2 text-[11px] font-semibold underline text-neutral-700 dark:text-neutral-300"
            >
              Choose another JAR
            </button>
          </div>
        </div>
      )}

      {/* Results View */}
      {splitResults.length > 0 && (
        <div className="space-y-4 animate-in fade-in duration-200">
          <div className="surface-card p-4 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <span className="text-[11px] font-mono text-neutral-500 dark:text-neutral-400 uppercase tracking-wider block">
                {sourceFile?.name} ({(sourceFile?.size ? sourceFile.size / 1024 : 0).toFixed(1)} KB)
              </span>
              <h4 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100 mt-0.5">
                Extracted {splitResults.length} Standalone J2ME Archives
              </h4>
            </div>

            <div className="flex items-center space-x-2 w-full sm:w-auto">
              <button
                onClick={resetAll}
                className="px-3 py-1.5 rounded-lg text-xs font-medium text-neutral-600 dark:text-neutral-400 hover:text-neutral-900 dark:hover:text-white transition hover:bg-neutral-100 dark:hover:bg-neutral-900"
              >
                Split Another
              </button>
              <button
                onClick={handleDownloadAllZip}
                className="flex-1 sm:flex-none px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 flex items-center justify-center space-x-1.5 transition active:scale-95 shadow-xs"
              >
                <Archive className="h-3.5 w-3.5" />
                <span>Download All (.ZIP)</span>
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {splitResults.map((item, idx) => (
              <div
                key={idx}
                className="surface-card p-3.5 rounded-xl flex flex-col justify-between space-y-3"
              >
                <div className="flex items-center space-x-2.5 min-w-0">
                  <div className="h-8 w-8 shrink-0 rounded-lg bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center text-neutral-600 dark:text-neutral-400">
                    <Package className="h-4 w-4" />
                  </div>
                  <div className="min-w-0">
                    <h5 className="text-xs font-semibold text-neutral-900 dark:text-neutral-100 truncate">
                      {item.metadata.name}
                    </h5>
                    <span className="text-[11px] text-neutral-500 dark:text-neutral-400 font-mono">
                      {(item.blob.size / 1024).toFixed(1)} KB
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => handleDownloadSingle(item)}
                  className="w-full py-1.5 px-2.5 rounded-lg text-xs font-medium bg-neutral-100 dark:bg-neutral-900 hover:bg-neutral-200/80 dark:hover:bg-neutral-800 text-neutral-800 dark:text-neutral-200 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center space-x-1.5 transition active:scale-95"
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
