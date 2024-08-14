package com.zqc.itineraryweb.utils.tencentyun;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.*;

import java.util.Arrays;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TLSSigAPIv2 {
    final private long sdkappid;
    final private String key;

    public TLSSigAPIv2(long sdkappid, String key) {
        this.sdkappid = sdkappid;
        this.key = key;
    }

    /**
     * Function: Used to issue UserSig that is required by the TRTC and IM services.
     * <p>
     * Parameter description:
     *
     * @param userid - User ID. The value can be up to 32 bytes in length and contain letters (a-z and A-Z), digits (0-9), underscores (_), and hyphens (-).
     * @param expire - UserSig expiration time, in seconds. For example, 86400 indicates that the generated UserSig will expire one day after being generated.
     * @return usersig - Generated signature.
     */
    public String genUserSig(String userid, long expire) {
        return genUserSig(userid, expire, null);
    }

    /**
     * Function:
     * Used to issue PrivateMapKey that is optional for room entry.
     * PrivateMapKey must be used together with UserSig but with more powerful permission control capabilities.
     * - UserSig can only control whether a UserID has permission to use the TRTC service. As long as the UserSig is correct, the user with the corresponding UserID can enter or leave any room.
     * - PrivateMapKey specifies more stringent permissions for a UserID, including whether the UserID can be used to enter a specific room and perform audio/video upstreaming in the room.
     * To enable stringent PrivateMapKey permission bit verification, you need to enable permission key in TRTC console > Application Management > Application Info.
     * <p>
     * Parameter description:
     *
     * @param userid       - User ID. The value can be up to 32 bytes in length and contain letters (a-z and A-Z), digits (0-9), underscores (_), and hyphens (-).
     * @param roomid       - ID of the room to which the specified UserID can enter.
     * @param expire       - PrivateMapKey expiration time, in seconds. For example, 86400 indicates that the generated PrivateMapKey will expire one day after being generated.
     * @param privilegeMap - Permission bits. Eight bits in the same byte are used as the permission switches of eight specific features:
     *                     - Bit 1: 0000 0001 = 1, permission for room creation
     *                     - Bit 2: 0000 0010 = 2, permission for room entry
     *                     - Bit 3: 0000 0100 = 4, permission for audio sending
     *                     - Bit 4: 0000 1000 = 8, permission for audio receiving
     *                     - Bit 5: 0001 0000 = 16, permission for video sending
     *                     - Bit 6: 0010 0000 = 32, permission for video receiving
     *                     - Bit 7: 0100 0000 = 64, permission for substream video sending (screen sharing)
     *                     - Bit 8: 1000 0000 = 200, permission for substream video receiving (screen sharing)
     *                     - privilegeMap == 1111 1111 == 255: Indicates that the UserID has all feature permissions of the room specified by roomid.
     *                     - privilegeMap == 0010 1010 == 42: Indicates that the UserID has only the permissions to enter the room and receive audio/video data.
     * @return usersig - Generate signature with userbuf
     */
    public String genPrivateMapKey(String userid, long expire, long roomid, long privilegeMap) {
        byte[] userbuf = genUserBuf(userid, roomid, expire, privilegeMap, 0, "");  //生成userbuf
        return genUserSig(userid, expire, userbuf);
    }

    /**
     * Function:
     * Used to issue PrivateMapKey that is optional for room entry.
     * PrivateMapKey must be used together with UserSig but with more powerful permission control capabilities.
     * - UserSig can only control whether a UserID has permission to use the TRTC service. As long as the UserSig is correct, the user with the corresponding UserID can enter or leave any room.
     * - PrivateMapKey specifies more stringent permissions for a UserID, including whether the UserID can be used to enter a specific room and perform audio/video upstreaming in the room.
     * To enable stringent PrivateMapKey permission bit verification, you need to enable permission key in TRTC console > Application Management > Application Info.
     * <p>
     * Parameter description:
     *
     * @param userid       - User ID. The value can be up to 32 bytes in length and contain letters (a-z and A-Z), digits (0-9), underscores (_), and hyphens (-).
     * @param expire       - PrivateMapKey expiration time, in seconds. For example, 86400 indicates that the generated PrivateMapKey will expire one day after being generated.
     * @param privilegeMap - Permission bits. Eight bits in the same byte are used as the permission switches of eight specific features:
     *                     - Bit 1: 0000 0001 = 1, permission for room creation
     *                     - Bit 2: 0000 0010 = 2, permission for room entry
     *                     - Bit 3: 0000 0100 = 4, permission for audio sending
     *                     - Bit 4: 0000 1000 = 8, permission for audio receiving
     *                     - Bit 5: 0001 0000 = 16, permission for video sending
     *                     - Bit 6: 0010 0000 = 32, permission for video receiving
     *                     - Bit 7: 0100 0000 = 64, permission for substream video sending (screen sharing)
     *                     - Bit 8: 1000 0000 = 200, permission for substream video receiving (screen sharing)
     *                     - privilegeMap == 1111 1111 == 255: Indicates that the UserID has all feature permissions of the room specified by roomid.
     *                     - privilegeMap == 0010 1010 == 42: Indicates that the UserID has only the permissions to enter the room and receive audio/video data.
     * @return usersig - Generate signature with userbuf
     */
    public String genPrivateMapKeyWithStringRoomID(String userid, long expire, String roomstr, long privilegeMap) {
        byte[] userbuf = genUserBuf(userid, 0, expire, privilegeMap, 0, roomstr);  //生成userbuf
        return genUserSig(userid, expire, userbuf);
    }

    private String hmacsha256(String identifier, long currTime, long expire, String base64Userbuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.sdkappid:" + sdkappid + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (null != base64Userbuf) {
            contentToBeSigned += "TLS.userbuf:" + base64Userbuf + "\n";
        }
        try {
            byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return (Base64.getEncoder().encodeToString(byteSig)).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return "";
        }
    }

    private String genUserSig(String userid, long expire, byte[] userbuf) {

        long currTime = System.currentTimeMillis() / 1000;

        JSONObject sigDoc = new JSONObject();
        sigDoc.put("TLS.ver", "2.0");
        sigDoc.put("TLS.identifier", userid);
        sigDoc.put("TLS.sdkappid", sdkappid);
        sigDoc.put("TLS.expire", expire);
        sigDoc.put("TLS.time", currTime);

        String base64UserBuf = null;
        if (null != userbuf) {
            base64UserBuf = Base64.getEncoder().encodeToString(userbuf).replaceAll("\\s*", "");
            sigDoc.put("TLS.userbuf", base64UserBuf);
        }
        String sig = hmacsha256(userid, currTime, expire, base64UserBuf);
        if (sig.isEmpty()) {
            return "";
        }
        sigDoc.put("TLS.sig", sig);
        Deflater compressor = new Deflater();
        compressor.setInput(sigDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return (new String(Base64URL.base64EncodeUrl(Arrays.copyOfRange(compressedBytes,
                0, compressedBytesLength)))).replaceAll("\\s*", "");
    }

    public byte[] genUserBuf(String account, long dwAuthID, long dwExpTime,
                             long dwPrivilegeMap, long dwAccountType, String RoomStr) {
        //视频校验位需要用到的字段,按照网络字节序放入buf中
        /*
         cVer    unsigned char/1 版本号，填0
         wAccountLen unsigned short /2   第三方自己的帐号长度
         account wAccountLen 第三方自己的帐号字符
         dwSdkAppid  unsigned int/4  sdkappid
         dwAuthID    unsigned int/4  群组号码
         dwExpTime   unsigned int/4  过期时间 ，直接使用填入的值
         dwPrivilegeMap  unsigned int/4  权限位，主播0xff，观众0xab
         dwAccountType   unsigned int/4  第三方帐号类型
         */

        //The fields required for the video check digit are placed in buf according to the network byte order.
        /*
         cVer    unsigned char/1 Version number, fill in 0
         wAccountLen unsigned short /2   Third party's own account length
         account wAccountLen Third party's own account characters
         dwSdkAppid  unsigned int/4  sdkappid
         dwAuthID    unsigned int/4  group number
         dwExpTime   unsigned int/4  Expiration time , use the filled value directly
         dwPrivilegeMap  unsigned int/4  Permission bits, host 0xff, audience 0xab
         dwAccountType   unsigned int/4  Third-party account type
        */
        int accountLength = account.length();
        int roomStrLength = RoomStr.length();
        int offset = 0;
        int bufLength = 1 + 2 + accountLength + 20;
        if (roomStrLength > 0) {
            bufLength = bufLength + 2 + roomStrLength;
        }
        byte[] userbuf = new byte[bufLength];

        //cVer
        if (roomStrLength > 0) {
            userbuf[offset++] = 1;
        } else {
            userbuf[offset++] = 0;
        }

        //wAccountLen
        userbuf[offset++] = (byte) ((accountLength & 0xFF00) >> 8);
        userbuf[offset++] = (byte) (accountLength & 0x00FF);

        //account
        for (; offset < 3 + accountLength; ++offset) {
            userbuf[offset] = (byte) account.charAt(offset - 3);
        }

        //dwSdkAppid
        userbuf[offset++] = (byte) ((sdkappid & 0xFF000000) >> 24);
        userbuf[offset++] = (byte) ((sdkappid & 0x00FF0000) >> 16);
        userbuf[offset++] = (byte) ((sdkappid & 0x0000FF00) >> 8);
        userbuf[offset++] = (byte) (sdkappid & 0x000000FF);

        //dwAuthId,房间号
        //dwAuthId, room number
        userbuf[offset++] = (byte) ((dwAuthID & 0xFF000000) >> 24);
        userbuf[offset++] = (byte) ((dwAuthID & 0x00FF0000) >> 16);
        userbuf[offset++] = (byte) ((dwAuthID & 0x0000FF00) >> 8);
        userbuf[offset++] = (byte) (dwAuthID & 0x000000FF);

        //expire，过期时间,当前时间 + 有效期（单位：秒）
        //expire,Expiration time, current time + validity period (unit: seconds)
        long currTime = System.currentTimeMillis() / 1000;
        long expire = currTime + dwExpTime;
        userbuf[offset++] = (byte) ((expire & 0xFF000000) >> 24);
        userbuf[offset++] = (byte) ((expire & 0x00FF0000) >> 16);
        userbuf[offset++] = (byte) ((expire & 0x0000FF00) >> 8);
        userbuf[offset++] = (byte) (expire & 0x000000FF);

        //dwPrivilegeMap，权限位
        //dwPrivilegeMap，Permission bits
        userbuf[offset++] = (byte) ((dwPrivilegeMap & 0xFF000000) >> 24);
        userbuf[offset++] = (byte) ((dwPrivilegeMap & 0x00FF0000) >> 16);
        userbuf[offset++] = (byte) ((dwPrivilegeMap & 0x0000FF00) >> 8);
        userbuf[offset++] = (byte) (dwPrivilegeMap & 0x000000FF);

        //dwAccountType，账户类型
        //dwAccountType，account type
        userbuf[offset++] = (byte) ((dwAccountType & 0xFF000000) >> 24);
        userbuf[offset++] = (byte) ((dwAccountType & 0x00FF0000) >> 16);
        userbuf[offset++] = (byte) ((dwAccountType & 0x0000FF00) >> 8);
        userbuf[offset++] = (byte) (dwAccountType & 0x000000FF);


        if (roomStrLength > 0) {
            //roomStrLen
            userbuf[offset++] = (byte) ((roomStrLength & 0xFF00) >> 8);
            userbuf[offset++] = (byte) (roomStrLength & 0x00FF);

            //roomStr
            for (; offset < bufLength; ++offset) {
                userbuf[offset] = (byte) RoomStr.charAt(offset - (bufLength - roomStrLength));
            }
        }
        return userbuf;
    }
}
