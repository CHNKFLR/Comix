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

import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.util.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by JackWhite20 on 14.07.2015.
 */
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    private boolean firstLoginStepDone = false;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //byteBuf.retain();

        ByteBuf copy = byteBuf.copy();

        try {
            ByteBuf buffer = byteBuf.retain();

            int length = Protocol.readVarInt2(buffer);
            //Console.getConsole().println("Length: " + length);
            if(length > 0) {
                int packetId = Protocol.readVarInt2(buffer);

                //Console.getConsole().println("Decoded packet ID: " + packetId);

                if(packetId == 0) {
                    //if(!firstLoginStepDone) {
                    Console.getConsole().println("Length: " + length);
                        int version = Protocol.readVarInt2(buffer);
                        String ip = Protocol.readString(buffer);
                        int port = buffer.readUnsignedShort();
                        int state = Protocol.readVarInt2(buffer);

                        if(state == 2) {
                            firstLoginStepDone = true;
                            Console.getConsole().println("HANDSHAKE: " + version + " - " + ip + ":" + port + " - " + "LOGIN");
                            String name = Protocol.readString(buffer);

                            channelHandlerContext.channel().pipeline().removeFirst();

                            Console.getConsole().println("AUTH WITH NAME: " + name);
                        }
                    /*}else {
                        String name = Protocol.readString(buffer);

                        channelHandlerContext.channel().pipeline().removeFirst();

                        Console.getConsole().println("AUTH WITH NAME: " + name);
                    }*/
                }/*else if(packetId == 1) {
                    //int sharedSecretLength = Protocol.readVarInt(buffer);
                    byte[] sharedSecret = Protocol.readArray(buffer);
                    byte[] verifyToken = Protocol.readArray(buffer);

                    Console.getConsole().println("SharedSecret: " + sharedSecret.length + " VerifyToken: " + verifyToken.length);
                }*/
            }
            list.add(copy.retain());
            copy = null;
        } finally {
            if(copy != null)
                copy.release();
        }
    }

}
