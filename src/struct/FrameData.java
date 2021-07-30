package struct;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import fighting.Attack;
import input.KeyData;
import setting.FlagSetting;
import setting.GameSetting;

/**
 * The class dealing with the information in the game such as the current frame
 * number, number of rounds and character information.
 */
public class FrameData {

	/**
	 * The character's data of both characters<br>
	 * Index 0 is P1, index 1 is P2.
	 */
	private CharacterData[] characterData;

	/**
	 * The current frame of the round.
	 */
	private int currentFrameNumber;

	/**
	 * The current round number.
	 */
	private int currentRound;

	/**
	 * The projectile data of both characters.
	 */
	private Deque<AttackData> projectileData;

	/**
	 * If this value is true, no data are available or they are dummy data.
	 */
	private boolean emptyFlag;

	/**
	 * The class constructor.
	 */
	public FrameData() {
		this.characterData = new CharacterData[] { null, null };
		this.currentFrameNumber = -1;
		this.currentRound = -1;
		this.projectileData = new LinkedList<AttackData>();
		this.emptyFlag = true;

	}

	/**
	 * The class constructor that creates a new instance of the FrameData class
	 * by copying the data passed as the arguments.
	 *
	 * @param characterData
	 *            an instance of the CharacterData class
	 * @param currentFrame
	 *            the frame number of the current frame
	 * @param currentRound
	 *            the round number of the current round
	 * @param projectileData
	 *            the queue that stores information on projectiles of P1 and P2
	 *
	 * @see CharacterData
	 * @see KeyData
	 */
	public FrameData(CharacterData[] characterData, int currentFrame, int currentRound,
			Deque<AttackData> projectileData) {
		this.characterData = new CharacterData[] { characterData[0], characterData[1] };
		this.currentFrameNumber = currentFrame;
		this.currentRound = currentRound;

		// make deep copy of the attacks list
		this.projectileData = new LinkedList<AttackData>();
		for (AttackData attack : projectileData) {
			this.projectileData.add(new AttackData(attack));
		}

		this.emptyFlag = false;
	}

	/**
	 * A copy constructor that creates a copy of an instance of the FrameData
	 * class by copying the values of the variables from an instance of the
	 * FrameData class passed as the argument.
	 *
	 * @param frameData
	 *            an instance of the FrameData class
	 */
	public FrameData(FrameData frameData) {
		this.characterData = new CharacterData[2];
		this.characterData[0] = frameData.getCharacter(true);
		this.characterData[1] = frameData.getCharacter(false);
		this.currentFrameNumber = frameData.getFramesNumber();
		this.currentRound = frameData.getRound();

		// make deep copy of the attacks list
		this.projectileData = new LinkedList<AttackData>();
		Deque<AttackData> temp = frameData.getProjectiles();
		for (AttackData attack : temp) {
			this.projectileData.add(new AttackData(attack));
		}

		this.emptyFlag = frameData.getEmptyFlag();

	}

	/**
	 * Returns an instance of the CharacterData class of the player specified by
	 * an argument.
	 *
	 * @param playerNumber
	 *            the number of the player. {@code true} if the player is P1, or
	 *            {@code false} if P2.
	 * @return an instance of the CharacterData class of the player
	 */
	public CharacterData getCharacter(boolean playerNumber) {
		CharacterData temp = this.characterData[playerNumber ? 0 : 1];

		return temp == null ? null : new CharacterData(temp);
	}

	/**
	 * Returns the expected remaining time in milliseconds of the current round.
	 * <br>
	 * When FightingICE was launched with the training mode, this method returns
	 * the max value of integer.
	 *
	 * @return the expected remaining time in milliseconds of the current round
	 */
	public int getRemainingTimeMilliseconds() {
		if (FlagSetting.trainingModeFlag) {
			return Integer.MAX_VALUE;
		} else {
			return GameSetting.ROUND_TIME - (int) (((float) this.currentFrameNumber / GameSetting.FPS) * 1000);
		}
	}

	/**
	 * Returns the expected remaining time in seconds of the current round.<br>
	 * When FightingICE was launched with the training mode, this method returns
	 * the max value of integer.
	 *
	 * @return the expected remaining time in seconds of the current round
	 * @deprecated Use {@link #getRemainingTimeMilliseconds()} instead. This
	 *             method has been renamed to more clearly reflect its purpose.
	 */
	public int getRemainingTime() {
		if (FlagSetting.trainingModeFlag) {
			return Integer.MAX_VALUE;
		} else {
			return (int) Math.ceil((float) getRemainingTimeMilliseconds() / 1000);
		}
	}

	/**
	 * Returns the number of remaining frames of the round. <br>
	 * When FightingICE was launched with the training mode, this method returns
	 * the max value of integer.
	 *
	 * @return the number of remaining frames of the round
	 */
	public int getRemainingFramesNumber() {
		if (FlagSetting.trainingModeFlag) {
			return Integer.MAX_VALUE;
		} else {
			return (GameSetting.ROUND_FRAME_NUMBER - currentFrameNumber);
		}
	}

