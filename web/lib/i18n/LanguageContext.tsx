"use client";

import React, { createContext, useContext, useState, useEffect } from "react";

export type Language = "en" | "vi";

export const translations = {
  en: {
    // Navbar
    app_title: "MTX JarSync",
    tab_replicate: "Replicate",
    tab_split: "Split X Merge",
    info_btn_title: "About MTX JarSync",
    theme_dark: "Dark Mode",
    theme_light: "Light Mode",
    modal_title: "J2ME Replication & Splitting Engine",
    modal_subtitle: "JVM bytecode manipulation tool for retro Java MIDlet suites.",
    modal_step1_title: "1. Replicate",
    modal_step1_desc: "Takes a single J2ME JAR and clones it into multiple independent instances with isolated package namespaces (root (a), b/, c/), isolated RecordStore storage, cloned static initializers (<clinit>), and an in-game switcher overlay.",
    modal_step2_title: "2. Split X Merge",
    modal_step2_desc: "Extracts an existing multi-account or combined JAR back into standalone single-app J2ME JARs that launch directly, or recombines selected sub-apps into customized merged packages.",
    close: "Close",

    // Page
    page_replicate_title: "Replicate J2ME Package",
    page_replicate_desc: "Replicate a single JAR into multiple isolated instances with bytecode isolation, static cloning, and an in-game switcher.",
    page_split_title: "Split X Merge Multi-Game Package",
    page_split_desc: "Split multi-session or combined JARs back into individual standalone playable packages, or merge selected sub-apps.",
    footer_text: "Powered by NSOMatrix™",
    footer_brand: "JarSync",

    // ReplicateStudio
    drop_jar_title: "Drop a J2ME .jar file here, or click to browse",
    drop_jar_subtitle: "Supports CLDC 1.0 / 1.1 and MIDP 2.0 / 2.1 mobile game packages.",
    browse_file: "Browse File",
    reading_jar: "Reading JAR",
    replicate_ready_header: "Package Inspection",
    change_file: "Change File",
    config_title: "Replication Configuration",
    clone_count_label: "Instance Count:",
    instances: "Instances",
    app_name_label: "Combined Package Name:",
    display_name_label: "Switcher Menu Name:",
    author_label: "Vendor / Author:",
    custom_icon_label: "Master Application Icon:",
    custom_icon_desc: "Custom 16x16 or 32x32 PNG icon for the master package.",
    upload_icon: "Upload Icon",
    remove_icon: "Remove",
    advanced_options: "Advanced Engine Options",
    opt_no_exit: "Nokia No-Exit Backgrounding",
    opt_no_exit_desc: "Prevents immediate process termination on exit command.",
    opt_screenshot: "Screenshot & PNG Encoder Tool",
    opt_screenshot_desc: "Embeds in-game screen capture utility (increases JAR size slightly).",
    btn_start_replicate: "Start Replication",
    processing_replicate: "Processing replication",
    success_replicate_title: "Replication Complete",
    success_replicate_desc: "Generated multi-instance JAR with isolated packages and direct in-game switcher.",
    btn_download_replicated: "Download Replicated JAR",
    replicate_another: "Replicate Another",
    btn_reconfigure: "Reconfigure",
    btn_reset_defaults: "Reset Settings",
    btn_reset: "Reset",
    instance_names_label: "Custom Instance Names",

    // SplitStudio
    split_drop_title: "Select a multi-game or replicated JAR to split",
    split_drop_subtitle: "Extracts standalone single JARs that launch directly, or recombines sub-apps into customized merged JARs.",
    split_extracted_title: "Extracted {count} Sub-Applications",
    split_extracted_desc: "Single JARs launch directly without switcher menu. Select multiple to merge.",
    split_another: "Split Another",
    btn_merge: "Merge",
    btn_download_all_zip: "Download All Standalones (.ZIP)",
    select_all: "Select All",
    deselect_all: "Deselect All",
    selected_count: "({selected}/{total} selected)",
    direct_launch_badge: "Direct Launch",
    direct_launch_hint: "Individual downloads open directly into the game",
    btn_download_standalone: "Download Standalone .JAR",
    tooltip_select_merge: "Select to merge",
    tooltip_deselect_merge: "Deselect from merge",
    status_merging: "Merging selected apps",
    status_splitting: "Splitting games",
    error_prefix: "Error",
    error_choose_another: "Choose another JAR",

    // Errors & Validations
    err_select_jar: "Please select a valid J2ME .jar file.",
    err_select_image: "Please select a valid image file (.png, .jpg, .webp).",
    err_read_image: "Failed to read image file.",
    err_read_jar: "Failed to read JAR: {msg}",
    err_replicate_failed: "Replication failed: {msg}",
    err_split_failed: "Split failed: {msg}",
    err_merge_failed: "Merge failed: {msg}",
  },
  vi: {
    // Navbar
    app_title: "MTX JarSync",
    tab_replicate: "Nhân bản",
    tab_split: "Tách X Ghép",
    info_btn_title: "Giới thiệu MTX JarSync",
    theme_dark: "Chế độ tối",
    theme_light: "Chế độ sáng",
    modal_title: "Công cụ Nhân bản & Tách Ghép J2ME",
    modal_subtitle: "Công cụ thao tác bytecode JVM dành cho các trò chơi Java MIDlet retro.",
    modal_step1_title: "1. Nhân bản",
    modal_step1_desc: "Nhân bản một tệp JAR J2ME thành nhiều phiên bản chạy độc lập với không gian tên package riêng biệt (root (a), b/, c/), bộ nhớ RecordStore cách ly, static initializer (<clinit>) được sao chép và menu chuyển đổi trong game.",
    modal_step2_title: "2. Tách X Ghép",
    modal_step2_desc: "Trích xuất tệp JAR đa tài khoản hoặc tổng hợp trở lại thành các tệp JAR J2ME đơn lẻ khởi chạy trực tiếp, hoặc ghép các ứng dụng con đã chọn thành gói ứng dụng tổng hợp tùy chỉnh.",
    close: "Đóng",

    // Page
    page_replicate_title: "Nhân bản gói J2ME",
    page_replicate_desc: "Nhân bản một tệp JAR thành nhiều phiên bản độc lập với cách ly bytecode, nhân bản static và menu chuyển đổi trong game.",
    page_split_title: "Tách X Ghép gói đa game",
    page_split_desc: "Tách các tệp JAR đa phiên hoặc tổng hợp thành các gói độc lập chạy trực tiếp, hoặc ghép các ứng dụng đã chọn.",
    footer_text: "Powered by NSOMatrix™",
    footer_brand: "JarSync",

    // ReplicateStudio
    drop_jar_title: "Kéo thả tệp .jar J2ME vào đây, hoặc nhấn để chọn",
    drop_jar_subtitle: "Hỗ trợ các trò chơi di động chuẩn CLDC 1.0 / 1.1 và MIDP 2.0 / 2.1.",
    browse_file: "Chọn tệp tin",
    reading_jar: "Đang đọc tệp JAR",
    replicate_ready_header: "Thông tin gói ứng dụng",
    change_file: "Đổi tệp khác",
    config_title: "Cấu hình nhân bản",
    clone_count_label: "Số lượng bản sao:",
    instances: "Bản sao",
    app_name_label: "Tên gói tổng hợp:",
    display_name_label: "Tên menu chuyển đổi:",
    author_label: "Tác giả / Nhà phát triển:",
    custom_icon_label: "Biểu tượng ứng dụng chính:",
    custom_icon_desc: "Biểu tượng PNG 16x16 hoặc 32x32 tùy chỉnh cho gói ứng dụng chính.",
    upload_icon: "Tải ảnh lên",
    remove_icon: "Xóa",
    advanced_options: "Tùy chọn nâng cao",
    opt_no_exit: "Chạy nền Nokia No-Exit",
    opt_no_exit_desc: "Ngăn ứng dụng đóng hoàn toàn khi bấm lệnh thoát.",
    opt_screenshot: "Công cụ chụp màn hình & mã hóa PNG",
    opt_screenshot_desc: "Tích hợp công cụ chụp ảnh màn hình trong game (tăng nhẹ dung lượng JAR).",
    btn_start_replicate: "Bắt đầu nhân bản",
    processing_replicate: "Đang xử lý nhân bản",
    success_replicate_title: "Nhân bản hoàn tất",
    success_replicate_desc: "Đã tạo tệp JAR đa bản sao với các package cách ly và menu chuyển đổi trong game.",
    btn_download_replicated: "Tải xuống tệp JAR đã nhân bản",
    replicate_another: "Nhân bản tệp khác",
    btn_reconfigure: "Cấu hình lại",
    btn_reset_defaults: "Đặt lại cài đặt",
    btn_reset: "Đặt lại",
    instance_names_label: "Tên tùy chỉnh từng bản sao",

    // SplitStudio
    split_drop_title: "Chọn tệp JAR nhiều game hoặc đã nhân bản để tách",
    split_drop_subtitle: "Trích xuất các tệp JAR độc lập chạy trực tiếp, hoặc ghép các ứng dụng con thành tệp JAR tổng hợp tùy chỉnh.",
    split_extracted_title: "Đã trích xuất {count} ứng dụng con",
    split_extracted_desc: "Tệp JAR đơn lẻ khởi chạy trực tiếp không qua menu chuyển đổi. Chọn nhiều ứng dụng để ghép.",
    split_another: "Tách tệp khác",
    btn_merge: "Ghép",
    btn_download_all_zip: "Tải xuống tất cả bản độc lập (.ZIP)",
    select_all: "Chọn tất cả",
    deselect_all: "Bỏ chọn tất cả",
    selected_count: "(Đã chọn {selected}/{total})",
    direct_launch_badge: "Khởi chạy trực tiếp",
    direct_launch_hint: "Bản tải xuống riêng lẻ mở trực tiếp vào trò chơi",
    btn_download_standalone: "Tải xuống JAR độc lập",
    tooltip_select_merge: "Chọn để ghép",
    tooltip_deselect_merge: "Bỏ chọn khỏi ghép",
    status_merging: "Đang ghép các ứng dụng đã chọn",
    status_splitting: "Đang tách các game",
    error_prefix: "Lỗi",
    error_choose_another: "Chọn tệp JAR khác",

    // Errors & Validations
    err_select_jar: "Vui lòng chọn tệp tin .jar J2ME hợp lệ.",
    err_select_image: "Vui lòng chọn tệp ảnh hợp lệ (.png, .jpg, .webp).",
    err_read_image: "Không thể đọc tệp hình ảnh.",
    err_read_jar: "Không thể đọc tệp JAR: {msg}",
    err_replicate_failed: "Nhân bản thất bại: {msg}",
    err_split_failed: "Tách thất bại: {msg}",
    err_merge_failed: "Ghép thất bại: {msg}",
  },
} as const;

export type TranslationKey = keyof typeof translations.en;

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: TranslationKey, params?: Record<string, string | number>) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<Language>("en");

  useEffect(() => {
    try {
      const saved = localStorage.getItem("mtx_jarsync_lang") as Language | null;
      if (saved === "en" || saved === "vi") {
        setLanguageState(saved);
      } else {
        // Detect browser language
        const navLang = navigator.language.toLowerCase();
        if (navLang.startsWith("vi")) {
          setLanguageState("vi");
        }
      }
    } catch {
      // Ignore localStorage access restrictions
    }
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    try {
      localStorage.setItem("mtx_jarsync_lang", lang);
    } catch {
      // Ignore
    }
  };

  const t = (key: TranslationKey, params?: Record<string, string | number>): string => {
    const dict = translations[language] || translations.en;
    let text = (dict as Record<string, string>)[key] || (translations.en as Record<string, string>)[key] || key;
    if (params) {
      for (const [k, v] of Object.entries(params)) {
        text = text.replace(new RegExp(`\\{${k}\\}`, "g"), String(v));
      }
    }
    return text;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error("useLanguage must be used within a LanguageProvider");
  }
  return context;
}
