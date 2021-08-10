import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.visitors.DefaultVisitorBattery;
import org.xml.sax.SAXException;
import simulator.Simulator;
import struct.CharacterData;
import struct.*;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI implementing MCTS
 *
 * @author Taichi Miyazaki
 */
public class Agent12 implements AIInterface {

    private Simulator simulator;
    private Key key;
    private CommandCenter commandCenter;
    private boolean playerNumber;
    private GameData gameData;

    /**
     * Main FrameData
     */
    private FrameData frameData;

    /**
     * Data with FRAME_AHEAD frames ahead of FrameData
     */
    private FrameData simulatorAheadFrameData;

    /**
     * All actions that could be performed by self character
     */
    private LinkedList<Action> myActions;

    /**
     * All actions that could be performed by the opponent character
     */
    private LinkedList<Action> oppActions;

    /**
     * self information
     */
    private CharacterData myCharacter;

    /**
     * opponent information
     */
    private CharacterData oppCharacter;

    /**
     * Number of adjusted frames (following the same recipe in JerryMizunoAI)
     */
    private static final int FRAME_AHEAD = 14;

    private ArrayList<MotionData> myMotion;

    private ArrayList<MotionData> oppMotion;

    private Action[] actionAir;

    private Action[] actionGround;

    private Action spSkill;

    private Node rootNode;

    /**
     * True if in debug mode, which will output related log
     */
    public static final boolean DEBUG_MODE = false;

    private static final File challengeModel = new File("./data/aiData/OurMcts/Challenge_Lgb.pmml");
    private static final File competenceModel = new File("./data/aiData/OurMcts/Competence_Randomforest.pmml");
    private static final File immersionModel = new File("./data/aiData/OurMcts/Immersion_Randomforest.pmml");
    private static final File valenceModel = new File("./data/aiData/OurMcts/Valence_Randomforest.pmml");

    private Evaluator challengeEvaluator;
    private Evaluator competenceEvaluator;
    private Evaluator immersionEvaluator;
    private Evaluator valenceEvaluator;

    private Deque<FrameData> trajectory;

    private ExecutorService executorService;
    @Override
    public void close() {
        executorService.shutdown();
    }

    @Override
    public void roundEnd(int p1Hp, int p2Hp, int frames) {
    }

    // @Override
    // public String getCharacter() {
    //   return CHARACTER_ZEN;
    // }

    @Override
    public void getInformation(FrameData frameData) {
        this.frameData = frameData;
        this.commandCenter.setFrameData(this.frameData, playerNumber);

        if (playerNumber) {
            myCharacter = frameData.getCharacter(true);
            oppCharacter = frameData.getCharacter(false);
        } else {
            myCharacter = frameData.getCharacter(false);
            oppCharacter = frameData.getCharacter(true);
        }
    }

    // playerNumber = true(P1) / false(P2)
    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        this.playerNumber = playerNumber;
        this.gameData = gameData;

        this.key = new Key();
        this.frameData = new FrameData();
        this.commandCenter = new CommandCenter();

        this.myActions = new LinkedList<Action>();
        this.oppActions = new LinkedList<Action>();

        executorService = Executors.newFixedThreadPool(48);

        challengeEvaluator = getEvaluator(challengeModel);
        competenceEvaluator = getEvaluator(competenceModel);
        immersionEvaluator = getEvaluator(immersionModel);
        valenceEvaluator = getEvaluator(valenceModel);

        trajectory = new LinkedList<FrameData>(){
            public boolean add(FrameData frameData) {
                boolean result;
                if (this.size() >= 20) {
                    super.removeFirst();
                }
                result = super.add(frameData);
                return result;
            }
        };

        simulator = gameData.getSimulator();

        actionAir =
                new Action[]{Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
                        Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
                        Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
                        Action.AIR_D_DB_BB};
        actionGround =
                new Action[]{Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
                        Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
                        Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
                        Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
                        Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
                        Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
        spSkill = Action.STAND_D_DF_FC;

        myMotion = this.playerNumber ? gameData.getMotionData(true) : gameData.getMotionData(false);
        oppMotion = this.playerNumber ? gameData.getMotionData(false) : gameData.getMotionData(true);

        return 0;
    }

    @Override
    public Key input() {
        return key;
    }

