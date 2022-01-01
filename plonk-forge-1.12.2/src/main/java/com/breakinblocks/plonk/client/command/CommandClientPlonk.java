package com.breakinblocks.plonk.client.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandClientPlonk extends CommandTreeBase implements IClientCommand {

    public CommandClientPlonk() {
        this.addSubcommand(new CommandDumpRenderTypes());
        this.addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName() {
        return "cplonk";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cplonk <subcommand> - Client Plonk Commands";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }
}
