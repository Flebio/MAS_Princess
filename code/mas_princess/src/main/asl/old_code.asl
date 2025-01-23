/* Soldier Agent Initialization */
state(spawn).
objective(ally_gate).
team(0).
hp(100).
att_damage(10).
att_range(1).

land_p(95).
bridge_p(5).

!start.

+!start <-
    /* !moveTowardTheBoundary.
    !move_random. */

/* Plan to move towards the boundary */
+!moveTowardTheBoundary: orientation(north) <-
    move(right);
    .wait(1000);
    !moveTowardTheBoundary.

+!moveTowardTheBoundary: orientation(south) <-
    move(left);
    .wait(1000);
    !moveTowardTheBoundary.

+!moveTowardTheBoundary: orientation(east) <-
    move(forward);
    .wait(1000);
    !moveTowardTheBoundary.

+!moveTowardTheBoundary: orientation(west) <-
    move(backward);
    .wait(1000);
    !moveTowardTheBoundary.

-!moveTowardTheBoundary <-
    .wait(1000);
    !moveTowardTheBoundary.

/* Plan for random movement */
+!move_random <-
    move(random);
    .wait(5000);
    !move_random.

-!move_random <-
    .wait(5000);
    !move_random.
