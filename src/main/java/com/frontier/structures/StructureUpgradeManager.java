package com.frontier.structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class StructureUpgradeManager 
{
    private final Structure structure;
    
    private boolean isUpgrading;
    private Queue<BlockPos> upgradeQueue = new LinkedList<>();
    private Map<BlockPos, BlockState> upgradeMap = new HashMap<>();
    
    private static final int BLOCK_UPGRADE_TICKS = 1;
    private int upgradeTicksElapsed = 0;
    
    public StructureUpgradeManager(Structure structure) 
    {
        this.structure = structure;
        this.isUpgrading = false;
    }
    
    public void upgrade(ServerWorld world) 
    {
        if (structure.getTier() >= structure.getMaxTier()) 
        {
            Frontier.LOGGER.info("Structure " + structure.getName() + " is already at maximum tier (" + structure.getTier() + ")");
            return;
        }
        
        this.isUpgrading = true;
        int newTier = structure.getTier() + 1;
        
        prepareUpgradeQueue(world, newTier);
    }
    
    private void prepareUpgradeQueue(ServerWorld world, int newTier) 
    {
        upgradeQueue.clear();
        upgradeMap.clear();
        
        // temporarily set to new tier to process structure
        int currentTier = structure.getTier();
        structure.setTier(newTier);
        
        StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
        {
            upgradeQueue.add(blockPos);
            upgradeMap.put(blockPos, blockState);
        });
        
        // reset to current tier
        structure.setTier(currentTier);
    }
    
    public void processTick(ServerWorld world) 
    {
        if (!isUpgrading) return;
        
        upgradeTicksElapsed++;
        
        if (upgradeTicksElapsed >= BLOCK_UPGRADE_TICKS) 
        {
            upgradeTicksElapsed = 0;
            
            if (!upgradeQueue.isEmpty()) 
            {
                BlockPos pos = upgradeQueue.poll();
                BlockState state = upgradeMap.get(pos);
                world.setBlockState(pos, state);
            } 
            
            else 
            {
                // upgrade completed
                int newTier = structure.getTier() + 1;
                structure.setTier(newTier);
                isUpgrading = false;
                structure.onUpgrade();
                SettlementManager.saveSettlements(world.getServer());
            }
        }
    }
    
    public boolean upgradeAvailable()  {
        return structure.getTier() < structure.getMaxTier();
    }
    
    public boolean isUpgrading() {
        return isUpgrading;
    }
    
    public void setUpgrading(boolean isUpgrading) {
        this.isUpgrading = isUpgrading;
    }
    
    public Queue<BlockPos> getUpgradeQueue() {
        return upgradeQueue;
    }
    
    public void setUpgradeQueue(Queue<BlockPos> upgradeQueue) {
        this.upgradeQueue = upgradeQueue;
    }
    
    public Map<BlockPos, BlockState> getUpgradeMap() {
        return upgradeMap;
    }
    
    public void setUpgradeMap(Map<BlockPos, BlockState> upgradeMap) {
        this.upgradeMap = upgradeMap;
    }
    
    public int getUpgradeTicksElapsed() {
        return upgradeTicksElapsed;
    }
    
    public void setUpgradeTicksElapsed(int upgradeTicksElapsed) {
        this.upgradeTicksElapsed = upgradeTicksElapsed;
    }
}