import { DataWriter } from './DataStream';

export abstract class CpInfo {
  constructor(public tag: number) {}
  abstract write(writer: DataWriter): void;
}

export class ConstantDummyInfo extends CpInfo {
  static INSTANCE = new ConstantDummyInfo();
  constructor() { super(0); }
  write(_writer: DataWriter): void {}
}

export class ConstantUtf8Info extends CpInfo {
  public replacement: string;
  public isPatched: boolean = false;

  constructor(public value: string) {
    super(1);
    this.replacement = value;
  }

  write(writer: DataWriter): void {
    writer.writeU1(1);
    writer.writeUTF(this.value);
  }
}

export class ConstantIntegerInfo extends CpInfo {
  constructor(public value: number) { super(3); }
  write(writer: DataWriter): void {
    writer.writeU1(3);
    writer.writeI4(this.value);
  }
}

export class ConstantFloatInfo extends CpInfo {
  constructor(public rawBits: number) { super(4); }
  write(writer: DataWriter): void {
    writer.writeU1(4);
    writer.writeI4(this.rawBits);
  }
}

export class ConstantLongInfo extends CpInfo {
  constructor(public value: bigint) { super(5); }
  write(writer: DataWriter): void {
    writer.writeU1(5);
    writer.writeI8(this.value);
  }
}

export class ConstantDoubleInfo extends CpInfo {
  constructor(public rawBits: bigint) { super(6); }
  write(writer: DataWriter): void {
    writer.writeU1(6);
    writer.writeI8(this.rawBits);
  }
}

export class ConstantClassInfo extends CpInfo {
  constructor(public nameIndex: number) { super(7); }
  write(writer: DataWriter): void {
    writer.writeU1(7);
    writer.writeU2(this.nameIndex);
  }
}

export class ConstantStringInfo extends CpInfo {
  constructor(public stringIndex: number) { super(8); }
  write(writer: DataWriter): void {
    writer.writeU1(8);
    writer.writeU2(this.stringIndex);
  }
}

export class ConstantFieldrefInfo extends CpInfo {
  constructor(public classIndex: number, public nameAndTypeIndex: number) { super(9); }
  write(writer: DataWriter): void {
    writer.writeU1(9);
    writer.writeU2(this.classIndex);
    writer.writeU2(this.nameAndTypeIndex);
  }
}

export class ConstantMethodrefInfo extends CpInfo {
  constructor(public classIndex: number, public nameAndTypeIndex: number) { super(10); }
  write(writer: DataWriter): void {
    writer.writeU1(10);
    writer.writeU2(this.classIndex);
    writer.writeU2(this.nameAndTypeIndex);
  }
}

export class ConstantInterfaceMethodrefInfo extends CpInfo {
  constructor(public classIndex: number, public nameAndTypeIndex: number) { super(11); }
  write(writer: DataWriter): void {
    writer.writeU1(11);
    writer.writeU2(this.classIndex);
    writer.writeU2(this.nameAndTypeIndex);
  }
}

export class ConstantNameAndTypeInfo extends CpInfo {
  constructor(public nameIndex: number, public descriptorIndex: number) { super(12); }
  write(writer: DataWriter): void {
    writer.writeU1(12);
    writer.writeU2(this.nameIndex);
    writer.writeU2(this.descriptorIndex);
  }
}

export class AttributeInfo {
  public length: number;
  public data: Uint8Array;

  constructor(public nameIndex: number, lengthOrData: number | Uint8Array, maybeData?: Uint8Array) {
    if (maybeData instanceof Uint8Array) {
      this.length = lengthOrData as number;
      this.data = maybeData;
    } else {
      this.data = lengthOrData as Uint8Array;
      this.length = this.data.length;
    }
  }

  write(writer: DataWriter): void {
    writer.writeU2(this.nameIndex);
    writer.writeI4(this.data.length);
    writer.writeBytes(this.data);
  }
}

export class FieldInfo {
  public attributes: AttributeInfo[] = [];
  constructor(public accessFlags: number, public nameIndex: number, public descriptorIndex: number) {}
  write(writer: DataWriter): void {
    writer.writeU2(this.accessFlags);
    writer.writeU2(this.nameIndex);
    writer.writeU2(this.descriptorIndex);
    writer.writeU2(this.attributes.length);
    for (const attr of this.attributes) {
      attr.write(writer);
    }
  }
}

export class MethodInfo {
  public attributes: AttributeInfo[] = [];
  constructor(public accessFlags: number, public nameIndex: number, public descriptorIndex: number) {}
  write(writer: DataWriter): void {
    writer.writeU2(this.accessFlags);
    writer.writeU2(this.nameIndex);
    writer.writeU2(this.descriptorIndex);
    writer.writeU2(this.attributes.length);
    for (const attr of this.attributes) {
      attr.write(writer);
    }
  }
}
