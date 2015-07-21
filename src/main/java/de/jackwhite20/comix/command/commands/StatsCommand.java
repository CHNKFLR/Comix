package de.jackwhite20.comix.command.commands;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.command.Command;
import de.jackwhite20.comix.network.ComixClient;
import de.jackwhite20.comix.util.Util;

import java.util.List;

/**
 * Created by JackWhite20 on 20.07.2015.
 */
public class StatsCommand extends Command {

    public StatsCommand(String name, String[] aliases, String description) {
        super(name, aliases, description);
    }

    @Override
    public boolean execute(String[] args) {
        List<ComixClient> clients = Comix.getInstance().getClients();

        long totalUpstreamBytesIn = 0;
        long totalDownstreamBytesOut = 0;

        long totalUpstreamBytesOut = 0;
        long totalDownstreamBytesIn = 0;

        for (ComixClient client : clients) {
            totalUpstreamBytesIn += client.getUpstreamBytesIn();
            totalDownstreamBytesOut += client.getDownstreamBytesOut();

            totalUpstreamBytesOut += client.getUpstreamBytesOut();
            totalDownstreamBytesIn += client.getDownstreamBytesIn();
        }

        Comix.getLogger().info("---------- Stats ----------");
        Comix.getLogger().info("Clients connected: " + clients.size());
        Comix.getLogger().info("Total Upstream in: " + Util.convertBytes(totalUpstreamBytesIn));
        Comix.getLogger().info("Total Downstream out: " + Util.convertBytes(totalDownstreamBytesOut));
        Comix.getLogger().info("Total Upstream out: " + Util.convertBytes(totalUpstreamBytesOut));
        Comix.getLogger().info("Total Downstream in: " + Util.convertBytes(totalDownstreamBytesIn));

        return true;
    }

}
