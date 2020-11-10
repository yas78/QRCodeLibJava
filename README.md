# QRCodeLibJava
QRCodeLibJavaは、Javaで書かれたQRコード生成ライブラリです。  
JIS X 0510に基づくモデル２コードシンボルを生成します。

## 特徴
- 数字・英数字・8ビットバイト・漢字モードに対応しています
- 分割QRコードを作成可能です
- 1bppまたは24bpp BMP・SVGファイルへ保存可能です
- 1bppまたは24bpp Imageオブジェクトとして取得可能です  
- 画像の配色(前景色・背景色)を指定可能です
- 8ビットバイトモードで使用する文字コードを指定可能です

## クイックスタート
QRCodeLibプロジェクト、またはQRCodeLib.jarへのビルドパスを設定してください。

## 使用方法
### 例１．最小限のコードを示します
```java
import java.awt.Image;

import ys.qrcode.Symbols;
import ys.qrcode.Symbol;

public void Example() {
    Symbols symbols = new Symbols();
    symbols.appendText("Hello World");

    Image image = symbols.get(0).getImage();
}
```

### 例２．誤り訂正レベルを指定する
Symbolsクラスのコンストラクタ引数に、ErrorCorrectionLevel列挙型の値を設定します。
```java
import ys.qrcode.ErrorCorrectionLevel;

Symbols symbols = new Symbols(ErrorCorrectionLevel.H);
```

### 例３．8ビットバイトモードで使用する文字コードを指定する
```java
Symbols symbols = new Symbols("utf-8");
```

### 例４．分割QRコードを作成する
型番1を超える場合に分割し、各QRコードのImageオブジェクトを取得する例を示します。
```java
Symbols symbols = new Symbols(1, true);
symbols.appendText("abcdefghijklmnopqrstuvwxyz");

for (Symbol symbol : symbols) {
    Image image = symbol.getImage();
}
```

### 例５．BMPファイルへ保存する
```java
Symbols symbols = new Symbols();
symbols.appendText("Hello World");
Symbol symbol = symbols.get(0);

symbol.saveBitmap("qrcode.bmp");
```

### 例６．SVGファイルへ保存する
```java
Symbols symbols = new Symbols();
symbols.appendText("Hello World");
Symbol symbol = symbols.get(0);

symbol.saveSvg("qrcode.svg");
```

### 例７．SVGデータを取得する
```java
Symbols symbols = new Symbols();
symbols.appendText("Hello World");
Symbol symbol = symbols.get(0);

String svg = symbol.getSvg();
```

### 例８．様々な画像形式で保存する
ImageIOクラスのwriteメソッドを使用します。

```java
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import ys.qrcode.Symbols;
import ys.qrcode.Symbol;

public void Example() {
    Symbols symbols = new Symbols();
    symbols.appendText("Hello World");

    BufferedImage image = symbols.get(0).getImage();

    try {
        // PNG
        ImageIO.write(image, "PNG", new java.io.File("qrcode.png"));
        // GIF
        ImageIO.write(image, "GIF", new java.io.File("qrcode.gif"));
        // JPEG
        ImageIO.write(image, "JPEG", new java.io.File("qrcode.jpg"));
    } catch (IOException e) {
        e.printStackTrace();
    }
}
````