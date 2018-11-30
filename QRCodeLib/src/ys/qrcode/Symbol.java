package ys.qrcode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import ys.image.BITMAPFILEHEADER;
import ys.image.BITMAPINFOHEADER;
import ys.image.RGBQUAD;
import ys.qrcode.encoder.QRCodeEncoder;
import ys.qrcode.format.CharCountIndicator;
import ys.qrcode.format.Codeword;
import ys.qrcode.format.DataCodeword;
import ys.qrcode.format.ModeIndicator;
import ys.qrcode.format.Module;
import ys.qrcode.format.RSBlock;
import ys.qrcode.format.StructuredAppend;
import ys.qrcode.format.SymbolSequenceIndicator;
import ys.qrcode.misc.BitSequence;
import ys.qrcode.misc.ColorCode;

/**
 * シンボルを表します。
 */
public class Symbol {
    private final Symbols _parent;

    private final int _position;

    private QRCodeEncoder _currEncoder;
    private EncodingMode  _currEncodingMode;
    private int           _currVersion;

    private int _dataBitCapacity;
    private int _dataBitCounter;

    private final List<QRCodeEncoder> _segments;
    private final int[]               _segmentCounter;

    /**
     * インスタンスを初期化します。
     *
     * @param parent
     *            親オブジェクト
     */
    public Symbol(Symbols parent) {
        _parent = parent;

        _position = parent.getCount();

        _currEncoder = null;
        _currEncodingMode = EncodingMode.UNKNOWN;
        _currVersion = parent.getMinVersion();

        _dataBitCapacity = 8 * DataCodeword.getTotalNumber(
            parent.getErrorCorrectionLevel(), parent.getMinVersion());
        _dataBitCounter = 0;

        _segments = new ArrayList<QRCodeEncoder>();
        _segmentCounter = new int[] {
            0, // UNKNOWN
            0, // NUMERIC
            0, // ALPHA_NUMERIC
            0, // EIGHT_BIT_BYTE
            0  // KANJI
        };

        if (parent.getStructuredAppendAllowed()) {
            _dataBitCapacity -= StructuredAppend.HEADER_LENGTH;
        }
    }

    /**
     * 親オブジェクトを取得します。
     */
    public Symbols getParent() {
        return _parent;
    }

    /**
     * 型番を取得します。
     */
    public int getVersion() {
        return _currVersion;
    }

    /**
     * 現在の符号化モードを取得します。
     */
    protected EncodingMode getCurrentEncodingMode() {
        return _currEncodingMode;
    }

    /**
     * シンボルに文字を追加します。
     *
     * @param c
     *      対象文字
     * @return
     *      シンボル容量が不足している場合は false を返します。
     */
    protected boolean tryAppend(char c) {
        int bitLength = _currEncoder.getCodewordBitLength(c);

        while (_dataBitCapacity < _dataBitCounter + bitLength) {
            if (_currVersion >= _parent.getMaxVersion()) {
                return false;
            }
            selectVersion();
        }

        _currEncoder.append(c);
        _dataBitCounter += bitLength;
        _parent.updateParity(c);
        return true;
    }

    /**
     * 符号化モードを設定します。
     *
     * @param encMode
     *      符号化モード
     * @param c
     *      符号化する最初の文字。この文字はシンボルに追加されません。
     * @return
     *      シンボル容量が不足している場合は false を返します。
     */
    protected boolean trySetEncodingMode(EncodingMode encMode, char c) {
        QRCodeEncoder encoder = QRCodeEncoder.createEncoder(
                encMode, _parent.getByteModeCharset());
        int bitLength = encoder.getCodewordBitLength(c);

        while (_dataBitCapacity <
                    (_dataBitCounter + ModeIndicator.LENGTH
                     + CharCountIndicator.getLength(_currVersion, encMode)
                     + bitLength)) {
            if (_currVersion >= _parent.getMaxVersion()) {
                return false;
            }
            selectVersion();
        }

        _dataBitCounter += ModeIndicator.LENGTH
                           + CharCountIndicator.getLength(_currVersion, encMode);
        _currEncoder = encoder;
        _segments.add(_currEncoder);
        _segmentCounter[encMode.toInt()] += 1;
        _currEncodingMode = encMode;
        return true;
    }

