# 2048-Q-Learning
My attempt as 2048 with q learning

After alot of trail and error i realised that using a Q-Table to store the Q-Values for each state-action pair is highly impractical as there is no way my computer can computer over a trillion values even with pruning being applied to the table. I am however pretty sure that the code is sound but I havn't found a good way to check it and so i have given up.

I plan on eventually applying deep Q-learning. 

** UPDATE **
I have implemented DQL successfully on 2048 in the repository 2048_DQL_python
