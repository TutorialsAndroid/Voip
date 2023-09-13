package com.app.voip.agora;
public class RtcTokenBuilder2Generator {
    public static String RtcTokenGenerate(
            String appId,
            String appCertificate,
            String channelName,
            int uid,
            int tokenExpirationInSeconds,
            int privilegeExpirationInSeconds
    ) {
        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        return token.buildTokenWithUid(appId, appCertificate,
                channelName, uid,
                RtcTokenBuilder2.Role.ROLE_SUBSCRIBER,
                tokenExpirationInSeconds, privilegeExpirationInSeconds
        );
    }
}
