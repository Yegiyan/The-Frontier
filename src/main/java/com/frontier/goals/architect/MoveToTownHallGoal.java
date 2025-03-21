package com.frontier.goals.architect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureType;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MoveToTownHallGoal extends Goal {
    private final ArchitectEntity architect;
    private Structure townHall;
    private int cooldown = 0;
    private final int COOLDOWN_TIME = 40; // 2 seconds @ 20 ticks per second
    private boolean reachedDestination = false;
    private BlockPos targetPosition;
    private final List<BlockPos> interiorPositions = new ArrayList<>();
    private int stuckTicks = 0;
    private BlockPos lastPosition;
    private int idleTime = 0;
    private final int IDLE_DURATION_MIN = 400; // 20 seconds minimum idle time
    private final int IDLE_DURATION_MAX = 1200; // 60 seconds maximum idle time
    private int currentIdleDuration = 0;
    private static final int MAX_FLOOD_FILL_SIZE = 100; // Prevent runaway flood fill

    public MoveToTownHallGoal(ArchitectEntity architect) {
        this.architect = architect;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        // Get the architect's settlement
        String settlementName = architect.getSettlerFaction();
        if (settlementName == null || settlementName.isEmpty() || settlementName.equals("N/A")) {
            if (architect.getWorld().getTime() % 100 == 0) {
                System.out.println("[Architect] Not in any settlement: " + settlementName);
            }
            return false;
        }

        Settlement settlement = SettlementManager.getSettlement(settlementName);
        if (settlement == null) {
            if (architect.getWorld().getTime() % 100 == 0) {
                System.out.println("[Architect] Settlement not found: " + settlementName);
            }
            return false;
        }

        // Find an active, constructed town hall
        for (Structure structure : settlement.getStructures()) {
            if (structure.getType() == StructureType.TOWNHALL && structure.isActive() && structure.isConstructed()) {
                townHall = structure;
                
                // Only find interior positions if they haven't been found yet
                if (interiorPositions.isEmpty()) {
                    findInteriorPositions();
                }
                
                // Only proceed if we have valid interior positions
                return !interiorPositions.isEmpty();
            }
        }

        if (architect.getWorld().getTime() % 100 == 0) {
            System.out.println("[Architect] No active town hall found in " + settlementName);
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        // Keep running if we have a valid town hall and either:
        // 1. We're still moving to our destination, or
        // 2. We've reached our destination and are idling inside
        return townHall != null && 
               townHall.isActive() && 
               townHall.isConstructed() && 
               !interiorPositions.isEmpty() && 
               (reachedDestination || !architect.getNavigation().isIdle());
    }

    @Override
    public void start() {
        if (townHall != null && !interiorPositions.isEmpty()) {
            // Choose a random position from our valid interior positions
            selectNewTargetPosition();
            moveToPosition();
            reachedDestination = false;
            stuckTicks = 0;
            lastPosition = architect.getBlockPos();
            
            // Set up idle duration for when we reach the destination
            Random random = architect.getRandom();
            currentIdleDuration = IDLE_DURATION_MIN + random.nextInt(IDLE_DURATION_MAX - IDLE_DURATION_MIN);
            idleTime = 0;
        }
    }

    @Override
    public void stop() {
        architect.getNavigation().stop();
        cooldown = COOLDOWN_TIME;
        reachedDestination = false;
        stuckTicks = 0;
    }

    // Idle behavior types
    private enum IdleActivityType {
        STANDING,    // Simply stand in place and look around
        WALKING,     // Walk to a new position
        INSPECTING   // Look at and "inspect" a nearby block
    }
    
    private IdleActivityType currentActivity = IdleActivityType.STANDING;
    private int activityDuration = 0;
    private BlockPos inspectionTarget = null;
    
    @Override
    public void tick() {
        if (townHall == null || interiorPositions.isEmpty()) {
            return;
        }

        // Check if we've reached our destination
        if (!reachedDestination && isNearPosition(targetPosition)) {
            System.out.println("[Architect] Reached destination at " + targetPosition);
            reachedDestination = true;
            stuckTicks = 0;
            idleTime = 0;
            
            // Start with standing activity when first reaching a destination
            currentActivity = IdleActivityType.STANDING;
            activityDuration = 60 + architect.getRandom().nextInt(60); // 3-6 seconds
        }

        // Behavior when inside the town hall
        if (reachedDestination) {
            idleTime++;
            
            // Perform the current idle activity
            switch (currentActivity) {
                case STANDING:
                    performStandingBehavior();
                    break;
                    
                case WALKING:
                    performWalkingBehavior();
                    break;
                    
                case INSPECTING:
                    performInspectionBehavior();
                    break;
            }
            
            // Check if it's time to change activities
            activityDuration--;
            if (activityDuration <= 0) {
                selectNextActivity();
            }
            
            // After idling for the total determined duration, move to a completely new spot
            if (idleTime >= currentIdleDuration) {
                System.out.println("[Architect] Total idle time reached, moving to new area");
                selectNewTargetPosition();
                moveToPosition();
                reachedDestination = false;
                stuckTicks = 0;
                
                // Set new idle duration for next position
                Random random = architect.getRandom();
                currentIdleDuration = IDLE_DURATION_MIN + random.nextInt(IDLE_DURATION_MAX - IDLE_DURATION_MIN);
                idleTime = 0;
            }
        } else {
            // Check if we're stuck while trying to reach the destination
            BlockPos currentPos = architect.getBlockPos();
            
            if (lastPosition != null && currentPos.equals(lastPosition)) {
                stuckTicks++;
                
                // If stuck for 2 seconds (40 ticks), try a new position
                if (stuckTicks > 40) {
                    System.out.println("[Architect] Stuck for 2 seconds, trying new position");
                    selectNewTargetPosition();
                    moveToPosition();
                    stuckTicks = 0;
                }
                
                // If really stuck (5 seconds / 100 ticks), check if we're close to town hall
                // and consider teleporting as a last resort
                if (stuckTicks > 100) {
                    double distanceToTownHall = getDistanceToTownHall();
                    if (distanceToTownHall < 16.0) { // Within 16 blocks
                        tryTeleportInside();
                        stuckTicks = 0;
                    }
                }
            } else {
                stuckTicks = 0;
                lastPosition = currentPos;
            }
            
            // If navigation stopped unexpectedly, try again
            if (architect.getNavigation().isIdle()) {
                moveToPosition();
            }
        }
    }
    
    /**
     * Select the next idle activity for the architect
     */
    private void selectNextActivity() {
        Random random = architect.getRandom();
        
        // More natural probabilities for different activities
        int roll = random.nextInt(100);
        
        if (roll < 40) {
            // 40% chance to stand and look around
            currentActivity = IdleActivityType.STANDING;
            activityDuration = 60 + random.nextInt(100); // 3-8 seconds
            System.out.println("[Architect] New activity: Standing for " + activityDuration + " ticks");
        }
        else if (roll < 80) {
            // 40% chance to walk to a nearby position
            currentActivity = IdleActivityType.WALKING;
            activityDuration = 100 + random.nextInt(100); // 5-10 seconds
            
            // Find a nearby position to walk to
            if (!interiorPositions.isEmpty()) {
                int attempts = 0;
                boolean foundPos = false;
                
                // Try to find a position that's not too far away
                while (attempts < 5 && !foundPos) {
                    BlockPos candidatePos = interiorPositions.get(random.nextInt(interiorPositions.size()));
                    double distance = architect.getBlockPos().getSquaredDistance(candidatePos);
                    
                    // If the position is 2-5 blocks away, use it
                    if (distance >= 4.0 && distance <= 25.0) {
                        targetPosition = candidatePos;
                        foundPos = true;
                    }
                    attempts++;
                }
                
                // If we couldn't find a good nearby position, just use any position
                if (!foundPos) {
                    targetPosition = interiorPositions.get(random.nextInt(interiorPositions.size()));
                }
                
                // Start walking to the new position
                architect.getNavigation().startMovingTo(
                    targetPosition.getX() + 0.5,
                    targetPosition.getY(),
                    targetPosition.getZ() + 0.5,
                    0.4  // Slower walking speed for idle walking
                );
                
                System.out.println("[Architect] New activity: Walking to " + targetPosition);
            } else {
                // Fallback if we have no interior positions
                currentActivity = IdleActivityType.STANDING;
                activityDuration = 40 + random.nextInt(80);
            }
        }
        else {
            // 20% chance to inspect something nearby
            currentActivity = IdleActivityType.INSPECTING;
            activityDuration = 60 + random.nextInt(60); // 3-6 seconds
            
            // Find something to inspect (a nearby block)
            findInspectionTarget();
            System.out.println("[Architect] New activity: Inspecting at " + inspectionTarget);
        }
    }
    
    /**
     * Perform the standing behavior (looking around)
     */
    private void performStandingBehavior() {
        Random random = architect.getRandom();
        
        // Occasionally look around
        if (random.nextInt(20) == 0) {
            // Look at a random position nearby
            double lookX = architect.getX() + random.nextDouble() * 5 - 2.5;
            double lookY = architect.getY() + random.nextDouble() * 2 - 0.5;
            double lookZ = architect.getZ() + random.nextDouble() * 5 - 2.5;
            architect.getLookControl().lookAt(lookX, lookY, lookZ);
        }
    }
    
    /**
     * Perform the walking behavior
     */
    private void performWalkingBehavior() {
        // Check if we've reached the local target position
        if (architect.getNavigation().isIdle()) {
            // We've reached the destination, switch to standing
            currentActivity = IdleActivityType.STANDING;
            activityDuration = 40 + architect.getRandom().nextInt(60); // 2-5 seconds
            System.out.println("[Architect] Reached walking destination, now standing");
        }
    }
    
    /**
     * Perform the inspection behavior (looking at a specific block)
     */
    private void performInspectionBehavior() {
        if (inspectionTarget != null) {
            // Focus on the inspection target
            architect.getLookControl().lookAt(
                inspectionTarget.getX() + 0.5,
                inspectionTarget.getY() + 0.5,
                inspectionTarget.getZ() + 0.5
            );
            
            // Occasionally "work" by moving hand/tool
            if (architect.getRandom().nextInt(20) == 0) {
                // Here we could trigger an animation if the Minecraft entity system supports it
                // For now we'll just simulate it with a log message
                System.out.println("[Architect] *inspects structure details*");
            }
        }
    }
    
    /**
     * Find a nearby block to inspect
     */
    private void findInspectionTarget() {
        Random random = architect.getRandom();
        World world = architect.getWorld();
        BlockPos architectPos = architect.getBlockPos();
        
        // Look for blocks around the architect that aren't air
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = architectPos.add(x, y, z);
                    
                    // If it's not air and not the block we're standing on
                    if (!world.getBlockState(pos).isAir() && 
                        !pos.equals(architectPos) && 
                        !pos.equals(architectPos.down())) {
                        potentialTargets.add(pos);
                    }
                }
            }
        }
        
        if (!potentialTargets.isEmpty()) {
            // Choose a random block to inspect
            inspectionTarget = potentialTargets.get(random.nextInt(potentialTargets.size()));
        } else {
            // If no blocks to inspect, just look at a random position
            inspectionTarget = architectPos.add(
                random.nextInt(3) - 1,
                random.nextInt(2),
                random.nextInt(3) - 1
            );
        }
    }

    /**
     * Find valid interior positions within the town hall structure
     */
    private void findInteriorPositions() {
        interiorPositions.clear();
        
        if (townHall == null) {
            System.out.println("[Architect] Town hall is null");
            return;
        }
        
        // Get structure information
        BlockPos originPos = townHall.getPosition();
        Direction facing = townHall.getFacing();
        int[] size = townHall.getStructureSize();
        
        System.out.println("[Architect] Town hall at " + originPos + ", facing " + facing + 
                          ", size: " + (size != null ? (size.length >= 3 ? size[0] + "x" + size[1] + "x" + size[2] : "invalid") : "null"));
        
        if (size == null || size.length < 3) {
            // Default to a reasonable size if we don't have valid size data
            size = new int[]{5, 3, 5};
            System.out.println("[Architect] Using default structure size: 5x3x5");
        }
        
        int width = size[0];
        int height = size[1];
        int depth = size[2];
        
        // Get a more generous bounding box based on the structure and facing
        // Add padding to ensure we catch the full structure
        BlockPos minPos, maxPos;
        
        switch (facing) {
            case NORTH:
                minPos = originPos.add(-width/2, 0, -depth);
                maxPos = originPos.add(width + width/2, height + 1, 1);
                break;
            case SOUTH:
                minPos = originPos.add(-width - width/2, 0, -1);
                maxPos = originPos.add(width/2, height + 1, depth);
                break;
            case EAST:
                minPos = originPos.add(-1, 0, -width - width/2);
                maxPos = originPos.add(depth, height + 1, width/2);
                break;
            case WEST:
                minPos = originPos.add(-depth, 0, -width/2);
                maxPos = originPos.add(1, height + 1, width + width/2);
                break;
            default:
                // For unknown facing, make a larger box in all directions
                minPos = originPos.add(-width, 0, -depth);
                maxPos = originPos.add(width, height + 1, depth);
        }
        
        // Make sure min coordinates are actually less than max coordinates
        int minX = Math.min(minPos.getX(), maxPos.getX());
        int minY = Math.min(minPos.getY(), maxPos.getY());
        int minZ = Math.min(minPos.getZ(), maxPos.getZ());
        
        int maxX = Math.max(minPos.getX(), maxPos.getX());
        int maxY = Math.max(minPos.getY(), maxPos.getY());
        int maxZ = Math.max(minPos.getZ(), maxPos.getZ());
        
        System.out.println("[Architect] Searching area: (" + minX + "," + minY + "," + minZ + ") to (" + 
                           maxX + "," + maxY + "," + maxZ + ")");
        
        // DEBUG: Test a specific position to see if it's standable
        BlockPos testPos = new BlockPos(originPos.getX(), originPos.getY(), originPos.getZ());
        boolean isTestPosStandable = isPositionStandable(testPos);
        System.out.println("[Architect] Test position at " + testPos + " standable: " + isTestPosStandable);
        
        // First approach: Grid search through the entire bounded area
        int positionsFound = 0;
        for (int y = minY; y <= maxY - 1; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isPositionStandable(pos)) {
                        // Check if this position is likely inside the structure
                        // (not just standing outside against a wall)
                        if (isPositionLikelyInterior(pos, minX, maxX, minZ, maxZ)) {
                            interiorPositions.add(pos);
                            positionsFound++;
                            
                            if (positionsFound % 5 == 0) {
                                System.out.println("[Architect] Found " + positionsFound + " standable positions so far");
                            }
                            
                            // Limit to a reasonable number of positions
                            if (positionsFound >= 20) {
                                break;
                            }
                        }
                    }
                }
                if (positionsFound >= 20) break;
            }
            if (positionsFound >= 20) break;
        }
        
        // If we found at least some positions, we're good
        if (!interiorPositions.isEmpty()) {
            System.out.println("[Architect] Found " + interiorPositions.size() + " interior positions in town hall via grid search");
            return;
        }
        
        // Second approach: Try to use architect's current position if it's inside
        BlockPos architectPos = architect.getBlockPos();
        if (architectPos.getX() >= minX && architectPos.getX() <= maxX &&
            architectPos.getY() >= minY && architectPos.getY() <= maxY &&
            architectPos.getZ() >= minZ && architectPos.getZ() <= maxZ &&
            isPositionStandable(architectPos)) {
            
            interiorPositions.add(architectPos);
            System.out.println("[Architect] Using architect's current position as interior position");
        }
        
        // Third approach: Try the structure origin itself and adjacent positions
        BlockPos[] possibleStarts = new BlockPos[] {
            originPos,
            originPos.north(), originPos.south(), 
            originPos.east(), originPos.west(),
            originPos.up(), originPos.up().north(), originPos.up().south(),
            originPos.up().east(), originPos.up().west()
        };
        
        for (BlockPos pos : possibleStarts) {
            if (isPositionStandable(pos)) {
                interiorPositions.add(pos);
                System.out.println("[Architect] Added position " + pos + " near origin");
                if (interiorPositions.size() >= 5) break;
            }
        }
        
        // If all else fails, just use the origin position regardless
        if (interiorPositions.isEmpty()) {
            interiorPositions.add(originPos);
            System.out.println("[Architect] Last resort: Using origin position " + originPos);
        }
        
        System.out.println("[Architect] Found " + interiorPositions.size() + " interior positions in town hall");
    }
    
    /**
     * Check if a position is likely to be inside the structure, not just at the edge
     */
    private boolean isPositionLikelyInterior(BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        // Check if we're not at the very edge of our search area
        if (pos.getX() <= minX + 1 || pos.getX() >= maxX - 1 ||
            pos.getZ() <= minZ + 1 || pos.getZ() >= maxZ - 1) {
            return false;
        }
        
        World world = architect.getWorld();
        int blocksAround = 0;
        
        // Count solid blocks around the position (at feet level)
        if (!world.getBlockState(pos.north()).getCollisionShape(world, pos.north()).isEmpty()) blocksAround++;
        if (!world.getBlockState(pos.south()).getCollisionShape(world, pos.south()).isEmpty()) blocksAround++;
        if (!world.getBlockState(pos.east()).getCollisionShape(world, pos.east()).isEmpty()) blocksAround++;
        if (!world.getBlockState(pos.west()).getCollisionShape(world, pos.west()).isEmpty()) blocksAround++;
        
        // If there are at least 1-2 solid blocks around us, we're probably inside a structure
        return blocksAround >= 1;
    }
    
    /**
     * Recursive flood fill to find connected walkable areas
     */
    private void floodFillWalkableArea(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Set<BlockPos> visited) {
        // Stop if we've already visited this position or have reached our limit
        if (visited.contains(pos) || visited.size() >= MAX_FLOOD_FILL_SIZE) {
            return;
        }
        
        // Check if position is within structure bounds
        if (pos.getX() < minX || pos.getX() > maxX ||
            pos.getY() < minY || pos.getY() > maxY ||
            pos.getZ() < minZ || pos.getZ() > maxZ) {
            return;
        }
        
        // Check if position is standable
        if (!isPositionStandable(pos)) {
            return;
        }
        
        // Mark position as visited
        visited.add(pos);
        
        // Recursively check adjacent positions
        floodFillWalkableArea(pos.north(), minX, minY, minZ, maxX, maxY, maxZ, visited);
        floodFillWalkableArea(pos.south(), minX, minY, minZ, maxX, maxY, maxZ, visited);
        floodFillWalkableArea(pos.east(), minX, minY, minZ, maxX, maxY, maxZ, visited);
        floodFillWalkableArea(pos.west(), minX, minY, minZ, maxX, maxY, maxZ, visited);
        
        // Also check up and down for staircases or other vertical passages
        floodFillWalkableArea(pos.up(), minX, minY, minZ, maxX, maxY, maxZ, visited);
        floodFillWalkableArea(pos.down(), minX, minY, minZ, maxX, maxY, maxZ, visited);
    }

    /**
     * Select a new target position from the list of interior positions
     */
    private void selectNewTargetPosition() {
        if (!interiorPositions.isEmpty()) {
            Random random = architect.getRandom();
            targetPosition = interiorPositions.get(random.nextInt(interiorPositions.size()));
        }
    }

    /**
     * Move the architect to the current target position
     */
    private void moveToPosition() {
        if (targetPosition == null) {
            return;
        }

        // Try direct pathfinding to the target
        boolean canReach = architect.getNavigation().startMovingTo(
            targetPosition.getX() + 0.5, 
            targetPosition.getY(), 
            targetPosition.getZ() + 0.5, 
            0.6
        );
        
        // If direct pathfinding fails, try to pathfind to a nearby position
        if (!canReach) {
            BlockPos doorPos = findDoorPosition();
            if (doorPos != null) {
                architect.getNavigation().startMovingTo(
                    doorPos.getX() + 0.5,
                    doorPos.getY(),
                    doorPos.getZ() + 0.5,
                    0.6
                );
            } else {
                // Try a different interior position
                for (int i = 0; i < 5 && !canReach && !interiorPositions.isEmpty(); i++) {
                    selectNewTargetPosition();
                    canReach = architect.getNavigation().startMovingTo(
                        targetPosition.getX() + 0.5, 
                        targetPosition.getY(), 
                        targetPosition.getZ() + 0.5, 
                        0.6
                    );
                }
            }
        }
    }
    
    /**
     * Check if the architect is near the target position
     */
    private boolean isNearPosition(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        
        double distSq = architect.getBlockPos().getSquaredDistance(pos);
        return distSq < 2.0; // Within ~1.4 blocks
    }
    
    /**
     * Calculate the distance to the town hall structure
     */
    private double getDistanceToTownHall() {
        if (townHall == null) {
            return Double.MAX_VALUE;
        }
        
        Vec3d architectPos = architect.getPos();
        BlockPos townHallPos = townHall.getPosition();
        
        return architectPos.squaredDistanceTo(
            townHallPos.getX(), 
            townHallPos.getY(), 
            townHallPos.getZ()
        );
    }
    
    /**
     * Try to teleport the architect inside the town hall as a last resort
     */
    private void tryTeleportInside() {
        if (targetPosition == null || !isPositionStandable(targetPosition)) {
            if (!interiorPositions.isEmpty()) {
                selectNewTargetPosition();
            } else {
                return;
            }
        }
        
        // Only teleport if we're close to the town hall and really stuck
        if (getDistanceToTownHall() < 16.0 && stuckTicks > 100) {
            architect.refreshPositionAndAngles(
                targetPosition.getX() + 0.5, 
                targetPosition.getY(), 
                targetPosition.getZ() + 0.5,
                architect.getYaw(), 
                architect.getPitch()
            );
            
            System.out.println("[Architect] Teleported inside town hall due to pathfinding difficulties");
            reachedDestination = true;
            stuckTicks = 0;
            
            // Start with standing activity when teleporting
            currentActivity = IdleActivityType.STANDING;
            activityDuration = 40 + architect.getRandom().nextInt(40);
        }
    }
    
    /**
     * Find the entrance/door position of the town hall
     */
    /**
     * Find the entrance/door position by locating an actual door or gate block
     */
    private BlockPos findDoorPosition() {
        if (townHall == null) {
            System.out.println("[Architect] Cannot find door position - town hall is null");
            return null;
        }
        
        BlockPos origin = townHall.getPosition();
        Direction facing = townHall.getFacing();
        int[] size = townHall.getStructureSize();
        
        if (size == null || size.length < 3) {
            System.out.println("[Architect] Cannot find door position - invalid structure size");
            // Use default size values
            size = new int[] {5, 3, 5};
        }
        
        System.out.println("[Architect] Looking for doors in structure at " + origin);
        
        // Define search area around the structure with some padding
        BlockPos minPos, maxPos;
        int width = size[0];
        int height = size[1];
        int depth = size[2];
        
        switch (facing) {
            case NORTH:
                minPos = origin.add(-width/2, 0, -depth);
                maxPos = origin.add(width + width/2, height + 1, 1);
                break;
            case SOUTH:
                minPos = origin.add(-width - width/2, 0, -1);
                maxPos = origin.add(width/2, height + 1, depth);
                break;
            case EAST:
                minPos = origin.add(-1, 0, -width - width/2);
                maxPos = origin.add(depth, height + 1, width/2);
                break;
            case WEST:
                minPos = origin.add(-depth, 0, -width/2);
                maxPos = origin.add(1, height + 1, width + width/2);
                break;
            default:
                // For unknown facing, make a larger box in all directions
                minPos = origin.add(-width, 0, -depth);
                maxPos = origin.add(width, height + 1, depth);
        }
        
        // Make sure min coordinates are actually less than max coordinates
        int minX = Math.min(minPos.getX(), maxPos.getX());
        int minY = Math.min(minPos.getY(), maxPos.getY());
        int minZ = Math.min(minPos.getZ(), maxPos.getZ());
        int maxX = Math.max(minPos.getX(), maxPos.getX());
        int maxY = Math.max(minPos.getY(), maxPos.getY());
        int maxZ = Math.max(minPos.getZ(), maxPos.getZ());
        
        // Scan for door blocks within the search area
        World world = architect.getWorld();
        List<BlockPos> doorPositions = new ArrayList<>();
        
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isDoorOrGate(world, pos)) {
                        // Found a door or gate!
                        doorPositions.add(pos);
                        System.out.println("[Architect] Found door at " + pos);
                    }
                }
            }
        }
        
        if (!doorPositions.isEmpty()) {
            // Found at least one door, check for a valid standable position next to it
            for (BlockPos doorPos : doorPositions) {
                BlockPos[] adjacentPositions = {
                    doorPos.north(), doorPos.south(), 
                    doorPos.east(), doorPos.west()
                };
                
                for (BlockPos adjacent : adjacentPositions) {
                    if (isPositionStandable(adjacent)) {
                        System.out.println("[Architect] Found standable position at " + adjacent + " next to door");
                        return adjacent;
                    }
                }
                
                // If no adjacent position is standable, try positions one block further out
                BlockPos[] furtherPositions = {
                    doorPos.north().north(), doorPos.south().south(), 
                    doorPos.east().east(), doorPos.west().west(),
                    doorPos.north().east(), doorPos.north().west(),
                    doorPos.south().east(), doorPos.south().west()
                };
                
                for (BlockPos further : furtherPositions) {
                    if (isPositionStandable(further)) {
                        System.out.println("[Architect] Found standable position at " + further + " near door");
                        return further;
                    }
                }
            }
            
            // If we found doors but no standable positions next to them,
            // at least we know the door position for reference
            System.out.println("[Architect] Found door(s) but no standable position nearby");
        } else {
            System.out.println("[Architect] No doors found in structure");
        }
        
        // If we couldn't find any doors or standable positions next to doors,
        // fall back to the previous algorithm
        
        // Try some positions directly in front of where the entrance would typically be
        Direction entranceDir = facing.getOpposite();
        BlockPos entranceCenter;
        
        switch (entranceDir) {
            case NORTH:
                entranceCenter = origin.add(width / 2, 0, 0);
                break;
            case SOUTH:
                entranceCenter = origin.add(width / 2, 0, depth - 1);
                break;
            case EAST:
                entranceCenter = origin.add(depth - 1, 0, width / 2);
                break;
            case WEST:
                entranceCenter = origin.add(0, 0, width / 2);
                break;
            default:
                entranceCenter = origin;
        }
        
        // Try the entrance center and positions offset from it
        List<BlockPos> candidatePositions = new ArrayList<>();
        candidatePositions.add(entranceCenter);
        
        for (int offset = 1; offset < Math.max(width, depth); offset++) {
            candidatePositions.add(entranceCenter.north(offset));
            candidatePositions.add(entranceCenter.south(offset));
            candidatePositions.add(entranceCenter.east(offset));
            candidatePositions.add(entranceCenter.west(offset));
        }
        
        // Also try positions directly around the origin
        candidatePositions.add(origin);
        candidatePositions.add(origin.north());
        candidatePositions.add(origin.south());
        candidatePositions.add(origin.east());
        candidatePositions.add(origin.west());
        
        // Try all candidates and return the first valid one
        for (BlockPos pos : candidatePositions) {
            if (isPositionStandable(pos)) {
                System.out.println("[Architect] Fallback: found standable position at " + pos);
                return pos;
            }
        }
        
        // If all else fails, just return the origin
        System.out.println("[Architect] Couldn't find valid door position or fallback, using origin " + origin);
        return origin;
    }
    
    /**
     * Check if a block is a door or gate
     */
    private boolean isDoorOrGate(World world, BlockPos pos) {
        try {
            String blockId = world.getBlockState(pos).getBlock().toString().toLowerCase();
            
            // Check for common door and gate block names
            return blockId.contains("door") || 
                   blockId.contains("gate") ||
                   // Also check for trapdoors as possible entrances
                   blockId.contains("trapdoor");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a position is valid for the architect to stand on
     */
    private boolean isPositionStandable(BlockPos pos) {
        World world = architect.getWorld();
        
        try {
            // Check if blocks at feet and head level are passable
            boolean feetClear = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
            boolean headClear = world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty();
            
            // Check if there's a solid block below to stand on
            boolean hasFloor = !world.getBlockState(pos.down()).getCollisionShape(world, pos.down()).isEmpty();
            
            if (!feetClear || !headClear || !hasFloor) {
                // For debugging, log what specific check failed
                if (!feetClear && architect.getWorld().getTime() % 100 == 0) {
                    System.out.println("[Architect] Position " + pos + " failed: feet not clear");
                }
                if (!headClear && architect.getWorld().getTime() % 100 == 0) {
                    System.out.println("[Architect] Position " + pos + " failed: head not clear");
                }
                if (!hasFloor && architect.getWorld().getTime() % 100 == 0) {
                    System.out.println("[Architect] Position " + pos + " failed: no floor");
                }
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("[Architect] Error checking if position is standable: " + e.getMessage());
            return false;
        }
    }
}