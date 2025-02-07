/* Archer Agent Initialization */
max_hp(80).
hp(HP) :- max_hp(HP).

att_damage(18).
miss_probability(30).
crit_probability(13).

p1(0.0).
p2(0.0).

!savePrincess.

+!spawn: max_hp(HP)
    <-
        respawn(true);
        ?state(spawn);
        -+hp(HP);
        !savePrincess.

-!spawn: max_hp(HP)
    <-
        !spawn.

+!savePrincess: position(K, J) & objective_position(H, I) & att_damage(AD) & state(S) & hp(HP)
    <-
        ?checkEnd(S);
        ?checkHP(HP);
        ?checkStructureEffect(S);
        ?allyPrincessInRange(S);
        ?enemyPrincessInRange(S);
        ?enemyInRange(S, AD);
        ?enemyGateInRange(S);
        !moveTowardsObjective.

-!savePrincess
    <-
        !savePrincess.

+?checkEnd(S)
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

-?checkEnd(S)
    <-
        true.

+?checkStructureEffect(S)
    <-
        ?(structure(ST, EP) & ST == bridge);

        .random(X);
        if(X <= (EP / 100.0)) {
            .drop_all_desires;
            .drop_all_intentions;
            .drop_all_events;
            .print("Dead. Respawning...");
            -+hp(0);
            !spawn;
        }.

-?checkStructureEffect(S)
    <-
        true.

+?checkHP(HP)
    <-
        ?(HP <= 0);
        .drop_all_desires;
        .drop_all_intentions;
        .drop_all_events;
        .print("Dead. Respawning...");
        !spawn.

-?checkHP(HP)
    <-
        true.

+update_hp(AD)[source(Sender)]: hp(HP)
    <-
        -update_hp(AD)[source(Sender)];
        -+hp(HP + AD).

+?enemyInRange(S, AD)
    <-
        utils.check_in_range(enemy_in_range);
        ?(target(T) & hp(HP) & HP > 0);

        if (T \== missed) {

            .random(X);

            if (crit_probability(P) & X <= (P / 100.0)) {
                .print("Critical attack!");
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

+?enemyGateInRange(S)
    <-
        utils.check_in_range(enemy_gate_in_range);
        ?(target(T) & hp(HP) & HP > 0);
        attack_gate(T);
        !savePrincess.

-?enemyGateInRange(S)
    <-
        true.

+?allyPrincessInRange(S)
    <-
        utils.check_in_range(ally_princess_in_range);
        ?(target(T) & hp(HP) & HP > 0);
        pick_up_princess(T);
        !savePrincess.

-?allyPrincessInRange(S)
    <-
        true.

+?enemyPrincessInRange(S)
    <-
        utils.check_in_range(enemy_princess_in_range);
        ?(target(T) & hp(HP) & HP > 0);
        pick_up_princess(T);
        !savePrincess.

-?enemyPrincessInRange(S)
    <-
        true.


/*
We prefer to go first in the direction that is farther from our position instead of simply going by chance (X >= 0.5).
This way, if we have already reached one of the two objective axis (either H or I), the agent only chooses to go in one direction.
The agents could encounter four different situations and they will move accordingly, fallbacking to random movements if the
chosen direction is occupied.
*/

/*
Case 1 - The agent moves either right or down, towards the object.
              |
    A         |
              |
--------------O---------------
              |
              |
              |
*/
+!moveTowardsObjective: position(K, J) & objective_position(H, I) & K <= H & J <= I & not(K == H & J == I)
    <-
        .random(X);
        utils.compute_percentages(K, J, H, I, right, down);

        if (p1(Y) & X <= Y) {
           absolute_move(right);
        } else {
           if (p2(G) & G == 0.0) {
               .random(Y);
               if (Y >= .5) {
                    absolute_move(left);
               } else {
                    absolute_move(up);
               }
           } else {
               absolute_move(down);
           }
        };

        !savePrincess.

/*
Case 2 - The agent moves either left or down, towards the object.
              |
              |        A
              |
--------------O---------------
              |
              |
              |
*/
+!moveTowardsObjective: position(K, J) & objective_position(H, I) & K >= H & J <= I & not(K == H & J == I)
    <-
        .random(X);
        utils.compute_percentages(K, J, H, I, left, down);

        if (p1(Y) & X <= Y) {
           absolute_move(left);
        } else {
           if (p2(G) & G == 0.0) {
               .random(Y);
               if (Y >= .5) {
                    absolute_move(right);
               } else {
                    absolute_move(up);
               }
           } else {
               absolute_move(down);
           }
        };

        !savePrincess.

/*
Case 3 - The agent moves either right or up, towards the object.
              |
              |
              |
--------------O---------------
              |
     A        |
              |
*/
+!moveTowardsObjective: position(K, J) & objective_position(H, I) & K <= H & J >= I & not(K == H & J == I)
    <-
        .random(X);
        utils.compute_percentages(K, J, H, I, right, up);

        if (p1(Y) & X <= Y) {
           absolute_move(right);
        } else {
           if (p2(G) & G == 0.0) {
               .random(Y);
               if (Y >= .5) {
                    absolute_move(left);
               } else {
                    absolute_move(down);
               }
           } else {
               absolute_move(up);
           }
        };

        !savePrincess.

/*
Case 4 - The agent moves either left or up, towards the object.
              |
              |
              |
--------------O---------------
              |
              |        A
              |
*/
+!moveTowardsObjective: position(K, J) & objective_position(H, I) & K >= H & J >= I & not(K == H & J == I)
    <-
        .random(X);
        utils.compute_percentages(K, J, H, I, left, up);

        if (p1(Y) & X <= Y) {
           absolute_move(left);
        } else {
           if (p2(G) & G == 0.0) {
               .random(Y);
               if (Y >= .5) {
                    absolute_move(right);
               } else {
                    absolute_move(down);
               }
           } else {
               absolute_move(up);
           }
        };

        !savePrincess.

-!moveTowardsObjective: position(K, J) & objective_position(H, I)
    <-
       !savePrincess.