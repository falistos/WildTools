package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.server.network;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.network.protocol.Packet;

public class PlayerConnection extends MappedObject<net.minecraft.server.network.PlayerConnection> {

    public PlayerConnection(net.minecraft.server.network.PlayerConnection handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
            name = "send",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void send(Packet<?> packet) {
        handle.a(packet);
    }

}