	/**
	 * Returns the number of frames since the beginning of the round.
	 *
	 * @return the number of frames since the beginning of the round
	 */
	public int getFramesNumber() {
		return this.currentFrameNumber;
	}

	/**
	 * Returns the current round number.
	 *
	 * @return the current round number
	 */
	public int getRound() {
		return this.currentRound;
	}

	/**
	 * Returns the projectile data of both characters.
	 *
	 * @return the projectile data of both characters
	 */
	public Deque<AttackData> getProjectiles() {
		// create a deep copy of the attacks list
		LinkedList<AttackData> attackList = new LinkedList<AttackData>();
		for (AttackData attack : this.projectileData) {
			attackList.add(new AttackData(attack));
		}
		return attackList;
	}

	/**
	 * Returns the projectile data of player 1.
	 *
	 * @return the projectile data of player 1
	 */
	public Deque<AttackData> getProjectilesByP1() {
		LinkedList<AttackData> attackList = new LinkedList<AttackData>();
		for (AttackData attack : this.projectileData) {
			if (attack.isPlayerNumber()) {
				attackList.add(new AttackData(attack));
			}
		}
		return attackList;
	}

	/**
	 * Returns the projectile data of player 2.
	 *
	 * @return the projectile data of player 2
	 */
	public Deque<AttackData> getProjectilesByP2() {
		LinkedList<AttackData> attackList = new LinkedList<AttackData>();
		for (AttackData attack : this.projectileData) {
			if (!attack.isPlayerNumber()) {
				attackList.add(new AttackData(attack));
			}
		}
		return attackList;
	}

	/**
	 * Returns true if this instance is empty, false if it contains meaningful
	 * data.
	 *
	 * @return {@code true} if this instance is empty, or {@code false} if it
	 *         contains meaningful data
	 */
	public boolean getEmptyFlag() {
		return this.emptyFlag;
	}

	/**
	 * Returns the horizontal distance between P1 and P2.
	 *
	 * @return the horizontal distance between P1 and P2
	 */
	public int getDistanceX(int player, int opponent) {
		int playerL = characterData[player].getLeft();
		int playerR = characterData[player].getRight();
		int opponentL = characterData[opponent].getLeft();
		int opponentR = characterData[opponent].getRight();

		if (playerL > opponentR || playerR < opponentL) {
			return Math.min(Math.abs(playerR - opponentL), Math.abs(playerL - opponentR));
		} else {
			return 0;
		}
	}

	/**
	 * Returns the vertical distance between P1 and P2.
	 *
	 * @return the vertical distance between P1 and P2
	 */
	public int getDistanceY(int player, int opponent) {
		int playerT = characterData[player].getTop();
		int playerB = characterData[player].getBottom();
		int opponentT = characterData[opponent].getTop();
		int opponentB = characterData[opponent].getBottom();

		int playerAboveBy = opponentT - playerB;
		int opponentAboveBy = playerT - opponentB;

		if (playerAboveBy > 0) {
			return playerAboveBy;
		} else if (opponentAboveBy > 0) {
			return -opponentAboveBy;
		} else {
			return 0;
		}
	}

	public int getAttackXDistance(AttackData attack, int target) {
		int targetL = characterData[target].getLeft();
		int targetR = characterData[target].getRight();
		int attackL = attack.getCurrentHitArea().getLeft();
		int attackR = attack.getCurrentHitArea().getRight();

		return Math.min(Math.abs(targetR - attackL), Math.abs(targetL - attackR));
	}

	public int getAttackYDistance(AttackData attack, int target) {
		int targetT = characterData[target].getTop();
		int targetB = characterData[target].getBottom();
		int attackT = attack.getCurrentHitArea().getTop();
		int attackB = attack.getCurrentHitArea().getBottom();

		if ((targetT <= attackT && attackT <= targetB) || (targetT <= attackB && attackB <= targetB)) {
			return 0;
		} else {
			return Math.min(Math.abs(targetT - attackB), Math.abs(attackT - targetB));
		}
	}

