/*
 * The MIT License
 *
 * Copyright 2021 shin211.
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
package kinugasa.game.test;

import kinugasa.game.GameManager;
import kinugasa.game.GameOption;
import kinugasa.game.GameTimeManager;
import kinugasa.game.GraphicsContext;
import kinugasa.game.I18N;
import kinugasa.game.LockUtil;
import kinugasa.game.field4.D2Idx;
import kinugasa.game.field4.FieldMap;
import kinugasa.game.field4.FieldMapCameraMode;
import kinugasa.game.field4.FieldMapCharacter;
import kinugasa.game.field4.FieldMapStorage;
import kinugasa.game.field4.FieldMapTile;
import kinugasa.game.field4.FourDirAnimation;
import kinugasa.game.field4.MapChipAttributeStorage;
import kinugasa.game.field4.MapChipSetStorage;
import kinugasa.game.field4.Node;
import kinugasa.game.field4.VehicleStorage;
import kinugasa.game.input.GamePadButton;
import kinugasa.game.input.GamePadStick;
import kinugasa.game.input.InputState;
import kinugasa.game.input.InputType;
import kinugasa.game.input.Keys;
import kinugasa.game.ui.FPSLabel;
import kinugasa.game.ui.FontModel;
import kinugasa.game.ui.MessageWindow;
import kinugasa.game.ui.SimpleMessageWindowModel;
import kinugasa.game.ui.SimpleTextLabelModel;
import kinugasa.game.ui.TextLabelSprite;
import kinugasa.game.ui.TextStorage;
import kinugasa.game.ui.TextStorageStorage;
import kinugasa.object.KVector;
import kinugasa.graphics.Animation;
import kinugasa.graphics.ColorChanger;
import kinugasa.graphics.ColorTransitionModel;
import kinugasa.graphics.FadeCounter;
import kinugasa.graphics.ImageUtil;
import kinugasa.graphics.SpriteSheet;
import kinugasa.object.FadeEffect;
import kinugasa.object.FourDirection;
import kinugasa.resource.KImage;
import kinugasa.resource.sound.Sound;
import kinugasa.resource.sound.SoundBuilder;
import kinugasa.resource.sound.SoundLoader;
import kinugasa.resource.sound.SoundStorage;
import kinugasa.util.FrameTimeCounter;

/**
 * ゲームのテスト実装です.
 *
 * @author shin211
 */
public class Test extends GameManager {

	public static void main(String... args) {
		LockUtil.deleteAllLockFile();
		new Test().gameStart(args);
	}

	private Test() {
		super(GameOption.defaultOption().setUseGamePad(true).setCenterOfScreen());
	}
	private TextLabelSprite operation;

