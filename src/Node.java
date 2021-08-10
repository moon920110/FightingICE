import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.lwjgl.system.CallbackI;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import setting.MctsSetting;

import aiinterface.CommandCenter;

import enumerate.Action;

//for multi thread control
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Node in MCTS
 *
 * @author Taichi Miyazaki
 */
public class Node {

    /**
     * UCT execution time
     */
    public static final int UCT_TIME = 165 * 1000000; //165 * 100000;

    /**
     * Value of C in UCB1
     */
    public static final double UCB_C = 2 * 1.414;

    /**
     * Depth of tree search
     */
    public static final int UCT_TREE_DEPTH = 5;

    /**
     * Threshold for generating a node
     */
    public static final int UCT_CREATE_NODE_THRESHOLD = 10;

    /**
     * Time for performing simulation
     */
    public static final int SIMULATION_TIME = 30;  // 0.5sec

    /**
     * Use when in need of random numbers
     */
    private Random rnd;

    /**
     * Parent node
     */
    private Node parent;

    /**
     * Child node
     */
    private Node[] children;

    /**
     * Node depth
     */
    private int depth;

    /**
     * Number of node visiting times
     */
    private int games;

    /**
     * UCB1 Value
     */
    private double ucb;

    /**
     * Evaluation value
     */
    private double score;
    private double lastestSimulatedScore;

    /**
     * All selectable actions of self AI
     */
    private LinkedList<Action> myActions;

    /**
     * All selectable actions of the opponent
     */
    private LinkedList<Action> oppActions;

    /**
     * Use in simulation
     */
    private Simulator simulator;

    /**
     * Selected action by self AI during search
     */
    private LinkedList<Action> selectedMyActions;

    /**
     * Self HP before simulation
     */
    private int myOriginalHp;

    /**
     * Opponent HP before simulation
     */
    private int oppOriginalHp;

    private FrameData frameData;
    private boolean playerNumber;
    private CommandCenter commandCenter;
    private GameData gameData;

    private boolean isCreateNode;

    private Evaluator anxietyEvaluator;
    private Evaluator boredomEvaluator;
    private Evaluator challengeEvaluator;
    private Evaluator competenceEvaluator;
    private Evaluator immersionEvaluator;
    private Evaluator valenceEvaluator;

    Deque<Action> mAction;
    Deque<Action> oppAction;
    Deque<FrameData> trajectory;