	public LinkedList<Double> getGameFeatures (int player, int opponent) {
		LinkedList<Double> features = new LinkedList<Double>();
		LinkedList<Double> emptyAttackFeatures = new LinkedList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		// player features
		features.add(characterData[player].isFront() ? 1.0 : 0.0);  // self_front
		features.add((double)characterData[player].getState().ordinal());  // self_state_id
		features.add((double)characterData[player].getAction().ordinal());  // self_action_id
		features.add((double)characterData[player].getCenterX());  // self_x
		features.add((double)characterData[player].getCenterY());  // self_y
		features.add((double)characterData[player].getSpeedX());  // self_speed_x
		features.add((double)characterData[player].getSpeedY());  // self_speed_y
		features.add((double)characterData[player].getRemainingFrame());  // self_remaining_frame
		features.add((double)characterData[player].getEnergy());  // self_energy
		features.add((double)characterData[player].getHp());  // self_hp
		features.add(isTargetApproaching(opponent, player));  // is_self_approaching
		// player attack features
		AttackData attack = characterData[player].getAttack();
		if (attack != null && attack.getAttackType() != 0) {
			features.add((double)attack.getAttackType());  // self_att_type
			features.add((double)attack.getSettingSpeedX());  // self_att_speed_x
			features.add((double)attack.getSettingSpeedY());  // self_att_speed_y
			features.add((double)attack.getHitDamage());  // self_att_damage
			features.add((double)attack.getGuardDamage());  // self_att_guard_damage
			features.add((double)attack.getImpactX());  // self_att_impact_x
			features.add((double)attack.getImpactY());  // self_att_impact_y
			features.add((double)getAttackXDistance(attack, opponent));  // self_att_distance_from_oppo_x
			features.add((double)getAttackYDistance(attack, opponent));  // self_att_distance_from_oppo_y
		} else {
			features.addAll(emptyAttackFeatures);
		}
		// player projectile features (closest)
		Deque<AttackData> projectiles = characterData[player].isPlayerNumber() ? getProjectilesByP1() : getProjectilesByP2();
		if (projectiles.size() > 0) {
			features.add((double)projectiles.size());  // self_proj_num
			LinkedList<Double> closestProjFeatures = getClosestProjectile(opponent, projectiles);
			features.addAll(closestProjFeatures);
		} else {
			features.add(0.0);  // self_proj_num
			features.addAll(emptyAttackFeatures);
		}
		return features;
	}

	public LinkedList<Double> getMctsScoringFeatures (boolean isPlayerP1) {
		LinkedList<Double> features = new LinkedList<Double>();

		int player;
		int opponent;
		if (isPlayerP1) {
			player = 0;
			opponent = 1;
		} else {
			player = 1;
			opponent = 0;
		}

		// common features
		features.add((double)getDistanceX(player, opponent));  // players_x_distance
		features.add((double)getDistanceY(player, opponent));  // players_y_distance
		features.add((double)(characterData[player].getHp() - characterData[opponent].getHp()));  // hp_diff

		// self features
		features.addAll(getGameFeatures(player, opponent));
		// oppo features
		features.addAll(getGameFeatures(opponent, player));


		return features;
	}

	public Double isTargetApproaching(int player, int target) {
		int playerX = characterData[player].getCenterX();
		int targetX = characterData[target].getCenterX();
		int targetSpeedX = characterData[target].getSpeedX();

		if (targetSpeedX  == 0) {
			return 0.0;
		} else if (Math.signum((float)(playerX - targetX)) == Math.signum((float)targetSpeedX)) {
			return 1.0;
		} else {
			return -1.0;
		}
	}

	public LinkedList<Double> getClosestProjectile(int target, Deque<AttackData> projectiles) {
		int targetL = characterData[target].getLeft();
		int targetR = characterData[target].getRight();
		int targetT = characterData[target].getTop();
		int targetB = characterData[target].getBottom();

		int closestXDistance = 1000;
		int closestYDistance = 1000;
		AttackData closestProjectile = new AttackData();

		for (AttackData projectile: projectiles) {
			int projL = projectile.getCurrentHitArea().getLeft();
			int projR = projectile.getCurrentHitArea().getRight();
			int projT = projectile.getCurrentHitArea().getTop();
			int projB = projectile.getCurrentHitArea().getBottom();
			int projDist = Math.min(Math.abs(targetR - projL), Math.abs(targetL - projR));
			if (projDist < closestXDistance) {
				if ((targetT <= projB && projB <= targetB) || (targetT <= projT && projT <= targetB)) {
					closestYDistance = 0;
				} else {
					closestYDistance = Math.min(Math.abs(targetT - projB), Math.abs(projT - targetB));
				}
				closestXDistance = projDist;
				closestProjectile = projectile;
			}
		}
		LinkedList<Double> closestProjFeatures = new LinkedList<Double>();
		closestProjFeatures.add((double)closestProjectile.getAttackType());
		closestProjFeatures.add((double)closestProjectile.getSpeedX());
		closestProjFeatures.add((double)closestProjectile.getSpeedY());
		closestProjFeatures.add((double)closestProjectile.getHitDamage());
		closestProjFeatures.add((double)closestProjectile.getGuardDamage());
		closestProjFeatures.add((double)closestProjectile.getImpactX());
		closestProjFeatures.add((double)closestProjectile.getImpactY());
		closestProjFeatures.add((double)closestXDistance);
		closestProjFeatures.add((double)closestYDistance);

		return closestProjFeatures;
	}
}
