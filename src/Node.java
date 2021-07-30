import java.util.*;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;

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

    /** UCT execution time */
    public static final int UCT_TIME = 165 * 1000000; //165 * 100000;

    /** Value of C in UCB1 */
    public static final double UCB_C = 3;

    /** Depth of tree search */
    public static final int UCT_TREE_DEPTH = 3;

    /** Threshold for generating a node */
    public static final int UCT_CREATE_NODE_THRESHOULD = 4;

    /** Time for performing simulation */
    public static final int SIMULATION_TIME = 286;

    /** Use when in need of random numbers */
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

    public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
                LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
                CommandCenter commandCenter, LinkedList<Action> selectedMyActions, Evaluator anxietyEvaluator,
                Evaluator boredomEvaluator, Evaluator challengeEvaluator, Evaluator competenceEvaluator,
                Evaluator immersionEvaluator, Evaluator valenceEvaluator) {
        this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter,
                anxietyEvaluator, boredomEvaluator, challengeEvaluator,
                competenceEvaluator, immersionEvaluator, valenceEvaluator);

        this.selectedMyActions = selectedMyActions;
    }

    public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
                LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
                CommandCenter commandCenter, Evaluator anxietyEvaluator, Evaluator boredomEvaluator,
                Evaluator challengeEvaluator, Evaluator competenceEvaluator, Evaluator immersionEvaluator,
                Evaluator valenceEvaluator) {
        this.frameData = frameData;
        this.parent = parent;
        this.myActions = myActions;
        this.oppActions = oppActions;
        this.gameData = gameData;
        this.simulator = new Simulator(gameData);
        this.playerNumber = playerNumber;
        this.commandCenter = commandCenter;
        this.anxietyEvaluator = anxietyEvaluator;
        this.boredomEvaluator = boredomEvaluator;
        this.challengeEvaluator = challengeEvaluator;
        this.competenceEvaluator = competenceEvaluator;
        this.immersionEvaluator = immersionEvaluator;
        this.valenceEvaluator = valenceEvaluator;

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
            // uctSingle();
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
                    // 특정 depth 밑의 노드들은 이 부분 때문에 uct 한번도 안 할 수도??
                    if (UCT_CREATE_NODE_THRESHOLD <= selectedNode.games) {
                        selectedNode.createNode();
                        selectedNode.isCreateNode = true;  // for debugging
                        score = selectedNode.uct();
                    } else {
                        score = selectedNode.playout();
                    }
                } else {
                    score = selectedNode.playout();
                }
            } else {
                if (selectedNode.depth < UCT_TREE_DEPTH) {
                    score = selectedNode.uct();
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
        try{
          score = runWorkers(selectedNode);
        }catch(InterruptedException  e){}
      } else {
        if (selectedNode.children == null) {
          if (selectedNode.depth < UCT_TREE_DEPTH) {
            if (UCT_CREATE_NODE_THRESHOULD <= selectedNode.games) {
              selectedNode.createNode();
              selectedNode.isCreateNode = true;
              score = selectedNode.uctMulti();
            } else {
              try{
                score = runWorkers(selectedNode);
              }catch(InterruptedException  e){}
            }
          } else {
            try{
              score = runWorkers(selectedNode);
            }catch(InterruptedException  e){}
          }
        } else {
          if (selectedNode.depth < UCT_TREE_DEPTH) {
            score = selectedNode.uctMulti();
          } else {
            try{
              score = runWorkers(selectedNode);
            }catch(InterruptedException  e){}
          }
        }
      }
      if (depth == 0) {
        this.score += score;
        games += this.children.length;
      }else{
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
                            anxietyEvaluator, boredomEvaluator, challengeEvaluator,
                            competenceEvaluator, immersionEvaluator, valenceEvaluator);
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
        int argIdx = 1;
        LinkedList<Double> features;
        for (FrameData fd: fds) {
            // It must be calculated on the user side
            features = fd.getMctsScoringFeatures(!playerNumber);
            for (Double feature: features) {
                arguments.put(FieldName.create('x' + Integer.toString(argIdx)), feature);
                argIdx++;
            }
        }
        Map<FieldName, ?> anxietyResult = anxietyEvaluator.evaluate(arguments);
        Map<FieldName, ?> boredomResult = boredomEvaluator.evaluate(arguments);
        Map<FieldName, ?> challengeResult = challengeEvaluator.evaluate(arguments);
        Map<FieldName, ?> competenceResult = competenceEvaluator.evaluate(arguments);
        Map<FieldName, ?> immersionResult = immersionEvaluator.evaluate(arguments);
        Map<FieldName, ?> valenceResult = valenceEvaluator.evaluate(arguments);

        int anxiety = (int)((ProbabilityDistribution)anxietyResult.get(FieldName.create("y"))).getResult();
        int boredom = (int)((ProbabilityDistribution)boredomResult.get(FieldName.create("y"))).getResult();
        int challenge = (int)((ProbabilityDistribution)challengeResult.get(FieldName.create("y"))).getResult();
        int competence = (int)((ProbabilityDistribution)competenceResult.get(FieldName.create("y"))).getResult();
        int immersion = (int)((ProbabilityDistribution)immersionResult.get(FieldName.create("y"))).getResult();
        int valence = (int)((ProbabilityDistribution)valenceResult.get(FieldName.create("y"))).getResult();
//        System.out.println(anxietyResult.get(FieldName.create("probability(0)")).getClass().getName());
        double anxietyScore = anxiety == 1
                ? -(Double)anxietyResult.get(FieldName.create("probability(1)"))
                : (Double)anxietyResult.get(FieldName.create("probability(0)"));
        double boredomScore = boredom == 1
                ? -(Double)boredomResult.get(FieldName.create("probability(1)"))
                : (Double)boredomResult.get(FieldName.create("probability(0)"));
        double challengeScore = challenge == 1
                ? -(Double)challengeResult.get(FieldName.create("probability(1)"))
                : (Double)challengeResult.get(FieldName.create("probability(0)"));
        double competenceScore = competence == 1
                ? (Double)competenceResult.get(FieldName.create("probability(1)"))
                : -(Double)competenceResult.get(FieldName.create("probability(0)"));
        double immersionScore = immersion == 1
                ? (Double)immersionResult.get(FieldName.create("probability(1)"))
                : -(Double)immersionResult.get(FieldName.create("probability(0)"));
        double valenceScore = valence == 1
                ? (Double)valenceResult.get(FieldName.create("probability(1)"))
                : -(Double)valenceResult.get(FieldName.create("probability(0)"));

        return anxietyScore + boredomScore + challengeScore + competenceScore + immersionScore + valenceScore;
//        return playerNumber ? (fd.getCharacter(true).getHp() - myOriginalHp) - (fd.getCharacter(false).getHp() - oppOriginalHp) : (fd
//                .getCharacter(false).getHp() - myOriginalHp) - (fd.getCharacter(true).getHp() - oppOriginalHp);
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
  
    public double runWorkers(Node selectedNode) throws InterruptedException{
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
}

// {"mode":"full","isActive":false}