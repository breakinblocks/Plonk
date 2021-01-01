package com.breakinblocks.plonk.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

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
    public String getUsage(CommandSource source) {
        return "/cplonk <subcommand> - Client Plonk Commands";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literal = build();
        for (IPlonkCommand subcommand : this.subcommands) {
            literal = literal.then(subcommand.build());
        }
        dispatcher.register(literal);
    }
}
