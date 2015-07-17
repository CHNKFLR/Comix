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

package de.jackwhite20.comix.network;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import de.jackwhite20.comix.util.Protocol;
import de.jackwhite20.comix.util.TargetData;
import de.jackwhite20.comix.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.net.InetSocketAddress;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class UpstreamHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ComixClient client;

    private BalancingStrategy strategy;

    private Channel upstreamChannel;

    private Channel downstreamChannel;

    public UpstreamHandler(BalancingStrategy strategy) {
        this.strategy = strategy;
    }

    public DownstreamHandler downstreamHandler;

    public DownstreamHandler getDownstreamHandler() {
        return downstreamHandler;
    }

    public void setClient(ComixClient client) {
        this.client = client;
    }

    public Channel getUpstreamChannel() {
        return upstreamChannel;
    }

    public void startProxying() throws  Exception {
        Console.getConsole().println("Starting proxying...");

        InetSocketAddress address = (InetSocketAddress) upstreamChannel.remoteAddress();
        TargetData target = this.strategy.selectTarget(address.getHostName(), address.getPort());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(upstreamChannel.eventLoop())
                .channel(upstreamChannel.getClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .handler(new DownstreamHandler(upstreamChannel));

        ChannelFuture f = bootstrap.connect(target.getHost(), target.getPort());

        //f.channel().pipeline().addFirst(new PacketDownstreamDecoder());
        //f.channel().pipeline().addFirst(new PacketDecoderNew());

        downstreamChannel = f.channel();

        //downstreamChannel.read();
        //downstreamChannel.writeAndFlush(firstPacket.retain());

        f.addListener((future) -> {
            //Console.getConsole().println("isSuccess: " + future.isSuccess());
            if (future.isSuccess()) {
                downstreamChannel.read();

                Console.getConsole().println("[" + Util.formatSocketAddress(upstreamChannel.remoteAddress()) + "] <-> [" + Util.formatSocketAddress(f.channel().remoteAddress()) + "] tunneled");
            } else {
                downstreamChannel.close();
            }
        });

        Console.getConsole().println("Downstream connected....");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        upstreamChannel = ctx.channel();

        upstreamChannel.read();
        //Comix.getInstance().addChannel(upstreamChannel);

        Console.getConsole().println("[" + Util.formatSocketAddress(upstreamChannel.remoteAddress()) + "] -> UpstreamHandler has connected");

/*        InetSocketAddress address = (InetSocketAddress) upstreamChannel.remoteAddress();
        TargetData target = this.strategy.selectTarget(address.getHostName(), address.getPort());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(upstreamChannel.eventLoop())
                .channel(upstreamChannel.getClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                //.handler(new PacketDownstreamDecoder())
                .handler(downstreamHandler = new DownstreamHandler(upstreamChannel));

        ChannelFuture f = bootstrap.connect(target.getHost(), target.getPort());

        //f.channel().pipeline().addFirst(new PacketDownstreamDecoder());
        //f.channel().pipeline().addFirst(new PacketDecoderNew());

        downstreamChannel = f.channel();

        f.addListener((future) -> {
            if (future.isSuccess()) {
                upstreamChannel.read();

                Console.getConsole().println("[" + Util.formatSocketAddress(upstreamChannel.remoteAddress()) + "] <-> [" + Util.formatSocketAddress(f.channel().remoteAddress()) + "] tunneled");
            } else {
                upstreamChannel.close();
            }
        });*/
    }

    private boolean loggedIn = false;

    private boolean downstreamConnected = false;

    private boolean decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf copy) {
        try {
            ByteBuf buffer = byteBuf.retain();

            if(buffer.readableBytes() <= 1)
                return false;

            Protocol.readVarInt(buffer); // Length

            if(buffer.readableBytes() <= 0)
                return false;

            int packetId = Protocol.readVarInt(buffer);

            if (packetId == 0 && buffer.readableBytes() > 0) {
                Protocol.readVarInt(buffer); // Protocol Version
                Protocol.readString(buffer); // Ip
                buffer.readUnsignedShort(); // Port
                int state = Protocol.readVarInt(buffer); // State

                if (state == 1) {
                    ByteBuf responseBuffer = Unpooled.buffer();
                    String response = Comix.getInstance().getStatusResponseString();
                    response = response.replace("%online%", "" + Comix.getInstance().getClientsOnline());
                    Protocol.writeVarInt(3 + response.length(), responseBuffer); // Size
                    Protocol.writeVarInt(0, responseBuffer); // Packet id
                    Protocol.writeString(response, responseBuffer); // Data as json string
                    channelHandlerContext.writeAndFlush(responseBuffer.retain());

                    ByteBuf pingResponse = Unpooled.buffer();
                    Protocol.writeVarInt(9, pingResponse); // Size
                    Protocol.writeVarInt(1, pingResponse); // Packet id
                    pingResponse.writeLong(System.currentTimeMillis()); // Read long from client
                    channelHandlerContext.writeAndFlush(pingResponse.retain());

                    channelHandlerContext.channel().pipeline().removeFirst();
                    channelHandlerContext.close();

                    return false;
                } else if (state == 2) {
                    InetSocketAddress address = (InetSocketAddress) upstreamChannel.remoteAddress();
                    TargetData target = this.strategy.selectTarget(address.getHostName(), address.getPort());

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(upstreamChannel.eventLoop())
                            .channel(upstreamChannel.getClass())
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.AUTO_READ, false)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                                    //.handler(new PacketDownstreamDecoder())
                            .handler(downstreamHandler = new DownstreamHandler(upstreamChannel));

                    ChannelFuture f = bootstrap.connect(target.getHost(), target.getPort());

                    //f.channel().pipeline().addFirst(new PacketDownstreamDecoder());
                    //f.channel().pipeline().addFirst(new PacketDecoderNew());

                    downstreamChannel = f.channel();

                    f.addListener((future) -> {
                        if (future.isSuccess()) {
                            //upstreamChannel.read();
                            downstreamChannel.writeAndFlush(copy.retain());
                            downstreamConnected = true;

                            Console.getConsole().println("[" + Util.formatSocketAddress(upstreamChannel.remoteAddress()) + "] <-> [" + Util.formatSocketAddress(f.channel().remoteAddress()) + "] tunneled");
                        } else {
                            upstreamChannel.close();
                        }
                    });

                    if(Comix.getInstance().getComixConfig().isMaintenance()) {
                        ByteBuf test = Unpooled.buffer();
                        String text = Comix.getInstance().getComixConfig().getMaintenanceKickMessage();
                        Protocol.writeVarInt(2 + text.length(), test);
                        Protocol.writeVarInt(0, test);
                        Protocol.writeString(text, test);
                        channelHandlerContext.writeAndFlush(test);

                        channelHandlerContext.close();
                    }
                    //upstreamHandler.startProxying();

                    String name = Protocol.readString(buffer);

                    //channelHandlerContext.channel().pipeline().removeFirst();

                    Console.getConsole().println("Player logged in: " + name);

                    loggedIn = true;

                    return true;
/*                    ComixClient comixClient = new ComixClient(name, upstreamHandler.getDownstreamHandler(), upstreamHandler);
                    Comix.getInstance().addClient(comixClient);
                    upstreamHandler.setClient(comixClient);*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        ByteBuf copy = byteBuf.copy();

        if(!loggedIn) {
            if(!decode(ctx, byteBuf, copy))
                return;
        }
        //Console.getConsole().println("After if(!loggedIn) -> " + ((downstreamChannel == null) ? "NULL" : "Ready"));

        if(!downstreamConnected) {
            ctx.channel().read();
            //Console.getConsole().println("Halt stopp!");
            return;
        }

        downstreamChannel.writeAndFlush(copy.retain()).addListener((future) -> {
            if(future.isSuccess()) {
                ctx.channel().read();
            }else {
                ctx.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (downstreamChannel != null) {
            if (downstreamChannel.isActive()) {
                downstreamChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }

            if(client != null)
                Comix.getInstance().removeClient(client);

            Console.getConsole().println("[" + Util.formatSocketAddress(ctx.channel().remoteAddress()) + "] -> UpstreamHandler has disconnected");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Channel ch = ctx.channel();
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
