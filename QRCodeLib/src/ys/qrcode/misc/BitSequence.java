package ys.qrcode.misc;

import java.util.ArrayList;
import java.util.List;

public class BitSequence {
    private List<Byte> _buffer;

    private int _bitCounter;
    private int _space;

    public BitSequence() {
        clear();
    }

    /**
     * ビット数を取得します。
     */
    public int getLength() {
        return _bitCounter;
    }

    public void clear()
    {
        _buffer = new ArrayList<Byte>();
        _bitCounter = 0;
        _space      = 0;
    }

    /**
     * 指定のビット数でデータを追加します。
     *
     * @param data
     *            追加するデータ
     * @param length
     *            データのビット数
     */
    public void append(int data, int length) {
        int remainingLength = length;
        int remainingData = data;

        while (remainingLength > 0) {
            if (_space == 0) {
                _space = 8;
                _buffer.add((byte) 0);
            }

            byte tmp = _buffer.get(_buffer.size() - 1);

            if (_space < remainingLength) {
                tmp = (byte) (Byte.toUnsignedInt(tmp) | remainingData >>> (remainingLength - _space));
                _buffer.set(_buffer.size() - 1, tmp);

                remainingData &= (1 << (remainingLength - _space)) - 1;

                _bitCounter += _space;
                remainingLength -= _space;
                _space = 0;

            } else {
                tmp = (byte) (Byte.toUnsignedInt(tmp) | remainingData << (_space - remainingLength));
                _buffer.set(_buffer.size() - 1, tmp);

                _bitCounter += remainingLength;
                _space -= remainingLength;
                remainingLength = 0;
            }
        }
    }

    /**
     * データのバイト配列を返します。
     */
    public byte[] getBytes() {
        byte[] buf = new byte[_buffer.size()];

        for (int i = 0; i < _buffer.size(); i++) {
            buf[i] = _buffer.get(i);
        }

        return buf;
    }
}
