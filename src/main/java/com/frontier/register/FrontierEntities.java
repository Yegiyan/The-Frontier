package com.frontier.register;

import com.frontier.Frontier;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.entities.settler.NomadEntity;
import com.frontier.entities.settler.SettlerEntity;
import com.frontier.renderers.SettlerEntityRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FrontierEntities
{
	public static final EntityType<NomadEntity> NOMAD_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier("settlers", "nomad"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NomadEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).trackRangeBlocks(128).build());
	
	public static final EntityType<ArchitectEntity> ARCHITECT_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier("settlers", "architect"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ArchitectEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).trackRangeBlocks(128).build());
	
	public static void register()
	{
		EntityRendererRegistry.register(NOMAD_ENTITY,(EntityRendererFactory.Context context) -> new SettlerEntityRenderer(context));
		FabricDefaultAttributeRegistry.register(NOMAD_ENTITY, SettlerEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2));
		
		EntityRendererRegistry.register(ARCHITECT_ENTITY,(EntityRendererFactory.Context context) -> new SettlerEntityRenderer(context));
		FabricDefaultAttributeRegistry.register(ARCHITECT_ENTITY, SettlerEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1));
	}
	
	public static EntityType<? extends SettlerEntity> getEntityType(String profession)
	{
        switch (profession)
        {
            case "Architect":
                return ARCHITECT_ENTITY;
            case "Nomad":
            	return NOMAD_ENTITY;
            default:
            	Frontier.LOGGER.error("FrontierEntities() - SettlerEntity type not found!");
            	return null;
        }
    }
}