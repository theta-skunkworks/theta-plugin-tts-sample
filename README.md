# THETA Plug-in TTS Sample

音声合成エンジン [Aquestalk](https://www.a-quest.com/products) を使って、設定値をしゃべる RICOH THETA プラグインです。

本体操作だけで露出補正値の設定と撮影ができます。

## 操作方法

| 操作                      | 機能                              |
| ------------------------- | --------------------------------- |
| シャッターボタン          | 静止画撮影                        |
| シャッターボタン (長押し) | 現在の露出補正値をしゃべる        |
| 無線ボタン                | 露出補正をプラスして、しゃべる    |
| モードボタン              | 露出補正をマイナス して、しゃべる |
| モードボタン (長押し)     | プラグイン起動・終了              |

## ビルド方法

Aquestalk のライブラリはリポジトリには含まれていません。

[Aquestalk ダウンロード](https://www.a-quest.com/download.html) から AquesTalk10 と AqKanji2Koe の Android 版をダウンロードして、以下のパスにファイルをコピーしてください。

### AquesTalk10

* `app/src/main/java/aquestalk/AquesTalk.java`
* `app/src/main/jniLibs/armeabi/libAquesTalk.so`

### AqKanji2Koe

* `app/src/main/java/aqkanji2koe/AqKanji2Koe.java`
* `app/src/main/jniLibs/armeabi/libAqKanji2Koe.so`
* `app/src/main/assets/aqdic.bin`
