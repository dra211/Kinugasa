/*
 * Copyright (C) 2023 Shinacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kinugasa.game.system;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kinugasa.game.GraphicsContext;
import kinugasa.game.I18N;
import kinugasa.game.ui.MessageWindow;
import kinugasa.game.ui.ScrollSelectableMessageWindow;
import kinugasa.game.ui.SimpleMessageWindowModel;
import kinugasa.game.ui.Text;
import kinugasa.util.StringUtil;

/**
 *
 * @vesion 1.0.0 - 2022/12/24_11:50:01<br>
 * @author Shinacho<br>
 */
public class ActionDescWindow extends PCStatusWindow {

	private ScrollSelectableMessageWindow mw;
	private List<Status> s;

	public ActionDescWindow(int x, int y, int w, int h, List<Status> s) {
		super(x, y, w, h);
		this.s = s;
		mw = new ScrollSelectableMessageWindow(x, y, w, h, 23, false);
		mw.setLoop(true);
		updateText();
	}
	private int pcIdx;

	@Override
	public void setPcIdx(int pcIdx) {
		this.pcIdx = pcIdx;
		updateText();
	}

	@Override
	public void nextPc() {
		pcIdx++;
		if (pcIdx >= s.size()) {
			pcIdx = 0;
		}
		mw.reset();
		updateText();
	}

	@Override
	public void prevPc() {
		pcIdx--;
		if (pcIdx < 0) {
			pcIdx = s.size() - 1;
		}
		mw.reset();
		updateText();
	}

	@Override
	public int getPcIdx() {
		return pcIdx;
	}

	private void updateText() {
		//SSMWに文字列を設定

		List<Text> t = new ArrayList<>();
		t.add(new Text("<---" + I18N.get(GameSystemI18NKeys.Xの行動,
				GameSystem.getInstance().getPCbyID(s.get(pcIdx).getId()).getVisibleName()) + "--->"));
		t.add(new Text("----" + ActionType.攻撃.getVisibleName() + "----"));
		for (Action a : s.get(pcIdx).getActions()
				.stream()
				.filter(p -> p.isBattle())
				.filter(p -> p.getType() == ActionType.攻撃)
				.sorted()
				.collect(Collectors.toList())) {
			StringBuilder sb = new StringBuilder();
			sb.append("  ").append(a.getVisibleName());
			//魔法は魔法ウインドウで見れるので表示しない
			sb.append(Text.getLineSep());
			String[] desc = StringUtil.safeSplit(a.getDesc(), Text.getLineSep());
			for (var v : desc) {
				sb.append("    ");
				sb.append(v).append(Text.getLineSep());
			}
			sb.append("    ");
			sb.append("(")
					.append(I18N.get(GameSystemI18NKeys.範囲))
					.append(":")
					.append(s.get(pcIdx).getAreaWithEqip(a));

			if (a.getMainEvents().stream().map(p -> p.getAtkAttr())
					.filter(p -> p != null)
					.count() > 0) {
				sb.append("、")
						.append(I18N.get(GameSystemI18NKeys.属性))
						.append(":");
				sb.append(a.getMainEvents()
						.stream()
						.filter(p -> p.getAtkAttr() != null)
						.map(p -> p.getAtkAttr().getVisibleName())
						.distinct()
						.collect(Collectors.toList()));
			}

			int i = 0;
			if (!a.getMainEvents().isEmpty()) {
				i = a.getMainEvents()
						.stream()
						.mapToInt(p -> (int) (p.getValue()))
						.map(p -> Math.abs(p))
						.sum();
			}

			if (i != 0) {
				sb.append("、")
						.append(I18N.get(GameSystemI18NKeys.基礎威力))
						.append(":")
						.append(i);
			}
			sb.append(")");

			t.addAll(Text.split(new Text(sb.toString())));
		}
		t.add(new Text("----" + ActionType.魔法.getVisibleName() + "----"));
		for (Action a : s.get(pcIdx).getActions()
				.stream()
				.filter(p -> p.isBattle())
				.filter(p -> p.getType() == ActionType.魔法)
				.sorted()
				.collect(Collectors.toList())) {
			StringBuilder sb = new StringBuilder();
			sb.append("  ").append(a.getVisibleName());
			sb.append("(")
					.append(I18N.get(GameSystemI18NKeys.範囲))
					.append(":")
					.append(s.get(pcIdx).getAreaWithEqip(a));

			if (a.getMainEvents().stream().map(p -> p.getAtkAttr())
					.filter(p -> p != null)
					.count() > 0) {
				sb.append("、")
						.append(I18N.get(GameSystemI18NKeys.属性))
						.append(":");
				sb.append(a.getMainEvents()
						.stream()
						.filter(p -> p.getAtkAttr() != null)
						.map(p -> p.getAtkAttr().getVisibleName())
						.distinct()
						.collect(Collectors.toList()));
			}

			int i = 0;
			if (!a.getMainEvents().isEmpty()) {
				i = a.getMainEvents()
						.stream()
						.mapToInt(p -> (int) (p.getValue()))
						.map(p -> Math.abs(p))
						.sum();
			}

			if (i != 0) {
				sb.append("、")
						.append(I18N.get(GameSystemI18NKeys.基礎威力))
						.append(":")
						.append(i);
			}
			sb.append(")");

			t.addAll(Text.split(new Text(sb.toString())));
		}
		mw.setText(t);
	}

	@Override
	public void update() {
		mw.update();
	}

	@Override
	public ScrollSelectableMessageWindow getWindow() {
		return mw;
	}

	@Override
	public void next() {
		mw.nextSelect();
	}

	@Override
	public void prev() {
		mw.prevSelect();
	}

	@Override
	public void draw(GraphicsContext g
	) {
		if (!isVisible() || !isExist()) {
			return;
		}
		mw.draw(g);
	}
}
