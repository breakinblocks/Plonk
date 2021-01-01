package com.breakinblocks.plonk.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public interface IPlonkCommand {
    String getName();

    String getUsage(CommandSource sender);

    default int getRequiredPermissionLevel() {
        return 4;
    }

    default int sendUsage(CommandSource source, int exitCode) {
        source.sendFeedback(new StringTextComponent(getUsage(source)), true);
        return exitCode;
    }

    default LiteralArgumentBuilder<CommandSource> build() {
        return Commands.literal(getName())
                .requires(source -> source.hasPermissionLevel(getRequiredPermissionLevel()))
                .executes(context -> sendUsage(context.getSource(), SINGLE_SUCCESS));
    }

    default void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(build());
    }
}
