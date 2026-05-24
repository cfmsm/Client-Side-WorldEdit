package com.github.cfmsm.cswe;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.minecraft.client.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSideWorldEdit implements ModInitializer {
	public static final String MOD_ID = "client-side-world-edit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static BlockPos pos1;
	public static BlockPos pos2;
	@Override
	public void onInitialize() {
		LOGGER.info("ClientSideWorldEdit has been initialized");
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			dispatcher.register(
					ClientCommandManager.literal("cp1")
							.executes(context -> {
								pos1=context.getSource().getPlayer().getBlockPos();
								context.getSource().sendFeedback(Text.literal("First positon set to ("+pos1.getX() + ", "+pos1.getY() + ", "+pos1.getZ() + ")").formatted(Formatting.GREEN));
								return 1;
							})
			);
			dispatcher.register(
					ClientCommandManager.literal("cp2")
							.executes(context -> {
								pos2=context.getSource().getPlayer().getBlockPos();
								context.getSource().sendFeedback(Text.literal("Second positon set to ("+pos2.getX() + ", "+pos2.getY() + ", "+pos2.getZ() + ")").formatted(Formatting.GREEN));
								return 1;
							})
			);
			dispatcher.register(
					ClientCommandManager.literal("cset")
							.then(
									ClientCommandManager.argument("block", StringArgumentType.greedyString()) //greedy string allows extra arguments such as replace <block>, destroy, strict, keep, hollow
											.executes(context -> {
												ClientPlayerEntity player = context.getSource().getPlayer();

												String block = StringArgumentType.getString(context, "block");

												int minX = Math.min(pos1.getX(), pos2.getX());
												int minY = Math.min(pos1.getY(), pos2.getY());
												int minZ = Math.min(pos1.getZ(), pos2.getZ());

												int maxX = Math.max(pos1.getX(), pos2.getX());
												int maxY = Math.max(pos1.getY(), pos2.getY());
												int maxZ = Math.max(pos1.getZ(), pos2.getZ());

												final int fillLimit = 32768;

												int sizeX = maxX - minX + 1;
                                                int sizeZ = maxZ - minZ + 1;

												int sliceSize = Math.max(1, fillLimit / (sizeX * sizeZ));

												for (int y = minY; y <= maxY; y += sliceSize) {
													int endY = Math.min(y + sliceSize - 1, maxY);

													player.networkHandler.sendChatCommand(
															"fill "
																	+ minX + " "
																	+ y + " "
																	+ minZ + " "
																	+ maxX + " "
																	+ endY + " "
																	+ maxZ + " "
																	+ block
													);
												}

												return 1;
											})
							)
			);
			dispatcher.register(
					ClientCommandManager.literal("cwalls").
							then(ClientCommandManager.argument("block", StringArgumentType.greedyString())
							.executes(context -> {
								if (pos1==null) {
									context.getSource().sendError(Text.literal("You have not set first position").formatted(Formatting.DARK_RED));
									return 0;
								}
								if (pos2==null) {
									context.getSource().sendError(Text.literal("You have not set second position").formatted(Formatting.DARK_RED));
									return 0;
								}
								String block = StringArgumentType.getString(context, "block");
								ClientPlayerEntity player = context.getSource().getPlayer();

								int minX = Math.min(pos1.getX(), pos2.getX());
								int minY = Math.min(pos1.getY(), pos2.getY());
								int minZ = Math.min(pos1.getZ(), pos2.getZ());

								int maxX = Math.max(pos1.getX(), pos2.getX());
								int maxY = Math.max(pos1.getY(), pos2.getY());
								int maxZ = Math.max(pos1.getZ(), pos2.getZ());
								player.networkHandler.sendChatCommand(
										"fill " + minX + " " + minY + " " + minZ + " " +
												minX + " " + maxY + " " + maxZ + " "
												+ block
								);
								player.networkHandler.sendChatCommand(
										"fill " + maxX + " " + minY + " " + minZ + " " +
												maxX + " " + maxY + " " + maxZ + " "
												+ block
								);
								player.networkHandler.sendChatCommand(
										"fill " + minX + " " + minY + " " + minZ + " " +
												maxX + " " + maxY + " " + minZ + " "
												+ block
								);
								player.networkHandler.sendChatCommand(
										"fill " + minX + " " + minY + " " + maxZ + " " +
												maxX + " " + maxY + " " + maxZ + " "
												+ block
								);

								context.getSource().sendFeedback(
										Text.literal("Walls created")
												.formatted(Formatting.GREEN)
								);

								return 1;
							})
			));
		});
	}
}