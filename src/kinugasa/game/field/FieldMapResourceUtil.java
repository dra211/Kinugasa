/*
 * The MIT License
 *
 * Copyright 2015 Dra.
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
package kinugasa.game.field;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import kinugasa.game.GameLog;
import kinugasa.graphics.ImageUtil;
import kinugasa.resource.ContentsIOException;
import kinugasa.resource.text.CSVFile;

/**
 * フィールドマップのリソースファイル作成時のユーティリティです.
 * <br>
 *
 * @version 1.0.0 - 2015/01/04<br>
 * @author Dra<br>
 * <br>
 */
public class FieldMapResourceUtil {

	/**
	 * .
	 * <br>
	 * Type1かつ、パーツが16bitである必要があります。<br>
	 * <br>
	 *
	 * @param input
	 * @param output
	 * @throws ContentsIOException
	 */
	public static void platinumCsvType1ToKGCsv(File input, File output)
			throws ContentsIOException {
		if (!input.exists() | output.exists()) {
			throw new ContentsIOException("File is Already Exists");
		}

		CSVFile reader = new CSVFile(input);
		CSVFile writer = new CSVFile(output);

		String[] convertTable = new String[65536];
		for (int i = 0, k = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				String y = Integer.toString(i);
				while (y.length() != 3) {
					y = "0" + y;
				}
				String x = Integer.toString(j);
				while (x.length() != 3) {
					x = "0" + x;
				}
				convertTable[k++] = y + x;
			}
		}
		List<String[]> inputData = reader.load().getData();

		for (String[] inputLine : inputData) {
			String[] outputLine = new String[inputLine.length];
			for (int j = 0; j < inputLine.length; j++) {
				outputLine[j] = convertTable[Integer.parseInt(inputLine[j].trim())];
			}
			writer.add(outputLine);
		}
		reader.dispose();
		writer.save();
		GameLog.printInfo("変換は正常に終了しました");
	}

	/**
	 * splitAsMapN(3桁)で切り出せる画像のIDのセットを出力します。 これはチップセットXMLを構築する際に有用です。<br>
	 *
	 * @param filePath チップセット画像のパス<br>
	 */
	public static void printImageId(String filePath, int w, int h) {
		BufferedImage image = ImageUtil.load(filePath);

		Map<String, BufferedImage> map = ImageUtil.splitAsMapN(image, w, h, 3);
		List<String> keyList = new ArrayList<String>(map.keySet());
		Collections.sort(keyList);
		for (int i = 0, size = keyList.size(); i < size; i++) {
			System.out.println(keyList.get(i));
		}
	}

	public static void main(String[] args) {
		String path = "resource/field/data/mapData/01.csv";
		String path2 = "resource/field/data/mapData/01_1.csv";
		FieldMapResourceUtil.platinumCsvType1ToKGCsv(new File(path), new File(path2));
	}
}
