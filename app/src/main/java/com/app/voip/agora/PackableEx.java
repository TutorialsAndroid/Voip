package com.app.voip.agora;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}