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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kinugasa.game.I18N;
import kinugasa.game.LoopCall;
import kinugasa.game.NoLoopCall;
import kinugasa.game.ui.ScrollSelectableMessageWindow;
import kinugasa.game.ui.Text;

/**
 *
 * @vesion 1.0.0 - 2022/11/23_15:37:41<br>
 * @author Shinacho<br>
 */
public class BattleCommandMessageWindow extends ScrollSelectableMessageWindow implements CommandWindow {

	private BattleCommand cmd;

	public BattleCommandMessageWindow(int x, int y, int w, int h) {
		super(x, y, w, h, 7, false);
		setLoop(true);
		updateText();
	}
	private int typeIdx = ActionType.values().length - 1;
	private ActionType type = ActionType.OTHER;
	private CmdAction selected;

	public void resetSelect() {
		setType(ActionType.OTHER);
		reset();
	}

	public void setType(ActionType t) {
		typeIdx = t.ordinal();
		type = ActionType.values()[typeIdx];
		updateText();
	}

	public void nextType() {
		typeIdx++;
		if (typeIdx >= ActionType.values().length) {
			typeIdx = 0;
		}
		type = ActionType.values()[typeIdx];
		updateText();
		if (GameSystem.isDebugMode()) {
			System.out.println("BCMW :" + selected);
		}
		setCurrent();
	}

	public void prevType() {
		typeIdx--;
		if (typeIdx < 0) {
			typeIdx = ActionType.values().length - 1;
		}
		type = ActionType.values()[typeIdx];
		updateText();
		if (GameSystem.isDebugMode()) {
			System.out.println("BCMW :" + selected);
		}
		setCurrent();
	}

	private void setCurrent() {
		GameSystem.getInstance().getBattleSystem().getTargetSystem().setCurrent(this);
	}

	@Override
	public CmdAction getSelectedCmd() {
		assert cmd != null : "BCMW cmd is null";
		return selected;
	}

	public boolean isSelected(String name) {
		return selected.getName().equals(name);
	}

	public BattleCommand getCmd() {
		return cmd;
	}

	public void setCmd(BattleCommand cmd) {
		this.cmd = cmd;
		updateText();
	}

	@LoopCall
	@Override
	public void update() {
		super.update();
	}

	@NoLoopCall
	private void updateText() {
		if (cmd == null) {
			if (GameSystem.isDebugMode()) {
				System.out.println("BCMW : cmd is null");
			}
			return;
		}
		String text = cmd.getUser().getStatus().getName();
		text = I18N.translate("WHATDO") + ", " + text + " ! ! ";
		text += "         <------------------[ " + type.displayName() + " ]------------------>";
		text += Text.getLineSep();
		int i = 0;
		List<CmdAction> actionList = cmd.getBattleActionOf(type);
		//アイテム以外の場合はバトル利用可能なアクションにフィルター
		if (type != ActionType.ITEM) {
			actionList = actionList.stream().filter(p -> p.isBattleUse()).collect(Collectors.toList());
		}

		Collections.sort(actionList);
		if (actionList.isEmpty()) {
			switch (type) {
				case ATTACK:
				case OTHER:
					throw new GameSystemException("ATTACK,OTHER action is empty");
				case ITEM:
					text += I18N.translate("NOTHING_ITEM") + Text.getLineSep();
					break;
				case MAGIC:
					text += I18N.translate("NOTHING_MAGIC") + Text.getLineSep();
					break;
				default:
					throw new AssertionError("BattleCommandMessageWindow : undefined type");
			}
			List<Text> t = Text.split(new Text(text));
			setText(t);
			super.getWindow().allText();
			selected = null;
			setCurrent();
			return;
		}

		selected = actionList.get(0);
		for (CmdAction b : actionList) {
			switch (type) {
				case ATTACK:
					text += b.getName() + ":" + b.getDesc();
					text += ("、")
							+ (I18N.translate("ACTION_ATTR"))
							+ (":");
					text += (b.getBattleEvent()
							.stream()
							.filter(p -> !AttrDescWindow.getUnvisibleAttrName().contains(p.getAttr().getName()))
							.map(p -> p.getAttr().getDesc())
							.distinct()
							.collect(Collectors.toList()));
					text += ("、")
							+ (I18N.translate("ACTION_EFFECT"))
							+ (":");
					//ENEMYが入っている場合、minを、そうでない場合はMAXを取る
					if (b.getBattleEvent().stream().anyMatch(p -> p.getTargetType().toString().contains("ENEMY"))) {
						text += (Math.abs(b.getBattleEvent()
								.stream()
								.mapToInt(p -> (int) (p.getValue()))
								.min()
								.getAsInt()));
					} else {
						text += (Math.abs(b.getBattleEvent()
								.stream()
								.mapToInt(p -> (int) (p.getValue()))
								.max()
								.getAsInt()));
					}
					text += Text.getLineSep();
					break;
				case ITEM:
					Item item = (Item) b;
					String e = (item.getEqipmentSlot() != null && getCmd().getUser().getStatus().isEqip(item.getName()))
							? "(E)"
							: "";
					text += e + b.getName() + Text.getLineSep();
					break;
				case MAGIC:
					String status = "";
					for (String s : BattleConfig.getMagicVisibleStatusKey()) {
						Map<StatusKey, Integer> map = b.selfBattleDirectDamage();
						int val = map.containsKey(StatusKeyStorage.getInstance().get(s)) ? map.get(StatusKeyStorage.getInstance().get(s)) : 0;
						status += " " + StatusKeyStorage.getInstance().get(s).getDesc() + ":" + val;
					}
					status += " (" + I18N.translate("SPELLTIME") + ":" + b.getSpellTime() + I18N.translate("TURN") + ")";
					text += b.getName() + " : " + status + Text.getLineSep();
					break;
				case OTHER:
					text += b.getName() + Text.getLineSep();
					break;
				default:
					throw new AssertionError("BCMW undefined type");
			}

			i++;
		}

		List<Text> t = Text.split(new Text(text));
		setText(t);
		super.getWindow().allText();
	}

	public void nextAction() {
		List<CmdAction> actionList = cmd.getBattleActionOf(type);
		if (actionList.isEmpty()) {
			return;
		}
		super.nextSelect();
		Collections.sort(actionList);
		selected = actionList.get(getSelectedIdx() - 1);
		if (GameSystem.isDebugMode()) {
			System.out.println("MCMW :" + selected);
		}
		setCurrent();
	}

	public void prevAction() {
		super.prevSelect();
		List<CmdAction> actionList = cmd.getBattleActionOf(type);
		if (actionList.isEmpty()) {
			return;
		}
		Collections.sort(actionList);
		selected = actionList.get(getSelectedIdx() - 1);
		if (GameSystem.isDebugMode()) {
			System.out.println("MCMW :" + selected);
		}
		setCurrent();
	}

}
