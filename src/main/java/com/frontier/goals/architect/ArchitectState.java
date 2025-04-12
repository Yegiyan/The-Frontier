package com.frontier.goals.architect;

public enum ArchitectState
{
    IDLE,                // default state, no specific task
    GOING_TO_TOWNHALL,   // actively navigating to the town hall
    IDLE_IN_TOWNHALL,    // inside the town hall and wandering/interacting
    GATHER_MATERIALS,    // gather materials (future state)
    BUILD_STRUCTURE,     // building structure (future state)
    REPAIR_STRUCTURE,    // repair structure (future state)
    UPGRADE_STRUCTURE    // upgrade structure (future state)
}