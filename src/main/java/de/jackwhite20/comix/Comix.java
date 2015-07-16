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
import de.jackwhite20.comix.config.ComixConfig;
import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.network.PacketDecoderNew;
import de.jackwhite20.comix.network.StatusResponse;
import de.jackwhite20.comix.network.UpstreamHandler;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import de.jackwhite20.comix.strategy.RoundRobinBalancingStrategy;
import de.jackwhite20.comix.util.TargetData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class Comix {

    private static Comix instance;

    private String balancerHost;

    private int balancerPort;

    private List<TargetData> targets = new ArrayList<>();

    private ServerBootstrap bootstrap;

    private BalancingStrategy balancingStrategy;

    private ComixConfig comixConfig;

    private StatusResponse statusResponse;

    private String statusResponseString;

    private ChannelGroup channelGroup;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private List<String> ipBlacklist = new ArrayList<>();

    public Comix(ComixConfig comixConfig) {
        instance = this;
        this.comixConfig = comixConfig;
        this.balancerHost = comixConfig.getHost();
        this.balancerPort = comixConfig.getPort();
        this.targets = comixConfig.getTargets();
    }

    public void start() {
        Console.getConsole().println("Starting Comix on " + balancerHost + ":" + balancerPort + "...");

        balancingStrategy = new RoundRobinBalancingStrategy(targets);

        channelGroup = new DefaultChannelGroup("clients", GlobalEventExecutor.INSTANCE);

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 200)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            // Simple IP-Blacklist
                            if(ipBlacklist.contains(ch.remoteAddress().getAddress().getHostAddress())) {
                                ch.close();
                                return;
                            }

                            UpstreamHandler upstreamHandler = new UpstreamHandler(balancingStrategy);
                            //p.addFirst(new IpFilterHandler());
                            //p.addFirst(new IpFilterHandler(balancingStrategy));
                            p.addFirst(new PacketDecoderNew(upstreamHandler));
                            //p.addFirst(new PacketDecoder());
                            //p.addFirst(new PacketDownstreamDecoder());
                            p.addLast(upstreamHandler);

                            Console.getConsole().println("[/" + p.channel().remoteAddress() + "] -> InitialHandler has connected");
                        }

                    });

            ChannelFuture f = bootstrap.bind(comixConfig.getPort()).sync();

            loadIpBlacklist();

            loadStatusResponse();

            Console.getConsole().println("Comix is started!");

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        channelGroup.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
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

            Console.getConsole().println("IP-Blacklist", (ipBlacklist.size() == 0) ? "File loaded..." : ipBlacklist.size() + " IPs loaded...");
        } catch (Exception e) {
            Console.getConsole().println("Error loading ip-blacklist.comix: " + e.getMessage());
        }
    }

    public String replaceColor(String input) {
        return input.replace("�", "\\u00A7");
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
            Console.getConsole().println("Favicon could not be loaded: " + e.getMessage());
            return "";
        }

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

            Console.getConsole().println("Favicon loaded...");

            statusResponse = new Gson().fromJson(stringBuilder.toString(), StatusResponse.class);
            statusResponseString = "{\"version\":{\"name\":\"" + statusResponse.getVersion().getName() + "\",\"protocol\":" + statusResponse.getVersion().getProtocol() + "},\"players\":{\"max\":" + statusResponse.getPlayers().getMax() + ",\"online\":" + statusResponse.getPlayers().getOnline() + "},\"description\":\"" + statusResponse.getDescription() + "\",\"favicon\":\"data:image/png;base64," + faviconString + "\",\"modinfo\":{\"type\":\"FML\",\"modList\":[]}}";

            Console.getConsole().println("Status Response loaded...");
        } catch (Exception e) {
            Console.getConsole().println("Error loading status.comix");
            e.printStackTrace();
        }
    }

    public void addChannel(Channel channel) {
        channelGroup.add(channel);
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

    public void setBalancingStrategy(BalancingStrategy balancingStrategy) {
        this.balancingStrategy = balancingStrategy;
    }

    public static Comix getInstance() {
        return instance;
    }

}
