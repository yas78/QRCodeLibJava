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
    private final Charset              _byteModeCharset;

    private Symbol _currSymbol;
    private int    _structuredAppendParity;

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
     * @param byteModeCharset
     *            バイトモードの文字エンコーディング
     */
    public Symbols(ErrorCorrectionLevel ecLevel, String byteModeCharset) {
        this(ecLevel, Constants.MAX_VERSION, false, byteModeCharset);
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
        this(40, allowStructuredAppend);
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
     * @param byteModeCharset
     *            バイトモードの文字エンコーディング
     */
    public Symbols(int maxVersion,
                   boolean allowStructuredAppend,
                   String byteModeCharset) {
        this(ErrorCorrectionLevel.M,
             maxVersion,
             allowStructuredAppend,
             byteModeCharset);
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
    public Symbols(ErrorCorrectionLevel ecLevel,
                   int maxVersion,
                   boolean allowStructuredAppend) {
        this(ecLevel, maxVersion, allowStructuredAppend, "Shift_JIS");
    }

    /**
     * インスタンスを初期化します。
     *
     * @param byteModeCharset
     *            バイトモードの文字エンコーディング
     */
    public Symbols(String byteModeCharset) {
        this(ErrorCorrectionLevel.M, 40, false, byteModeCharset);
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
     * @param byteModeCharset
     *            バイトモードの文字エンコーディング
     */
    public Symbols(ErrorCorrectionLevel ecLevel,
                   int maxVersion,
                   boolean allowStructuredAppend,
                   String byteModeCharset) {
        if (maxVersion < Constants.MIN_VERSION ||
            maxVersion > Constants.MAX_VERSION) {
            throw new IllegalArgumentException("maxVersion");
        }

        _items = new ArrayList<Symbol>();

        _minVersion = 1;

        _errorCorrectionLevel       = ecLevel;
        _maxVersion                 = maxVersion;
        _structuredAppendAllowed    = allowStructuredAppend;
        _byteModeCharset            = Charset.forName(byteModeCharset);

        _structuredAppendParity = 0;
        _currSymbol = new Symbol(this);

        _items.add(_currSymbol);
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
     * バイトモードの文字エンコーディングを取得します。
     */
    protected Charset getByteModeCharset() {
        return _byteModeCharset;
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
    protected int getStructuredAppendParity() {
        return _structuredAppendParity;
    }

    /**
     * シンボルを追加します。
     */
    private Symbol add() {
        assert _structuredAppendAllowed;
        assert _items.size() < 16;

        _currSymbol = new Symbol(this);
        _items.add(_currSymbol);

        return _currSymbol;
    }

    /**
     * 文字列を追加します。
     */
    public void appendString(String s) {
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
     * @param startIndex
     *            評価を開始する位置
     */
    private EncodingMode selectInitialMode(String s, int startIndex) {
        int version = _currSymbol.getVersion();

        if (KanjiEncoder.isInSubset(s.charAt(startIndex))) {
            return EncodingMode.KANJI;
        } else if (ByteEncoder.isInExclusiveSubset(s.charAt(startIndex))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        } else if (AlphanumericEncoder.isInExclusiveSubset(s.charAt(startIndex))) {
            int cnt = 0;
            boolean flg = false;

            for (int i = startIndex; i < s.length(); i++) {
                if (AlphanumericEncoder.isInExclusiveSubset(s.charAt(i))) {
                    cnt++;
                } else {
                    break;
                }
            }

            if (version >= 1 && version <= 9) {
                flg = cnt < 6;
            } else if (version >= 10 && version <= 26) {
                flg = cnt < 7;
            } else if (version >= 27 && version <= 40) {
                flg = cnt < 8;
            } else {
                throw new InternalError();
            }

            if (flg) {
                if ((startIndex + cnt) < s.length()) {
                    if (ByteEncoder.isInExclusiveSubset(s.charAt(startIndex + cnt))) {
                        return EncodingMode.EIGHT_BIT_BYTE;
                    } else {
                        return EncodingMode.ALPHA_NUMERIC;
                    }
                } else {
                    return EncodingMode.ALPHA_NUMERIC;
                }
            } else {
                return EncodingMode.ALPHA_NUMERIC;
            }
        } else if (NumericEncoder.isInSubset(s.charAt(startIndex))) {
            int cnt = 0;
            boolean flg1 = false;
            boolean flg2 = false;

            for (int i = startIndex; i < s.length(); i++) {
                if (NumericEncoder.isInSubset(s.charAt(i))) {
                    cnt++;
                } else {
                    break;
                }
            }

            if (version >= 1 && version <= 9) {
                flg1 = cnt < 4;
                flg2 = cnt < 7;
            } else if (version >= 10 && version <= 26) {
                flg1 = cnt < 4;
                flg2 = cnt < 8;
            } else if (version >= 27 && version <= 40) {
                flg1 = cnt < 5;
                flg2 = cnt < 9;
            } else {
                throw new InternalError();
            }

            if (flg1) {
                if ((startIndex + cnt) < s.length()) {
                    flg1 = ByteEncoder.isInExclusiveSubset(s.charAt(startIndex + cnt));
                } else {
                    flg1 = false;
                }
            }

            if (flg2) {
                if ((startIndex + cnt) < s.length()) {
                    flg2 = AlphanumericEncoder.isInExclusiveSubset(s.charAt(startIndex + cnt));
                } else {
                    flg2 = false;
                }
            }

            if (flg1) {
                return EncodingMode.EIGHT_BIT_BYTE;
            } else if (flg2) {
                return EncodingMode.ALPHA_NUMERIC;
            } else {
                return EncodingMode.NUMERIC;
            }
        } else {
            throw new InternalError();
        }
    }

    /**
     * 数字モードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param startIndex
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInNumericMode(String s, int startIndex) {
        if (ByteEncoder.isInExclusiveSubset(s.charAt(startIndex))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        } else if (KanjiEncoder.isInSubset(s.charAt(startIndex))) {
            return EncodingMode.KANJI;
        } else if (AlphanumericEncoder.isInExclusiveSubset(s.charAt(startIndex))) {
            return EncodingMode.ALPHA_NUMERIC;
        } else {
            return EncodingMode.NUMERIC;
        }
    }

    /**
     * 英数字モードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param startIndex
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInAlphanumericMode(String s, int startIndex) {
        int version = _currSymbol.getVersion();

        if (KanjiEncoder.isInSubset(s.charAt(startIndex))) {
            return EncodingMode.KANJI;
        } else if (ByteEncoder.isInExclusiveSubset(s.charAt(startIndex))) {
            return EncodingMode.EIGHT_BIT_BYTE;
        }

        int cnt = 0;
        boolean flg = false;

        for (int i = startIndex; i < s.length(); i++) {
            if (!AlphanumericEncoder.isInSubset(s.charAt(i))) {
                break;
            }

            if (NumericEncoder.isInSubset(s.charAt(i))) {
                cnt++;
            } else {
                flg = true;
                break;
            }
        }

        if (flg) {
            if (version >= 1 && version <= 9) {
                flg = cnt >= 13;
            } else if (version >= 10 && version <= 26) {
                flg = cnt >= 15;
            } else if (version >= 27 && version <= 40) {
                flg = cnt >= 17;
            } else {
                throw new InternalError();
            }

            if (flg) {
                return EncodingMode.NUMERIC;
            }
        }

        return EncodingMode.ALPHA_NUMERIC;
    }

    /**
     * バイトモードから切り替えるモードを決定します。
     *
     * @param s
     *            対象文字列
     * @param startIndex
     *            評価を開始する位置
     */
    private EncodingMode selectModeWhileInByteMode(String s, int startIndex) {
        int version = _currSymbol.getVersion();

        int cnt = 0;
        boolean flg = false;

        if (KanjiEncoder.isInSubset(s.charAt(startIndex))) {
            return EncodingMode.KANJI;
        }

        for (int i = startIndex; i < s.length(); i++) {
            if (!ByteEncoder.isInSubset(s.charAt(i))) {
                break;
            }

            if (NumericEncoder.isInSubset(s.charAt(i))) {
                cnt++;
            } else if (ByteEncoder.isInExclusiveSubset(s.charAt(i))) {
                flg = true;
                break;
            } else {
                break;
            }
        }

        if (flg) {
            if (version >= 1 && version <= 9) {
                flg = cnt >= 6;
            } else if (version >= 10 && version <= 26) {
                flg = cnt >= 8;
            } else if (version >= 27 && version <= 40) {
                flg = cnt >= 9;
            } else {
                throw new InternalError();
            }

            if (flg) {
                return EncodingMode.NUMERIC;
            }
        }

        cnt = 0;
        flg = false;

        for (int i = startIndex; i < s.length(); i++) {
            if (!ByteEncoder.isInSubset(s.charAt(i))) {
                break;
            }

            if (AlphanumericEncoder.isInExclusiveSubset(s.charAt(i))) {
                cnt++;
            } else if (ByteEncoder.isInExclusiveSubset(s.charAt(i))) {
                flg = true;
                break;
            } else {
                break;
            }
        }

        if (flg) {
            if (version >= 1 && version <= 9) {
                flg = cnt >= 11;
            } else if (version >= 10 && version <= 26) {
                flg = cnt >= 15;
            } else if (version >= 27 && version <= 40) {
                flg = cnt >= 16;
            } else {
                throw new InternalError();
            }

            if (flg) {
                return EncodingMode.ALPHA_NUMERIC;
            }
        }

        return EncodingMode.EIGHT_BIT_BYTE;
    }

    /**
     * 構造的連接のパリティを更新します。
     *
     * @param c
     *      パリティ計算対象の文字
     */
    void updateParity(char c) {

        byte[] charBytes = String.valueOf(c).getBytes(_byteModeCharset);

        for (int i = 0; i < charBytes.length; i++) {
            _structuredAppendParity ^= Byte.toUnsignedInt(charBytes[i]);
        }
    }

    private int curr = 0;

    @Override
    public boolean hasNext() {
        return curr < _items.size();
    }

    @Override
    public Symbol next() {
        return _items.get(curr++);
    }

    @Override
    public Iterator<Symbol> iterator() {
        return _items.iterator();
    }
}
