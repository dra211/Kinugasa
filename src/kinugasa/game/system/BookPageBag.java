/*
 * The MIT License
 *
 * Copyright 2022 Shinacho.
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
package kinugasa.game.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @vesion 1.0.0 - 2022/12/25_10:50:54<br>
 * @author Shinacho<br>
 */
public class BookPageBag {

	private Map<BookPage, Integer> map = new HashMap<>();

	public BookPageBag() {
	}

	public Map<BookPage, Integer> getMap() {
		return map;
	}

	public void addAll(List<BookPage> list) {
		for (BookPage p : list) {
			if (map.containsKey(p) && map.get(p) == 99) {
				continue;
			}
			if (map.containsKey(p)) {
				map.put(p, map.get(p) + 1);
			} else {
				map.put(p, 1);
			}

		}
	}

	public void add(BookPage p) {
		if (map.containsKey(p) && map.get(p) == 99) {
			return;
		}
		if (map.containsKey(p)) {
			map.put(p, map.get(p) + 1);
		} else {
			map.put(p, 1);
		}
	}

	public int size() {
		return map.size();
	}

}