    /**
     * 型番を決定します。
     */
    private void selectVersion() {
        for (int i = 1; i < _segmentCounter.length; i++) {
            int num = _segmentCounter[i];
            EncodingMode encMode = EncodingMode.getEnum(i);

            _dataBitCounter += num * CharCountIndicator.getLength(_currVersion + 1, encMode)
                               - num * CharCountIndicator.getLength(_currVersion + 0, encMode);
        }

        _currVersion++;
        _dataBitCapacity = 8 * DataCodeword.getTotalNumber(
            _parent.getErrorCorrectionLevel(), _currVersion);
        _parent.setMinVersion(_currVersion);

        if (_parent.getStructuredAppendAllowed()) {
            _dataBitCapacity -= StructuredAppend.HEADER_LENGTH;
        }
    }

    /**
     * データブロックを返します。
     */
    private byte[][] buildDataBlock() {
        byte[] dataBytes = getMessageBytes();

        int numPreBlocks = RSBlock.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion, true);
        int numFolBlocks = RSBlock.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion, false);

        byte[][] ret = new byte[numPreBlocks + numFolBlocks][];

        int numPreBlockDataCodewords = RSBlock.getNumberDataCodewords(
                _parent.getErrorCorrectionLevel(), _currVersion, true);
        int index = 0;

        for (int i = 0; i < numPreBlocks; i++) {
            byte[] data = new byte[numPreBlockDataCodewords];
            System.arraycopy(dataBytes, index, data, 0, data.length);
            index += data.length;
            ret[i] = data;
        }

        int numFolBlockDataCodewords = RSBlock.getNumberDataCodewords(
                _parent.getErrorCorrectionLevel(), _currVersion, false);

        for (int i = numPreBlocks; i < numPreBlocks + numFolBlocks; i++) {
            byte[] data = new byte[numFolBlockDataCodewords];
            System.arraycopy(dataBytes, index, data, 0, data.length);
            index += data.length;
            ret[i] = data;
        }

        return ret;
    }

    /**
     * 誤り訂正データ領域のブロックを生成します。
     *
     * @param dataBlock
     *            データ領域のブロック
     */
    private byte[][] buildErrorCorrectionBlock(byte[][] dataBlock) {
        int numECCodewords = RSBlock.getNumberECCodewords(
                _parent.getErrorCorrectionLevel(), _currVersion);
        int numPreBlocks = RSBlock.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion, true);
        int numFolBlocks = RSBlock.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion, false);

        byte[][] ret = new byte[numPreBlocks + numFolBlocks][];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new byte[numECCodewords];
        }

        int[] gp = GeneratorPolynomials.get(numECCodewords);

        for (int blockIndex = 0; blockIndex < dataBlock.length; blockIndex++) {
            int size = dataBlock[blockIndex].length + ret[blockIndex].length;
            int[] data = new int[size];
            int eccIndex = data.length - 1;

            for (int i = 0; i < dataBlock[blockIndex].length; i++) {
                data[eccIndex] = Byte.toUnsignedInt(dataBlock[blockIndex][i]);// & 0xFF;
                eccIndex--;
            }

            for (int i = data.length - 1; i >= numECCodewords; i--) {
                if (data[i] > 0) {
                    int exp = GaloisField256.toExp(data[i]);
                    eccIndex = i;

                    for (int j = gp.length - 1; j >= 0; j--) {
                        data[eccIndex] ^= GaloisField256.toInt((gp[j] + exp) % 255);
                        eccIndex--;
                    }
                }
            }

            eccIndex = numECCodewords - 1;

            for (int i = 0; i < ret[blockIndex].length; i++) {
                ret[blockIndex][i] = (byte) data[eccIndex];
                eccIndex--;
            }
        }

        return ret;
    }

    /**
     * 符号化領域のバイトデータを返します。
     */
    private byte[] getEncodingRegionBytes() {
        byte[][] dataBlock = buildDataBlock();
        byte[][] ecBlock = buildErrorCorrectionBlock(dataBlock);

        int numCodewords = Codeword.getTotalNumber(_currVersion);

        int numDataCodewords = DataCodeword.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion);

        byte[] ret = new byte[numCodewords];

        int index = 0;
        int n;

        n = 0;
        while (index < numDataCodewords) {
            int r = n % dataBlock.length;
            int c = n / dataBlock.length;

            if (c <= dataBlock[r].length - 1) {
                ret[index] = dataBlock[r][c];
                index++;
            }

            n++;
        }

        n = 0;
        while (index < numCodewords) {
            int r = n % ecBlock.length;
            int c = n / ecBlock.length;

            if (c <= ecBlock[r].length - 1) {
                ret[index] = ecBlock[r][c];
                index++;
            }

            n++;
        }

        return ret;
    }

    /**
     * コード語に変換するメッセージビット列を返します。
     */
    private byte[] getMessageBytes() {
        BitSequence bs = new BitSequence();

        if (_parent.getCount() > 1) {
            writeStructuredAppendHeader(bs);
        }

        writeSegments(bs);
        writeTerminator(bs);
        writePaddingBits(bs);
        writePadCodewords(bs);

        return bs.getBytes();
    }

    private void writeStructuredAppendHeader(BitSequence bs) {
        bs.append(ModeIndicator.STRUCTURED_APPEND_VALUE,
                  ModeIndicator.LENGTH);
        bs.append(_position,
                  SymbolSequenceIndicator.POSITION_LENGTH);
        bs.append(_parent.getCount() - 1,
                  SymbolSequenceIndicator.TOTAL_NUMBER_LENGTH);
        bs.append(_parent.getStructuredAppendParity(),
                  StructuredAppend.PARITY_DATA_LENGTH);
    }

    private void writeSegments(BitSequence bs) {
        for (QRCodeEncoder segment : _segments) {
            bs.append(segment.getModeIndicator(), ModeIndicator.LENGTH);
            bs.append(segment.getCharCount(),
                      CharCountIndicator.getLength(
                              _currVersion, segment.getEncodingMode()));

            byte[] data = segment.getBytes();

            for (int i = 0; i <= data.length - 2; i++) {
                bs.append(Byte.toUnsignedInt(data[i]), 8);
            }

            int codewordBitLength = segment.getBitCount() % 8;

            if (codewordBitLength == 0) {
                codewordBitLength = 8;
            }

            bs.append(Byte.toUnsignedInt(data[data.length - 1])
                        >> (8 - codewordBitLength), codewordBitLength);
        }
    }

    private void writeTerminator(BitSequence bs) {
        int terminatorLength = _dataBitCapacity - _dataBitCounter;

        if (terminatorLength > ModeIndicator.LENGTH) {
            terminatorLength = ModeIndicator.LENGTH;
        }

        bs.append(ModeIndicator.TERMINATOR_VALUE, terminatorLength);
    }

    private void writePaddingBits(BitSequence bs) {
        if (bs.getLength() % 8 > 0) {
            bs.append(0x0, 8 - (bs.getLength() % 8));
        }
    }

    private void writePadCodewords(BitSequence bs) {
        int numDataCodewords = DataCodeword.getTotalNumber(
                _parent.getErrorCorrectionLevel(), _currVersion);

        boolean flag = true;

        while (bs.getLength() < 8 * numDataCodewords) {
            bs.append(flag ? 236 : 17, 8);
            flag = !flag;
        }
    }

    /**
     * シンボルの明暗パターンを返します。
     */
    private int[][] getModuleMatrix() {
        int numModulesPerSide = Module.getNumModulesPerSide(_currVersion);

        int[][] moduleMatrix = new int[numModulesPerSide][];

        for (int i = 0; i < moduleMatrix.length; i++) {
            moduleMatrix[i] = new int[moduleMatrix.length];
        }

        FinderPattern.place(moduleMatrix);
        Separator.place(moduleMatrix);
        TimingPattern.place(moduleMatrix);

        if (_currVersion >= 2) {
            AlignmentPattern.place(moduleMatrix, _currVersion);
        }

        FormatInfo.placeTempBlank(moduleMatrix);

        if (_currVersion >= 7) {
            VersionInfo.placeTempBlank(moduleMatrix);
        }

        placeSymbolChar(moduleMatrix);

        RemainderBit.place(moduleMatrix);

        int maskPatternReference = Masking.apply(
                moduleMatrix, _currVersion, _parent.getErrorCorrectionLevel());

        FormatInfo.place(moduleMatrix,
                         _parent.getErrorCorrectionLevel(),
                         maskPatternReference);

        if (_currVersion >= 7) {
            VersionInfo.place(moduleMatrix, _currVersion);
        }

        return moduleMatrix;
    }

    /**
     * シンボルキャラクタを配置します。
     */
    private void placeSymbolChar(int[][] moduleMatrix) {
        byte[] data = getEncodingRegionBytes();

        int r = moduleMatrix.length - 1;
        int c = moduleMatrix[0].length - 1;

        boolean toLeft = true;
        int rowDirection = -1;

        for (int i = 0; i < data.length; i++) {
            int bitPos = 7;

            while (bitPos >= 0) {
                if (moduleMatrix[r][c] == 0) {
                    moduleMatrix[r][c] = (data[i] & (1 << bitPos)) > 0 ? 1 : -1;
                    bitPos--;
                }

                if (toLeft) {
                    c--;
                } else {
                    if ((r + rowDirection) < 0) {
                        r = 0;
                        rowDirection = 1;
                        c--;

                        if (c == 6) {
                            c = 5;
                        }
                    } else if ((r + rowDirection) > (moduleMatrix.length - 1)) {
                        r = moduleMatrix.length - 1;
                        rowDirection = -1;
                        c--;

                        if (c == 6) {
                            c = 5;
                        }
                    } else {
                        r += rowDirection;
                        c++;
                    }
                }

                toLeft = !toLeft;
            }
        }
    }

    /**
     * 1bppビットマップファイルのバイトデータを返します。
     */
    public byte[] get1bppDIB() {
        return get1bppDIB(5);
    }

    /**
     * 1bppビットマップファイルのバイトデータを返します。
     *
     * @param moduleSize
     *            モジュールサイズ
     */
    public byte[] get1bppDIB(int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        return get1bppDIB(moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 1bppビットマップファイルのバイトデータを返します。
     *
     * @param moduleSize
     *            モジュールサイズ
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public byte[] get1bppDIB(int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        Color foreColor = Color.decode(foreRgb);
        Color backColor = Color.decode(backRgb);

        int[][] moduleMatrix = QuietZone.place(getModuleMatrix());

        int width   = moduleSize * moduleMatrix.length;
        int height  = width;

        int hByteLen = (width + 7) / 8;

        int pack8bit = 0;
        if (width % 8 > 0)
            pack8bit = 8 - (width % 8);

        int pack32bit = 0;
        if (hByteLen % 4 > 0)
            pack32bit = 8 * (4 - (hByteLen % 4));

        BitSequence bs = new BitSequence();

        for (int r = moduleMatrix.length - 1; r >= 0; r--) {
            for (int i = 1; i <= moduleSize; i++) {
                for (int c = 0; c < moduleMatrix[r].length; c++) {
                    for (int j = 1; j <= moduleSize; j++) {
                        bs.append(moduleMatrix[r][c] > 0 ? 0 : 1, 1);
                    }
                }

                bs.append(0, pack8bit);
                bs.append(0, pack32bit);
            }
        }

        byte[] dataBlock = bs.getBytes();

        BITMAPFILEHEADER bfh = new BITMAPFILEHEADER();
        bfh.bfType          = 0x4D42;
        bfh.bfSize          = 62 + dataBlock.length;
        bfh.bfReserved1     = 0;
        bfh.bfReserved2     = 0;
        bfh.bfOffBits       = 62;

        BITMAPINFOHEADER bih = new BITMAPINFOHEADER();
        bih.biSize          = 40;
        bih.biWidth         = width;
        bih.biHeight        = height;
        bih.biPlanes        = 1;
        bih.biBitCount      = 1;
        bih.biCompression   = 0;
        bih.biSizeImage     = 0;
        bih.biXPelsPerMeter = 3780; // 96dpi
        bih.biYPelsPerMeter = 3780; // 96dpi
        bih.biClrUsed       = 0;
        bih.biClrImportant  = 0;

        RGBQUAD[] palette = new RGBQUAD[] { new RGBQUAD(), new RGBQUAD() };

        palette[0].rgbBlue      = (byte) foreColor.getBlue();
        palette[0].rgbGreen     = (byte) foreColor.getGreen();
        palette[0].rgbRed       = (byte) foreColor.getRed();
        palette[0].rgbReserved  = 0;

        palette[1].rgbBlue      = (byte) backColor.getBlue();
        palette[1].rgbGreen     = (byte) backColor.getGreen();
        palette[1].rgbRed       = (byte) backColor.getRed();
        palette[1].rgbReserved  = 0;

        byte[] ret = new byte[62 + dataBlock.length];

        byte[] bytes;
        int offset = 0;

        bytes = bfh.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bih.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = palette[0].getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = palette[1].getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = dataBlock;
        System.arraycopy(bytes, 0, ret, offset, bytes.length);

        return ret;
    }

    /**
     * 24bppビットマップファイルのバイトデータを返します。
     */
    public byte[] get24bppDIB() {
        return get24bppDIB(5);
    }

    /**
     * 24bppビットマップファイルのバイトデータを返します。
     *
     * @param moduleSize
     *            モジュールサイズ
     */
    public byte[] get24bppDIB(int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        return get24bppDIB(moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 24bppビットマップファイルのバイトデータを返します。
     *
     * @param moduleSize
     *            モジュールサイズ
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public byte[] get24bppDIB(int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        Color foreColor = Color.decode(foreRgb);
        Color backColor = Color.decode(backRgb);

        int[][] moduleMatrix = QuietZone.place(getModuleMatrix());

        int width  = moduleSize * moduleMatrix.length;
        int height = width;

        int hByteLen = 3 * width;

        int pack4byte = 0;
        if (hByteLen % 4 > 0)
            pack4byte = 4 - (hByteLen % 4);

        byte[] dataBlock = new byte[(hByteLen + pack4byte) * height];

        int idx = 0;

        for (int r = moduleMatrix.length - 1; r >= 0; r--) {
            for (int i = 1; i <= moduleSize; i++) {
                for (int c = 0; c < moduleMatrix[r].length; c++) {
                    for (int j = 1; j <= moduleSize; j++) {
                        Color color = moduleMatrix[r][c] > 0 ? foreColor : backColor;
                        dataBlock[idx++] = (byte) color.getBlue();
                        dataBlock[idx++] = (byte) color.getGreen();
                        dataBlock[idx++] = (byte) color.getRed();
                    }
                }

                idx += pack4byte;
            }
        }

        BITMAPFILEHEADER bfh = new BITMAPFILEHEADER();
        bfh.bfType      = 0x4D42;
        bfh.bfSize      = 54 + dataBlock.length;
        bfh.bfReserved1 = 0;
        bfh.bfReserved2 = 0;
        bfh.bfOffBits   = 54;

        BITMAPINFOHEADER bih = new BITMAPINFOHEADER();
        bih.biSize          = 40;
        bih.biWidth         = width;
        bih.biHeight        = height;
        bih.biPlanes        = 1;
        bih.biBitCount      = 24;
        bih.biCompression   = 0;
        bih.biSizeImage     = 0;
        bih.biXPelsPerMeter = 3780; // 96dpi
        bih.biYPelsPerMeter = 3780; // 96dpi
        bih.biClrUsed       = 0;
        bih.biClrImportant  = 0;

        byte[] ret = new byte[54 + dataBlock.length];

        byte[] bytes;
        int offset = 0;

        bytes = bfh.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bih.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = dataBlock;
        System.arraycopy(bytes, 0, ret, offset, bytes.length);

        return ret;
    }

    /**
     * 1bppのシンボル画像を返します。
     */
    public BufferedImage get1bppImage() {
        return get1bppImage(5);
    }

    /**
     * 1bppのシンボル画像を返します。
     *
     * @param moduleSize
     *            モジュールサイズ(px)
     */
    public BufferedImage get1bppImage(int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        return get1bppImage(moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 1bppのシンボル画像を返します。
     *
     * @param moduleSize
     *            モジュールサイズ(px)
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public BufferedImage get1bppImage(int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        byte[] dib = get1bppDIB(moduleSize, foreRgb, backRgb);
        BufferedImage ret = null;

        try (InputStream bs = new ByteArrayInputStream(dib)) {
            try {
                ret = ImageIO.read(bs);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    /**
     * 24bppのシンボル画像を返します。
     */
    public BufferedImage get24bppImage() {
        return get24bppImage(5);
    }

    /**
     * 24bppのシンボル画像を返します。
     *
     * @param moduleSize
     *            モジュールサイズ(px)
     */
    public BufferedImage get24bppImage(int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        return get24bppImage(moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 24bppのシンボル画像を返します。
     *
     * @param moduleSize
     *            モジュールサイズ(px)
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public BufferedImage get24bppImage(int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        byte[] dib = get24bppDIB(moduleSize, foreRgb, backRgb);
        BufferedImage ret = null;

        try (InputStream bs = new ByteArrayInputStream(dib)) {
            try {
                ret = ImageIO.read(bs);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    /**
     * 1bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     */
    public void save1bppDIB(String fileName) {
        save1bppDIB(fileName, 5);
    }

    /**
     * 1bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     * @param moduleSize
     *            モジュールサイズ(px)
     */
    public void save1bppDIB(String fileName, int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        save1bppDIB(fileName, moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 1bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     * @param moduleSize
     *            モジュールサイズ(px)
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public void save1bppDIB(String fileName, int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        byte[] dib = get1bppDIB(moduleSize, foreRgb, backRgb);

        try (FileOutputStream s = new FileOutputStream(fileName);) {
            s.write(dib);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 24bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     */
    public void save24bppDIB(String fileName) {
        save24bppDIB(fileName, 5);
    }

    /**
     * 24bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     * @param moduleSize
     *            モジュールサイズ(px)
     */
    public void save24bppDIB(String fileName, int moduleSize) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        save24bppDIB(fileName, moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    /**
     * 24bppシンボル画像をファイルに保存します。
     *
     * @param fileName
     *            ファイル名
     * @param moduleSize
     *            モジュールサイズ(px)
     * @param foreRgb
     *            前景色
     * @param backRgb
     *            背景色
     */
    public void save24bppDIB(String fileName, int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < 1) {
            throw new IllegalArgumentException("moduleSize");
        }

        byte[] dib = get24bppDIB(moduleSize, foreRgb, backRgb);

        try (FileOutputStream stream = new FileOutputStream(fileName);) {
            stream.write(dib);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
