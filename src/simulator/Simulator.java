package simulator;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import enumerate.Action;
import fighting.Motion;
import struct.FrameData;
import struct.GameData;

import struct.CharacterData;
import enumerate.State;
import struct.MotionData;
import java.util.Random;

/**
 * The class of the simulator.
 */
public class Simulator {

	/**
	 * The variable that holds invariant information in the game.
	 */
	private GameData gameData;

	private Action[] actionAir;
	private Action[] actionGround;
	private Action spSkill;

	/**
	 * The class constructor that creates an instance of the Simulator class by
	 * using an instance of the GameData class.
	 *
	 * @param gameData
	 *            an instance of the GameData class
	 */
	public Simulator(GameData gameData) {
		this.gameData = gameData;
		actionAir =
        new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
            Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
            Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
            Action.AIR_D_DB_BB};
    	actionGround =
        new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
            Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
            Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
            Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
            Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
            Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
    	spSkill = Action.STAND_D_DF_FC;
	}

	/**
	 * Simulates the progression of a fight starting from a given
	 * {@link FrameData} instance and executing specified actions for both
	 * players.<br>
	 * The resulting {@link FrameData} is returned, and can be used to assess
	 * the outcome of the simulation.<br>
	 *
	 * Note that when the character is on ground, all AIR actions will be
	 * considered invalid by the simulator. Likewise, all GROUND actions are
	 * considered invalid if the character is in air. <br>
	 * To simulate AIR actions when the character is initially on ground, add
	 * Action.JUMP to the action list before the AIR action.<br>
	 * For example, myAct.add(Action.JUMP); myAct.add(Action.AIR_A);
	 *
	 * @param frameData
	 *            frame data at the start of simulation
	 * @param playerNumber
	 *            boolean value which identifies P1/P2. {@code true} if the
	 *            player is P1, or {@code false} if P2.
	 * @param myAct
	 *            actions to be performed by the player identified by the
	 *            parameter `player`
	 * @param oppAct
	 *            actions to be performed by the opponent of the player
	 *            identified by the parameter `player`
	 * @param simulationLimit
	 *            the number of frames to be simulated.<br>
	 *            If `simulationLimit` is less than the number of frames
	 *            required for executing the actions of a given player, the
	 *            simulator will simulate the player's actions up to
	 *            `simulationLimit` frames; otherwise, the simulator will
	 *            simulate the player's all actions and then continue the
	 *            simulation until the `simulationLimit`-th frame assuming no
	 *            actions are performed by the player.
	 *
	 * @return the frame data after the simulation
	 */
	public ArrayList<FrameData> simulate(FrameData frameData, boolean playerNumber, Deque<Action> myAct, Deque<Action> oppAct,
			int simulationLimit, boolean logging) {

		// Creates deep copy of each action's list
		LinkedList<Action> myActions = new LinkedList<Action>();
		LinkedList<Action> oppActions = new LinkedList<Action>();
		
		FrameData CurrentSate = frameData;
		ArrayList<FrameData> LoggedFrameDatas = new ArrayList<FrameData>(simulationLimit);

		Random rnd = new Random();

		ArrayList<Deque<Action>> tempActionList = new ArrayList<Deque<Action>>(2);
		Deque<Action> tempP1Act = ((playerNumber ? myAct : oppAct) == null) ? null
				: new LinkedList<Action>(playerNumber ? myAct : oppAct);
		Deque<Action> tempP2Act = ((!playerNumber ? myAct : oppAct) == null) ? null
				: new LinkedList<Action>(!playerNumber ? myAct : oppAct);
		tempActionList.add(tempP1Act);
		tempActionList.add(tempP2Act);

		ArrayList<ArrayList<Motion>> tempMotionList = new ArrayList<ArrayList<Motion>>(2);
//		ArrayList<Motion> p1MotionData = this.gameData.getMotion(playerNumber ? true : false);
//		ArrayList<Motion> p2MotionData = this.gameData.getMotion(!playerNumber ? true : false);
		ArrayList<Motion> p1MotionData = this.gameData.getMotion(true);
		ArrayList<Motion> p2MotionData = this.gameData.getMotion(false);
		tempMotionList.add(p1MotionData);
		tempMotionList.add(p2MotionData);
		
		int nowFrame = frameData.getFramesNumber();
		SimFighting simFighting = new SimFighting();
		
		// for debugging
		int debug_int = 0;

		int inner_limit = 0;
		if (logging){
			for (int i = 0; i < simulationLimit; i++) {
				//select current avalable actions of player
				CharacterData myCharacter = CurrentSate.getCharacter(playerNumber);
				ArrayList<MotionData> myMotion = this.gameData.getMotionData(playerNumber);
				myActions = setMyAction(myActions, myCharacter, myMotion);

				//select current avalable actions of opponent
				CharacterData oppCharacter = CurrentSate.getCharacter(!playerNumber);
				ArrayList<MotionData> oppMotion = this.gameData.getMotionData(!playerNumber);
				oppActions = setMyAction(oppActions, oppCharacter, oppMotion);
				
				//sample a random action from avalable actions, then append it to ActionList
				tempActionList.get(0).add(myActions.get(rnd.nextInt(myActions.size())));
				tempActionList.get(1).add(oppActions.get(rnd.nextInt(oppActions.size())));

				//initialize the simulator with the selected actions
				simFighting.initialize(tempMotionList, tempActionList, new FrameData(frameData), playerNumber);

				//simulate until sample all player's actions, or player can move, and the number of steps is less than the simulationLimit
				while ((tempActionList.get((playerNumber ? 0:1)).size() != 0 || !CurrentSate.getCharacter(playerNumber).isControl())&&(inner_limit<=simulationLimit)){
					simFighting.processingFight(nowFrame);
					//save current state
					CurrentSate = simFighting.createFrameData(nowFrame, frameData.getRound());
					LoggedFrameDatas.add(CurrentSate);
					
					nowFrame++;
					inner_limit++;
				}
				i = inner_limit - 1;
			}
		}else{
			simFighting.initialize(tempMotionList, tempActionList, new FrameData(frameData), playerNumber);
			for (int i = 0; i < simulationLimit; i++) {
				simFighting.processingFight(nowFrame);
				nowFrame++;
			}
			LoggedFrameDatas.add(simFighting.createFrameData(nowFrame, frameData.getRound()));
		}

		return LoggedFrameDatas;
	}

	public LinkedList<Action> setMyAction(LinkedList<Action> Actions, CharacterData myCharacter, ArrayList<MotionData> myMotion) {
		Actions.clear();
		int energy = myCharacter.getEnergy();
	
		if (myCharacter.getState() == State.AIR) {
		  for (int i = 0; i < actionAir.length; i++) {
			if (Math.abs(myMotion.get(Action.valueOf(actionAir[i].name()).ordinal())
				.getAttackStartAddEnergy()) <= energy) {
				Actions.add(actionAir[i]);
			}
		  }
		} else {
		  if (Math.abs(myMotion.get(Action.valueOf(spSkill.name()).ordinal())
			  .getAttackStartAddEnergy()) <= energy) {
			Actions.add(spSkill);
		  }
	
		  for (int i = 0; i < actionGround.length; i++) {
			if (Math.abs(myMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
				.getAttackStartAddEnergy()) <= energy) {
				Actions.add(actionGround[i]);
			}
		  }
		}
		return Actions;
	}
}
