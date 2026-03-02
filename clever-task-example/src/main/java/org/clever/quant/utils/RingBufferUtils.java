package org.clever.quant.utils;

import org.clever.core.Assert;
import org.clever.core.RingBuffer;

/**
 * 作者：lizw <br/>
 * 创建时间：2026/03/02 22:04 <br/>
 */
public class RingBufferUtils {
    /**
     * 从指定索引位置开始向前获取数据(包含{@code lastIdx}位置数据)
     *
     * @param ringBuffer 环形缓冲区
     * @param lastIdx    指定索引位置(从0开始)
     * @param size       向前获取的数据量
     * @return 返回指定数据集合(数据量可能不够)
     */
    public static <T> RingBuffer.BufferContent<T> getContent(RingBuffer<T> ringBuffer, long lastIdx, int size) {
        Assert.notNull(ringBuffer, "参数 ringBuffer 不能为 null");
        Assert.isTrue(lastIdx >= 0, "参数 lastIdx 必须大于等于 0");
        Assert.isTrue(size > 0, "参数 size 必须大于等于 0");
        long startIndex = lastIdx - size + 1;
        if (startIndex < 0) {
            startIndex = 0;
            size = (int) (lastIdx + 1);
        }
        return ringBuffer.getBuffer(startIndex, size);
    }
}
