/*
 * Copyright (c) 2015 "JackWhite20"
 *
 * This file is part of Comix.
 *
 * Comix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jackwhite20.comix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.jackwhite20.comix.command.CommandManager;
import de.jackwhite20.comix.command.commands.*;
import de.jackwhite20.comix.config.ComixConfig;
import de.jackwhite20.comix.config.Config;
import de.jackwhite20.comix.config.response.StatusResponse;
import de.jackwhite20.comix.handler.ComixChannelInitializer;
import de.jackwhite20.comix.logger.ComixLogger;
import de.jackwhite20.comix.network.ComixClient;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import de.jackwhite20.comix.strategy.RoundRobinBalancingStrategy;
import de.jackwhite20.comix.util.TargetData;
import de.jackwhite20.comix.whitelist.Whitelist;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import org.fusesource.jansi.AnsiConsole;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class Comix implements Runnable {

    private static Comix instance;

    private static Logger logger;

    private static ConsoleReader consoleReader;

    private boolean running;

    private String balancerHost;

    private int balancerPort;

    private List<TargetData> targets = new ArrayList<>();

    private BalancingStrategy balancingStrategy;

    private ComixConfig comixConfig;

    private StatusResponse statusResponse;

    private String statusResponseString;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private List<String> ipBlacklist = new ArrayList<>();

    private Whitelist whitelist;

    private List<ComixClient> clients = Collections.synchronizedList(new ArrayList<>());

    private CommandManager commandManager = new CommandManager();

    public Comix() {
        instance = this;
        running = true;

        try {
            consoleReader = new ConsoleReader();
            consoleReader.setExpandEvents(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.setProperty( "java.net.preferIPv4Stack", "true" );

        ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.DISABLED );

        AnsiConsole.systemInstall();

        LogManager.getLogManager().reset();

        logger = new ComixLogger(consoleReader);

        logger.info("------ Comix v.0.1 ------");

        loadConfig();

        logger.info((targets.size() > 0) ? "Targets:" : "No Target Servers found!");
        targets.forEach(t -> logger.info(t.getName() + " - " + t.getHost() + ":" + t.getPort()));

        logger.info("Registering commands...");

        commandManager.addCommand(new HelpCommand("help",  new String[] {"h", "?"}, "List of commands"));
        commandManager.addCommand(new ReloadCommand("reload",  new String[] {"r"}, "Reloads 'ip-blacklist.comix', 'status.comix' and 'whitelist.comix'"));
        commandManager.addCommand(new MaintenanceCommand("maintenance",  new String[] {"m"}, "Switches between Maintenance"));
        commandManager.addCommand(new KickallCommand("kickall",  new String[] {"ka"}, "Kicks all players from Comix"));
        commandManager.addCommand(new ClearCommand("clear",  new String[] {"c"}, "Clears the screen"));
        commandManager.addCommand(new StopCommand("stop",  new String[] {"end"}, "Stops Comix"));

        List<String> cmds = new ArrayList<>();
        commandManager.getCommands().forEach(c -> cmds.add(c.getName()));
        consoleReader.addCompleter(new StringsCompleter(cmds));

        logger.info("Starting Comix on " + balancerHost + ":" + balancerPort + "...");

        balancingStrategy = new RoundRobinBalancingStrategy(targets);

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(comixConfig.getThreads());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, comixConfig.getBacklog())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .childOption(ChannelOption.SO_TIMEOUT, 4000)
                    .childHandler(new ComixChannelInitializer());

            ChannelFuture f = bootstrap.bind(comixConfig.getPort()).sync();

            reload();

            logger.info("Comix is started!");

            f.channel().closeFuture().sync();

            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void kickAll() {
        clients.forEach(c -> c.getUpstreamHandler().getUpstreamChannel().close());
    }

    public void reload() {
        loadIpBlacklist();
        loadWhitelist();
        loadStatusResponse();
    }

    public boolean maintainMode() {
        if(!comixConfig.isMaintenance()) {
            comixConfig.setMaintenance(true);
            saveConfig();
            loadStatusResponse();

            return true;
        }else {
            comixConfig.setMaintenance(false);
            saveConfig();
            loadStatusResponse();

            return false;
        }
    }

    public void saveConfig() {
        String status = new GsonBuilder().setPrettyPrinting().create().toJson(comixConfig, ComixConfig.class);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("config.comix")));
            writer.write(status);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        start();
    }

    private void loadConfig() {
        try {
            this.comixConfig = Config.loadConfig("");

            this.balancerHost = comixConfig.getHost();
            this.balancerPort = comixConfig.getPort();
            this.targets = comixConfig.getTargets();

            logger.info("Config loaded...");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to load Comix Config file!");
            System.exit(1);
        }
    }

    private void loadWhitelist() {
        try {
            new File("whitelist.comix").createNewFile();

            BufferedReader bufferedReader = new BufferedReader(new FileReader("whitelist.comix"));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }

            whitelist = new Gson().fromJson(stringBuilder.toString(), Whitelist.class);

            if(!whitelist.isEnabled())
                logger.info("Whitelist loaded...");
            else
                logger.info("Whitelisted: " + String.join(", ", whitelist.getNames()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while loading 'whitelist.comix'!");
        }
    }

    public boolean isIpBanned(String ip) {
        return ipBlacklist.contains(ip);
    }

    public boolean isWhitelisted(String name) {
        return whitelist.getNames().contains(name);
    }

    public boolean isWhitelistEnabled() {
        return whitelist.isEnabled();
    }

    public String getWhitelistKickMessage() {
        return whitelist.getMessage();
    }

    public void shutdown() {
        running = false;

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        //TODO: Shutdown more "nicely"
        System.exit(0);
    }

    private void loadIpBlacklist() {
        try {
            new File("ip-blacklist.comix").createNewFile();

            BufferedReader bufferedReader = new BufferedReader(new FileReader("ip-blacklist.comix"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line != "")
                    ipBlacklist.add(line);
            }

            logger.log(Level.INFO, "IP-Blacklist", (ipBlacklist.size() == 0) ? "File loaded..." : ipBlacklist.size() + " IPs loaded...");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while loading ip-blacklist.comix: " + e.getMessage());
        }
    }

    public String replaceColor(String input) {
        return input.replace("§", "\\u00A7");
    }

    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while loading favicon: " + e.getMessage());
            return "";
        }

        logger.info("Favicon loaded...");

        return imageString;
    }

    public void loadStatusResponse() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("status.comix"));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }

            String faviconString = encodeToString(ImageIO.read(new File("favicon.png")), "png");

            statusResponse = new Gson().fromJson(stringBuilder.toString(), StatusResponse.class);
            if(!comixConfig.isMaintenance())
                statusResponseString = "{\"version\":{\"name\":\"" + statusResponse.getVersion().getName() + "\",\"protocol\":" + statusResponse.getVersion().getProtocol() + "},\"players\":{\"max\":" + statusResponse.getPlayers().getMax() + ",\"online\":" + statusResponse.getPlayers().getOnline() + ", \"sample\":[{\"name\":\"" + statusResponse.getPlayers().getSample() + "\",\"id\":\"00000000-0000-0000-0000-000000000000\"}]},\"description\":\"" + statusResponse.getDescription() + "\",\"favicon\":\"data:image/png;base64," + faviconString + "\",\"modinfo\":{\"type\":\"FML\",\"modList\":[]}}";
            else {
                statusResponseString = "{\"version\":{\"name\":\"" + comixConfig.getMaintenancePingMessage() + "\",\"protocol\":0},\"players\":{\"max\":" + statusResponse.getPlayers().getMax() + ",\"online\":" + statusResponse.getPlayers().getOnline() + "},\"description\":\"" + comixConfig.getMaintenanceDescription() + "\",\"favicon\":\"data:image/png;base64," + faviconString + "\",\"modinfo\":{\"type\":\"FML\",\"modList\":[]}}";
            }

            logger.info("Status Response loaded...");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while loading status.comix");
            e.printStackTrace();
        }
    }

    public void broadcast(ByteBuf buffer) {
        clients.forEach(comixClient -> comixClient.getUpstreamHandler().getUpstreamChannel().writeAndFlush(buffer));
    }

    public void addClient(ComixClient comixClient) {
        clients.add(comixClient);
    }

    public void removeClient(ComixClient comixClient) {
        clients.remove(comixClient);
    }

    public int getClientsOnline() {
        return clients.size();
    }

    public boolean isRunning() {
        return running;
    }

    public ComixConfig getComixConfig() {
        return comixConfig;
    }

    public String getStatusResponseString() {
        return statusResponseString;
    }

    public void setComixConfig(ComixConfig comixConfig) {
        this.comixConfig = comixConfig;
    }

    public BalancingStrategy getBalancingStrategy() {
        return balancingStrategy;
    }

    public StatusResponse getStatusResponse() {
        return statusResponse;
    }

    public void setStatusResponse(StatusResponse statusResponse) {
        this.statusResponse = statusResponse;
    }

    public static ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Comix getInstance() {
        return instance;
    }

}
