/* Soldier Agent Initialization */
state(spawn).
objective(ally_gate).
team(0).
hp(100).
att_damage(10).
att_range(1).

land_p(95).
bridge_p(5).

!savePrincess.

+!savePrincess <-
    !move_towards_objective.

-!savePrincess <-
    .wait(10000);
    !savePrincess.

/* Move towards the gate based on position in the base */
+!move_towards_objective: state(spawn) & zone_type(bbase) & position(K, J) & K < 5 & J <  10  /* Upper Part For Blue */
    <-
       .random(X); /* Randomly go right (>= 0.5) or down (< 0.5) */
       if (X >= 0.5) {
           .print("Going right.");
           !absolute_move(right);
       } else {
           .print("Going down.");
           !absolute_move(down);
       }.

+!move_towards_objective: state(spawn) & zone_type(bbase) & position(K, J) & K < 5 & J >= 10  /* Lower Part For Blue */
    <-
       .random(X); /* Randomly go right (>= 0.5) or up (< 0.5) */
       if (X >= 0.5) {
           .print("Going right.");
            !absolute_move(right);
       } else {
           .print("Going up.");
            !absolute_move(up);
       }.

-!move_towards_objective
    <-
       !move_towards_objective.

/*
    ABSOLUTE_MOVE_LEFT
*/

+!absolute_move(left): orientation(north) <-
    move(left);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(left): orientation(south) <-
    move(right);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(left): orientation(east) <-
    move(backward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(left): orientation(west) <-
    move(forward);
    .wait(1000);
    !move_towards_objective.

/*
    ABSOLUTE_MOVE_RIGHT
*/

+!absolute_move(right): orientation(north) <-
    move(right);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(right): orientation(south) <-
    move(left);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(right): orientation(east) <-
    move(forward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(right): orientation(west) <-
    move(backward);
    .wait(1000);
    !move_towards_objective.

/*
    ABSOLUTE_MOVE_UP
*/

+!absolute_move(up): orientation(north) <-
    move(forward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(up): orientation(south) <-
    move(backward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(up): orientation(east) <-
    move(left);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(up): orientation(west) <-
    move(right);
    .wait(1000);
    !move_towards_objective.

/*
    ABSOLUTE_MOVE_DOWN
*/

+!absolute_move(down): orientation(north) <-
    move(backward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(down): orientation(south) <-
    move(forward);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(down): orientation(east) <-
    move(right);
    .wait(1000);
    !move_towards_objective.

+!absolute_move(down): orientation(west) <-
    move(left);
    .wait(1000);
    !move_towards_objective.