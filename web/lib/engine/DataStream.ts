export class DataReader {
  private view: DataView;
  public pos: number = 0;

  constructor(public buffer: Uint8Array) {
    this.view = new DataView(buffer.buffer, buffer.byteOffset, buffer.byteLength);
  }

  readU1(): number {
    return this.buffer[this.pos++];
  }

  readU2(): number {
    const val = this.view.getUint16(this.pos, false);
    this.pos += 2;
    return val;
  }

  readI2(): number {
    const val = this.view.getInt16(this.pos, false);
    this.pos += 2;
    return val;
  }

  readU4(): number {
    const val = this.view.getUint32(this.pos, false);
    this.pos += 4;
    return val;
  }

  readI4(): number {
    const val = this.view.getInt32(this.pos, false);
    this.pos += 4;
    return val;
  }

  readI8(): bigint {
    const val = this.view.getBigInt64(this.pos, false);
    this.pos += 8;
    return val;
  }

  readBytes(len: number): Uint8Array {
    const slice = this.buffer.subarray(this.pos, this.pos + len);
    this.pos += len;
    return slice;
  }

  readUTF(): string {
    const len = this.readU2();
    const bytes = this.readBytes(len);
    let out = '';
    let i = 0;
    while (i < len) {
      const c = bytes[i++];
      switch (c >> 4) {
        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
          out += String.fromCharCode(c);
          break;
        case 12: case 13: {
          const char2 = bytes[i++];
          out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
          break;
        }
        case 14: {
          const char2 = bytes[i++];
          const char3 = bytes[i++];
          out += String.fromCharCode(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
          break;
        }
      }
    }
    return out;
  }
}

export class DataWriter {
  private chunks: Uint8Array[] = [];
  private currentChunk: Uint8Array;
  private offset: number = 0;
  private chunkSize: number;

  constructor(chunkSize: number = 65536) {
    this.chunkSize = chunkSize;
    this.currentChunk = new Uint8Array(chunkSize);
  }

  private ensure(len: number) {
    if (this.offset + len > this.chunkSize) {
      this.chunks.push(this.currentChunk.subarray(0, this.offset));
      this.currentChunk = new Uint8Array(Math.max(this.chunkSize, len));
      this.offset = 0;
    }
  }

  writeU1(val: number) {
    this.ensure(1);
    this.currentChunk[this.offset++] = val & 0xFF;
  }

  writeU2(val: number) {
    this.ensure(2);
    this.currentChunk[this.offset++] = (val >> 8) & 0xFF;
    this.currentChunk[this.offset++] = val & 0xFF;
  }

  writeI4(val: number) {
    this.ensure(4);
    this.currentChunk[this.offset++] = (val >> 24) & 0xFF;
    this.currentChunk[this.offset++] = (val >> 16) & 0xFF;
    this.currentChunk[this.offset++] = (val >> 8) & 0xFF;
    this.currentChunk[this.offset++] = val & 0xFF;
  }

  writeI8(val: bigint) {
    this.ensure(8);
    for (let i = 56; i >= 0; i -= 8) {
      this.currentChunk[this.offset++] = Number((val >> BigInt(i)) & BigInt(0xFF));
    }
  }

  writeBytes(bytes: Uint8Array) {
    if (bytes.length === 0) return;
    this.ensure(bytes.length);
    this.currentChunk.set(bytes, this.offset);
    this.offset += bytes.length;
  }

  writeUTF(str: string) {
    // Modified UTF-8 encoder
    const utf8Bytes: number[] = [];
    for (let i = 0; i < str.length; i++) {
      const c = str.charCodeAt(i);
      if (c >= 0x0001 && c <= 0x007F) {
        utf8Bytes.push(c);
      } else if (c > 0x07FF || c === 0) {
        utf8Bytes.push(0xE0 | ((c >> 12) & 0x0F));
        utf8Bytes.push(0x80 | ((c >> 6) & 0x3F));
        utf8Bytes.push(0x80 | ((c >> 0) & 0x3F));
      } else {
        utf8Bytes.push(0xC0 | ((c >> 6) & 0x1F));
        utf8Bytes.push(0x80 | ((c >> 0) & 0x3F));
      }
    }
    this.writeU2(utf8Bytes.length);
    this.ensure(utf8Bytes.length);
    this.currentChunk.set(utf8Bytes, this.offset);
    this.offset += utf8Bytes.length;
  }

  toByteArray(): Uint8Array {
    let totalLen = 0;
    for (const c of this.chunks) totalLen += c.byteLength;
    totalLen += this.offset;

    const result = new Uint8Array(totalLen);
    let p = 0;
    for (const c of this.chunks) {
      result.set(c, p);
      p += c.byteLength;
    }
    result.set(this.currentChunk.subarray(0, this.offset), p);
    return result;
  }
}
