package ys.qrcode;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ys.qrcode.encoder.AlphanumericEncoder;
import ys.qrcode.encoder.ByteEncoder;
import ys.qrcode.encoder.KanjiEncoder;
import ys.qrcode.encoder.NumericEncoder;

/**
 * シンボルのコレクションを表します。
 */
public class Symbols implements Iterable<Symbol>, java.util.Iterator<Symbol> {
    private final List<Symbol> _items;

    private int _minVersion;

    private final ErrorCorrectionLevel _errorCorrectionLevel;
    private final int                  _maxVersion;
    private final boolean              _structuredAppendAllowed;
    private final Charset              _charset;

    private Symbol _currSymbol;
    private int    _parity;

    private final NumericEncoder      _encNumeric;
    private final AlphanumericEncoder _encAlpha;
    private final KanjiEncoder        _encKanji;
    private final ByteEncoder         _encByte;

    /**
     * インスタンスを初期化します。
     */
    public Symbols() {
        this(ErrorCorrectionLevel.M);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param ecLevel
     *            誤り訂正レベル
     */
    public Symbols(ErrorCorrectionLevel ecLevel) {
        this(ecLevel, Constants.MAX_VERSION, false);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param ecLevel
     *            誤り訂正レベル
     * @param charsetName
     *            文字セット名
     */
    public Symbols(ErrorCorrectionLevel ecLevel, String charsetName) {
        this(ecLevel, Constants.MAX_VERSION, false, charsetName);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param maxVersion
     *            型番の上限
     */
    public Symbols(int maxVersion) {
        this(maxVersion, false);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param allowStructuredAppend
     *            複数シンボルへの分割を許可するには true を指定します。
     */
    public Symbols(boolean allowStructuredAppend) {
        this(Constants.MAX_VERSION, allowStructuredAppend);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param maxVersion
     *            型番の上限
     * @param allowStructuredAppend
     *            複数シンボルへの分割を許可するには true を指定します。
     */
    public Symbols(int maxVersion, boolean allowStructuredAppend) {
        this(ErrorCorrectionLevel.M, maxVersion, allowStructuredAppend);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param maxVersion
     *            型番の上限
     * @param allowStructuredAppend
     *            複数シンボルへの分割を許可するには true を指定します。
     * @param charsetName
     *            文字セット名
     */
    public Symbols(int maxVersion, boolean allowStructuredAppend, String charsetName) {
        this(ErrorCorrectionLevel.M, maxVersion, allowStructuredAppend, charsetName);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param ecLevel
     *            誤り訂正レベル
     * @param maxVersion
     *            型番の上限
     * @param allowStructuredAppend
     *            複数シンボルへの分割を許可するには true を指定します。
     */
    public Symbols(ErrorCorrectionLevel ecLevel, int maxVersion, boolean allowStructuredAppend) {
        this(ecLevel, maxVersion, allowStructuredAppend, "Shift_JIS");
    }

    /**
     * インスタンスを初期化します。
     *
     * @param charsetName
     *            文字セット名
     */
    public Symbols(String charsetName) {
        this(ErrorCorrectionLevel.M, Constants.MAX_VERSION, false, charsetName);
    }

    /**
     * インスタンスを初期化します。
     *
     * @param ecLevel
     *            誤り訂正レベル
     * @param maxVersion
     *            型番の上限
     * @param allowStructuredAppend
     *            複数シンボルへの分割を許可するには true を指定します。
     * @param charsetName
     *            文字セット名
     */
    public Symbols(ErrorCorrectionLevel ecLevel, int maxVersion, boolean allowStructuredAppend, String charsetName) {
        if (!(Constants.MIN_VERSION <= maxVersion && maxVersion <= Constants.MAX_VERSION)) {
            throw new IllegalArgumentException("maxVersion");
        }

        _items = new ArrayList<Symbol>();

        _minVersion = 1;

        _errorCorrectionLevel = ecLevel;
        _maxVersion = maxVersion;
        _structuredAppendAllowed = allowStructuredAppend;
        _charset = Charset.forName(charsetName);

        _parity = 0;
        _currSymbol = new Symbol(this);

        _items.add(_currSymbol);

        _encNumeric = new NumericEncoder(_charset);
        _encAlpha = new AlphanumericEncoder(_charset);

        if (_charset.name().toLowerCase().equals("shift_jis")) {
            _encKanji = new KanjiEncoder(_charset);
        } else {
            _encKanji = null;
        }

        _encByte = new ByteEncoder(_charset);
    }

    /**
     * インデックス番号を指定してSymbolオブジェクトを取得します。
     */
    public Symbol get(int index) {
        return _items.get(index);
    }

    /**
     * シンボル数を取得します。
     */
    public int getCount() {
        return _items.size();
    }

    /**
     * 型番の下限を取得します。
     */
    protected int getMinVersion() {
        return _minVersion;
    }

    /**
     * 型番の下限を設定します。
     */
    protected void setMinVersion(int value) {
        _minVersion = value;
    }

    /**
     * 型番の上限を取得します。
     */
    protected int getMaxVersion() {
        return _maxVersion;
    }

    /**
     * 文字セットを取得します。
     */
    protected Charset getCharset() {
        return _charset;
    }

    /**
     * 誤り訂正レベルを取得します。
     */
    protected ErrorCorrectionLevel getErrorCorrectionLevel() {
        return _errorCorrectionLevel;
    }

    /**
     * 構造的連接モードの使用可否を取得します。
     */
    protected boolean getStructuredAppendAllowed() {
        return _structuredAppendAllowed;
    }

    /**
     * 構造的連接のパリティを取得します。
     */
    protected int getParity() {
        return _parity;
    }

    /**
     * シンボルを追加します。
     */
    private Symbol add() {
        _currSymbol = new Symbol(this);
        _items.add(_currSymbol);

        return _currSymbol;
    }

    /**
     * 文字列を追加します。
     */
    public void appendText(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("s");
        }

        for (int i = 0; i < s.length(); i++) {
            EncodingMode oldMode = _currSymbol.getCurrentEncodingMode();
            EncodingMode newMode;

            switch (oldMode) {
            case UNKNOWN:
                newMode = selectInitialMode(s, i);
                break;
            case NUMERIC:
                newMode = selectModeWhileInNumericMode(s, i);
                break;
            case ALPHA_NUMERIC:
                newMode = selectModeWhileInAlphanumericMode(s, i);
                break;
            case EIGHT_BIT_BYTE:
                newMode = selectModeWhileInByteMode(s, i);
                break;
            case KANJI:
                newMode = selectInitialMode(s, i);
                break;
            default:
                throw new InternalError();
            }

            if (newMode != oldMode) {
                if (!_currSymbol.trySetEncodingMode(newMode, s.charAt(i))) {
                    if (!_structuredAppendAllowed || _items.size() == 16) {
                        throw new IllegalArgumentException("String too long");
                    }

                    add();
                    newMode = selectInitialMode(s, i);
                    _currSymbol.trySetEncodingMode(newMode, s.charAt(i));
                }
            }

            if (!_currSymbol.tryAppend(s.charAt(i))) {
                if (!_structuredAppendAllowed || _items.size() == 16) {
                    throw new IllegalArgumentException("String too long");
                }

                add();
                newMode = selectInitialMode(s, i);
                _currSymbol.trySetEncodingMode(newMode, s.charAt(i));
                _currSymbol.tryAppend(s.charAt(i));
            }
        }
    }

    /**
     * 初期モードを決定します。
     *
     * @param s
     *            対象文字列
     * @param start
     *            評価を開始する位置
     */
    private EncodingMode selectInitialMode(String s, int start) {
        if (_encKanji != null) {
            if (_encKanji.inSubset(s.charAt(start))) {
                return EncodingMode.KANJI;
            }
        }

        if (_encByte.inExclusiveSubset(s.charAt(start))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        }

        if (_encAlpha.inExclusiveSubset(s.charAt(start))) {
            return selectModeWhenInitialDataAlphanumeric(s, start);
        }

        if (_encNumeric.inSubset(s.charAt(start))) {
            return selectModeWhenInitialDataNumeric(s, start);
        }

        throw new InternalError();
    }

    private EncodingMode selectModeWhenInitialDataAlphanumeric(String s, int start) {
        int cnt = 0;

        for (int i = start; i < s.length(); i++) {
            if (_encAlpha.inExclusiveSubset(s.charAt(i))) {
                cnt++;
            } else {
                break;
            }
        }

        int version = _currSymbol.getVersion();
        boolean flg;

        if (1 <= version && version <= 9) {
            flg = cnt < 6;
        } else if (10 <= version && version <= 26) {
            flg = cnt < 7;
        } else if (27 <= version && version <= 40) {
            flg = cnt < 8;
        } else {
            throw new InternalError();
        }

        if (flg) {
            if ((start + cnt) < s.length()) {
                if (_encByte.inSubset(s.charAt(start + cnt))) {
                    return EncodingMode.EIGHT_BIT_BYTE;
                }
            }
        }

        return EncodingMode.ALPHA_NUMERIC;
    }

    private EncodingMode selectModeWhenInitialDataNumeric(String s, int start) {
        int cnt = 0;

        for (int i = start; i < s.length(); i++) {
            if (_encNumeric.inSubset(s.charAt(i))) {
                cnt++;
            } else {
                break;
            }
        }

        int version = _currSymbol.getVersion();
        boolean flg;

        if (1 <= version && version <= 9) {
            flg = cnt < 4;
        } else if (10 <= version && version <= 26) {
            flg = cnt < 4;
        } else if (27 <= version && version <= 40) {
            flg = cnt < 5;
        } else {
            throw new InternalError();
        }

        if (flg) {
            if ((start + cnt) < s.length()) {
                if (_encByte.inExclusiveSubset(s.charAt(start + cnt))) {
                    return EncodingMode.EIGHT_BIT_BYTE;
                }
            }
        }

        if (1 <= version && version <= 9) {
            flg = cnt < 7;
        } else if (10 <= version && version <= 26) {
            flg = cnt < 8;
        } else if (27 <= version && version <= 40) {
            flg = cnt < 9;
        } else {
            throw new InternalError();
        }

        if (flg) {
            if ((start + cnt) < s.length()) {
                if (_encAlpha.inExclusiveSubset(s.charAt(start + cnt))) {
                    return EncodingMode.ALPHA_NUMERIC;
                }
            }
        }

        return EncodingMode.NUMERIC;
    }

    /**
     * 数字モードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param start
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInNumericMode(String s, int start) {
        if (_encKanji != null) {
            if (_encKanji.inSubset(s.charAt(start))) {
                return EncodingMode.KANJI;
            }
        }

        if (_encByte.inExclusiveSubset(s.charAt(start))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        }

        if (_encAlpha.inExclusiveSubset(s.charAt(start))) {
            return EncodingMode.ALPHA_NUMERIC;
        }

        return EncodingMode.NUMERIC;
    }

    /**
     * 英数字モードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param start
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInAlphanumericMode(String s, int start) {
        if (_encKanji != null) {
            if (_encKanji.inSubset(s.charAt(start))) {
                return EncodingMode.KANJI;
            }
        }

        if (_encByte.inExclusiveSubset(s.charAt(start))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        }

        if (mustChangeAlphanumericToNumeric(s, start)) {
            return EncodingMode.NUMERIC;
        }

        return EncodingMode.ALPHA_NUMERIC;
    }

    private boolean mustChangeAlphanumericToNumeric(String s, int start) {
        boolean ret = false;
        int cnt = 0;

        for (int i = start; i < s.length(); i++) {
            if (!_encAlpha.inSubset(s.charAt(i))) {
                break;
            }

            if (_encNumeric.inSubset(s.charAt(i))) {
                cnt++;
            } else {
                ret = true;
                break;
            }
        }

        if (ret) {
            int version = _currSymbol.getVersion();

            if (1 <= version && version <= 9) {
                ret = cnt >= 13;
            } else if (10 <= version && version <= 26) {
                ret = cnt >= 15;
            } else if (27 <= version && version <= 40) {
                ret = cnt >= 17;
            } else {
                throw new InternalError();
            }
        }

        return ret;
    }

    /**
     * バイトモードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param start
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInByteMode(String s, int start) {
        if (_encKanji != null) {
            if (_encKanji.inSubset(s.charAt(start))) {
                return EncodingMode.KANJI;
            }
        }

        if (mustChangeByteToNumeric(s, start)) {
            return EncodingMode.NUMERIC;
        }

        if (mustChangeByteToAlphanumeric(s, start)) {
            return EncodingMode.ALPHA_NUMERIC;
        }

        return EncodingMode.EIGHT_BIT_BYTE;
    }

    private boolean mustChangeByteToNumeric(String s, int start) {
        boolean ret = false;
        int cnt = 0;

        for (int i = start; i < s.length(); i++) {
            if (!_encByte.inSubset(s.charAt(i))) {
                break;
            }

            if (_encNumeric.inSubset(s.charAt(i))) {
                cnt++;
            } else if (_encByte.inExclusiveSubset(s.charAt(i))) {
                ret = true;
                break;
            } else {
                break;
            }
        }

        if (ret) {
            int version = _currSymbol.getVersion();

            if (1 <= version && version <= 9) {
                ret = cnt >= 6;
            } else if (10 <= version && version <= 26) {
                ret = cnt >= 8;
            } else if (27 <= version && version <= 40) {
                ret = cnt >= 9;
            } else {
                throw new InternalError();
            }
        }

        return ret;
    }

    private boolean mustChangeByteToAlphanumeric(String s, int start) {
        boolean ret = false;
        int cnt = 0;

        for (int i = start; i < s.length(); i++) {
            if (!_encByte.inSubset(s.charAt(i))) {
                break;
            }

            if (_encAlpha.inExclusiveSubset(s.charAt(i))) {
                cnt++;
            } else if (_encByte.inExclusiveSubset(s.charAt(i))) {
                ret = true;
                break;
            } else {
                break;
            }
        }

        if (ret) {
            int version = _currSymbol.getVersion();

            if (1 <= version && version <= 9) {
                ret = cnt >= 11;
            } else if (10 <= version && version <= 26) {
                ret = cnt >= 15;
            } else if (27 <= version && version <= 40) {
                ret = cnt >= 16;
            } else {
                throw new InternalError();
            }
        }

        return ret;
    }

    /**
     * 構造的連接のパリティを更新します。
     *
     * @param c
     *            パリティ計算対象の文字
     */
    void updateParity(char c) {
        byte[] charBytes;
        charBytes = String.valueOf(c).getBytes(_charset);

        for (byte b : charBytes) {
            _parity ^= Byte.toUnsignedInt(b);
        }
    }

    private int _curr = 0;

    @Override
    public boolean hasNext() {
        return _curr < _items.size();
    }

    @Override
    public Symbol next() {
        return _items.get(_curr++);
    }

    @Override
    public Iterator<Symbol> iterator() {
        return _items.iterator();
    }
}
