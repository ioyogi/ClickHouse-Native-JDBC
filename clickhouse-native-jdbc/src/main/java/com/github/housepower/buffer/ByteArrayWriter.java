/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import java.io.IOException;

public class ByteArrayWriter implements BuffedWriter {
    private final int columnSize;

    private ByteBuf buf;
    private final CompositeByteBuf compositeBuf;

    public ByteArrayWriter(int columnSize) {
        this.columnSize = columnSize;
        this.buf = ByteBufAllocator.DEFAULT.buffer(columnSize, columnSize);
        this.compositeBuf = ByteBufAllocator.DEFAULT.compositeBuffer().addComponent(buf);
    }

    @Override
    public void writeBinary(byte byt) throws IOException {
        buf.writeByte(byt);
        flushToTarget(false);
    }

    @Override
    public void writeBinary(byte[] bytes) throws IOException {
        writeBinary(bytes, 0, bytes.length);
    }

    @Override
    public void writeBinary(byte[] bytes, int offset, int length) throws IOException {
        if (buf.maxFastWritableBytes() < length) {
            int partial = buf.maxFastWritableBytes();
            buf.writeBytes(bytes, offset, partial);
            flushToTarget(true);
            offset += partial;
            length -= partial;
        }
        buf.writeBytes(bytes, offset, length);
        flushToTarget(false);
    }

    @Override
    public void flushToTarget(boolean force) {
        if (buf.maxFastWritableBytes() > 0 && !force)
            return;

        buf = ByteBufAllocator.DEFAULT.buffer(columnSize, columnSize);
        compositeBuf.addComponent(buf);
    }

    public CompositeByteBuf getBuf() {
        return compositeBuf;
    }
}
