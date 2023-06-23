/*
 * The MIT License
 *
 * Copyright 2013 Shinacho.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package kinugasa.resource.sound;

/**
 * サウンドのループ位置を決定するためのフレーム数をカプセル化します.
 * <br>
 * <br>
 *
 * @version 1.0.0 - 2013/01/13_18:47:52<br>
 * @author Shinacho<br>
 */
public class LoopPoint {

	public static final int EOF = -1;
	/**
	 * サウンドの開始を表す定数です. これは0と同値です。<br>
	 */
	public static final int START = 0;
	//
	/**
	 * ファイルの終端まで再生した後、最初に戻る設定です.
	 */
	public static final LoopPoint END_TO_START = new LoopPoint(EOF, START);
	/**
	 * ループを使用しない設定です.
	 */
	public static final LoopPoint NO_USE = null;

	/**
	 * 文字列をもとにLoopPoint要素として使用できるint値を返します.
	 *
	 * @param valueString "EOF"又は"START"またはintを送信します。 大文字小文字は区別されません。<br>
	 * @return　対応するint値を返します。<br>
	 * @throws NumberFormatException 判定できない文字列を送信された場合に投げられます。<br>
	 */
	public static int valueOf(String valueString)
			throws NumberFormatException {
		if ("EOF".equals(valueString.toUpperCase())) {
			return EOF;
		}
		if ("START".equals(valueString.toUpperCase())) {
			return START;
		}
		return Integer.parseInt(valueString);
	}

	/**
	 * 時間をフレーム数に変換する. 戻り値はintに丸められます.<br>
	 *
	 * @param sec 時間を秒単位で指定.<br>
	 * @param freq 周波数.このクラスの定数を使用できる.<br>
	 *
	 * @return 指定された秒数のフレーム数.<br>
	 */
	public static int secToFrame(double sec, int freq) {
		return (int) (sec * freq);
	}
	//
	/**
	 * ループ開始位置.
	 */
	private int from;
	/**
	 * ループ時に戻った先の位置.
	 */
	private int to;

	/**
	 * ループ位置を作成.
	 *
	 * @param from ループ開始位置.<br>
	 * @param to ループ時に戻った先の位置.<br>
	 */
	public LoopPoint(int from, int to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * ループ開始位置を取得.
	 *
	 * @return ループ開始位置.<br>
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * ループ時に戻る位置を取得.
	 *
	 * @return ループ時に戻る位置.<br>
	 */
	public int getTo() {
		return to;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LoopPoint other = (LoopPoint) obj;
		if (this.from != other.from) {
			return false;
		}
		if (this.to != other.to) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = (int) (13 * hash + this.from);
		hash = (int) (13 * hash + this.to);
		return hash;
	}

	@Override
	public String toString() {
		return "LoopPoint{" + "from=" + from + ", to=" + to + '}';
	}
}