    public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
                LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
                CommandCenter commandCenter, LinkedList<Action> selectedMyActions,
                Evaluator challengeEvaluator, Evaluator competenceEvaluator,
                Evaluator immersionEvaluator, Evaluator valenceEvaluator, Deque<FrameData> trajectory) {
        this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter,
                challengeEvaluator, competenceEvaluator,
                immersionEvaluator, valenceEvaluator, trajectory);

        this.selectedMyActions = selectedMyActions;
    }

    public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
                LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
                CommandCenter commandCenter, Evaluator challengeEvaluator,
                Evaluator competenceEvaluator, Evaluator immersionEvaluator,
                Evaluator valenceEvaluator, Deque<FrameData> trajectory) {
        this.frameData = frameData;
        this.parent = parent;
        this.myActions = myActions;
        this.oppActions = oppActions;
        this.gameData = gameData;
        this.simulator = new Simulator(gameData);
        this.playerNumber = playerNumber;
        this.commandCenter = commandCenter;
        this.challengeEvaluator = challengeEvaluator;
        this.competenceEvaluator = competenceEvaluator;
        this.immersionEvaluator = immersionEvaluator;
        this.valenceEvaluator = valenceEvaluator;
        this.trajectory = trajectory;

        if (this.trajectory.size() < MctsSetting.TRAJECTORY_CAPACITY) {
            for (int i = 0; i < MctsSetting.TRAJECTORY_CAPACITY; i++) {
                this.trajectory.add(this.frameData);
            }
        } else {
            this.trajectory.add(this.frameData);
        }
        this.selectedMyActions = new LinkedList<Action>();

        this.rnd = new Random();
        this.mAction = new LinkedList<Action>();
        this.oppAction = new LinkedList<Action>();

        CharacterData myCharacter = playerNumber ? frameData.getCharacter(true) : frameData.getCharacter(false);
        CharacterData oppCharacter = playerNumber ? frameData.getCharacter(false) : frameData.getCharacter(true);
        myOriginalHp = myCharacter.getHp();
        oppOriginalHp = oppCharacter.getHp();

        if (this.parent != null) {
            this.depth = this.parent.depth + 1;
        } else {
            this.depth = 0;
        }
    }

    /**
     * Perform MCTS
     *
     * @return action of the most visited node
     */
    public Action mcts() {
        // Repeat UCT as many times as possible
        long start = System.nanoTime();
        for (; System.nanoTime() - start <= UCT_TIME; ) {
//             uctSingle();
            uctMulti();
        }

        return getBestScoreAction();  //getBestVisitAction();
    }

    /**
     * Perform a playout (simulation)
     *
     * @return the evaluation value of the playout
     */
    public double playout() {
        long beforeTime = System.currentTimeMillis();

        mAction.clear();
        oppAction.clear();

        for (int i = 0; i < selectedMyActions.size(); i++) {
            mAction.add(selectedMyActions.get(i));
        }

        // when selectedMyActions is 0
        for (int i = 0; i < 1 - selectedMyActions.size(); i++) {
            mAction.add(myActions.get(rnd.nextInt(myActions.size())));
        }

        for (int i = 0; i < 1; i++) {// (int i = 0; i < 5; i++) {
            oppAction.add(oppActions.get(rnd.nextInt(oppActions.size())));
        }

//        Map<FieldName, Object> arguments =
        ArrayList<FrameData> FrameData_arr =
                simulator.simulate(frameData, playerNumber, mAction, oppAction, SIMULATION_TIME, true); // Perform simulation

        long afterTime = System.currentTimeMillis();
        // long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        // System.out.println("시간차이(m) : "+secDiffTime);
        return getScore(FrameData_arr);
    }

    /**
     * Perform UCT
     *
     * @return the evaluation value
     */
    public double uctSingle() {

        Node selectedNode = null;
        double bestUcb;

        bestUcb = -99999;

        for (Node child : this.children) {
            if (child.games == 0) {
                child.ucb = 9999 + rnd.nextInt(50);
            } else {
                child.ucb = getUcb(child.score / child.games, games, child.games);
            }


            if (bestUcb < child.ucb) {
                selectedNode = child;
                bestUcb = child.ucb;
            }

        }

        double score = 0;
        if (selectedNode.games == 0) {
            score = selectedNode.playout();
        } else {
            if (selectedNode.children == null) {
                if (selectedNode.depth < UCT_TREE_DEPTH) {
                    if (UCT_CREATE_NODE_THRESHOLD <= selectedNode.games) {
                        selectedNode.createNode();
                        selectedNode.isCreateNode = true;  // for debugging
                        score = selectedNode.uctSingle();
                    } else {
                        score = selectedNode.playout();
                    }
                } else {
                    score = selectedNode.playout();
                }
            } else {
                if (selectedNode.depth < UCT_TREE_DEPTH) {
                    score = selectedNode.uctSingle();
                } else {
                    selectedNode.playout();
                }
            }

        }

        selectedNode.games++;
        selectedNode.score += score;

        if (depth == 0) {
            games++;
        }

        return score;
    }

    public double uctMulti() {

        Node selectedNode = null;
        double bestUcb;
        bestUcb = -99999;

        for (Node child : this.children) {
            if (child.games == 0) {
                child.ucb = 9999 + rnd.nextInt(50);
            } else {
                child.ucb = getUcb(child.score / child.games, games, child.games);
            }

            if (bestUcb < child.ucb) {
                selectedNode = child;
                bestUcb = child.ucb;
            }
        }

        double score = 0;
        if (selectedNode.games == 0) {
            try {
                score = runWorkers(selectedNode);
            } catch (InterruptedException e) {
            }
        } else {
            if (selectedNode.children == null) {
                if (selectedNode.depth < UCT_TREE_DEPTH) {
                    if (UCT_CREATE_NODE_THRESHOLD <= selectedNode.games) {
                        selectedNode.createNode();
                        selectedNode.isCreateNode = true;
                        score = selectedNode.uctMulti();
                    } else {
                        try {
                            score = runWorkers(selectedNode);
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    try {
                        score = runWorkers(selectedNode);
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                if (selectedNode.depth < UCT_TREE_DEPTH) {
                    score = selectedNode.uctMulti();
                } else {
                    try {
                        score = runWorkers(selectedNode);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if (depth == 0) {
            this.score += score;
            games += this.children.length;
        } else {
            selectedNode.parent.score += score;
            games += selectedNode.parent.children.length;
        }

        return score;
    }

    /**
     * Generate a node
     */
    public void createNode() {

        this.children = new Node[myActions.size()];

        for (int i = 0; i < children.length; i++) {

            LinkedList<Action> my = new LinkedList<Action>();
            for (Action act : selectedMyActions) {
                my.add(act);
            }

            my.add(myActions.get(i));

            children[i] =
                    new Node(frameData, this, myActions, oppActions, gameData,
                            playerNumber, commandCenter, my,
                            challengeEvaluator, competenceEvaluator,
                            immersionEvaluator, valenceEvaluator,
                            trajectory);
        }
    }

    /**
     * Return the action of the most visited node
     *
     * @return Action of the most visited node
     */
    public Action getBestVisitAction() {

        int selected = -1;
        double bestGames = -9999;

        for (int i = 0; i < children.length; i++) {

            if (OurMctsAi.DEBUG_MODE) {
                System.out.println("Evaluation value:" + children[i].score / children[i].games + ",Number of trials:"
                        + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));
            }

            if (bestGames < children[i].games) {
                bestGames = children[i].games;
                selected = i;
            }
        }

        if (OurMctsAi.DEBUG_MODE) {
            System.out.println(myActions.get(selected) + ",Total number of trails:" + games);
            System.out.println("");
        }

        return this.myActions.get(selected);
    }

    /**
     * Return the action of the highest score node
     *
     * @return Action of the highest score node
     */
    public Action getBestScoreAction() {

        int selected = -1;
        double bestScore = -9999;

        for (int i = 0; i < children.length; i++) {

            System.out.println("Evaluation value:" + children[i].score / children[i].games + ",Number of trials:"
                    + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));

            double meanScore = children[i].score / children[i].games;
            if (bestScore < meanScore) {
                bestScore = meanScore;
                selected = i;
            }
        }

        System.out.println(myActions.get(selected) + ",Total number of trails:" + games);
        System.out.println("");

        return this.myActions.get(selected);
    }

    /**
     * Return the evaluation value
     *
     * @param fds frame data (including information such as hp)
     * @return the evaluation value
     */
    public double getScore(ArrayList<FrameData> fds) {
        Map<FieldName, Object> arguments = new HashMap<FieldName, Object>();
        FrameData prevFrame = null;
        long start = System.currentTimeMillis();
        Deque<FrameData> trajectory = new LinkedList<FrameData>(this.trajectory){
            public boolean add(FrameData frameData) {
                boolean result;
                if (this.size() >= MctsSetting.TRAJECTORY_CAPACITY) {
                    super.removeFirst();
                }
                result = super.add(frameData);
                return result;
            }
        };
        for (FrameData fd: fds) {
            trajectory.add(fd);
        }
        for (String name: MctsSetting.ARGUMENT_NAMES) {
            arguments.put(FieldName.create(name), 0.0);
        }
        for (FrameData fd : trajectory) {
            // It must be calculated on the user side
            arguments = fd.getMctsScoringFeatures(!playerNumber, arguments, prevFrame);
            prevFrame = fd;
        }
        arguments = normalizeArguments(arguments);

        Map<FieldName, ?> challengeResult = challengeEvaluator.evaluate(arguments);
        Map<FieldName, ?> competenceResult = competenceEvaluator.evaluate(arguments);
        Map<FieldName, ?> immersionResult = immersionEvaluator.evaluate(arguments);
        Map<FieldName, ?> valenceResult = valenceEvaluator.evaluate(arguments);

        int challenge = (int) ((ProbabilityDistribution) challengeResult.get(FieldName.create("Ch_rank"))).getResult();
        int competence = (int) ((ProbabilityDistribution) competenceResult.get(FieldName.create("Co_rank"))).getResult();
        int immersion = (int) ((ProbabilityDistribution) immersionResult.get(FieldName.create("Im_rank"))).getResult();
        int valence = (int) ((ProbabilityDistribution) valenceResult.get(FieldName.create("Va_rank"))).getResult();

        double challengeScore = challenge == 1
                ? -(Double) challengeResult.get(FieldName.create("probability(1)"))
                : (Double) challengeResult.get(FieldName.create("probability(0)"));
        double competenceScore = competence == 1
                ? (Double) competenceResult.get(FieldName.create("probability(1)"))
                : -(Double) competenceResult.get(FieldName.create("probability(0)"));
        double immersionScore = immersion == 1
                ? (Double) immersionResult.get(FieldName.create("probability(1)"))
                : -(Double) immersionResult.get(FieldName.create("probability(0)"));
        double valenceScore = valence == 1
                ? (Double) valenceResult.get(FieldName.create("probability(1)"))
                : -(Double) valenceResult.get(FieldName.create("probability(0)"));

        return valenceScore;// challengeScore + competenceScore + immersionScore + valenceScore;
    }

    /**
     * Return the UCB1 value calculated from the evaluation value, the total number of playouts(trails), and the number of playouts of the corresponding action
     *
     * @param score Evaluation value
     * @param n     Total number of trails
     * @param ni    The number of playouts of the corresponding action
     * @return UCB1 value
     */
    public double getUcb(double score, int n, int ni) {
        return score + UCB_C * Math.sqrt((2 * Math.log(n)) / ni);
    }

    public void printNode(Node node) {
        System.out.println("Total number of trails:" + node.games);
        for (int i = 0; i < node.children.length; i++) {
            System.out.println(i + ",Trails:" + node.children[i].games + ",Depth:" + node.children[i].depth
                    + ",score:" + node.children[i].score / node.children[i].games + ",ucb:"
                    + node.children[i].ucb);
        }
        System.out.println("");
        for (int i = 0; i < node.children.length; i++) {
            if (node.children[i].isCreateNode) {
                printNode(node.children[i]);
            }
        }
    }

    public void addGames() {
        this.games++;
    }

    public void setScore(double score) {
        this.lastestSimulatedScore = 0;
        this.score += score;
        this.lastestSimulatedScore = score;
    }

    public double runWorkers(Node selectedNode) throws InterruptedException {
        double score = 0;
        CountDownLatch countDownLatch = new CountDownLatch(selectedNode.parent.children.length);

        for (Node child : selectedNode.parent.children) {
            new Thread(new MultiTheadPlayout(child, countDownLatch)).start();
        }
        countDownLatch.await();

        for (Node child : selectedNode.parent.children) {
            score += child.lastestSimulatedScore;
        }
        return score;
    }

    public Map<FieldName, Object> normalizeArguments(Map<FieldName, Object> arguments) {
        Map<String, Double> maxDict = new HashMap<>();
        Map<String, Double> minDict = new HashMap<>();
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(MctsSetting.MIN_MAX_PATH));
            String line;
            String titles[] = new String[179];
            int row = 0;
            while ((line = csvReader.readLine()) != null) {
                if (row == 0) {
                    titles = line.split(",");
                } else if (row == 1) {
                    String values[] = line.split(",");
                    for (int i = 1; i < titles.length; i++) {
                        maxDict.put(titles[i], Double.parseDouble(values[i]));
                    }
                } else {
                    String values[] = line.split(",");
                    for (int i = 1; i < titles.length; i++) {
                        minDict.put(titles[i], Double.parseDouble(values[i]));
                    }
                }
                row++;
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // init count variables
        double totalFrameCnt = 300.;
        double sec = 5.;
        double selfAppCnt = (double) arguments.get(FieldName.create("self_approaching_ratio"));
        double selfMACnt =  (double) arguments.get(FieldName.create("self_moving_away_ratio"));
        double oppoAppCnt = (double) arguments.get(FieldName.create("oppo_approaching_ratio"));
        double oppoMACnt = (double) arguments.get(FieldName.create("oppo_approaching_ratio"));
        double selfMovingCnt = selfAppCnt + selfMACnt;
        double oppoMovingCnt = oppoAppCnt + oppoMACnt;
        double selfAttCnt = 0, oppoAttCnt = 0, selfProjCnt = 0, oppoProjCnt = 0;
        for (int i = 1; i < 5; i++) {
            selfAttCnt += (double) arguments.get(FieldName.create(String.format("self_attack_type%d_ratio", i)));
            oppoAttCnt += (double) arguments.get(FieldName.create(String.format("oppo_attack_type%d_ratio", i)));
            selfProjCnt += (double) arguments.get(FieldName.create(String.format("self_projectiles_type%d_ratio", i)));
            oppoProjCnt += (double) arguments.get(FieldName.create(String.format("oppo_projectiles_type%d_ratio", i)));
        }
        for (String argName: MctsSetting.ARGUMENT_NAMES) {
            if (argName.equals("self_approaching_ratio") || argName.equals("self_moving_away_ratio")){
                if (selfMovingCnt > 0) {
                    arguments = replaceArguments(arguments, argName, selfMovingCnt);
                }
            } else if (argName.equals("oppo_approaching_ratio") || argName.equals("oppo_moving_away_ratio")) {
                if (oppoMovingCnt > 0) {
                    arguments = replaceArguments(arguments, argName, oppoMovingCnt);
                }
            } else if (argName.equals("self_avg_approaching_speed")) {
                if (selfAppCnt > 0) {
                    arguments = replaceArguments(arguments, argName, selfAppCnt);
                }
            } else if (argName.equals("self_avg_moving_away_speed")) {
                if (selfMACnt > 0) {
                    arguments = replaceArguments(arguments, argName, selfMACnt);
                }
            } else if (argName.equals("oppo_avg_approaching_speed")) {
                if (oppoAppCnt > 0) {
                    arguments = replaceArguments(arguments, argName, oppoAppCnt);
                }
            } else if (argName.equals("oppo_avg_moving_away_speed")) {
                if (oppoMACnt > 0) {
                    arguments = replaceArguments(arguments, argName, oppoMACnt);
                }
            } else if (argName.equals("self_attack_type1_ratio")
                    || argName.equals("self_attack_type2_ratio")
                    || argName.equals("self_attack_type3_ratio")
                    || argName.equals("self_attack_type4_ratio")
                    || argName.equals("self_attack_avg_damage")) {
                if (selfAttCnt > 0) {
                    arguments = replaceArguments(arguments, argName, selfAttCnt);
                }
            } else if (argName.equals("oppo_attack_type1_ratio")
                    || argName.equals("oppo_attack_type2_ratio")
                    || argName.equals("oppo_attack_type3_ratio")
                    || argName.equals("oppo_attack_type4_ratio")
                    || argName.equals("oppo_attack_avg_damage")) {
                if (oppoAttCnt > 0) {
                    arguments = replaceArguments(arguments, argName, oppoAttCnt);
                }
            } else if (argName.equals("self_projectiles_type1_ratio")
                    || argName.equals("self_projectiles_type2_ratio")
                    || argName.equals("self_projectiles_type3_ratio")
                    || argName.equals("self_projectiles_type4_ratio")
                    || argName.equals("self_projectiles_avg_damage")
                    || argName.equals("self_avg_projectiles_num")) {
                if (selfProjCnt > 0) {
                    arguments = replaceArguments(arguments, argName, selfProjCnt);
                }
            } else if (argName.equals("oppo_projectiles_type1_ratio")
                    || argName.equals("oppo_projectiles_type2_ratio")
                    || argName.equals("oppo_projectiles_type3_ratio")
                    || argName.equals("oppo_projectiles_type4_ratio")
                    || argName.equals("oppo_projectiles_avg_damage")
                    || argName.equals("oppo_avg_projectiles_num")) {
                if (oppoProjCnt > 0) {
                    arguments = replaceArguments(arguments, argName, oppoProjCnt);
                }
            } else if (argName.equals("self_be_hit_per_second")
                    || argName.equals("self_hit_per_second")
                    || argName.equals("self_guard_per_second")
                    || argName.equals("self_blocked_per_second")
                    || argName.equals("avg_hp_zero_crossing")
                    || argName.equals("avg_self_hp_reducing_speed")
                    || argName.equals("avg_oppo_hp_reducing_speed")
                    || argName.equals("avg_self_energy_gaining_speed")
                    || argName.equals("avg_oppo_energy_gaining_speed")
                    || argName.equals("avg_self_energy_reducing_speed")
                    || argName.equals("avg_oppo_energy_reducing_speed")) {
                arguments = replaceArguments(arguments, argName, sec);
            } else {
                arguments = replaceArguments(arguments, argName, totalFrameCnt);
            }
            FieldName fn = FieldName.create(argName);
            double v = (double) arguments.get(fn);
            // (v - min) / (max - min)
            if (maxDict.get(argName) != 0) {
                arguments.replace(fn, (v - minDict.get(argName)) / (maxDict.get(argName) - minDict.get(argName)));
            }
        }

        return arguments;
    }

    public Map<FieldName, Object> replaceArguments(Map<FieldName, Object> arguments, String name, double divider) {
        FieldName fieldName = FieldName.create(name);
        double v = (double) arguments.get(fieldName);
        arguments.replace(fieldName, v / divider);
        return arguments;
    }
}

// {"mode":"full","isActive":false}