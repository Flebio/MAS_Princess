/* Warrior Agent Initialization */
team(0).
hp(100).
att_damage(10).
att_range(1).

!start.

+!start <-
    !move_random.

/* Plan for random movement */
+!move_random <-
    move(random);
    !move_random.
