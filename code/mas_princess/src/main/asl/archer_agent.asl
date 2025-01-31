/* Warrior Agent Initialization */
hp(80).
att_damage(10).

p1(0.0).
p2(0.0).

!spawn.

+!spawn
    <-
        //.wait(5000);
        -+hp(80);
        !savePrincess.

-!spawn
    <-
        .wait(5000).
        !savePrincess.


+!savePrincess: position(K, J) & objective_position(H, I) & att_damage(AD) & state(S) & hp(HP)
    <-
        ?check_win(S);
        ?check_hp(HP);
        ?allyPrincessInRange(S);
        ?enemyPrincessInRange(S);
        ?enemyInRange(S, update_hp(AD));
        ?enemyGateInRange(S);
        !move_towards_objective.

-!savePrincess
    <-
        !savePrincess.

+?check_win(S)
    <-
        ?(S == end);
        .drop_all_desires;
        .drop_all_events;
        .print("Game finished.").

-?check_win(S)
    <-
        .wait(1).

+?check_hp(HP)
    <-
        ?(HP <= 0);
        .drop_all_desires;
        .drop_all_events;
        .print("Dead. Respawning...");
        respawn(true);
        !spawn.

-?check_hp(HP)
    <-
        .wait(1).

+update_hp(AD)[source(Sender)]: hp(HP)
    <-
        -update_hp(AD)[source(Sender)];
        //.print("Received damage ", AD , " from ", Sender, ". Remaining hps: ", HP - AD);
        -+hp(HP - AD).

+?enemyInRange(S, AttackMessage)
    <-
        //.print("Checking if enemy is in range...");
        utils.check_in_range(enemy_in_range);
        ?(target(T)); // Test goal: Checks for enemies in range
        .send(T, tell, AttackMessage);
        attack_enemy(T);
        !savePrincess.

-?enemyInRange(S, AttackMessage)
    <-
        .wait(1).
        //.fail.
        //.print("I got zero enemies.").

+?enemyGateInRange(S)
    <-
        //.print("Checking if gate is in range...");
        utils.check_in_range(enemy_gate_in_range);
        ?(target(T)); // Test goal: Checks for enemies in range
        attack_gate(T);
        !savePrincess.

-?enemyGateInRange(S)
    <-
        .wait(1).
        //.fail.
        //.print("I got zero gates.").

+?allyPrincessInRange(S)
    <-
        //.print("Checking if princess is in range...");
        utils.check_in_range(ally_princess_in_range);
        ?(target(T)); // Test goal: Checks for enemies in range
        pick_up_princess(T);
        !savePrincess.

-?allyPrincessInRange(S)
    <-
        .wait(1).
        //.fail.
        //.print("I got zero princesses.").

+?enemyPrincessInRange(S)
    <-
        //.print("Checking if princess is in range...");
        utils.check_in_range(enemy_princess_in_range);
        ?(target(T)); // Test goal: Checks for enemies in range
        pick_up_princess(T);
        !savePrincess.

-?enemyPrincessInRange(S)
    <-
        .wait(1).
        //.fail.
        //.print("I got zero princesses.").


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