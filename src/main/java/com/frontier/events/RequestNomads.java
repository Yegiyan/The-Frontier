package com.frontier.events;

import java.util.Random;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.SpawnEntity;
import com.frontier.entities.NomadEntity;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class RequestNomads
{
	public static void registerCallback() 
	{
	    UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> 
	    {
	    	PlayerData playerData = PlayerData.map.get(player.getUuid());
	    	if (playerData != null)
	    	{
	    		if (!world.isClient && world.isDay() && playerData.getProfession().equals("Leader") && player.getStackInHand(hand).getItem() == Items.CLOCK && world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.BELL) 
		        {
		            Random rand = new Random();
		            int distance = 32;

		            BlockPos bellPos = blockHitResult.getBlockPos();
		            double angle = rand.nextDouble() * Math.PI * 2;
		            int x = bellPos.getX() + (int) (Math.cos(angle) * distance);
		            int z = bellPos.getZ() + (int) (Math.sin(angle) * distance);
		            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
		            BlockPos spawnPos = new BlockPos(x, y, z);
		            System.out.println("Spawn Pos: " + spawnPos);

		            NomadEntity nomad = Frontier.NOMAD_ENTITY.create(world);
		            nomad.setBellPosition(bellPos);
		            nomad.refreshPositionAndAngles(spawnPos, 0, 0);
		            SpawnEntity.nomad((NomadEntity) nomad, "N/A", "Nomad", world);

		            Frontier.sendMessage((ServerPlayerEntity) player, "Bell rung!", Formatting.WHITE);
		            return ActionResult.SUCCESS;
		        }
	    		else
	    			return ActionResult.PASS;
	    	}
	        else
	        {
	        	System.err.println("RequestNomads() playerData is null!");
	        	return ActionResult.PASS;
	        }
	    });
	}
}