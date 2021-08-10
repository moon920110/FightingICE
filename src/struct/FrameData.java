package struct;

import java.util.*;

import fighting.Attack;
import input.KeyData;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import setting.FlagSetting;
import setting.GameSetting;
import setting.MctsSetting;

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

	public Map<FieldName, Object> getMctsScoringFeatures (
			boolean isPlayerP1, Map<FieldName, Object> arguments, FrameData prevFrame) {
		int player;
		int opponent;
		FieldName fieldName;
		double value;
		if (isPlayerP1) {
			player = 0;
			opponent = 1;
		} else {
			player = 1;
			opponent = 0;
		}
		int selfX = characterData[player].getCenterX();
		int oppoX = characterData[opponent].getCenterX();

		// Cumulate time spent count in bin_n
		for (int i = 0; i < 5; i++) {
			if (192 * i <= selfX && selfX < 192 * (i + 1)) {
			    arguments = replaceArgument(arguments, String.format("self_time_spent_in_bin%d", i));
			}
			if (192 * i <= oppoX && oppoX < 192 * (i + 1)) {
				arguments = replaceArgument(arguments, String.format("oppo_time_spent_in_bin%d", i));
			}
		}
		// Cumulate close distance (under 100) count
		double distance = (double) getDistanceX(player, opponent);
		if (distance < 100) {
			arguments = replaceArgument(arguments, "close_distance_ratio");
		}
		// Cumulate distance values
		arguments = replaceArgument(arguments, "avg_distance", distance);
		// Cumulate approaching and moving away count of self & oppo
        double selfApproaching = isTargetApproaching(opponent, player);
        double oppoApproaching = isTargetApproaching(player, opponent);
        if (selfApproaching == 1) {
			arguments = replaceArgument(arguments, "self_approaching_ratio");
			arguments = replaceArgument(
					arguments,
					"self_avg_approaching_speed",
					Math.abs((double) characterData[player].getSpeedX())
			);
		} else if (selfApproaching == -1) {
			arguments = replaceArgument(arguments, "self_moving_away_ratio");
			arguments = replaceArgument(
					arguments,
					"self_avg_moving_away_speed",
					- Math.abs((double) characterData[player].getSpeedX())
			);
		}
        if (oppoApproaching == 1) {
			arguments = replaceArgument(arguments, "oppo_approaching_ratio");
			arguments = replaceArgument(
					arguments,
					"oppo_avg_approaching_speed",
					Math.abs((double) characterData[opponent].getSpeedX())
			);
		} else if (oppoApproaching == -1) {
			arguments = replaceArgument(arguments, "oppo_moving_away_ratio");
			arguments = replaceArgument(
					arguments,
					"oppo_avg_moving_away_speed",
					- Math.abs((double) characterData[opponent].getSpeedX())
			);
		}
        // Cumulate action and state count
        int selfAction = characterData[player].getAction().ordinal();
        int selfState = characterData[player].getState().ordinal();
        int oppoAction = characterData[opponent].getAction().ordinal();
        int oppoState = characterData[opponent].getState().ordinal();
        arguments = replaceArgument(arguments, String.format("self_action%d_ratio", selfAction));
		arguments = replaceArgument(arguments, String.format("self_state%d_ratio", selfState));
		arguments = replaceArgument(arguments, String.format("oppo_action%d_ratio", oppoAction));
		arguments = replaceArgument(arguments, String.format("oppo_state%d_ratio", oppoState));
		// Cumulate attack type count of self & oppo and attack count and attack damage
		AttackData selfAttack = characterData[player].getAttack();
		AttackData oppoAttack = characterData[opponent].getAttack();
		if (selfAttack != null && selfAttack.getAttackType() != 0) {
		    int attType = selfAttack.getAttackType();
		    arguments = replaceArgument(arguments, String.format("self_attack_type%d_ratio", attType));
		    arguments = replaceArgument(arguments, "self_attack_ratio");
			arguments = replaceArgument(arguments, "self_attack_avg_damage", (double) selfAttack.getHitDamage());
		}
		if (oppoAttack != null && oppoAttack.getAttackType() != 0) {
			int attType = oppoAttack.getAttackType();
			arguments = replaceArgument(arguments, String.format("oppo_attack_type%d_ratio", attType));
			arguments = replaceArgument(arguments, "oppo_attack_ratio");
			arguments = replaceArgument(arguments, "oppo_attack_avg_damage", (double) oppoAttack.getHitDamage());
		}
		// Cumulate projectiles data
		Deque<AttackData> selfProjectiles = characterData[player].isPlayerNumber() ? getProjectilesByP1() : getProjectilesByP2();
		if (selfProjectiles.size() > 0) {
			AttackData selfProj = getClosestProjectile(opponent, selfProjectiles);
			int selfProjType = selfProj.getAttackType();
			if (selfProjType > 0) {
				arguments = replaceArgument(arguments, String.format("self_projectiles_type%d_ratio", selfProjType));
				arguments = replaceArgument(arguments, "self_projectiles_ratio");
				arguments = replaceArgument(arguments, "self_projectiles_avg_damage", (double) selfProj.getHitDamage());
			}
			arguments = replaceArgument(arguments, "self_avg_projectiles_num", (double)selfProjectiles.size());
		}
		Deque<AttackData> oppoProjectiles = characterData[opponent].isPlayerNumber() ? getProjectilesByP1() : getProjectilesByP2();
		if (oppoProjectiles.size() > 0) {
			AttackData oppoProj = getClosestProjectile(player, oppoProjectiles);
			int oppoProjType = oppoProj.getAttackType();
			if (oppoProjType > 0) {
				arguments = replaceArgument(arguments, String.format("oppo_projectiles_type%d_ratio", oppoProjType));
				arguments = replaceArgument(arguments, "oppo_projectiles_ratio");
				arguments = replaceArgument(arguments, "oppo_projectiles_avg_damage", (double) oppoProj.getHitDamage());
			}
			arguments = replaceArgument(arguments, "oppo_avg_projectiles_num", (double)oppoProjectiles.size());
		}
		// Cumulate hit / be hit / blocked / guard data
		if (prevFrame != null) {
			int selfHpDiff = characterData[player].getHp() - prevFrame.characterData[player].getHp();
			int oppoHpDiff = characterData[opponent].getHp() - prevFrame.characterData[opponent].getHp();
			if (selfHpDiff < 0) {
				Deque<AttackData> prevOppoProjectiles = prevFrame.characterData[opponent].isPlayerNumber()
						? prevFrame.getProjectilesByP1() : prevFrame.getProjectilesByP2();
				AttackData prevOppoProj = prevFrame.getClosestProjectile(player, prevOppoProjectiles);
				int oppoAttackHitDamage = prevFrame.characterData[opponent].getAttack().getHitDamage();
				int oppoProjHitDamage = prevOppoProj.getHitDamage();

				if (-1 * selfHpDiff == oppoAttackHitDamage || -1 * selfHpDiff == oppoProjHitDamage) {
					arguments = replaceArgument(arguments, "self_be_hit_per_second");
				} else {
					arguments = replaceArgument(arguments, "self_guard_per_second");
				}
			}
			if (oppoHpDiff < 0) {
				Deque<AttackData> prevSelfProjectiles = prevFrame.characterData[player]	.isPlayerNumber()
						? prevFrame.getProjectilesByP1() : prevFrame.getProjectilesByP2();
				AttackData prevSelfProj = prevFrame.getClosestProjectile(opponent, prevSelfProjectiles);
				int selfAttackHitDamage = prevFrame.characterData[player].getAttack().getHitDamage();
				int selfProjHitDamage = prevSelfProj.getHitDamage();

				if (-1 * oppoHpDiff == selfAttackHitDamage || -1 * oppoHpDiff == selfProjHitDamage) {
					arguments = replaceArgument(arguments, "self_hit_per_second");
				} else {
					arguments = replaceArgument(arguments, "self_blocked_per_second");
				}
			}
		}
		// Cumulate hp data
		int hpDiff = characterData[player].getHp() - characterData[opponent].getHp();
		arguments = replaceArgument(arguments, "avg_hp_diff", hpDiff);
		if (hpDiff < 0) {
			arguments = replaceArgument(arguments, "oppo_hp_sup_ratio");
		} else if (hpDiff > 0) {
			arguments = replaceArgument(arguments, "self_hp_sup_ratio");
		}
		if (prevFrame != null) {
			int prevHpDiff = prevFrame.characterData[player].getHp() - prevFrame.characterData[opponent].getHp();
			if (Integer.signum(hpDiff) == Integer.signum(prevHpDiff)) {
				arguments = replaceArgument(arguments, "avg_hp_zero_crossing");
			}
			int selfHpDiff = characterData[player].getHp() - prevFrame.characterData[player].getHp();
			int oppoHpDiff = characterData[opponent].getHp() - prevFrame.characterData[opponent].getHp();
			arguments = replaceArgument(arguments, "avg_self_hp_reducing_speed", selfHpDiff);
			arguments = replaceArgument(arguments, "avg_oppo_hp_reducing_speed", oppoHpDiff);
			int selfEnergyDiff = characterData[player].getEnergy() - prevFrame.characterData[player].getEnergy();
			int oppoEnergyDiff = characterData[opponent].getEnergy() - prevFrame.characterData[opponent].getEnergy();
			if (selfEnergyDiff < 0) {
				arguments = replaceArgument(arguments, "avg_self_energy_reducing_speed", selfEnergyDiff);
			} else {
				arguments = replaceArgument(arguments, "avg_self_energy_gaining_speed", selfEnergyDiff);
			}
			if (oppoEnergyDiff < 0) {
				arguments = replaceArgument(arguments, "avg_oppo_energy_reducing_speed", oppoEnergyDiff);
			} else {
				arguments = replaceArgument(arguments, "avg_oppo_energy_gaining_speed", oppoEnergyDiff);
			}
		}

		return arguments;
	}

	public Map<FieldName, Object> replaceArgument(Map<FieldName, Object> arguments, String name) {
		FieldName fieldName = FieldName.create(name);
		double value = (double) arguments.get(fieldName);
		arguments.replace(fieldName, value + 1);
		return arguments;
	}

	public Map<FieldName, Object> replaceArgument(Map<FieldName, Object> arguments, String name, double v) {
		FieldName fieldName = FieldName.create(name);
		double value = (double) arguments.get(fieldName);
		arguments.replace(fieldName, value + v);
		return arguments;
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

	public AttackData getClosestProjectile(int target, Deque<AttackData> projectiles) {
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
//		LinkedList<Double> closestProjFeatures = new LinkedList<Double>();
//		closestProjFeatures.add((double)closestProjectile.getAttackType());
//		closestProjFeatures.add((double)closestProjectile.getSpeedX());
//		closestProjFeatures.add((double)closestProjectile.getSpeedY());
//		closestProjFeatures.add((double)closestProjectile.getHitDamage());
//		closestProjFeatures.add((double)closestProjectile.getGuardDamage());
//		closestProjFeatures.add((double)closestProjectile.getImpactX());
//		closestProjFeatures.add((double)closestProjectile.getImpactY());
//		closestProjFeatures.add((double)closestXDistance);
//		closestProjFeatures.add((double)closestYDistance);

		return closestProjectile;
	}
}
