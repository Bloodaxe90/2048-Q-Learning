<h1 align="center">2048 with Q-Learning</h1>

<h2>Description:</h2>
<p>
After implementing the Expectimax algorithm on 2048 (https://github.com/Bloodaxe90/Connect4_Minimax), I decided to try applying the Q-Learning algorithm to 2048 using a tabular approach.
</p>
<p>
As some may already know, using a Q-table to store Q-values for every state-action pair in 2048 is highly impractical, as the state space is enormous, with over a trillion possible entries. Unfortunately, I only realized this after fully implementing Q-Learning and building a functional UI. With this realization, I promptly gave up on the project, but with the goal of later redeeming the attempt by implementing Deep Q-Learning on 2048 instead.
</p>
<p>
<strong>UPDATE:</strong> I have implemented DQL successfully on 2048! You can find it here: https://github.com/Bloodaxe90/2048_DQL_python
</p>

<h2>Usage:</h2>
<p>Run the main method in the <code>Application</code> class.</p>

<h2>Hyperparameters:</h2>
<p>All can be found in the <code>Q_Training</code> class, apart from <code>WIN_VALUE</code> which is found in <code>GameController</code>.</p>
<ul>
  <li><code>LEARNING_RATE</code> (double)</li>
  <li><code>DISCOUNT_VALUE</code> (double)</li>
  <li><code>EXPLORATION_RATE</code> (double): The initial exploration rate</li>
  <li><code>NUM_EPISODES</code> (int)</li>
  <li><code>THRESHOLD_FREQUENCY</code> (int): Minimum usage filter — state-action pairs with frequencies below this after every 1000 episodes are pruned from the Q-table</li>
  <li><code>DECAY_RATE</code> (double): Decay rate for the usage frequencies, learning rate, and exploration rate</li>
  <li><code>ACTUAL_Q_TABLE</code> (String): Filename used to store/load the Q-table</li>
  <li><code>WIN_VALUE</code> (int)</li>
</ul>

<h2>Controls:</h2>
<ul>
  <li><strong>Load Q-Table:</strong> Loads the stored Q-table</li>
  <li><strong>Store Q-Table:</strong> Saves the current Q-table</li>
  <li><strong>Start Training:</strong> Trains the agent for <code>NUM_EPISODES</code> and, once training has begun, allows you to pause/resume non-visual training and stop/restart visual training</li>
  <li><strong>Visualise:</strong> When checked, shows the agent's moves in real time</li>
</ul>

<h2>Results:</h2>
<p>
As mentioned in the description, this project wasn't a complete success, but I learned a great deal along the way. I still managed to implement a UI, serialization techniques, and multithreading. Additionally, in my efforts to get it working I implemented a pruning system to remove rarely visited state-action pairs from the Q-table.
</p>

![image](https://github.com/user-attachments/assets/7f7e049e-1861-463b-9575-773e9d479bc3)





