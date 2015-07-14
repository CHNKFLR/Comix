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

import de.jackwhite20.comix.config.ComixConfig;
import de.jackwhite20.comix.console.Console;
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
        Console.getConsole().println("Starting Comix on " + balancerHost + "...");

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

                            p.addLast(new UpstreamHandler(balancingStrategy));

                            Console.getConsole().println("[/" + p.channel().remoteAddress() + "] <-> InitialHandler has connected");
                        }

                    });

            ChannelFuture f = bootstrap.bind(comixConfig.getPort()).sync();

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

    public void addChannel(Channel channel) {
        channelGroup.add(channel);
    }

    public ComixConfig getComixConfig() {
        return comixConfig;
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
