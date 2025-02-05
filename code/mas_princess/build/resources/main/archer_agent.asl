/* Warrior Agent Initialization */
hp(80).
att_damage(18).
miss_probability(30).
crit_probability(13).

p1(0.0).
p2(0.0).

!savePrincess.

+!spawn
    <-
        respawn(true);
        ?state(spawn);
        -+hp(80);
        !savePrincess.

-!spawn
    <-
        !spawn.

+!savePrincess: position(K, J) & objective_position(H, I) & att_damage(AD) & state(S) & hp(HP)
    <-
        ?check_win(S);
        ?check_hp(HP);
        ?check_structure_effect(S);
        ?check_win(S);
        ?check_hp(HP);
        ?allyPrincessInRange(S);
        ?enemyPrincessInRange(S);
        ?enemyInRange(S, AD);
        ?enemyGateInRange(S);
        !move_towards_objective.

-!savePrincess
    <-
        !savePrincess.

+?check_win(S)
    <-
        ?(S == win | S == lost);
        .drop_all_desires;
        .drop_all_intentions;
        .drop_all_events;

        if (S == win) {
            .print("Game finished. We won.");
        } else {
            .print("Game finished. We lost.");
        }

        .my_name(N);
        .kill_agent(N).

-?check_win(S)
    <-
        true.

+?check_structure_effect(S)
    <-
        ?(structure(ST, EP) & ST == bridge);

        .random(X);
        if(X <= (EP / 100.0)) {
            -+hp(0);
            .drop_all_desires;
            .drop_all_intentions;
            .drop_all_events;
            .print("Dead. Respawning...");
            //respawn(true);
            !spawn;
        }.

-?check_structure_effect(S)
    <-
        true.

+?check_hp(HP)
    <-
        ?(HP <= 0);
        .drop_all_desires;
        .drop_all_intentions;
        .drop_all_events;
        .print("Dead. Respawning...");
        //respawn(true);
        !spawn.

-?check_hp(HP)
    <-
        true.

+update_hp(AD)[source(Sender)]: hp(HP)
    <-
        -update_hp(AD)[source(Sender)];
        //.print("Received damage ", AD , " from ", Sender, ". Remaining hps: ", HP + AD);
        -+hp(HP + AD).

+?enemyInRange(S, AD)
    <-
        //.print("Checking if enemy is in range...");
        utils.check_in_range(enemy_in_range);
        ?(target(T) & hp(HP) & HP > 0); // Test goal: Checks for enemies in range

        if (T \== missed) {

            .random(X);

            if (crit_probability(P) & X <= (P / 100.0)) {
                AttackMessage = update_hp((-AD)*5);
                .send(T, tell, AttackMessage);
                attack_enemy(T, true);
            } else {
                AttackMessage = update_hp(-AD);
                .send(T, tell, AttackMessage);
                attack_enemy(T, false);
            }

        } else {
            .print("Attack missed.");
        }

        !savePrincess.

-?enemyInRange(S, AD)
    <-
        true.
        //.fail.
        //.print("I got zero enemies.").

+?enemyGateInRange(S)
    <-
        //.print("Checking if gate is in range...");
        utils.check_in_range(enemy_gate_in_range);
        ?(target(T) & hp(HP) & HP > 0); // Test goal: Checks for enemies in range
        attack_gate(T);
        !savePrincess.

-?enemyGateInRange(S)
    <-
        true.
        //.fail.
        //.print("I got zero gates.").

+?allyPrincessInRange(S)
    <-
        //.print("Checking if princess is in range...");
        utils.check_in_range(ally_princess_in_range);
        ?(target(T) & hp(HP) & HP > 0); // Test goal: Checks for enemies in range
        pick_up_princess(T);
        !savePrincess.

-?allyPrincessInRange(S)
    <-
        true.
        //.fail.
        //.print("I got zero princesses.").

+?enemyPrincessInRange(S)
    <-
        //.print("Checking if princess is in range...");
        utils.check_in_range(enemy_princess_in_range);
        ?(target(T) & hp(HP) & HP > 0); // Test goal: Checks for enemies in range
        pick_up_princess(T);
        !savePrincess.

-?enemyPrincessInRange(S)
    <-
        true.
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

-!move_towards_objective: position(K, J) & objective_position(H, I)
    <-
       //.print("Conditions failed. Position: (", K, ",", J, ") - Objective (", H, ",", I, ")");
       !savePrincess.