	@Override
	protected void startUp() {
		SoundLoader.loadList("resource/bgm/BGM.csv");
		SoundLoader.loadList("resource/se/SE.csv");

		TextStorageStorage.getInstance().readFromXML("resource/field/data/text/000.xml");
		//--------------------------------------
		MapChipAttributeStorage.getInstance().readFromXML("resource/field/data/attr/ChipAttributes.xml");
		VehicleStorage.getInstance().readFromXML("resource/field/data/vehicle/01.xml");
		VehicleStorage.getInstance().setCurrentVehicle("WALK");
		MapChipSetStorage.getInstance().readFromXML("resource/field/data/chipSet/01.xml");
		MapChipSetStorage.getInstance().readFromXML("resource/field/data/chipSet/02.xml");
		FieldMapStorage.getInstance().readFromXML("resource/field/data/mapBuilder/builder.xml");
		int w = (int) ((float) (720 / 32 / 2) - 1);
		int h = (int) ((float) (480 / 32 / 2) + 1);
		fm = FieldMapStorage.getInstance().get("ズシ").build();
		fm.setCurrentIdx(new D2Idx(9, 9));

		fm.getCamera().setMode(FieldMapCameraMode.FREE);
		FieldMap.setDebugMode(true);
		// プレイヤーキャラクターの表示座標計算
		int screenW = FieldMapStorage.getScreenWidth();
		int screenH = FieldMapStorage.getScreenHeight();
		float x = screenW / 2 - 16;
		float y = screenH / 2 - 16;
		c = new FieldMapCharacter(x, y, 32, 32, new D2Idx(21, 21),
				new FourDirAnimation(
						new Animation(new FrameTimeCounter(12), new SpriteSheet("resource/char/chara2.png").rows(0, 32, 32).images()),
						new Animation(new FrameTimeCounter(12), new SpriteSheet("resource/char/chara2.png").rows(32, 32, 32).images()),
						new Animation(new FrameTimeCounter(12), new SpriteSheet("resource/char/chara2.png").rows(64, 32, 32).images()),
						new Animation(new FrameTimeCounter(12), new SpriteSheet("resource/char/chara2.png").rows(96, 32, 32).images())
				),
				FourDirection.NORTH
		);
		FieldMap.setPlayerCharacter(c);
		fm.getCamera().updateToCenter();
		//
		//----------------------------------------------------------------------
		//
		screenShot = new SoundBuilder("resource/se/screenShot.wav").builde().load();
		String operaionText = "(LS) " + I18N.translate("MOVE");
		operation = new TextLabelSprite(operaionText, new SimpleTextLabelModel(FontModel.DEFAULT.clone()), 420, 450);
		//
		ts = fm.getTextStorage();

	}
	FieldMap fm;
	FieldMapCharacter c;
	TextStorage ts;
	MessageWindow mw;
	FPSLabel fps = new FPSLabel(0, 12);
	private Sound screenShot;
	int stage = 0;
	FadeEffect effect;

	@Override
	protected void dispose() {
	}

