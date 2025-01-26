/* Soldier Agent Initialization */
state(spawn).
team(0).
hp(100).
att_damage(10).
att_range(1).

p1(0.0).
p2(0.0).
land_p(95).
bridge_p(5).

!savePrincess.

+!savePrincess: position(K, J) & objective_position(H, I) & objective(D)
    <-
        ?objective_reached(D, K, J, H, I);
        !move_towards_objective.

-!savePrincess
    <-
        .wait(1000);
        !savePrincess.

+?objective_reached(D, K, J, H, I)
    <-
        .print("Checking if objective is reached...");
        ?(K == H & J == I); // Test goal: Check if the agent's position matches the objective's position
        .print("Objective reached at: (", H, ",", I, ")");
        -+state(D). // Update the state to match the objective
        .print("State updated to: ", D).

-?objective_reached(D, K, J, H, I)
    <-
        .print("Objective not yet reached.").

/* Move towards the gate based on position in the base */
/* We prefer to go first in the direction that is farther from our position instead of (X >= 0.5).
This way, if we have already reached a correct axis (either H or I), the agent only chooses to go in one direction. */
+!move_towards_objective: position(K, J) & objective_position(H, I) & K <= H & J <= I & not(K == H & J == I)
    <-
       .wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, right, down);
       /* .print("Primo caso"); */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           .print("Going right.");
           absolute_move(right);
       } else {
           if (p2(G) & G == 0.0) {
               .print("Stuck, moving random.");
               absolute_move(random);
           } else {
               .print("Going down.");
               absolute_move(down);
           }
       };

       !savePrincess.

+!move_towards_objective: position(K, J) & objective_position(H, I) & K >= H & J <= I & not(K == H & J == I)
    <-
       .wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, left, down);
       /* .print("Secondo caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           .print("Going left.");
           absolute_move(left);
       } else {
           if (p2(G) & G == 0.0) {
               .print("Stuck, moving random.");
               absolute_move(random);
           } else {
               .print("Going down.");
               absolute_move(down);
           }
       };

       !savePrincess.


+!move_towards_objective: position(K, J) & objective_position(H, I) & K <= H & J >= I & not(K == H & J == I)
    <-
       .wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, right, up);
       /* .print("Terzo caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           .print("Going right.");
           absolute_move(right);
       } else {
           if (p2(G) & G == 0.0) {
               .print("Stuck, moving random.");
               absolute_move(random);
           } else {
               .print("Going up.");
               absolute_move(up);
           }
       };

       !savePrincess.


+!move_towards_objective: position(K, J) & objective_position(H, I) & K >= H & J >= I & not(K == H & J == I)
    <-
       .wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, left, up);
       /* .print("Quarto caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           .print("Going left.");
           absolute_move(left);
       } else {
           if (p2(G) & G == 0.0) {
               .print("Stuck, moving random.");
               absolute_move(random);
           } else {
               .print("Going up.");
               absolute_move(up);
           }
       };

       !savePrincess.

-!move_towards_objective
    <-
       .print("Conditions failed.");
       !move_towards_objective.