/* Soldier Agent Initialization */
hp(80).
att_damage(10).

p1(0.0).
p2(0.0).

!savePrincess.

+!savePrincess: position(K, J) & objective_position(H, I) & objective(D) & att_damage(A) & state(S)
    <-
        ?enemyInRange(S, update_hp(A));
        !move_towards_objective.

-!savePrincess
    <-
        !savePrincess.

+?check_hp(A,D)
    <-
      ?(A - D <= 0);
      .drop_all_desires;
      //.drop_all_intentions;
      .print("mortooooooooooo");
      respawn(true);
      -+hp(80);
      !savePrincess.

-?check_hp(A,D)
    <-
      .wait(1).

+update_hp(D)[source(Sender)]: hp(A)
    <-
      -update_hp(D)[source(Sender)];
      -+hp(A - D);
      ?check_hp(A, D).
      //.print("Received damage ", D , " from ", Sender, ". Remaining hps: ", A - D).

//-update_hp(D)[source(Sender)]: hp(A)
    //<-
      //wait(1).

+?enemyInRange(S, AttackMessage)
    <-
        //.print("Checking if enemy is in range...");
        utils.check_enemy_in_range;
        ?(target(T)); // Test goal: Checks for enemies in range
        .send(T, tell, AttackMessage);
        attack_enemy(T);
        !savePrincess.

-?enemyInRange(S, AttackMessage)
    <-
      .wait(1).
      //.fail.
        //.print("I got zero enemies.").


/* Move towards the gate based on position in the base */
/* We prefer to go first in the direction that is farther from our position instead of (X >= 0.5).
This way, if we have already reached a correct axis (either H or I), the agent only chooses to go in one direction. */
+!move_towards_objective: position(K, J) & objective_position(H, I) & K <= H & J <= I & not(K == H & J == I)
    <-
       //.wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, right, down);
       /* .print("Primo caso"); */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           /* .print("Going right."); */
           // -+position(K+1,J);
           absolute_move(right);
       } else {
           if (p2(G) & G == 0.0) {
               /* .print("Stuck, moving random."); */
               .random(Y);
               if (Y >= .5) {
                    // -+position(K-1,J);
                    absolute_move(left);
               } else {
                    // -+position(K,J-1);
                    absolute_move(up);
               }
           } else {
               /* .print("Going down."); */
               // -+position(K,J+1);
               absolute_move(down);
           }
       };

       !savePrincess.

+!move_towards_objective: position(K, J) & objective_position(H, I) & K >= H & J <= I & not(K == H & J == I)
    <-
       //.wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, left, down);
       /* .print("Secondo caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           /* .print("Going left."); */
           // -+position(K-1,J);
           absolute_move(left);
       } else {
           if (p2(G) & G == 0.0) {
               /* .print("Stuck, moving random."); */
               .random(Y);
               if (Y >= .5) {
                    // -+position(K+1,J);
                    absolute_move(right);
               } else {
                    // -+position(K,J-1);
                    absolute_move(up);
               }
           } else {
               /* .print("Going down."); */
               // -+position(K,J+1);
               absolute_move(down);
           }
       };

       !savePrincess.


+!move_towards_objective: position(K, J) & objective_position(H, I) & K <= H & J >= I & not(K == H & J == I)
    <-
       //.wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, right, up);
       /* .print("Terzo caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           /* .print("Going right."); */
           // -+position(K+1,J);
           absolute_move(right);
       } else {
           if (p2(G) & G == 0.0) {
               /* .print("Stuck, moving random."); */
               .random(Y);
               if (Y >= .5) {
                    // -+position(K-1,J);
                    absolute_move(left);
               } else {
                    // -+position(K,J+1);
                    absolute_move(down);
               }
           } else {
               /* .print("Going up."); */
               // -+position(K,J-1);
               absolute_move(up);
           }
       };

       !savePrincess.


+!move_towards_objective: position(K, J) & objective_position(H, I) & K >= H & J >= I & not(K == H & J == I)
    <-
       //.wait(1000);
       .random(X);
       utils.compute_percentages(K, J, H, I, left, up);
       /* .print("Quarto caso");  */
       /* .print("Random value: ",X);  */

       if (p1(Y) & X <= Y) {
           /* .print("Going left."); */
           // -+position(K-1,J);
           absolute_move(left);
       } else {
           if (p2(G) & G == 0.0) {
               /* .print("Stuck, moving random."); */
               .random(Y);
               if (Y >= .5) {
                    // -+position(K+1,J);
                    absolute_move(right);
               } else {
                    // -+position(K,J+1);
                    absolute_move(down);
               }
           } else {
               /* .print("Going up."); */
               // -+position(K,J-1);
               absolute_move(up);
           }
       };

       !savePrincess.

-!move_towards_objective
    <-
       .print("Conditions failed.");
       !savePrincess.