    @Override
    public void processing() {

        if (canProcessing()) {
            if (commandCenter.getSkillFlag()) {
                key = commandCenter.getSkillKey();
            } else {
                key.empty();
                commandCenter.skillCancel();

                mctsPrepare(); // Some preparation for MCTS
                rootNode =
                        new Node12(simulatorAheadFrameData, null, myActions, oppActions, gameData, playerNumber,
                                commandCenter, challengeEvaluator, competenceEvaluator,
                                immersionEvaluator, valenceEvaluator, trajectory);
                rootNode.createNode();

                Action bestAction = rootNode.mcts(executorService); // Perform MCTS
                if (DEBUG_MODE) {
                    rootNode.printNode(rootNode);
                }

                commandCenter.commandCall(bestAction.name()); // Perform an action selected by MCTS
            }
        }
    }

    /**
     * Determine whether or not the AI can perform an action
     *
     * @return whether or not the AI can perform an action
     */
    public boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingTimeMilliseconds() > 0;
    }

    /**
     * Some preparation for MCTS
     * Perform the process for obtaining FrameData with 14 frames ahead
     */
    public void mctsPrepare() {
        ArrayList<FrameData> FrameData_arr = simulator.simulate(frameData, playerNumber, null, null, FRAME_AHEAD, false);
        simulatorAheadFrameData = FrameData_arr.get(FrameData_arr.size() - 1);

        myCharacter = simulatorAheadFrameData.getCharacter(playerNumber);
        oppCharacter = simulatorAheadFrameData.getCharacter(!playerNumber);

        setMyAction();
        setOppAction();
    }

    public void setMyAction() {
        myActions.clear();

        int energy = myCharacter.getEnergy();

        if (myCharacter.getState() == State.AIR) {
            for (int i = 0; i < actionAir.length; i++) {
                if (Math.abs(myMotion.get(Action.valueOf(actionAir[i].name()).ordinal())
                        .getAttackStartAddEnergy()) <= energy) {
                    myActions.add(actionAir[i]);
                }
            }
        } else {
            if (Math.abs(myMotion.get(Action.valueOf(spSkill.name()).ordinal())
                    .getAttackStartAddEnergy()) <= energy) {
                myActions.add(spSkill);
            }

            for (int i = 0; i < actionGround.length; i++) {
                if (Math.abs(myMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
                        .getAttackStartAddEnergy()) <= energy) {
                    myActions.add(actionGround[i]);
                }
            }
        }

    }

    public void setOppAction() {
        oppActions.clear();

        int energy = oppCharacter.getEnergy();

        if (oppCharacter.getState() == State.AIR) {
            for (int i = 0; i < actionAir.length; i++) {
                if (Math.abs(oppMotion.get(Action.valueOf(actionAir[i].name()).ordinal())
                        .getAttackStartAddEnergy()) <= energy) {
                    oppActions.add(actionAir[i]);
                }
            }
        } else {
            if (Math.abs(oppMotion.get(Action.valueOf(spSkill.name()).ordinal())
                    .getAttackStartAddEnergy()) <= energy) {
                oppActions.add(spSkill);
            }

            for (int i = 0; i < actionGround.length; i++) {
                if (Math.abs(oppMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
                        .getAttackStartAddEnergy()) <= energy) {
                    oppActions.add(actionGround[i]);
                }
            }
        }
    }

    public Evaluator getEvaluator(File model) {
        try {
            Evaluator evaluator = new LoadingModelEvaluatorBuilder() {
                @Override
                protected void checkSchema(ModelEvaluator<?> modelEvaluator) {
                    Model model = modelEvaluator.getModel();
                    MiningSchema miningSchema = model.getMiningSchema();
                    List<InputField> inputFields = modelEvaluator.getInputFields();
                    List<InputField> groupFields = Collections.emptyList();
                    if (modelEvaluator instanceof HasGroupFields) {
                        HasGroupFields hasGroupFields = (HasGroupFields) modelEvaluator;
                        groupFields = hasGroupFields.getGroupFields();
                    }

                    if (inputFields.size() + groupFields.size() > 2000) {
                        throw new InvalidElementException("Model has too many input fields", miningSchema);
                    } else {
                        List<TargetField> targetFields = modelEvaluator.getTargetFields();
                        List<OutputField> outputFields = modelEvaluator.getOutputFields();
                        if (targetFields.size() + outputFields.size() < 1) {
                            throw new InvalidElementException("Model does not have any target or output fields", miningSchema);
                        }
                    }
                }
            }
                    .setLocatable(false)
                    .setVisitors(new DefaultVisitorBattery())
                    .load(model)
                    .build();

            return evaluator;
        } catch (IOException | JAXBException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}