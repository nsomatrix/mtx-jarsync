"use client";

import React, { useState, useRef } from "react";
import { JarMetadata, JarProcessor, ReplicateOptions } from "../../lib/engine/JarProcessor";
import {
  Upload,
  Copy,
  Settings2,
  Download,
  AlertCircle,
  Package,
  Check,
  ChevronDown,
  ChevronUp,
  Loader2,
} from "lucide-react";

export function ReplicateStudio() {
  const [sourceFile, setSourceFile] = useState<File | null>(null);
  const [metadata, setMetadata] = useState<JarMetadata | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");
  const [resultBlob, setResultBlob] = useState<Blob | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showAdvanced, setShowAdvanced] = useState(false);

  const [cloneCount, setCloneCount] = useState<number>(3);
  const [cloneNames, setCloneNames] = useState<string[]>([]);
  const [options, setOptions] = useState({
    appName: "",
    displayName: "",
    author: "",
    featureScreenshot: false,
    featureList: true,
    featureMainPageList: false,
    featureNoExit: true,
  });

  const [customIcon, setCustomIcon] = useState<{ url: string; bytes: Uint8Array } | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const iconInputRef = useRef<HTMLInputElement>(null);

  const handleCustomIcon = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    if (!file.type.startsWith("image/") && !file.name.match(/\.(png|jpg|jpeg|gif|webp)$/i)) {
      setErrorMessage("Please select a valid image file (.png, .jpg, .webp).");
      return;
    }
    try {
      const buf = await file.arrayBuffer();
      const url = URL.createObjectURL(file);
      setCustomIcon({ url, bytes: new Uint8Array(buf) });
    } catch {
      setErrorMessage("Failed to read image file.");
    }
  };

  const handleFile = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    if (!file.name.toLowerCase().endsWith(".jar")) {
      setErrorMessage("Please select a valid J2ME .jar file.");
      return;
    }

    setErrorMessage(null);
    setResultBlob(null);
    setIsLoadingFile(true);

    try {
      const { metadata: meta } = await JarProcessor.inspectJar(file);
      setSourceFile(file);
      setMetadata(meta);

      const defaultBase = meta.name || file.name.replace(/\.[^/.]+$/, "");
      setOptions({
        appName: `${defaultBase} Multi`,
        displayName: `${defaultBase} Multi`,
        author: meta.vendor || "MTX JarSync",
        featureScreenshot: false,
        featureList: true,
        featureMainPageList: false,
        featureNoExit: true,
      });

      const initialNames: string[] = [];
      for (let i = 1; i <= 26; i++) {
        initialNames.push(`${defaultBase} ${i}`);
      }
      setCloneNames(initialNames);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(`Failed to read JAR: ${msg}`);
    } finally {
      setIsLoadingFile(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    handleFile(e.dataTransfer.files);
  };

  const updateCloneName = (index: number, val: string) => {
    setCloneNames((prev) => {
      const copy = [...prev];
      copy[index] = val;
      return copy;
    });
  };

  const handleStartReplicate = async () => {
    if (!sourceFile || !metadata) return;
    setIsProcessing(true);
    setProgress(0);
    setResultBlob(null);
    setErrorMessage(null);

    try {
      const replicateOpts: ReplicateOptions = {
        count: cloneCount,
        displayNames: cloneNames.slice(0, cloneCount),
        appName: options.appName,
        displayName: options.displayName,
        author: options.author,
        featureScreenshot: options.featureScreenshot,
        featureList: options.featureList,
        featureMainPageList: options.featureMainPageList,
        featureNoExit: options.featureNoExit,
        customIconBytes: customIcon?.bytes,
      };

      const blob = await JarProcessor.replicateJar(sourceFile, replicateOpts, (pct, status) => {
        setProgress(pct);
        setStatusMessage(status);
      });
      setResultBlob(blob);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(`Replication failed: ${msg}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDownload = () => {
    if (!resultBlob || !options.appName) return;
    const url = URL.createObjectURL(resultBlob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${options.appName.replace(/[^a-zA-Z0-9_\-]/g, "_")}.jar`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const reset = () => {
    setSourceFile(null);
    setMetadata(null);
    setResultBlob(null);
    setErrorMessage(null);
    setCustomIcon(null);
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
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`border border-dashed rounded-xl p-6 sm:p-12 text-center cursor-pointer transition-all duration-200 ${
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
            {isLoadingFile ? (
              <Loader2 className="h-5 w-5 animate-spin text-neutral-500" />
            ) : (
              <Upload className="h-5 w-5" />
            )}
          </div>
          <h3 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100">
            {isLoadingFile ? "Inspecting JAR metadata..." : "Select a J2ME .JAR file to replicate"}
          </h3>
          <p className="text-xs text-neutral-600 dark:text-neutral-400 mt-1 max-w-sm mx-auto leading-normal">
            Drag and drop or browse. We replicate its classes and state into multiple isolated accounts in one JAR.
          </p>
          <button
            type="button"
            className="mt-4 px-4 py-1.5 rounded-lg text-xs font-semibold bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 transition active:scale-95 shadow-xs"
          >
            Browse File
          </button>
        </div>
      )}

      {/* Error Message */}
      {errorMessage && (
        <div className="p-3.5 rounded-lg bg-red-50/80 dark:bg-neutral-900 border border-red-200 dark:border-red-900/50 text-red-900 dark:text-red-300 flex items-start space-x-2.5 text-xs animate-in fade-in duration-200">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-red-600 dark:text-red-400" />
          <div className="flex-1">
            <span className="font-semibold block text-red-950 dark:text-red-200">Error</span>
            <p className="opacity-90">{errorMessage}</p>
          </div>
        </div>
      )}

      {/* Loaded App Configuration */}
      {sourceFile && metadata && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start animate-in fade-in duration-200">
          {/* Left Column: Clones & Titles */}
          <div className="lg:col-span-7 space-y-4">
            {/* Hidden custom icon input */}
            <input
              type="file"
              ref={iconInputRef}
              accept="image/png,image/jpeg,image/webp,image/gif"
              className="hidden"
              onChange={(e) => handleCustomIcon(e.target.files)}
            />

            {/* Source Info Card */}
            <div className="surface-card p-4 rounded-xl flex items-center justify-between">
              <div className="flex items-center space-x-3.5 min-w-0">
                <div
                  onClick={() => iconInputRef.current?.click()}
                  className="relative group cursor-pointer shrink-0"
                  title="Click to customize master launcher icon"
                >
                  <div className="h-11 w-11 rounded-lg bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 flex items-center justify-center overflow-hidden p-1 transition group-hover:border-neutral-400 dark:group-hover:border-neutral-600">
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

                <div className="min-w-0">
                  <div className="flex items-center space-x-2">
                    <h4 className="text-sm font-semibold text-neutral-900 dark:text-neutral-100 truncate">
                      {metadata.name}
                    </h4>
                    {customIcon && (
                      <span className="text-[10px] font-mono px-1.5 py-0.5 rounded-sm bg-neutral-100 dark:bg-neutral-900 text-neutral-600 dark:text-neutral-300 border border-neutral-200/80 dark:border-neutral-800">
                        Custom Icon
                      </span>
                    )}
                  </div>
                  <div className="flex items-center space-x-2 text-xs text-neutral-500 dark:text-neutral-400 truncate mt-0.5">
                    <span>{metadata.vendor} • {(metadata.fileSize / 1024).toFixed(1)} KB</span>
                    <span>•</span>
                    {customIcon ? (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setCustomIcon(null);
                        }}
                        className="text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200 underline text-[11px]"
                      >
                        Reset Icon
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
                        Change Icon
                      </button>
                    )}
                  </div>
                </div>
              </div>
              <button
                onClick={reset}
                className="text-xs font-medium text-neutral-500 hover:text-neutral-900 dark:text-neutral-400 dark:hover:text-neutral-100 transition px-2.5 py-1 rounded-md hover:bg-neutral-100 dark:hover:bg-neutral-900 shrink-0"
              >
                Change JAR
              </button>
            </div>

            {/* Clone Count Selector */}
            <div className="surface-card p-4 rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-neutral-600 dark:text-neutral-400">
                    Replication Instances
                  </h4>
                  <p className="text-xs text-neutral-500 dark:text-neutral-400 mt-0.5">
                    How many isolated copies to bake into the JAR (2 to 26).
                  </p>
                </div>
                <span className="text-xs font-mono font-bold px-2.5 py-1 rounded-md bg-neutral-100 dark:bg-neutral-900 border border-neutral-200/80 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100">
                  {cloneCount} Clones
                </span>
              </div>

              {/* Slider & Quick Buttons */}
              <div className="space-y-2.5 pt-1">
                <input
                  type="range"
                  min={2}
                  max={26}
                  value={cloneCount}
                  onChange={(e) => setCloneCount(parseInt(e.target.value))}
                  className="w-full accent-neutral-900 dark:accent-neutral-100 cursor-pointer h-1.5 bg-neutral-200 dark:bg-neutral-800 rounded-lg"
                />
                <div className="flex items-center gap-1 sm:gap-1.5 pt-0.5">
                  {[2, 3, 5, 10, 15, 20, 26].map((num) => (
                    <button
                      key={num}
                      type="button"
                      onClick={() => setCloneCount(num)}
                      className={`flex-1 min-w-0 py-1 sm:py-1.5 text-[11px] sm:text-xs rounded-lg font-mono transition border ${
                        cloneCount === num
                          ? "bg-neutral-900 text-white dark:bg-neutral-100 dark:text-black border-transparent font-semibold shadow-xs"
                          : "border-neutral-200/80 dark:border-neutral-800 text-neutral-600 dark:text-neutral-400 hover:bg-neutral-100 dark:hover:bg-neutral-900"
                      } text-center`}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Display Names per Clone */}
            <div className="surface-card p-4 rounded-xl space-y-3">
              <h4 className="text-xs font-semibold uppercase tracking-wider text-neutral-600 dark:text-neutral-400">
                Clone Titles (In-Game Switcher Display)
              </h4>
              <div className="max-h-60 overflow-y-auto space-y-2 pr-1">
                {Array.from({ length: cloneCount }).map((_, i) => (
                  <div key={i} className="flex items-center space-x-2 text-xs">
                    <span className="w-16 font-mono text-[11px] text-neutral-500 dark:text-neutral-400 shrink-0 select-none">
                      #{i + 1} ({i === 0 ? "root" : String.fromCharCode(97 + i)})
                    </span>
                    <input
                      type="text"
                      value={cloneNames[i] || ""}
                      onChange={(e) => updateCloneName(i, e.target.value)}
                      placeholder={`Clone ${i + 1}`}
                      className="flex-1 min-w-0 px-3 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/80 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 text-xs focus:outline-hidden focus:border-neutral-400 dark:focus:border-neutral-600 transition"
                    />
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Column: Metadata Settings & Build */}
          <div className="lg:col-span-5 space-y-4">
            <div className="surface-card p-4 rounded-xl space-y-4">
              <div className="flex items-center space-x-2 text-xs font-semibold uppercase tracking-wider text-neutral-600 dark:text-neutral-400 pb-2 border-b border-neutral-100 dark:border-neutral-800">
                <Settings2 className="h-4 w-4" />
                <span>Archive Metadata</span>
              </div>

              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-neutral-600 dark:text-neutral-400 font-medium mb-1">
                    Collection Name
                  </label>
                  <input
                    type="text"
                    value={options.appName}
                    onChange={(e) => setOptions({ ...options, appName: e.target.value })}
                    className="w-full px-3 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/80 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400 transition"
                  />
                </div>

                <div>
                  <label className="block text-neutral-600 dark:text-neutral-400 font-medium mb-1">
                    Display Title (MIDlet-1)
                  </label>
                  <input
                    type="text"
                    value={options.displayName}
                    onChange={(e) => setOptions({ ...options, displayName: e.target.value })}
                    className="w-full px-3 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/80 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400 transition"
                  />
                </div>

                <div>
                  <label className="block text-neutral-600 dark:text-neutral-400 font-medium mb-1">
                    Vendor / Author
                  </label>
                  <input
                    type="text"
                    value={options.author}
                    onChange={(e) => setOptions({ ...options, author: e.target.value })}
                    className="w-full px-3 py-1.5 rounded-lg bg-neutral-50 dark:bg-neutral-900/80 border border-neutral-200 dark:border-neutral-800 text-neutral-900 dark:text-neutral-100 focus:outline-hidden focus:border-neutral-400 transition"
                  />
                </div>
              </div>

              {/* Advanced Flags */}
              <div className="pt-2 border-t border-neutral-100 dark:border-neutral-800">
                <button
                  type="button"
                  onClick={() => setShowAdvanced(!showAdvanced)}
                  className="flex items-center justify-between w-full text-xs font-medium text-neutral-600 dark:text-neutral-400 hover:text-neutral-900 dark:hover:text-neutral-200 py-1"
                >
                  <span>Advanced J2ME Options</span>
                  {showAdvanced ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                </button>

                {showAdvanced && (
                  <div className="space-y-2.5 pt-2 text-xs">
                    <label className="flex items-center justify-between cursor-pointer">
                      <span className="text-neutral-700 dark:text-neutral-300">Nokia No-Exit Hook</span>
                      <input
                        type="checkbox"
                        checked={options.featureNoExit}
                        onChange={(e) => setOptions({ ...options, featureNoExit: e.target.checked })}
                        className="accent-neutral-900 dark:accent-neutral-100 h-4 w-4 rounded-sm"
                      />
                    </label>
                    <label className="flex items-center justify-between cursor-pointer">
                      <span className="text-neutral-700 dark:text-neutral-300">In-Game Switcher List</span>
                      <input
                        type="checkbox"
                        checked={options.featureList}
                        onChange={(e) => setOptions({ ...options, featureList: e.target.checked })}
                        className="accent-neutral-900 dark:accent-neutral-100 h-4 w-4 rounded-sm"
                      />
                    </label>
                    <label className="flex items-center justify-between cursor-pointer">
                      <span className="text-neutral-700 dark:text-neutral-300">Screenshot Overlay</span>
                      <input
                        type="checkbox"
                        checked={options.featureScreenshot}
                        onChange={(e) => setOptions({ ...options, featureScreenshot: e.target.checked })}
                        className="accent-neutral-900 dark:accent-neutral-100 h-4 w-4 rounded-sm"
                      />
                    </label>
                  </div>
                )}
              </div>

              {/* Processing Progress */}
              {isProcessing && (
                <div className="space-y-1.5 pt-2">
                  <div className="flex justify-between text-xs font-mono">
                    <span className="text-neutral-600 dark:text-neutral-400 truncate mr-2">{statusMessage || "Processing..."}</span>
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

              {/* Action Buttons */}
              {!resultBlob ? (
                <button
                  disabled={isProcessing}
                  onClick={handleStartReplicate}
                  className={`w-full py-2.5 px-3 sm:px-4 rounded-lg font-semibold text-xs flex items-center justify-center space-x-2 transition ${
                    !isProcessing
                      ? "bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 active:scale-[0.99] shadow-xs"
                      : "bg-neutral-200 dark:bg-neutral-800 text-neutral-400 dark:text-neutral-500 cursor-not-allowed"
                  }`}
                >
                  {isProcessing ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin shrink-0" />
                  ) : (
                    <Copy className="h-3.5 w-3.5 shrink-0" />
                  )}
                  <span className="truncate">{isProcessing ? "Replicating Bytecode..." : `Replicate into ${cloneCount} Clones`}</span>
                </button>
              ) : (
                <div className="space-y-2">
                  <button
                    onClick={handleDownload}
                    className="w-full py-2.5 px-3 sm:px-4 rounded-lg font-semibold text-xs bg-neutral-900 text-white dark:bg-white dark:text-black hover:opacity-90 active:scale-[0.99] flex items-center justify-center space-x-2 transition shadow-xs"
                  >
                    <Download className="h-3.5 w-3.5 shrink-0" />
                    <span className="truncate">Download Replicated JAR ({(resultBlob.size / 1024).toFixed(1)} KB)</span>
                  </button>
                  <div className="flex items-center justify-center space-x-1.5 text-xs text-neutral-600 dark:text-neutral-400">
                    <Check className="h-3.5 w-3.5 shrink-0" />
                    <span className="truncate">Multi-instance JAR successfully generated</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
