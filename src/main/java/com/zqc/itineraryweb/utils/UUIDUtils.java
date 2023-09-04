package com.zqc.itineraryweb.utils;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.UUID;

@Component
public class UUIDUtils {

    /**
     * 随机生成 UUID
     *
     * @return byte
     */
    public static byte[] generateUUIDToBytes() {
        UUID uuid = UUID.randomUUID();
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    /**
     * 二进制UUID转换成UUID对象
     *
     * @param bytes UUID字节
     * @return UUID对象
     */
    public static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long mostSigBits = byteBuffer.getLong();
        long leastSigBits = byteBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * UUID对象转换成字节
     *
     * @param uuid UUID对象
     * @return byte
     */
    public static byte[] convertUUIDToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

}
