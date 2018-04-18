package setting;

import java.util.ArrayList;

import enumerate.BackgroundType;
import python.PyGatewayServer;

/**
 * キャラクターの最大HPや試合の繰り返し回数など、試合を行う際に必要な設定を扱うクラス．
 */
public final class LaunchSetting {

	/**
	 * P1,P2の最大HPを格納する配列．
	 */
	public static int[] maxHp = { 400, 400 };

	/**
	 * P1,P2の最大エネルギーを格納する配列．
	 */
	public static int[] maxEnergy = { 300, 300 };

	/**
	 * P1,P2のAI名を格納する配列．<br>
	 * キーボードの場合は"Keyboard"が格納される．
	 */
	public static String[] aiNames = { "KeyBoard", "KeyBoard" };

	/**
	 * P1,P2のキャラクター名．
	 */
	public static String[] characterNames = { "ZEN", "ZEN" };

	/**
	 * 利用するデバイスタイプ．<br>
	 * {@code 0} if the device type is keyboard，or {@code 1} if AI.
	 */
	public static char[] deviceTypes = { 0, 0 };

	/**
	 * Pythonを利用するときのポート番号．
	 */
	public static int py4jPort = 4242;

	/**
	 * 試合を繰り返して行う回数．
	 */
	public static int repeatNumber = 1;

	/**
	 * 画素を反転させるプレイヤーの番号．
	 */
	public static int invertedPlayer = 0;

	/**
	 * 背景の種類．
	 */
	public static BackgroundType backgroundType = BackgroundType.IMAGE;

	/**
	 * リプレイデータの名前．
	 */
	public static String replayName = "None";

	/**
	 * 試合の繰り返し回数のカウンタ．
	 */
	public static int repeatedCount = 0;

	/**
	 * PythonでJavaの処理を行うためのゲートウェイサーバー．
	 */
	public static PyGatewayServer pyGatewayServer = null;

	/**
	 * P1として用いるAI群
	 * AutomationMode時ゲームを終了せず、P1のAIを変えたい場合に使用する
	 */
	public static String[] ai1sNames;
	
	/**
	 * P2として用いるAI群
	 * AutomationMode時ゲームを終了せず、P2のAIを変えたい場合に使用する
	 */
	public static String[] ai2sNames;
	
	/**
	 * AutomationMode時AIを変える場合の現在の組み合わせ
	 */
	public static int currentComb = 0;
	
	/**
	 * AutomationMode時AIを変える場合の周期
	 */
	public static int rotationCount = 1;
	
	/**
	 * AutomationMode時 現在の組み合わせでの実行回数
	 */
	public static int currentRunNumber = 0;

}
