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

import java.io.BufferedReader;
import java.io.FileReader;
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
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            UpstreamHandler upstreamHandler = new UpstreamHandler(balancingStrategy);
                            //p.addFirst(new IpFilterHandler());
                            p.addFirst(new PacketDecoderNew(upstreamHandler));
                            //p.addFirst(new PacketDecoder());
                            //p.addFirst(new PacketDownstreamDecoder());
                            p.addLast(upstreamHandler);

                            Console.getConsole().println("[/" + p.channel().remoteAddress() + "] <-> InitialHandler has connected");
                        }

                    });

            ChannelFuture f = bootstrap.bind(comixConfig.getPort()).sync();

            Console.getConsole().println("Comix is started!");

            loadStatusResponse();

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

    public String replaceColor(String input) {
        return input.replace("§", "\\u00A7");
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

            statusResponse = new Gson().fromJson(stringBuilder.toString(), StatusResponse.class);
            statusResponseString = "{\"version\":{\"name\":\"" + statusResponse.getVersion().getName() + "\",\"protocol\":" + statusResponse.getVersion().getProtocol() + "},\"players\":{\"max\":" + statusResponse.getPlayers().getMax() + ",\"online\":" + statusResponse.getPlayers().getOnline() + "},\"description\":\"" + statusResponse.getDescription() + "\",\"modinfo\":{\"type\":\"FML\",\"modList\":[]}}";

            Console.getConsole().println("StatusResponse string loaded...");
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
