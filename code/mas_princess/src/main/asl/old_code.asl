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
    !move_random.

/* Plan for random movement */
+!move_random <-
    absolute_move(random);
    .wait(10000);
    !move_random.

-!move_random <-
    .wait(10000);
    !move_random.
