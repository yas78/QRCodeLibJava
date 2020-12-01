package ys.qrcode;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import ys.image.ColorCode;
import ys.image.DIB;
import ys.image.GraphicPath;
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


public class Symbol {

    private final int DEFAULT_MODULE_SIZE = 5;
    private final int MIN_MODULE_SIZE = 2;

    private final Symbols _parent;

    private final int _position;

    private QRCodeEncoder _currEncoder;
    private EncodingMode  _currEncodingMode;
    private int           _currVersion;

    private int _dataBitCapacity;
    private int _dataBitCounter;

    private final List<QRCodeEncoder> _segments;
    private final int[]               _segmentCounter;

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

    public Symbols getParent() {
        return _parent;
    }

    public int getVersion() {
        return _currVersion;
    }

    protected EncodingMode getCurrentEncodingMode() {
        return _currEncodingMode;
    }

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
                data[eccIndex] = Byte.toUnsignedInt(dataBlock[blockIndex][i]);
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
        bs.append(_parent.getParity(),
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
            AlignmentPattern.place(_currVersion, moduleMatrix);
        }

        FormatInfo.placeTempBlank(moduleMatrix);

        if (_currVersion >= 7) {
            VersionInfo.placeTempBlank(moduleMatrix);
        }

        placeSymbolChar(moduleMatrix);
        RemainderBit.place(moduleMatrix);

        Masking.apply(_currVersion, _parent.getErrorCorrectionLevel(), moduleMatrix);

