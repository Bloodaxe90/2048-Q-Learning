package com.example._2048qlearning;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Q_Training extends Thread {

    private final GameController gameController;
    private Random random;

    private final Square[][] currentState = new Square[4][4];
    private final String[] actions = new String[]{
            "UP", "DOWN", "LEFT", "RIGHT"
    };
    private final ArrayList<String> futureStatesAsStrings = new ArrayList<>();
    private ConcurrentHashMap<String, Double> qTable = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> stateActionFrequency = new ConcurrentHashMap<>();
    private static double LEARNING_RATE = 0.1;
    private static final double DISCOUNT_VALUE = 0.9;
    private static double EXPLORATION_RATE = 1;
    private static final int NUM_EPISODES = 1000000;
    private static final int SLEEP_TIME = 300;
    private static final int THRESHOLD_FREQUENCY = 10;
    private static final double DECAY_RATE = 0.995;
    private static final String ACTUAL_Q_TABLE = "src/main/resources/com/example/_2048qlearning/Q_Table.dat";
    private boolean visualiseTraining;
    private final Object lock = new Object();
    private boolean training = false;


    public Q_Training(GameController gameController, Random random) {
        this.gameController = gameController;
        this.random = random;
    }


    @Override
    //Training method
    public void run() {

        ArrayList<Integer> totalMoves = new ArrayList<>();
        String currentStateAsString;
        String action;
        int largestValue = Integer.MIN_VALUE;
        int largeValueCount = 0;
        int moveCount;
        int winCount = 0;
        int currentScore;
        int reward;
        String stateActionPair;
        double oldQValue;
        double maxNextQValue;
        double newQValue;

        System.out.println("Q-Training Started");

        for (int episode = 0; episode < NUM_EPISODES;) {

            //Check if training is paused
            synchronized (lock) {
                while(!training) {
                    System.out.println("Training paused");
                    try {
                        lock.wait();
                        System.out.println("Training resumed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Thread was interrupted");
                        return;
                    }
                }
            }

            //Resetting the game for the next/first episode
            moveCount = 0;
            reset();
            tryRenderJFX();


            //Simulation an episode of 2048
            while(gameController.checkTerminal(currentState) == null) {
                currentStateAsString  = getStateAsString(currentState);
                action = chooseAction(gameController.getDeepBoardCopy(currentState));
                currentScore = gameController.getScoreManager().getCurrentScore();

                gameController.moveMergeSquares(currentState, action, visualiseTraining, true);
                // ^-Current state now is the next state

                //Only updates the Q-Table if the training IS NOT visualised
                if(!visualiseTraining ) {
                    reward = getReward(currentState, currentScore) - (moveCount * 2);
                    moveCount ++;

                    //Update Q-Value
                    stateActionPair = currentStateAsString + action;
                    oldQValue = qTable.getOrDefault(currentStateAsString + action, 0.0);
                    maxNextQValue = getMaxNextQValue(getStateAsString(currentState));
                    //TD Equation
                    newQValue = oldQValue + (LEARNING_RATE * ((reward + (DISCOUNT_VALUE * maxNextQValue)) - oldQValue));
                    qTable.put(stateActionPair, newQValue);

                    stateActionFrequency.put(stateActionPair, stateActionFrequency.getOrDefault(stateActionPair, 0) + 1);

                } else if (!training) {
                    break;
                }

                tryRenderJFX();
            }

            if(!visualiseTraining) {
                String result = gameController.checkTerminal(currentState);
                if (result.equals("W")) {
                    winCount++;
                    totalMoves.add(moveCount);
                };

                int largestCurrentValue = getLargestStateValue(currentState);
                if(largestCurrentValue > largestValue) {
                    largeValueCount = 1;
                    largestValue = largestCurrentValue;
                } else if (largestCurrentValue == largestValue) {
                    largeValueCount++;
                }



                largestValue = Math.max(getLargestStateValue(currentState), largestValue);
                if (episode < 100) {
                    if (episode % 10 == 0) {
                        System.out.println(episode + " Episodes reached");

                    }
                } else {
                    if (episode % 1000 == 0) {
                        int averageMoveCount = totalMoves.stream().mapToInt(Integer::intValue).sum() / (totalMoves.size() -1);
                        System.out.println(episode + " Episode reached: | Largest square value: " + largestValue + " appearing " + largeValueCount + " times | Table Size: " + qTable.size() + " | Average Moves taken: " + averageMoveCount + " | LR: " + LEARNING_RATE + " | ER: " + EXPLORATION_RATE);
                        largestValue = Integer.MIN_VALUE;
                        largeValueCount = 0;
                        winCount = 0;
                        totalMoves.clear();
                        pruneQTable();
                    }
                }

                //Only counts an episodes has occurred if training isn't being visualised
                episode++;

                applyDecay(episode);
            }
        }

        System.out.println("Training session complete");
        gameController.setBoardTrainingComplete();
    }

    //Applying Behavioral Policy
    private String chooseAction(Square[][] currentState) {
        //If training is NOT visual the agent does not explore
        if (Math.random() <= EXPLORATION_RATE && !visualiseTraining) {
            return actions[random.nextInt(4)];
        } else {
            return getBestAction(currentState);
        }
    }

    private void pruneQTable() {
        // Calculate the reward threshold as the 75th percentile of the range of values
        List<Double> qValues = new ArrayList<>(qTable.values());
        Collections.sort(qValues);

        int index = (int) Math.ceil(0.75 * qValues.size()) - 1;
        double rewardThreshold = qValues.get(index);


        List<String> toRemove = new ArrayList<>();
        for (String stateAction : qTable.keySet()) {
            int frequency = stateActionFrequency.getOrDefault(stateAction, 0);
            double qValue = qTable.getOrDefault(stateAction, 0.0);

            if (frequency < THRESHOLD_FREQUENCY || qValue < rewardThreshold) {
                toRemove.add(stateAction);
            }
        }

        for (String stateAction : toRemove) {
            qTable.remove(stateAction);
            stateActionFrequency.remove(stateAction);
        }
    }

    private void applyDecay(int episode) {

        //Learning rate decaying
        double finalLearningRate = 0.01; // Minimum learning rate
        double decayRate = (0.1 - finalLearningRate) / NUM_EPISODES;
        LEARNING_RATE = Math.max(finalLearningRate, 0.1 - decayRate * 0.1);

        //decaying the frequencies of all state action pairs decaying
        for (String stateAction : stateActionFrequency.keySet()) {
            int currentFrequency = stateActionFrequency.get(stateAction);
            int decayedFrequency = (int) (currentFrequency * DECAY_RATE);
            stateActionFrequency.put(stateAction, decayedFrequency);
        }

        //Exploration rate decay
        double minExplorationRate = 0.01; // Minimum exploration rate
        EXPLORATION_RATE = Math.max(minExplorationRate, 1 * Math.pow(DECAY_RATE, episode / (double)NUM_EPISODES));
    }

    private int getReward(Square[][] currentState, int previousScore) {
        int reward = 0;

        //Reward is set to the increase in score between currentState and nextState
        int currentScore = gameController.getScoreManager().getCurrentScore();
        reward += (currentScore - previousScore);


        //Penalty for invalid moves i.e. moves that cause no square movement or merges and so cause no Random Squares to be spawned
        if(!gameController.isValidMove()) {
            reward -= 10;
        }

        String result = gameController.checkTerminal(currentState);
        if (result != null) {
            if (result.equals("W")) {
                //Large reward for a win
                reward += 1000;
            } else if (result.equals("L")) {
                //Large penalty for a loss
                reward -= 1000;
            }
        }

        //Rewards for achieving Milestone Squares
        reward += Arrays.stream(currentState)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .mapToInt(Square::getValue)
                .map(value -> {
                    if (value == 256) return 20;
                    if (value == 512) return 50;
                    if (value == 1024) return 100;
                    return 0;
                })
                .sum();

        return reward;
    }

    private void tryRenderJFX() {
        try {
            if(visualiseTraining) {
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.out.println("Background thread interrupted");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void pauseTraining() {
        synchronized (lock) {
            training = false;
        }
    }

    public void resumeTraining() {
        synchronized (lock) {
            training = true;
            lock.notify();
        }
    }

    private double getMaxNextQValue(String nextStateAsString) {
        return qTable.entrySet().stream()
                .filter(e -> e.getKey().startsWith(nextStateAsString))
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(0.0);
    }

    private String getBestAction(Square[][] currentState) {
        double bestQValue = Double.NEGATIVE_INFINITY;
        String bestAction = null;

        for (String action : actions) {
            gameController.moveMergeSquares(currentState, action, false, false);

            if (!gameController.isValidMove()) {
                continue;
            }

            getFutureStatesAsString(currentState);
            if (!futureStatesAsStrings.isEmpty()) {
                for (String futureStateAsString : futureStatesAsStrings) {
                    double qValue = qTable.getOrDefault(futureStateAsString + action , 0.0);

                    if (bestQValue < qValue) {
                        bestQValue = qValue;
                        bestAction = action;
                    }
                }
            }
        }

        return bestAction;
    }

    private String getStateAsString(Square[][] board) {
        StringBuilder state = new StringBuilder();
        for (Square[] squares : board) {
            for (Square square : squares) {
                if (square != null) {
                    state.append(square.getValue());
                } else {
                    state.append(0);
                }
            }
        }
        return state.toString();
    }

    private void getFutureStatesAsString(Square[][] board) {
        futureStatesAsStrings.clear();
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                if(board[column][row] == null) {
                    board[column][row] = new Square(gameController, 2, column, row, false);
                    futureStatesAsStrings.add(getStateAsString(board));

                    // Add 4 tile
                    board[column][row].setValue(4);
                    futureStatesAsStrings.add(getStateAsString(board));

                    // Restore original square
                    board[column][row] = null;
                }
            }
        }
    }

    private int getLargestStateValue(Square[][] currentState) {
        return Arrays.stream(currentState).flatMap(Arrays::stream).filter(Objects::nonNull).mapToInt(Square::getValue).max().orElse(0);
    }

    public void serializeQTable() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ACTUAL_Q_TABLE))) {
            out.writeObject(qTable);
            out.writeObject(stateActionFrequency);
            System.out.println("Tables serialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserializeQTable() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ACTUAL_Q_TABLE))) {
            qTable = (ConcurrentHashMap<String, Double>) in.readObject();
            stateActionFrequency = (ConcurrentHashMap<String, Integer>) in.readObject();
            System.out.println("Tables deserialized successfully.");
            System.out.println("Contents of QTable: " + qTable);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isVisualiseTraining() {
        return visualiseTraining;
    }

    public boolean isTraining() {
        return training;
    }

    public void setVisualiseTraining(boolean visualiseTraining) {
        this.visualiseTraining = visualiseTraining;
    }

    public void reset() {
        Arrays.stream(currentState).flatMap(Arrays::stream).filter(Objects::nonNull).forEach(square -> gameController.getSquareManager().destroySquare(currentState, square));
        gameController.reset(currentState, visualiseTraining);
    }
}
