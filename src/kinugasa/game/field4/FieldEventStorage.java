/*
 * The MIT License
 *
 * Copyright 2022 Dra.
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
package kinugasa.game.field4;

import java.awt.Point;
import java.util.Optional;
import kinugasa.resource.Disposable;
import kinugasa.resource.Nameable;
import kinugasa.resource.Storage;

/**
 *
 * @vesion 1.0.0 - 2022/11/08_19:24:33<br>
 * @author Dra211<br>
 */
public class FieldEventStorage extends Storage<FieldEvent> implements Nameable, Disposable {

	private String name;

	public FieldEventStorage(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void dispose() {
		clear();
	}

	public FieldEvent get(int x, int y) {
		return get(new D2Idx(x, y));
	}

	public FieldEvent get(D2Idx p) {
		for (FieldEvent e : this) {
			if (e.getLocation().equals(p)) {
				return e;
			}
		}
		return null;
	}

}