	@Override
	protected void update(GameTimeManager gtm) {
		fps.setGtm(gtm);
		InputState is = InputState.getInstance();
		switch (stage) {
			case 0:
				if (is.isPressed(GamePadButton.A, InputType.SINGLE)) {
					FieldMapTile t = fm.getCurrentTile();
					if (t.hasInNode()) {
						effect = new FadeEffect(FieldMapStorage.getScreenWidth(), FieldMapStorage.getScreenHeight(),
								new ColorChanger(
										ColorTransitionModel.valueOf(0),
										ColorTransitionModel.valueOf(0),
										ColorTransitionModel.valueOf(0),
										new FadeCounter(0, +6)
								));
						if (mw != null) {
							mw.setVisible(false);
						}
						nextStage();
					}
				}
				//MW処理
				//会話開始
				// 会話送り、選択の処理
				if (fm.canTalk()) {
					String operaionText = "(A)" + I18N.translate("TALK") + " / " + "(LS)" + I18N.translate("MOVE");
					operation.setText(operaionText);
				} else {
					String operaionText = "(LS)" + I18N.translate("MOVE");
					operation.setText(operaionText);
				}
				if (is.isPressed(GamePadButton.A, InputType.SINGLE)) {
					if (mw != null && mw.isVisible()) {
						if (!mw.isAllVisible()) {
							mw.allText();
						} else if (mw.isChoice()) {
							if (mw.getChoiceOption().hasNext()) {
								mw.choicesNext();
							} else {
								fm.closeMessagWindow();
							}
						} else if (mw.hasNext()) {
							mw.next();
						} else {
							fm.closeMessagWindow();
						}
					} else if (fm.canTalk()) {
						mw = fm.talk();
					}
				}
				if (mw != null && mw.isChoice()) {
					if (is.isPressed(GamePadButton.POV_DOWN, InputType.SINGLE)) {
						mw.nextSelect();
					}
					if (is.isPressed(GamePadButton.POV_UP, InputType.SINGLE)) {
						mw.prevSelect();
					}
				}
				//テスト用カメラ処理
				if (is.isPressed(GamePadButton.LB, InputType.SINGLE)) {
					fm.getCamera().setMode(FieldMapCameraMode.FOLLOW_TO_CENTER);
				}
				if (is.isPressed(GamePadButton.RB, InputType.SINGLE)) {
					fm.getCamera().setMode(FieldMapCameraMode.FREE);
				}

				//フィールドマップのカメラ移動・・・メッセージウインドウが表示されている間は移動不可とする
				if (mw == null || !mw.isVisible()) {
					float speed = VehicleStorage.getInstance().getCurrentVehicle().getSpeed();
					fm.setVector(new KVector(is.getGamePadState().sticks.LEFT.getLocation(speed)));
					fm.move();
				}
				//NPC/MW更新
				fm.update();
				//プレイヤーキャラクターの向き更新
				if (mw == null || !mw.isVisible()) {
					if (fm.getCamera().getMode() == FieldMapCameraMode.FOLLOW_TO_CENTER) {
						if (!is.getGamePadState().sticks.LEFT.getLocation().equals(GamePadStick.NOTHING)) {
							if (is.getGamePadState().sticks.LEFT.is(FourDirection.EAST)) {
								c.to(FourDirection.EAST);
							} else if (is.getGamePadState().sticks.LEFT.is(FourDirection.WEST)) {
								c.to(FourDirection.WEST);
							}
							if (is.getGamePadState().sticks.LEFT.is(FourDirection.NORTH)) {
								c.to(FourDirection.NORTH);
							} else if (is.getGamePadState().sticks.LEFT.is(FourDirection.SOUTH)) {
								c.to(FourDirection.SOUTH);
							}
						}
					}
				}
				//スクリーンショット系の処理
				if (is.isPressed(Keys.M, InputType.SINGLE)) {
					KImage image = fm.createMiniMap(0.25f, false, false, true);
					ImageUtil.save("resource/test/miniMap.png", image.get());
					screenShot.stopAndPlay();
				}
				if (is.isPressed(Keys.F12, InputType.SINGLE)) {
					ImageUtil.screenShot("resource/test/screenShot.png", getWindow().getBounds());
					screenShot.stopAndPlay();
				}
				//エンカウント処理
				if (fm.isEncount()) {
					SoundStorage.getInstance().get("SE").get("効果音＿戦闘開始.wav").load().stopAndPlay();
				}
				break;
			case 1:
				effect.update();
				if (effect.isEnded()) {
					effect = new FadeEffect(FieldMapStorage.getScreenWidth(), FieldMapStorage.getScreenHeight(),
							new ColorChanger(
									ColorTransitionModel.valueOf(0),
									ColorTransitionModel.valueOf(0),
									ColorTransitionModel.valueOf(0),
									ColorTransitionModel.valueOf(255)
							));
					nextStage();
				}
				break;
			case 2:
				fm = fm.changeMap(fm.getCurrentTile().getNode());
				nextStage();
				break;
			case 3:
				effect = new FadeEffect(FieldMapStorage.getScreenWidth(), FieldMapStorage.getScreenHeight(),
						new ColorChanger(
								ColorTransitionModel.valueOf(0),
								ColorTransitionModel.valueOf(0),
								ColorTransitionModel.valueOf(0),
								new FadeCounter(255, -6)
						));
				nextStage();
				break;
			case 4:
				effect.update();
				if (effect.isEnded()) {
					nextStage();
				}
				break;

		}

	}

	private void nextStage() {
		System.out.println("MAIN STAGE : " + stage + " -> " + (stage + 1));
		stage++;
		if (stage == 5) {
			stage = 0;
		}
	}

	@Override
	protected void draw(GraphicsContext gc) {
		fm.draw(gc);
		if (mw != null) {
			mw.draw(gc);
		}
		operation.draw(gc);
		if (effect != null) {
			effect.draw(gc);
		}
		fps.draw(gc);
	}

}
