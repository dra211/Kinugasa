/*
 * The MIT License
 *
 * Copyright 2023 Shinacho.
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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kinugasa.game.GameLog;
import kinugasa.game.GraphicsContext;
import kinugasa.game.I18N;
import kinugasa.game.LoopCall;
import kinugasa.game.NoLoopCall;
import kinugasa.game.field4.PlayerCharacterSprite;
import kinugasa.game.field4.VehicleStorage;
import kinugasa.game.system.BattleCommand;
import kinugasa.game.system.BattleFieldSystem;
import kinugasa.game.system.BattleMessageWindowSystem;
import kinugasa.game.system.BattleResultValues;
import kinugasa.game.system.BattleSystem;
import kinugasa.game.system.BattleTargetSystem;
import kinugasa.game.system.ConditionManager;
import kinugasa.game.system.EncountInfo;
import kinugasa.game.system.Enemy;
import kinugasa.game.system.EnemySet;
import kinugasa.game.system.EnemySetStorage;
import kinugasa.game.system.GameSystem;
import kinugasa.game.system.MagicSpell;
import static kinugasa.game.system.TargetType.SELF;
import kinugasa.game.ui.Text;
import kinugasa.object.AnimationSprite;
import kinugasa.object.BasicSprite;
import kinugasa.object.Drawable;
import kinugasa.object.FourDirection;
import kinugasa.object.KVector;
import kinugasa.object.Sprite;
import kinugasa.resource.sound.Sound;
import kinugasa.resource.sound.SoundStorage;
import kinugasa.util.FrameTimeCounter;
import kinugasa.util.Random;

/**
 *
 * @vesion 1.0.0 - 2023/05/10_14:07:36<br>
 * @author Shinacho<br>
 */
public class BattleSystem implements Drawable {

	private static final BattleSystem INSTANCE = new BattleSystem();

	public static BattleSystem getInstance() {
		return INSTANCE;
	}

	private BattleSystem() {
	}

	//ターン数
	private int turn = 0;
	//--------------------------------------------------------初期化・終了化
	//プレイヤ戦闘開始前位置
	private List<Point2D.Float> partyInitialLocation = new ArrayList<>();
	//プレイヤ戦闘開始前向き
	private List<FourDirection> partyInitialDir = new ArrayList<>();
	//プレイヤ初期移動目標座標
	private List<Point2D.Float> partyTargetLocationForFirstMove = new ArrayList<>();
	//戦闘開始前BGM
	private Sound prevBGM;
	//戦闘BGM
	private Sound currentBGM;
	//勝利遷移ロジック名、敗北遷移ロジック名
	private String winLogicName, loseLogicName;
	//--------------------------------------------------------表示中・実行中
	//敵のスプライトとステータス
	private List<Enemy> enemies = new ArrayList<>();
	//このターンのバトルコマンド順序
	private LinkedList<BattleCommand> commandsOfThisTurn = new LinkedList<>();
	//このターンのバトルコマンド順序
	private LinkedHashMap<Integer, List<MagicSpell>> magics = new LinkedHashMap<>();
	//表示中バトルアクション・アニメーション
	private List<AnimationSprite> animation = new ArrayList<>();
	//実行中バトルアクションから生成されたアクション待機時間
	private FrameTimeCounter currentBAWaitTime;
	//行動中コマンド
	private BattleCommand currentCmd;
	//ActionMessage表示時間
	private int messageWaitTime = 66;
	//戦闘結果
	private BattleResultValues battleResultValue = null;
	//カレントBAのNPC残移動ポイント
	private int remMovePoint;
	//移動開始時の位置
	private Point2D.Float moveIinitialLocation;
	//現在のステージ
	private StageHolder stage;
	//-------------------------------------------------------------------システム
	//メッセージウインドウシステムのインスタンス
	private BattleMessageWindowSystem messageWindowSystem;
	//ターゲット選択システムのインスタンス
	private BattleTargetSystem targetSystem;
	//バトルフィールドインスタンス
	private BattleFieldSystem battleFieldSystem;
	//状態異常マネージャ
	private ConditionManager conditionManager;
	//バトル終了フラグ（true=終了
	private boolean end = false;
	//戦況図モードかどうか
	private boolean showMode = false;

	class StageHolder {

		private Stage stage;

		public void setStage(Stage stage) {
			if (GameSystem.isDebugMode()) {
				System.out.println(" changeStage : [" + this.stage + "] to [" + stage + "]");
			}
			if (stage == Stage.EXECUTING_ACTION) {
				if (currentBAWaitTime == null) {
					currentBAWaitTime = new FrameTimeCounter(messageWaitTime);
				}
			}
			this.stage = stage;
		}

		public Stage getStage() {
			return stage;
		}

	}

	public enum Stage {
		/**
		 * 開始?初期移動まで
		 */
		STARTUP,
		/**
		 * 初期移動中
		 */
		INITIAL_MOVING,
		/**
		 * ユーザ操作待ち
		 */
		WAITING_EXEC_CMD,
		/**
		 * コマンド選択中
		 */
		CMD_SELECT,
		/**
		 * 逃げアニメーション実行中。終わったらWAITに入る。
		 */
		ESCAPING,
		/**
		 * プレイヤーキャラクタ移動中。確定アクションが呼ばれるまで何もしない。
		 */
		PLAYER_MOVE,
		/**
		 * ターゲット選択中。execが呼ばれるまで何もしない。
		 */
		TARGET_SELECT,
		/**
		 * 自動処理実行中。アクションメッセージが表示されるので、OKを押すと次に進む。
		 */
		EXECUTING_ACTION,
		/**
		 * ステータス閲覧中
		 */
		SHOW_STATUS,
		/**
		 * アイテム詳細確認中
		 */
		SHOW_ITEM,
		/**
		 * 敵移動実行中。終わったらWAITに入る。
		 */
		EXECUTING_MOVE,
		/**
		 * バトルは終了して、ゲームシステムからの終了指示を待っている。
		 */
		BATLE_END,
		/**
		 * アイテム用途選択画面
		 */
		ITEM_CHOICE_USE,
	}

