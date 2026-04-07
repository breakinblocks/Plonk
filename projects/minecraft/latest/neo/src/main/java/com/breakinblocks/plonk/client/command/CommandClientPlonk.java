package com.breakinblocks.plonk.client.command;

import com.breakinblocks.plonk.common.command.IPlonkCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.LinkedList;
import java.util.List;

public class CommandClientPlonk implements IPlonkCommand {

    private final List<IPlonkCommand> subcommands = new LinkedList<>();

    public CommandClientPlonk() {
        addSubcommand(new CommandDumpRenderTypes());
    }

    private void addSubcommand(IPlonkCommand command) {
        subcommands.add(command);
    }

    @Override
    public String getName() {
        return "cplonk";
    }

    @Override
    public String getUsage(CommandSourceStack source) {
        return "/cplonk <subcommand> - Client Plonk Commands";
    }

    @Override
    public PermissionCheck getPermissionCheck() {
        return Commands.LEVEL_ALL;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literal = build();
        for (IPlonkCommand subcommand : this.subcommands) {
            literal = literal.then(subcommand.build());
        }
        dispatcher.register(literal);
    }
}
