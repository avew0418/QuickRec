# QuickRec

QuickRec 是一款追求極簡、快速與可靠的 Android 語音錄音應用程式。它採用高對比度的美學設計，並利用獨特的實體按鍵手勢，在應用程式處於前景時瞬間觸發錄音。

## 主要功能

- **極簡與高對比度 UI**：乾淨、無干擾的介面設計，專為即時操作而生。
- **實體按鍵快捷錄音**：透過自訂的實體音量鍵手勢瞬間觸發錄音：
  - **手勢**：長按 `音量上鍵` + 連按三次 `音量下鍵`
  - *注意：此手勢僅在應用程式處於前景時有效。*
- **簡單可靠**：內建穩健的狀態機以處理快捷鍵邏輯，並直接整合了簡單可靠的 `MediaRecorder`。
- **清晰的架構**：良好的專案目錄結構 (`hotkey`, `permission`, `recording`, `storage`, `ui`)，便於快速開發與後續維護。

## 專案架構

```
app/src/main/java/com/quickrec/
├── hotkey/      # 處理實體音量鍵手勢的狀態機與邏輯
├── permission/  # 處理麥克風錄音與儲存空間等必要權限
├── recording/   # MediaRecorder 整合與音訊擷取邏輯
├── storage/     # 已儲存錄音檔的檔案管理
└── ui/          # 使用 Jetpack Compose 打造的高對比度使用者介面
```

## 快速開始

### 環境需求

- Android Studio (建議使用最新版本)
- Minimum SDK: 24 (或依據 `build.gradle.kts` 的設定)

### 建置專案

1. 複製此儲存庫 (Clone the repository)：
   ```bash
   git clone https://github.com/yourusername/QuickRec.git
   ```
2. 使用 Android Studio 開啟專案。
3. 與 Gradle 檔案同步專案。
4. 建置專案並在 Android 裝置或模擬器上執行。

## 使用說明

1. 開啟 QuickRec 應用程式。
2. 在系統提示時，授予必要的麥克風和儲存空間權限。
3. 若要開始錄音，你可以使用畫面上的控制按鈕，或是使用實體按鍵手勢（長按 `音量上鍵` + 連按三次 `音量下鍵`）。
4. 若要停止錄音，鬆開手勢或使用畫面上的停止按鈕即可。

## 授權條款

本專案採用 MIT 授權條款 - 詳見 LICENSE 檔案。