	private enum MessageType {
		INITIAL_ENEMY_INFO {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		SPELL_BUT_NO_TARGET {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		SPELL_BUT_SHORTAGE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		SPELL_SUCCESS {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		STOPING_BY_CONDITION {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		STOP_BECAUSE_CONFU {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		IS_MOVED {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_USE_AVO {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_USE_DEFENCE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_IS_MOVE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_IS_COMMIT_MOVE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_IS_SHOW_STATUS {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_IS_ESCAPE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		PC_IS_ESCAPE_MISS {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		NO_TARGET {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}

		},
		TARGET_SELECT {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		EQIP_ITEM {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		UNEQIP_ITEM {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		CANT_EQIP {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		ITEM_WHO_TO_USE {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		ITEM_WHO_TO_PASS {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		ITEM_WHO_TO_THROW {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		SPELL_START {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		ACTION_SUCCESS {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
			}
		},
		BATTLE_RESULT {
			@Override
			String get(CmdAction a, Status user, List<String> option, ActionResult res) {
				return toString();
				//battleResultValueに結果が入っている
			}
		},;

		abstract String get(CmdAction a, Status user, List<String> option, ActionResult res);
	}

	//---------------------------------------UTIL--------------------------------------
	@Deprecated
	public void setBattleResultValue(BattleResultValues battleResultValue) {
		this.battleResultValue = battleResultValue;
	}

	/**
	 * 現在のステージを取得します
	 *
	 * @return ステージ
	 */
	public Stage getStage() {
		return stage.getStage();
	}

	/**
	 * 現在のターン数を取得します。
	 *
	 * @return ターン数、1スタート。
	 */
	public int getTurn() {
		return turn;
	}

	private List<BattleCharacter> getAllChara() {
		List<BattleCharacter> result = new ArrayList<>();
		result.addAll(enemies);
		result.addAll(GameSystem.getInstance().getParty());
		return result;
	}

	//------------------------------------初期化---------------------------------------
	public void encountInit(EncountInfo enc) {
		if (GameSystem.isDebugMode()) {
			System.out.println(" -----------------BATTLE_START------------------------------------------------");
		}
		stage = new StageHolder();
		stage.setStage(BattleSystem.Stage.STARTUP);
		//エンカウント情報の取得
		EnemySetStorage ess = enc.getEnemySetStorage().load();
		EnemySet es = ess.get();
		//前BGMの停止
		prevBGM = enc.getPrevBGM();
		if (prevBGM != null) {
			switch (es.getPrevBgmMode()) {
				case NOTHING:
					break;
				case PAUSE:
					prevBGM.pause();
					break;
				case STOP:
					prevBGM.stop();
					break;
				case STOP_AND_PLAY:
					prevBGM.stopAndPlay();
					break;
			}
		}
		//バトルBGMの再生
		if (es.hasBgm()) {
			SoundStorage.getInstance().get(es.getBgmMapName()).stopAll();
			currentBGM = es.getBgm().load();
			currentBGM.stopAndPlay();
		}
		//敵取得
		enemies = es.create();
		ess.dispose();
		//初期化
		GameSystem gs = GameSystem.getInstance();
		battleFieldSystem = BattleFieldSystem.getInstance();
		battleFieldSystem.init(enc.getChipAttribute());
		targetSystem = BattleTargetSystem.getInstance();
		targetSystem.init();
		messageWindowSystem = BattleMessageWindowSystem.getInstance();
		messageWindowSystem.init();
		conditionManager = ConditionManager.getInstance();
		//念のためパーティーのアクションを更新
		GameSystem.getInstance().getPartyStatus().forEach(p -> p.updateAction());

		//出現MSG設定用マップ
		Map<String, Long> enemyNum = enemies.stream().collect(Collectors.groupingBy(Enemy::getId, Collectors.counting()));
		//出現MSG設定
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Long> e : enemyNum.entrySet()) {
			sb.append(e.getKey()).append(I18N.translate("WAS")).append(e.getValue()).append(I18N.translate("APPEARANCE")).append(Text.getLineSep());
		}
		//敵出現情報をセット
		setMsg(MessageType.INITIAL_ENEMY_INFO, List.of(sb.toString()));
		messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.ACTION,
				BattleMessageWindowSystem.InfoVisible.OFF);

		//リセット
		currentCmd = null;
		currentBAWaitTime = null;
		commandsOfThisTurn.clear();
		turn = 0;
		animation.clear();
		targetSystem.unsetCurrent();

		//敵の配置
		putEnemy();

		//味方の配置
		putParty();
		assert partyTargetLocationForFirstMove.size() == gs.getParty().size() : "initial move target is missmatch";

		//初期移動実行へ
		stage.setStage(BattleSystem.Stage.INITIAL_MOVING);
	}

	private void putParty() {
		GameSystem gs = GameSystem.getInstance();
		partyInitialDir.clear();
		partyInitialLocation.clear();
		partyTargetLocationForFirstMove.clear();

		//戦闘開始前位置・向き退避
		List<PlayerCharacterSprite> partySprite = gs.getPartySprite();
		List<Status> partyStatus = gs.getPartyStatus();
		for (BasicSprite s : partySprite) {
			partyInitialDir.add(s.getVector().round());
			partyInitialLocation.add(s.getLocation());
		}

		int size = partySprite.get(0).getImageHeight();
		//配置
		float y = battleFieldSystem.getPartyArea().y + battleFieldSystem.getPartyArea().height / (partySprite.size() + 1) - size;
		for (int i = 0; i < partySprite.size(); i++) {
			float x = partyStatus.get(i).getPartyLocation() == PartyLocation.FRONT
					? battleFieldSystem.getPartyArea().x
					: battleFieldSystem.getPartyArea().x + battleFieldSystem.getPartyArea().width - size;
			partyTargetLocationForFirstMove.add(new Point2D.Float(x, y));
			partySprite.get(i).setLocation(x + 200, y);
			partySprite.get(i).to(FourDirection.WEST);
			partySprite.get(i).setVector(new KVector(KVector.WEST, VehicleStorage.getInstance().get(BattleConfig.initialPCMoveVehicleKey).getSpeed()));
			size = partySprite.get(i).getImageHeight();
			y += size * 2;
		}

		//アイテム使用をアクションに追加する
		for (PlayerCharacter pc : gs.getParty()) {
			pc.getStatus().getActions().addAll(pc.getStatus().getItemBag().getItems());
		}
	}

	private void putEnemy() {
		List<Sprite> checkList = new ArrayList<>();
		for (Enemy e : enemies) {
			float w = e.getSprite().getWidth();
			float h = e.getSprite().getHeight();
			L2:
			do {
				e.getSprite().setLocation(Random.randomLocation(battleFieldSystem.getEnemytArea(), w, h));

				boolean hit = false;
				for (Sprite ee : checkList) {
					hit |= e.getSprite().hit(ee);
				}
				for (Sprite os : battleFieldSystem.getObstacle()) {
					hit |= e.getSprite().hit(os);
				}
				if (!hit) {
					break L2;
				}
			} while (true);
			checkList.add(e.getSprite());
		}

		Collections.sort(enemies, (Enemy o1, Enemy o2) -> (int) (o1.getSprite().getY() - o2.getSprite().getY()));
	}

	//------------------------------------------PP処理M------------------------------------
	void turnStart() {
		turn++;
		if (GameSystem.isDebugMode()) {
			System.out.println(" -----------------TURN[" + turn + "] START-----------------");
		}
		//このターンのバトルコマンドを作成
		List<BattleCharacter> list = getAllChara();
		if (SpeedCalcModelStorage.getInstance().getCurrent() == null) {
			throw new GameSystemException("speed calc model is null");
		}
		SpeedCalcModelStorage.getInstance().getCurrent().sort(list);

		//行動順にバトルコマンドを格納
		assert commandsOfThisTurn.isEmpty() : "turnStart:cmd is not empty";
		for (BattleCharacter c : list) {
			BattleCommand.Mode mode = c.isPlayer() ? BattleCommand.Mode.PC : BattleCommand.Mode.CPU;
			commandsOfThisTurn.add(new BattleCommand(mode, c));
		}

		//このターンの魔法詠唱完了イベントをランダムな位置に割り込ませる
		List<MagicSpell> ms = magics.get(turn);
		if (ms != null) {
			if (!ms.isEmpty()) {
				//commandsOfThisTurn
				for (MagicSpell s : ms) {
					//魔法実行イベントをランダムな位置に割り込ませる
					BattleCommand bc = new BattleCommand(s.isPlayer()
							? BattleCommand.Mode.PC
							: BattleCommand.Mode.CPU,
							s.getUser())
							.setAction(Arrays.asList(s.getAction()))
							.setMagicSpell(true);
					int idx = Random.randomAbsInt(commandsOfThisTurn.size());
					//割り込ませるユーザの通常アクションを破棄する
					BattleCommand remove = null;
					for (BattleCommand c : commandsOfThisTurn) {
						if (c.getUser().equals(bc.getUser())) {
							remove = c;
						}
					}
					//ユーザがアンターゲット状態の場合、コマンドは破棄
					if (remove != null) {
						if (!remove.getUser().getStatus().hasConditions(false, BattleConfig.getUntargetConditionNames())) {
							commandsOfThisTurn.remove(remove);
						}
						//削除してから割り込み実行
						commandsOfThisTurn.add(idx, bc);
					}
				}
				//詠唱中リストからこのターンのイベントを削除
				magics.remove(turn);
			}
		}
		//PC・NPCの状態異常の経過ターン更新・継続ダメージ処理
		updateCondition();

		//このターン行動可否をコマンドに設定
		for (BattleCommand cmd : commandsOfThisTurn) {
			if (cmd.getUser().getStatus().isConfu()) {
				//混乱
				cmd.setConfu(true);
			}
			if (!cmd.getUser().getStatus().canMoveThisTurn()) {
				//その他行動不能の状態異常
				cmd.setStop(true);
			}
		}

		stage.setStage(BattleSystem.Stage.WAITING_EXEC_CMD);
	}

	@NoLoopCall
	private void updateCondition() {
		//HPが0になったときなどの状態異常を付与する
		conditionManager.setCondition(GameSystem.getInstance().getPartyStatus());
		conditionManager.setCondition(enemies.stream().map(p -> p.getStatus()).collect(Collectors.toList()));
		//スプライトの非表示化処理
		//アンターゲットコンディション発生中のユーザによるコマンドを除去
		List<BattleCommand> remove = new ArrayList<>();
		for (BattleCommand cmd : commandsOfThisTurn) {
			if (cmd.getUser().getStatus().hasConditions(false, BattleConfig.getUntargetConditionNames())) {
				remove.add(cmd);
				cmd.getUser().getSprite().setVisible(false);
			}
		}
		commandsOfThisTurn.removeAll(remove);

		//状態異常の効果時間を引く
		enemies.stream().map(p -> p.getStatus()).forEach(p -> p.update());
		GameSystem.getInstance().getPartyStatus().forEach(p -> p.update());

	}
	//
	//--------------------------------END_BATTLE------------------------------------------
	//

	void endBattle() {
		//味方の配置の初期化
		List<PlayerCharacterSprite> partySprite = GameSystem.getInstance().getPartySprite();
		for (int i = 0; i < partySprite.size(); i++) {
			partySprite.get(i).to(partyInitialDir.get(i));
			partySprite.get(i).setLocation(partyInitialLocation.get(i));
		}
		//敵番号の初期化
		EnemyBlueprint.initEnemyNoMap();
		//逃げたコンディションで非表示になっている場合表示する
		//アイテムアクションを削除する
		for (PlayerCharacter pc : GameSystem.getInstance().getParty()) {
			//逃げた後死亡、死亡した後逃げるはできないので、これで問題ないはず
			if (pc.getStatus().hasCondition(BattleConfig.ConditionName.escaped)) {
				pc.getSprite().setVisible(true);
				//逃げたコンディションを外す
				pc.getStatus().removeCondition(BattleConfig.ConditionName.escaped);

				List<CmdAction> removeList = pc.getStatus().getActions().stream().filter(p -> p.getType() == ActionType.ITEM).collect(Collectors.toList());
				pc.getStatus().getActions().removeAll(removeList);
			}
		}
		//BGMの処理
		if (currentBGM != null) {
			currentBGM.stop();
			currentBGM.dispose();
		}
		if (prevBGM != null) {
			prevBGM.play();
		}
		end = true;
	}

	BattleResultValues getBattleResultValue() {
		if (!end) {
			throw new GameSystemException("this battle is end not yet.");
		}
		assert battleResultValue != null : "battle is end, but result is null";
		return battleResultValue;
	}

	//次のコマンドを取得。NPCまたはPC。NPCの場合は自動実行。魔法詠唱イベントも自動実行。
	//このメソッドを起動して次のアクションを取得する。
	//取得したアクションがPCならコマンドウインドウが自動で開かれているので、選択する。
	//選択後、execPCActionを実行する。
	public BattleCommand execCmd() {
		//すべてのコマンドを実行したら次のターンを開始
		if (commandsOfThisTurn.isEmpty()) {
			turnStart();
		}
		currentCmd = commandsOfThisTurn.getFirst();
		assert currentCmd != null : "currentCMD is null";
		commandsOfThisTurn.removeFirst();
		BattleCharacter user = currentCmd.getUser();

		if (GameSystem.isDebugMode()) {
			System.out.println(" currentCMD:" + currentCmd);
		}

		//ターゲットシステム初期化
		targetSystem.unsetCurrent();
		currentBAWaitTime = null;
		//アンターゲット状態異常の場合、メッセージ出さずに次に送る
		if (user.getStatus().hasConditions(false, BattleConfig.getUntargetConditionNames())) {
			if (GameSystem.isDebugMode()) {
				System.out.println(user.getStatus().getName() + " is bad condition");
			}
			stage.setStage(BattleSystem.Stage.WAITING_EXEC_CMD);
			return execCmd();
		}
		//防御または回避中の場合、1ターンだけ有効なため、今回そのフラグを外す
		if (user.getStatus().hasCondition(BattleConfig.ConditionName.defence)) {
			user.getStatus().removeCondition(BattleConfig.ConditionName.defence);
		}
		if (user.getStatus().hasCondition(BattleConfig.ConditionName.avoidance)) {
			user.getStatus().removeCondition(BattleConfig.ConditionName.avoidance);
		}

		//状態異常で動けないときスキップ（メッセージは出す
		if (currentCmd.isStop()) {
			setMsg(MessageType.STOPING_BY_CONDITION, null, user.getStatus(), List.of(user.getStatus().moveStopDesc().getKey().getDesc()));
			stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
			return currentCmd;
		}

		//混乱で動けないときは、停止またはバトルアクションを適当に取得して自動実行する
		if (currentCmd.isConfu()) {
			if (Random.percent(BattleConfig.confuStopP)) {
				//動けない
				CmdAction ba = currentCmd.getFirstBattleAction();
				setMsg(MessageType.STOP_BECAUSE_CONFU, ba, user.getStatus());
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return currentCmd;
			} else {
				//動けるが混乱
				CmdAction ba = currentCmd.randomAction();
				currentBAWaitTime = ba.createWaitTime();
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				execAction(ba);
				return currentCmd;
			}
		}

		//魔法詠唱完了イベントの場合、PCでもNPCでも自動実行、（詠唱中コンディションを外す
		//魔法のコストとターゲットは、詠唱開始と終了の2回判定する。
		//ここは「詠唱終了時」の処理。
		if (currentCmd.isMagicSpell()) {
			CmdAction ba = currentCmd.getFirstBattleAction();//1つしか入っていない
			//現状でのターゲットを取得
			ActionTarget target = BattleTargetSystem.instantTarget(currentCmd.getUser(), ba);
			//ターゲットがいない場合、詠唱失敗のメッセージ出す
			if (target.isEmpty()) {
				setMsg(MessageType.SPELL_BUT_NO_TARGET, ba, user.getStatus());
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return currentCmd;
			}
			//現状のステータスで対価を支払えるか確認
			//※実際に支払うのはexecしたとき。
			Map<StatusKey, Integer> damage = ba.selfBattleDirectDamage();
			//ダメージを合算
			StatusValueSet simulateDamage = user.getStatus().simulateDamage(damage);
			//ダメージがあって、マイナスの項目がある場合、対価を支払えないため空振り
			//この魔法の消費項目を取得
			if (!damage.isEmpty() && simulateDamage.hasMinus()) {
				//しかしMPが足りない
				List<String> shortageStatusDesc = simulateDamage.stream().filter(p -> p.getValue() < 0).map(p -> StatusKeyStorage.getInstance().get(p.getName()).getDesc()).collect(Collectors.toList());
				setMsg(MessageType.SPELL_BUT_SHORTAGE, ba, user.getStatus(), shortageStatusDesc);
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return currentCmd;
			}
			//詠唱成功、魔法効果発動
			target.forEach(p -> p.getStatus().setDamageCalcPoint());
			ActionResult res = ba.exec(target);
			setMsg(MessageType.SPELL_SUCCESS, ba, res);
			animation.addAll(res.getAnimation());
			currentBAWaitTime = res.getWaitTime().clone();
			stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
			return currentCmd;
		}//魔法ここまで

		//NPC
		if (currentCmd.getMode() == BattleCommand.Mode.CPU) {
			//NPCアクションを自動実行、この中でステージも変わるしMSGも設定される
			execAction(currentCmd.getBattleActionEx(((Enemy) currentCmd.getUser()).getAI(), ActionType.OTHER, ActionType.ITEM));
			return currentCmd;
		}

		//PCのアクション実行
		//カレントコマンド内容をコマンドウインドウに表示、その他ウインドウは閉じる
		messageWindowSystem.getCmdW().setCmd(currentCmd);
		messageWindowSystem.getCmdW().resetSelect();
		messageWindowSystem.setVisible(
				BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.CMD_SELECT,
				BattleMessageWindowSystem.InfoVisible.OFF);

		//ターゲットシステムをウインドウの初期選択で初期化
		assert messageWindowSystem.getCmdW().getSelectedCmd() != null : "cmdW initial select action is null";
		targetSystem.setCurrent(currentCmd.getUser(), messageWindowSystem.getCmdW().getSelectedCmd());

		//PCの操作なので、カレントコマンドのユーザオペレーション要否フラグをONに設定
		currentCmd.setUserOperation(true);

		stage.setStage(BattleSystem.Stage.CMD_SELECT);
		return currentCmd;
	}

	public OperationResult execPcAction() {
		//移動後攻撃か通常攻撃かを判定
		boolean afterMove = messageWindowSystem.getAfterMoveW().isVisible();
		//コマンドウインドウまたは移動後攻撃ウインドウからアクションを取得
		if (!afterMove) {
			if (messageWindowSystem.getCmdW().getSelectedCmd() == null) {
				return OperationResult.CANCEL;//使える魔法／アイテムがない
			}
		}
		return execAction(afterMove
				? messageWindowSystem.getAfterMoveW().getSelectedCmd()
				: messageWindowSystem.getCmdW().getSelectedCmd());
	}

	//アクション実行（コミット、窓口）
	OperationResult execAction(CmdAction a) {
		currentBAWaitTime = a.createWaitTime();
		//PC,NPC問わず選択されたアクションを実行する。

		//ウインドウ状態初期化・・・アクション実行前
		messageWindowSystem.setVisible(
				BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.NOTHING,//アクションＭＳＧは後で設定して出す
				BattleMessageWindowSystem.InfoVisible.ON);

		//カレントユーザ
		BattleCharacter user = currentCmd.getUser();

		//混乱中の場合
		if (user.getStatus().isConfu()) {
			//ターゲットシステムのカレント起動しないで対象を取得する
			ActionTarget tgt = BattleTargetSystem.instantTarget(user, a);
			return execAction(a, tgt);
		}

		//NPCの場合
		if (!user.isPlayer()) {
			//アクションの効果範囲に相手がいるか、インスタント確認
			ActionTarget tgt = BattleTargetSystem.instantTarget(user, a);
			if (tgt.isEmpty()) {
				//ターゲットがいない場合で、移動アクションを持っている場合は移動開始
				if (user.getStatus().hasAction(BattleConfig.ActionName.move)) {
					//移動ターゲットは最も近いPCとする
					Point2D.Float tgtLocation = ((Enemy) user).getAI().targetLocation(user);
					user.setTargetLocation(tgtLocation, a.getAreaWithEqip(user));
					//移動距離を初期化
					remMovePoint = (int) user.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue();
					//移動した！のメッセージ表示
					setMsg(MessageType.IS_MOVED, a, user.getStatus());
					messageWindowSystem.setVisible(
							BattleMessageWindowSystem.StatusVisible.ON,
							BattleMessageWindowSystem.Mode.ACTION,
							BattleMessageWindowSystem.InfoVisible.OFF);
					stage.setStage(BattleSystem.Stage.EXECUTING_MOVE);
					return OperationResult.SUCCESS;
				} else {
					//移動できないので何もしない
					return OperationResult.MISS;
				}
			} else {
				//ターゲットがいる場合は即時実行
				return execAction(a, tgt);
			}
		}

		//PCの処理
		assert user.isPlayer() : "PC action, but action is not PC";
		assert a.isBattleUse() : "action is cant use in battle";
		assert user.getStatus().getActions().contains(a) : "user not have action";

		//PCの特殊コマンドの処理
		if (a.getType() == ActionType.OTHER) {
			if (a.getName().equals(BattleConfig.ActionName.avoidance)) {
				//回避・回避状態を付与する
				user.getStatus().addCondition(BattleConfig.ConditionName.avoidance);
				setMsg(MessageType.PC_USE_AVO, a, user.getStatus());
				return OperationResult.SUCCESS;
			}
			if (a.getName().equals(BattleConfig.ActionName.defence)) {
				//防御・防御状態を付与する
				user.getStatus().addCondition(BattleConfig.ConditionName.defence);
				setMsg(MessageType.PC_USE_DEFENCE, a, user.getStatus());
				return OperationResult.SUCCESS;
			}
			if (a.getName().equals(BattleConfig.ActionName.move)) {
				//移動開始・初期位置を格納
				moveIinitialLocation = user.getSprite().getLocation();
				messageWindowSystem.setVisible(
						BattleMessageWindowSystem.StatusVisible.ON,
						BattleMessageWindowSystem.Mode.AFTER_MOVE,
						BattleMessageWindowSystem.InfoVisible.ON);
				List<CmdAction> action = user.getStatus().getActions(ActionType.ATTACK);
				action.add(ActionStorage.getInstance().get(BattleConfig.ActionName.commit));
				Collections.sort(action);
				messageWindowSystem.getAfterMoveW().setActions(action);
				//ターゲットシステムのエリア表示を有効化：値はMOV
				targetSystem.setCurrent(user, a);
				stage.setStage(BattleSystem.Stage.PLAYER_MOVE);
				return OperationResult.MOVE;
			}
			if (a.getName().equals(BattleConfig.ActionName.commit)) {
				//移動終了・キャラクタの向きとターゲット座標のクリアをする
				messageWindowSystem.setVisible(
						BattleMessageWindowSystem.StatusVisible.ON,
						BattleMessageWindowSystem.Mode.NOTHING,//すぐ切り替わる
						BattleMessageWindowSystem.InfoVisible.ON);
				user.unsetTarget();
				user.to(FourDirection.WEST);
				stage.setStage(BattleSystem.Stage.WAITING_EXEC_CMD);
				return OperationResult.SUCCESS;
			}
			if (a.getName().equals(BattleConfig.ActionName.status)) {
				//ステータス表示
				int i = 0;
				for (; !GameSystem.getInstance().getPartyStatus().equals(currentCmd.getUser().getStatus()); i++);
				messageWindowSystem.setStatusDescPCIDX(i);
				messageWindowSystem.setVisible(
						BattleMessageWindowSystem.StatusVisible.OFF,
						BattleMessageWindowSystem.Mode.SHOW_STATUS_DESC,
						BattleMessageWindowSystem.InfoVisible.OFF);
				stage.setStage(BattleSystem.Stage.SHOW_STATUS);
				return OperationResult.SHOW_STATUS;
			}
			if (a.getName().equals(BattleConfig.ActionName.escape)) {
				//逃げる・逃げられるか判定
				//前提として、移動ポイント内にバトルエリアの境界（左右）がなければならない
				Point2D.Float w, e;
				int movPoint = (int) user.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue();
				e = (Point2D.Float) user.getSprite().getCenter().clone();
				e.x += movPoint;
				w = (Point2D.Float) user.getSprite().getCenter().clone();
				w.x -= movPoint;
				if (!battleFieldSystem.getBattleFieldAllArea().contains(e)) {
					//逃走成功（→）
					user.getStatus().addCondition(ConditionValueStorage.getInstance().get(BattleConfig.ConditionName.escaped).getKey());
					user.setTargetLocation(e, 0);
					user.to(FourDirection.EAST);
					setMsg(MessageType.PC_IS_ESCAPE, a, user.getStatus());
					stage.setStage(BattleSystem.Stage.ESCAPING);
					return OperationResult.SUCCESS;
				}
				if (!battleFieldSystem.getBattleFieldAllArea().contains(w)) {
					//逃走成功（←）
					user.getStatus().addCondition(ConditionValueStorage.getInstance().get(BattleConfig.ConditionName.escaped).getKey());
					user.setTargetLocation(w, 0);
					user.to(FourDirection.WEST);
					setMsg(MessageType.PC_IS_ESCAPE, a, user.getStatus());
					stage.setStage(BattleSystem.Stage.ESCAPING);
					return OperationResult.SUCCESS;
				}
//TODO:NPCの逃げはここでない。
//				//NPCの場合、逃げる体制に入る
//				if (!user.isPlayer()) {
//					remMovePoint = (int) user.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue();
//					user.setTargetLocation(w, 1);
//					setStage(BattleSystem.Stage.EXECUTING_MOVE, "execAction");
//					return OperationResult.SUCCESS;
//				}
				//逃げられない
				setMsg(MessageType.PC_IS_ESCAPE_MISS, a, user.getStatus());
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return OperationResult.CANCEL;
			}
		}

		//アイテム選択時の処理
		if (a.getType() == ActionType.ITEM) {
			//アイテム使用　※アイテムextendsアクション
			//アイテムChoiceUseを開くだけ。
			messageWindowSystem.openItemChoiceUse();
			stage.setStage(BattleSystem.Stage.ITEM_CHOICE_USE);
			return OperationResult.ITEM_CHOICE_USE;
		}

		//ターゲットシステム起動要否判定
		boolean needTargetSystem = false;
		if (user.isPlayer()) {
			//ターゲット選択はプレイヤーのみ
			if (a.getType() != ActionType.OTHER) {
				//その他イベント以外はターゲット選択必要
				needTargetSystem = true;
			}
			//ターゲットシステムが初期化状態の場合、ターゲット選択必要
			needTargetSystem = targetSystem.isEmpty();
		}
		//ターゲットシステム起動
		if (needTargetSystem) {
			//ターゲットシステムを起動する前に、インスタントターゲットでターゲットがいるか確認する。いない場合キャンセルにする。
			if (!BattleTargetSystem.instantTarget(user, a).hasAnyTargetChara()) {//魔法で現状ターゲットがいない場合もここで吸収される
				setMsg(MessageType.NO_TARGET, a, user.getStatus());
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return OperationResult.CANCEL;
			}
			//ターゲット選択へ
			setMsg(MessageType.TARGET_SELECT, a, user.getStatus());
			targetSystem.setCurrent(user, a);
			stage.setStage(BattleSystem.Stage.TARGET_SELECT);
			return OperationResult.TO_TARGET_SELECT;
		}
		targetSystem.setCurrent(user, a);
		List<String> tgt = targetSystem.getSelected().getTarget().stream().map(p -> p.getName()).collect(Collectors.toList());
		tgt.add(currentCmd.getUser().getName());
		setMsg(MessageType.TARGET_SELECT, tgt);
		return execAction(a, targetSystem.getSelected());
	}

	//アクション実行（コミット、ターゲットあり）
	OperationResult execAction(CmdAction ba, ActionTarget tgt) {
		messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.ACTION,
				BattleMessageWindowSystem.InfoVisible.OFF);
		//ターゲットシステムが呼ばれているので、初期化
		targetSystem.unsetCurrent();
		//カレントユーザ
		BattleCharacter user = currentCmd.getUser();

		//魔法詠唱開始の場合、詠唱中リストに追加して戻る。
		if (ba.getType() == ActionType.MAGIC) {
			//現状のステータスで対価を支払えるか確認
			//※実際に支払うのはexecしたとき。
			Map<StatusKey, Integer> damage = ba.selfBattleDirectDamage();
			//ダメージを合算
			StatusValueSet simulateDamage = user.getStatus().simulateDamage(damage);
			//ダメージがあって、-の項目がある場合、対価を支払えないため空振り
			//この魔法の消費項目を取得
			List<String> shortageKey = new ArrayList<>();
			if (!damage.isEmpty() && simulateDamage.hasMinus()) {
				//対象項目で1つでも0の項目があったら空振り
				List<String> shortageStatusDesc = simulateDamage.stream().filter(p -> p.getValue() < 0).map(p -> StatusKeyStorage.getInstance().get(p.getName()).getDesc()).collect(Collectors.toList());
				setMsg(MessageType.SPELL_BUT_SHORTAGE, ba, user.getStatus(), shortageStatusDesc);
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return user.isPlayer() ? OperationResult.CANCEL : OperationResult.MISS;
			}
			//ターゲット存在確認、現状でいない場合、空振り。発動時の再チェックがここ。
			if (tgt.isEmpty() && !ba.battleEventIsOnly(SELF)) {
				setMsg(MessageType.SPELL_BUT_NO_TARGET, ba, user.getStatus());
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				return user.isPlayer() ? OperationResult.CANCEL : OperationResult.MISS;
			}

			if (ba.getSpellTime() > 0) {
				//詠唱時間がある場合は詠唱開始
				addSpelling(user, ba);//MSG、STAGEもこの中で行う。
				return OperationResult.SUCCESS;
			}
		}

		//ターゲット不在の場合、空振り（ミス）、念のための処理、多分いらない
		if (tgt.isEmpty()) {
			setMsg(MessageType.NO_TARGET, ba, user.getStatus());
			currentBAWaitTime = new FrameTimeCounter(messageWaitTime);
			stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
			return OperationResult.MISS;
		}
		assert !tgt.isEmpty() : "target is empty(execAction)";
		//ターゲット存在のため、アクション実行
		tgt.getTarget().forEach(p -> p.getStatus().setDamageCalcPoint());
		ActionResult res = ba.exec(tgt);
		setMsg(MessageType.ACTION_SUCCESS, ba, res);
		animation.addAll(res.getAnimation());
		currentBAWaitTime = new FrameTimeCounter(ba.getWaitTime());
		stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
		//アイテムがREMOVEされている可能性があるため、全員のアクションを改める
		getAllChara().forEach(p -> p.getStatus().updateItemAction());
		return OperationResult.SUCCESS;

	}

	//PCの移動をキャンセルして、移動前の位置に戻す。確定は「commit」タイプのアクションから。
	public void cancelPCsMove() {
		currentCmd.getUser().getSprite().setLocation(moveIinitialLocation);
		currentCmd.getUser().unsetTarget();
		currentCmd.getUser().to(FourDirection.WEST);

		messageWindowSystem.getCmdW().setCmd(currentCmd);
		messageWindowSystem.setVisible(
				BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.CMD_SELECT,
				BattleMessageWindowSystem.InfoVisible.ON);
		stage.setStage(BattleSystem.Stage.WAITING_EXEC_CMD);
	}

	private void addSpelling(BattleCharacter user, CmdAction ba) {
		if (ba.getSpellTime() == 0) {
			throw new GameSystemException("this magic is spell time is 0, bud logic : " + ba);
		}
		int t = turn + ba.getSpellTime();
		if (magics.containsKey(t)) {
			magics.get(t).add(new MagicSpell(user, ba, user.isPlayer()));
		} else {
			List<MagicSpell> list = new ArrayList();
			list.add(new MagicSpell(user, ba, user.isPlayer()));
			magics.put(t, list);
		}
		//詠唱を開始したを表示
		setMsg(MessageType.SPELL_START, ba, user.getStatus());
		currentBAWaitTime = new FrameTimeCounter(messageWaitTime);
		stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
	}

	public void itemChoiceUseWindowNextSelect() {
		if (!messageWindowSystem.getItemChoiceUseW().isVisible()) {
			throw new GameSystemException("item choice use, but window is not active");
		}
		messageWindowSystem.getItemChoiceUseW().nextSelect();
	}

	public void itemChoiceUseWindowPrevSelect() {
		if (!messageWindowSystem.getItemChoiceUseW().isVisible()) {
			throw new GameSystemException("item choice use, but window is not active");
		}
		messageWindowSystem.getItemChoiceUseW().prevSelect();
	}

	public void itemChoiceUseCommit() {
		itemChoiceMode = -1;
		//米ターゲットセレクトに遷移する可能性がある
		if (!messageWindowSystem.getItemChoiceUseW().isVisible()) {
			throw new GameSystemException("item choice use, but window is not active");
		}
		int selected = messageWindowSystem.itemChoiceUseCommit();
		int area = 0;
		Item i = (Item) messageWindowSystem.getCmdW().getSelectedCmd();
		switch (selected) {
			case BattleMessageWindowSystem.ITEM_CHOICE_USE_CHECK:
				//チェックウインドウを出す
				messageWindowSystem.setVisible(
						BattleMessageWindowSystem.StatusVisible.ON,
						BattleMessageWindowSystem.Mode.SHOW_ITEM_DESC,
						BattleMessageWindowSystem.InfoVisible.OFF);
				stage.setStage(BattleSystem.Stage.SHOW_ITEM);
				break;
			case BattleMessageWindowSystem.ITEM_CHOICE_USE_EQIP:
				//装備できない
				if (i.getEqipmentSlot() == null) {
					setMsg(MessageType.CANT_EQIP, List.of(i.getName()));
					stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
					return;
				}
				//装備した・外した
				assert i.getEqipmentSlot() != null : "item is not eqip";
				if (currentCmd.getUser().getStatus().isEqip(i.getName())) {
					currentCmd.getUser().getStatus().removeEqip(i);
				} else {
					currentCmd.getUser().getStatus().addEqip(i);
				}
				MessageType t = currentCmd.getUser().getStatus().isEqip(i.getName())
						? MessageType.UNEQIP_ITEM
						: MessageType.EQIP_ITEM;
				setMsg(t, List.of(i.getName()));
				stage.setStage(BattleSystem.Stage.EXECUTING_ACTION);
				break;
			case BattleMessageWindowSystem.ITEM_CHOICE_USE_USE:
				itemChoiceMode = BattleMessageWindowSystem.ITEM_CHOICE_USE_USE;
				area = (int) (currentCmd.getUser().getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue() / 2);
				setMsg(MessageType.ITEM_WHO_TO_USE, List.of(i.getName()));
				break;
			case BattleMessageWindowSystem.ITEM_CHOICE_USE_PASS:
				itemChoiceMode = BattleMessageWindowSystem.ITEM_CHOICE_USE_PASS;
				area = (int) (currentCmd.getUser().getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue() / 2);
				setMsg(MessageType.ITEM_WHO_TO_PASS, List.of(i.getName()));
				break;
			case BattleMessageWindowSystem.ITEM_CHOICE_USE_THROW:
				itemChoiceMode = BattleMessageWindowSystem.ITEM_CHOICE_USE_THROW;
				area = (int) (currentCmd.getUser().getStatus().getEffectedStatus().get(BattleConfig.StatusKey.str).getValue() * 3);
				setMsg(MessageType.ITEM_WHO_TO_THROW, List.of(i.getName()));
				break;
			default:
				throw new AssertionError("undefined item choice use No");
		}
		//誰に？
		if (itemChoiceMode >= 0) {
			ActionTarget t = BattleTargetSystem.instantTarget(currentCmd.getUser(), i, area,
					itemChoiceMode == BattleMessageWindowSystem.ITEM_CHOICE_USE_THROW,
					itemChoiceMode == BattleMessageWindowSystem.ITEM_CHOICE_USE_USE
					|| itemChoiceMode == BattleMessageWindowSystem.ITEM_CHOICE_USE_PASS
			);
			List<String> tgt = t.getTarget().stream().map(p -> p.getName()).collect(Collectors.toList());
			tgt.add((itemChoiceMode == BattleMessageWindowSystem.ITEM_CHOICE_USE_THROW) ? 0 : tgt.size(), t.getUser().getName());
			setMsg(MessageType.TARGET_SELECT, tgt);
			//ターゲット選択へ
			stage.setStage(BattleSystem.Stage.TARGET_SELECT);
		}
	}
	//アイテムChoiceUseインデックス：-1：ターゲット選択未使用
	private int itemChoiceMode = -1;
	//AfterMoveAction更新用の前回検査時の攻撃可否
	private boolean prevAttackOK = false;

	//移動後攻撃の設定を行う。引数で攻撃できるかを渡す。
	@LoopCall
	public void setAftedMoveAction(boolean attackOK) {
		if (!messageWindowSystem.getAfterMoveW().isVisible()) {
			throw new GameSystemException("after move window is not visible");
		}
		if (prevAttackOK == attackOK) {
			return;
		}
		prevAttackOK = attackOK;
		List<CmdAction> afterMoveActions = new ArrayList<>();
		if (attackOK) {
			afterMoveActions.addAll(currentCmd.getBattleActions().stream().filter(p -> p.getType() == ActionType.ATTACK).collect(Collectors.toList()));
		}
		afterMoveActions.add(ActionStorage.getInstance().get(BattleConfig.ActionName.commit));
		Collections.sort(afterMoveActions);
		if (!attackOK) {
			targetSystem.getCurrentArea().setVisible(false);
			targetSystem.getCurrentArea().setArea(0);
		} else {
			targetSystem.getCurrentArea().setVisible(true);
		}

		messageWindowSystem.getAfterMoveW().setActions(afterMoveActions);
	}

	public void nextTargetSelect() {
		messageWindowSystem.getTgtW().nextSelect();
	}

	public void prevTargetSelect() {
		messageWindowSystem.getTgtW().prevSelect();
	}

	public void statusWindowNextPage() {
		messageWindowSystem.statusDescWindowNextPage();
	}

	public void statusWindowNextSelect() {
		messageWindowSystem.statusDescWindowNextSelect();
	}

	public void statusWindowPrevSelect() {
		messageWindowSystem.statusDescWindowPrevSelect();
	}

	public void commitTargetSelect() {
		if (itemChoiceMode != -1) {

		}
	}

	/**
	 * 戦況図モードの切替
	 */
	public void switchShowMode() {
		showMode = !showMode;
		if (showMode) {
			messageWindowSystem.setVisibleFromSave();
		} else {
			messageWindowSystem.saveVisible();
			messageWindowSystem.setVisible(
					BattleMessageWindowSystem.StatusVisible.OFF,
					BattleMessageWindowSystem.Mode.NOTHING,
					BattleMessageWindowSystem.InfoVisible.OFF);

		}
	}

	private void setMsg(MessageType t) {
		//t == BATTLE_END
		String s = t.get(null, null, null, null);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	private void setMsg(MessageType t, List<String> option) {
		String s = t.get(null, null, option, null);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	private void setMsg(MessageType t, CmdAction a, ActionResult res) {
		String s = t.get(a, null, null, res);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	private void setMsg(MessageType t, CmdAction a, Status user) {
		String s = t.get(a, user, null, null);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	private void setMsg(MessageType t, CmdAction a, Status user, List<String> option) {
		String s = t.get(a, user, option, null);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	private void setMsg(MessageType t, CmdAction a, Status user, ActionResult res) {
		String s = t.get(a, user, null, res);
		messageWindowSystem.getActionResultW().setText(s);
		messageWindowSystem.getActionResultW().allText();
	}

	public void update() {
		battleFieldSystem.update();
		messageWindowSystem.update();
		targetSystem.update();
		enemies.forEach(v -> v.update());

		//ターゲットシステムのカレント表示位置更新
		if (targetSystem.getCurrentArea().isVisible()) {
			targetSystem.getCurrentArea().setLocationByCenter(currentCmd.getSpriteCenter());
		}

		//勝敗判定
		List<BattleWinLoseLogic> winLoseLogic = BattleConfig.getWinLoseLogic();
		if (winLoseLogic.isEmpty()) {
			throw new GameSystemException("win lose logic is empty, this battle never end.");
		}
		List<Status> party = GameSystem.getInstance().getPartyStatus();
		List<Status> enemy = enemies.stream().map(p -> p.getStatus()).collect(Collectors.toList());
		for (BattleWinLoseLogic l : winLoseLogic) {
			BattleResult result = l.isWinOrLose(party, enemy);
			if (result == BattleResult.NOT_YET) {
				continue;
			}
			//戦闘終了処理
			String nextLogicName = result == BattleResult.WIN ? winLogicName : loseLogicName;
			int exp = enemies.stream().mapToInt(p -> (int) p.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.exp).getValue()).sum();
			List<Item> dropItems = new ArrayList<>();
			for (Enemy e : enemies) {
				List<DropItem> items = e.getDropItem();
				for (DropItem i : items) {
					//ドロップアイテムの確率判定
					if (Random.percent(i.getP())) {
						dropItems.addAll(i.cloneN());
					}
				}
			}
			battleResultValue = new BattleResultValues(result, exp, dropItems, nextLogicName);

			if (GameSystem.isDebugMode()) {
				System.out.println(" this battle is ended");
			}
			stage.setStage(Stage.BATLE_END);
			setMsg(MessageType.BATTLE_RESULT);
			messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
					BattleMessageWindowSystem.Mode.BATTLE_RESULT,
					BattleMessageWindowSystem.InfoVisible.OFF);
		}

		//効果の終わったアニメーションを取り除く
		//アニメーションはアクションの待機時間より長く表示することも可能なためstage外で実施
		List<AnimationSprite> removeList = new ArrayList<>();
		for (AnimationSprite a : animation) {
			if (a.getAnimation() == null) {//nullは基本入っていないのでもしあったら消す
				removeList.add(a);
				continue;
			}
			if (a.getAnimation().isEnded() || !a.isVisible() || !a.isExist()) {
				removeList.add(a);
			}
		}
		animation.removeAll(removeList);

		//ステージ別処理
		GameSystem gs = GameSystem.getInstance();
		switch (stage.getStage()) {
			case STARTUP:
				//スタートアップ時にupdateが呼ばれることはないためエラー
				throw new GameSystemException("update call before start");
			case INITIAL_MOVING:
				//プレイヤーキャラクターが目標の座標に近づくまで移動を実行、目的地に近づいたらステージWAITに変える
				gs.getPartySprite().forEach(v -> v.move());
				//移動終了判定
				boolean initialMoveEnd = true;
				for (int i = 0; i < gs.getPartySprite().size(); i++) {
					float speed = gs.getPartySprite().get(i).getSpeed();
					if (initialMoveEnd &= partyTargetLocationForFirstMove.get(i).distance(gs.getPartySprite().get(i).getLocation()) <= speed) {
						gs.getPartySprite().get(i).setLocation(partyTargetLocationForFirstMove.get(i));
						gs.getParty().get(i).unsetTarget();
					}
				}
				if (initialMoveEnd) {
					messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
							BattleMessageWindowSystem.Mode.NOTHING,
							BattleMessageWindowSystem.InfoVisible.OFF);
					stage.setStage(Stage.WAITING_EXEC_CMD);
				}
				break;
			case ESCAPING:
				//プレイヤーキャラクターが目標の座標に近づくまで移動を実行、目的地に近づいたらステージWAITに変える
				currentCmd.getUser().moveToTgt();

				if (!currentCmd.getUser().isMoving()) {
					//PC全員逃げ判定、全員逃げた場合、戦闘終了
					if (party.stream().allMatch(p -> p.hasCondition(BattleConfig.ConditionName.escaped))) {
						//全員逃げた
						//アンターゲット状態異常付与（逃げる以外）の敵のEXPを合計して渡す
						int exp = 0;
						for (Enemy e : enemies) {
							if (e.getStatus().hasConditions(false, BattleConfig.getUntargetConditionNames()
									.stream().filter(p -> !p.equals(BattleConfig.ConditionName.escaped)).collect(Collectors.toList()))) {
								exp += (int) e.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.exp).getValue();
							}
						}
						battleResultValue = new BattleResultValues(BattleResult.ESCAPE, exp, new ArrayList<>(), winLogicName);
						stage.setStage(Stage.BATLE_END);
						setMsg(MessageType.BATTLE_RESULT);
						messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
								BattleMessageWindowSystem.Mode.BATTLE_RESULT,
								BattleMessageWindowSystem.InfoVisible.OFF);
						break;
					}
					//NPC全員逃げ判定
					if (enemies.stream().allMatch(p -> p.getStatus().hasCondition(BattleConfig.ConditionName.escaped))) {
						//アンターゲット状態異常付与（逃げる以外）の敵のEXPを合計して渡す
						int exp = 0;
						for (Enemy e : enemies) {
							if (e.getStatus().hasConditions(false, BattleConfig.getUntargetConditionNames()
									.stream().filter(p -> !p.equals(BattleConfig.ConditionName.escaped)).collect(Collectors.toList()))) {
								exp += (int) e.getStatus().getEffectedStatus().get(BattleConfig.StatusKey.exp).getValue();
							}
						}
						battleResultValue = new BattleResultValues(BattleResult.ESCAPE, exp, new ArrayList<>(), winLogicName);
						//全員逃げた
						battleResultValue = new BattleResultValues(BattleResult.ESCAPE, 0, new ArrayList<>(), winLogicName);
						stage.setStage(Stage.BATLE_END);
						setMsg(MessageType.BATTLE_RESULT);
						messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
								BattleMessageWindowSystem.Mode.BATTLE_RESULT,
								BattleMessageWindowSystem.InfoVisible.OFF);
						break;
					}
					stage.setStage(Stage.WAITING_EXEC_CMD);
					break;
				}
				break;
			case CMD_SELECT:
			case WAITING_EXEC_CMD:
			case PLAYER_MOVE:
			case TARGET_SELECT:
				//プレイヤーの行動まちなので、何もしない。
				//コマンドウインドウ等から処理を実行される
				//次にバトルコマンドを取得したとき、NPCならNPCの行動のステージに入る。
				break;
			case EXECUTING_ACTION:
				//カレントBATimeが切れるまで待つ
				assert currentBAWaitTime != null : "EXECITING_ACTION buf tc is null";
				if (currentBAWaitTime.isReaching()) {
					currentBAWaitTime = null;
					stage.setStage(Stage.WAITING_EXEC_CMD);
				}
				break;
			case EXECUTING_MOVE:
				//NPCの移動実行、！！！！！！！！移動かんりょぅしたらメッセージウインドウ閉じる
				currentCmd.getUser().moveToTgt();
				remMovePoint--;
				//移動ポイントが切れた場合、移動終了してユーザコマンド待ちに移行
				if (remMovePoint <= 0 || !currentCmd.getUser().isMoving()) {
					currentCmd.getUser().unsetTarget();
					stage.setStage(Stage.WAITING_EXEC_CMD);
					return;
				}
				//移動ポイントが切れていない場合で、移動ポイントが半分以上残っている場合は攻撃可能
				//半分以下の場合は行動終了
				if (remMovePoint < currentCmd.getUser().getStatus().getEffectedStatus().get(BattleConfig.StatusKey.move).getValue()) {
					return;
				}
				//アクションを抽選・・・このステージに入るときは必ずENEMYなのでキャスト失敗しない
				CmdAction eba = currentCmd.getBattleActionOf(((Enemy) currentCmd.getUser()).getAI(), ActionType.ATTACK);
				if (eba == null) {
					if (GameSystem.isDebugMode()) {
						System.out.println(" enemy " + currentCmd.getUser().getStatus().getName() + " try afterMoveAttack, but dont have attackCMD");
					}
					return;
				}

				// イベント対象者別にターゲットを設定
				ActionTarget tgt = BattleTargetSystem.instantTarget(currentCmd.getUser(), eba);

				//ターゲットがいない場合、何もしない
				if (tgt.isEmpty()) {
					return;
				}

				//移動後攻撃実行
				ActionResult res = eba.exec(tgt);
				updateCondition();
				currentBAWaitTime = eba.createWaitTime();
				setMsg(MessageType.ACTION_SUCCESS, eba, res);
				animation.addAll(res.getAnimation());
				//アクション実行中に入る
				stage.setStage(Stage.EXECUTING_ACTION);
				break;
			case ITEM_CHOICE_USE:
			case SHOW_ITEM:
			case SHOW_STATUS:
				//何もしない（専用メソッドから操作する
				break;
			case BATLE_END:
				//何もしない（ユーザ操作待ち
				break;
			default:
				throw new AssertionError("UNDEFINED STAGE");
		}
	}

	@Override
	public void draw(GraphicsContext g) {
		battleFieldSystem.draw(g);

		//エネミーとPCのY座標による描画順の更新
//		List<BattleCharacter> charas = getAllChara();
//		Collections.sort(charas, (BattleCharacter o1, BattleCharacter o2) -> (int) (o1.getSprite().getY() - o2.getSprite().getY()));
		enemies.forEach(p -> p.draw(g));
		GameSystem.getInstance().getPartySprite().forEach(p -> p.draw(g));

		animation.forEach(v -> v.draw(g));

		targetSystem.draw(g);

		messageWindowSystem.draw(g);
	}

	//ターゲット選択モードをキャンセルして閉じる。アクション選択に戻る
	public void cancelTargetSelect() {
		messageWindowSystem.setVisible(BattleMessageWindowSystem.StatusVisible.ON,
				BattleMessageWindowSystem.Mode.CMD_SELECT,
				BattleMessageWindowSystem.InfoVisible.ON);
		targetSystem.unsetCurrent();
		stage.setStage(Stage.CMD_SELECT);
	}

	BattleMessageWindowSystem getMessageWindowSystem() {
		return messageWindowSystem;
	}

	BattleTargetSystem getTargetSystem() {
		return targetSystem;
	}

	BattleFieldSystem getBattleFieldSystem() {
		return battleFieldSystem;
	}

	public void nextCmdSelect() {
		messageWindowSystem.getCmdW().nextAction();
	}

	public void prevCmdSelect() {
		messageWindowSystem.getCmdW().prevAction();
	}

	public void nextCmdType() {
		messageWindowSystem.getCmdW().nextType();
	}

	public void prevCmdType() {
		messageWindowSystem.getCmdW().prevType();
	}

	@LoopCall
	public boolean isBattleEnd() {
		return stage.getStage() == Stage.BATLE_END;
	}

	@LoopCall
	public boolean waitAction() {
		return stage.getStage() == Stage.EXECUTING_ACTION;
	}

	List<Enemy> getEnemies() {
		return enemies;
	}

}
