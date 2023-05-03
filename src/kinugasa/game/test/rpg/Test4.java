/*
 * The MIT License
 *
 * Copyright 2023 Dra.
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
package kinugasa.game.test.rpg;

import java.util.List;
import kinugasa.game.GameManager;
import kinugasa.game.GameOption;
import kinugasa.game.GameTimeManager;
import kinugasa.game.GraphicsContext;
import kinugasa.game.LockUtil;
import kinugasa.game.input.InputState;
import kinugasa.game.input.InputType;
import kinugasa.game.input.Keys;
import kinugasa.game.ui.ScrollSelectableMessageWindow;
import kinugasa.game.ui.Text;

/**
 *
 * @vesion 1.0.0 - 2023/05/03_16:12:32<br>
 * @author Dra211<br>
 */
public class Test4 extends GameManager {

	public static void main(String[] args) {
		LockUtil.deleteAllLockFile();
		new Test4().gameStart();
	}

	private Test4() {
		super(GameOption.defaultOption());
	}

	private ScrollSelectableMessageWindow w = new ScrollSelectableMessageWindow(12, 12, 480, 250, 6);

	{
		w.setLoop(true);
		w.setText(List.of(
				new Text("あいうえお0"),
				new Text("あいうえお1"),
				new Text("あいうえお2"),
				new Text("あいうえお3"),
				new Text("あいうえお4"),
				new Text("あいうえお5"),
				new Text("あいうえお6"),
				new Text("あいうえお7"),
				new Text("あいうえお8"),
				new Text("あいうえお9"),
				new Text("あいうえお10"),
				new Text("あいうえお11")));
	}

	@Override
	protected void startUp() {
	}

	@Override
	protected void dispose() {
	}

	@Override
	protected void update(GameTimeManager gtm, InputState is) {
		if (is.isPressed(Keys.DOWN, InputType.SINGLE)) {
			w.nextSelect();
		}
		if (is.isPressed(Keys.UP, InputType.SINGLE)) {
			w.prevSelect();
		}
		w.update();
	}

	@Override
	protected void draw(GraphicsContext gc) {
		w.draw(gc);
	}

}