        return moduleMatrix;
    }

    private void placeSymbolChar(int[][] moduleMatrix) {
        byte[] data = getEncodingRegionBytes();

        int r = moduleMatrix.length - 1;
        int c = moduleMatrix[0].length - 1;

        boolean toLeft = true;
        int rowDirection = -1;

        for (byte value : data) {
            int bitPos = 7;

            while (bitPos >= 0) {
                if (moduleMatrix[r][c] == 0) {
                    moduleMatrix[r][c] = (value & (1 << bitPos)) > 0 ? 1 : -1;
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

    public byte[] getBitmap() {
        return getBitmap(DEFAULT_MODULE_SIZE);
    }

    public byte[] getBitmap(int moduleSize) {
        return getBitmap1bpp(moduleSize, ColorCode.BLACK, ColorCode.WHITE);
    }

    public byte[] getBitmap(int moduleSize, boolean monochrome) {
        return getBitmap(moduleSize, monochrome, ColorCode.BLACK, ColorCode.WHITE);
    }

    public byte[] getBitmap(int moduleSize, boolean monochrome, String foreRgb, String backRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        if (ColorCode.isWebColor(backRgb) == false) {
            throw new IllegalArgumentException("backRgb");
        }

        if (monochrome) {
            return getBitmap1bpp(moduleSize, foreRgb, backRgb);
        } else {
            return getBitmap24bpp(moduleSize, foreRgb, backRgb);
        }
    }

    private byte[] getBitmap1bpp(int moduleSize, String foreRgb, String backRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        if (ColorCode.isWebColor(backRgb) == false) {
            throw new IllegalArgumentException("backRgb");
        }

        Color foreColor = Color.decode(foreRgb);
        Color backColor = Color.decode(backRgb);

        int[][] moduleMatrix = QuietZone.place(getModuleMatrix());

        int width, height;
        width = height = moduleSize * moduleMatrix.length;

        int rowBytesLen = (width + 7) / 8;

        int pack8bit = 0;
        if (width % 8 > 0) {
            pack8bit = 8 - (width % 8);
        }

        int pack32bit = 0;
        if (rowBytesLen % 4 > 0) {
            pack32bit = 8 * (4 - (rowBytesLen % 4));
        }

        int rowSize = (width + pack8bit + pack32bit) / 8;
        byte[] bitmapData = new byte[rowSize * height];
        int offset = 0;

        BitSequence bs = new BitSequence();

        for (int r = moduleMatrix.length - 1; r >= 0; r--) {
            bs.clear();

            for (int c = 0; c < moduleMatrix[r].length; c++) {
                int color = moduleMatrix[r][c] > 0 ? 0 : 1;

                for (int i = 1; i <= moduleSize; i++) {
                    bs.append(color, 1);
                }
            }
            bs.append(0, pack8bit);
            bs.append(0, pack32bit);

            byte[] bitmapRow = bs.getBytes();

            for (int i = 1; i <= moduleSize; i++) {
                System.arraycopy(bitmapRow, 0, bitmapData, offset, rowSize);
                offset += rowSize;
            }
        }

        return DIB.build1bppDIB(bitmapData, width, height, foreColor, backColor);
    }

    private byte[] getBitmap24bpp(int moduleSize, String foreRgb, String backRgb) {
        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        Color foreColor = Color.decode(foreRgb);
        Color backColor = Color.decode(backRgb);

        int[][] moduleMatrix = QuietZone.place(getModuleMatrix());

        int width, height;
        width = height  = moduleSize * moduleMatrix.length;

        int rowBytesLen = 3 * width;

        int pack4byte = 0;
        if (rowBytesLen % 4 > 0)
            pack4byte = 4 - (rowBytesLen % 4);

        int rowSize = rowBytesLen + pack4byte;
        byte[] bitmapData = new byte[rowSize * height];
        int offset = 0;

        for (int r = moduleMatrix.length - 1; r >= 0; r--) {
            byte[] bitmapRow = new byte[rowSize];
            int idx = 0;

            for (int c = 0; c < moduleMatrix[r].length; c++) {
                Color color = moduleMatrix[r][c] > 0 ? foreColor : backColor;

                for (int i = 1; i <= moduleSize; i++) {
                    bitmapRow[idx++] = (byte) color.getBlue();
                    bitmapRow[idx++] = (byte) color.getGreen();
                    bitmapRow[idx++] = (byte) color.getRed();
                }
            }

            for (int i = 1; i <= moduleSize; i++) {
                System.arraycopy(bitmapRow, 0, bitmapData, offset, rowSize);
                offset += rowSize;
            }
        }

        return DIB.build24bppDIB(bitmapData, width, height);
    }

    public String getBitmapBase64() {
        return getBitmapBase64(4);
    }

    public String getBitmapBase64(int moduleSize) {
        return getBitmapBase64(moduleSize, false);
    }

    public String getBitmapBase64(int moduleSize, boolean monochrome) {
        return getBitmapBase64(moduleSize, monochrome, ColorCode.BLACK, ColorCode.WHITE);
    }

    public String getBitmapBase64(int moduleSize, boolean monochrome, String foreRgb, String backRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        if (ColorCode.isWebColor(backRgb) == false) {
            throw new IllegalArgumentException("backRgb");
        }

        byte[] dib;

        if (monochrome) {
            dib = getBitmap1bpp(moduleSize, foreRgb, backRgb);
        } else {
            dib = getBitmap24bpp(moduleSize, foreRgb, backRgb);
        }

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(dib);
    }

    public BufferedImage getImage() {
        return getImage(4);
    }

    public BufferedImage getImage(int moduleSize) {
        return getImage(moduleSize, false);
    }

    public BufferedImage getImage(int moduleSize, boolean monochrome) {
        return getImage(moduleSize, monochrome, ColorCode.BLACK, ColorCode.WHITE);
    }

    public BufferedImage getImage(int moduleSize, boolean monochrome, String foreRgb, String backRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        if (ColorCode.isWebColor(backRgb) == false) {
            throw new IllegalArgumentException("backRgb");
        }

        byte[] dib;

        if (monochrome) {
            dib = getBitmap1bpp(moduleSize, foreRgb, backRgb);
        } else {
            dib = getBitmap24bpp(moduleSize, foreRgb, backRgb);
        }

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

    public void saveBitmap(String fileName) {
        saveBitmap(fileName, DEFAULT_MODULE_SIZE);
    }

    public void saveBitmap(String fileName, int moduleSize) {
        saveBitmap(fileName, moduleSize, false);
    }

    public void saveBitmap(String fileName, int moduleSize, boolean monochrome) {
        saveBitmap(fileName, moduleSize, monochrome, ColorCode.BLACK, ColorCode.WHITE);
    }

    public void saveBitmap(String fileName, int moduleSize, boolean monochrome, String foreRgb, String backRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        if (ColorCode.isWebColor(backRgb) == false) {
            throw new IllegalArgumentException("backRgb");
        }

        byte[] dib;

        if (monochrome) {
            dib = getBitmap1bpp(moduleSize, foreRgb, backRgb);
        } else {
            dib = getBitmap24bpp(moduleSize, foreRgb, backRgb);
        }

        try (FileOutputStream s = new FileOutputStream(fileName);) {
            s.write(dib);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveSvg(String fileName) {
        saveSvg(fileName, DEFAULT_MODULE_SIZE);
    }

    public void saveSvg(String fileName, int moduleSize) {
        saveSvg(fileName, moduleSize, ColorCode.BLACK);
    }

    public void saveSvg(String fileName, int moduleSize, String foreRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName");
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        String newLine = System.lineSeparator();

        String svg = getSvg(moduleSize, foreRgb);
        String svgFile =
            "<?xml version='1.0' encoding='UTF-8' standalone='no'?>" + newLine +
            "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 20010904//EN'" + newLine +
            "    'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'>" + newLine +
            svg + newLine;

        try (FileWriter fw = new FileWriter(fileName);) {
            fw.write(svgFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getSvg() {
        return getSvg(5);
    }

    public String getSvg(int moduleSize) {
        return getSvg(moduleSize, ColorCode.BLACK);
    }

    public String getSvg(int moduleSize, String foreRgb) {
        if (_dataBitCounter == 0) {
            throw new IllegalStateException();
        }

        if (moduleSize < MIN_MODULE_SIZE) {
            throw new IllegalArgumentException("moduleSize");
        }

        if (ColorCode.isWebColor(foreRgb) == false) {
            throw new IllegalArgumentException("foreRgb");
        }

        int[][] moduleMatrix = QuietZone.place(getModuleMatrix());

        int width, height;
        width = height = moduleSize * moduleMatrix.length;

        int[][] image = new int[height][];

        int r = 0;
        for (int[] row : moduleMatrix) {
            int[] imageRow = new int[width];
            int c = 0;

            for (int value : row) {
                for (int j = 0; j < moduleSize; j++) {
                    imageRow[c] = value;
                    c++;
                }
            }

            for (int i = 0; i < moduleSize; i++) {
                image[r] = imageRow;
                r++;
            }
        }

        Point[][] paths = GraphicPath.FindContours(image);
        StringBuilder buf = new StringBuilder();
        String newLine = System.lineSeparator();
        String indent = new String(new char[11]).replace("\0", " ");

        for (Point[] path : paths) {
            buf.append(indent + "M ");

            for (Point p : path) {
                buf.append(String.valueOf(p.x) + "," + String.valueOf(p.y) + " ");
            }

            buf.append("Z" + newLine);
        }

        String data = buf.toString().trim();
        String svg =
            "<svg xmlns=\'http://www.w3.org/2000/svg\'" + newLine +
            "    width=\'" + width + "\' height=\'" + height + "\' viewBox=\'0 0 " + width + " " + height + "\'>" + newLine +
            "    <path fill='" + foreRgb + "' stroke='" + foreRgb + "' stroke-width='1'" + newLine +
            "        d=\'" + data + "\'" + newLine +
            "    />" + newLine +
            "</svg>";

        return svg;
    }
}
