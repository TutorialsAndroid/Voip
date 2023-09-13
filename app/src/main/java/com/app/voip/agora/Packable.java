package com.app.voip.agora;

public interface Packable {
    ByteBuf marshal(ByteBuf out);
}
