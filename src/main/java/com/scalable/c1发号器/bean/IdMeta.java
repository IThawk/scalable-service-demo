package com.scalable.c1发号器.bean;

/**
 * 使用的是无符号右移，因为产生的id包含的每一位二进制位都代表特殊的含义，所以没有数学上的正负意义，最左边的一位二进制也不是用来表示符号的。
 * 我们看到在做无符号右移操作的时候使用了屏蔽字(-1L ^ -1L << genMethodBits)，这用于从id数字中取出我们想要的某个属性的值，具体流程：
 * 格式：			版本		类型		生产方式	时间		序列号		机器id
 * id数字：		0101	1010	11011	00000	1001	0001	
 * 生成方式字段右移：	0000	0000	0000	0101	1010	11011
 * 与屏蔽字：		0000	0000	0000	0000	0000	11111
 * 获得生成方式字段：	0000	0000	0000	0000	0000	11011
 * 屏蔽字的形成：这里的-1L为64位全1的二进制数字，首先将其左移属性值所在位置的位移，生成方式属性从右边开始的位置到数字最右边一位全为0，再与-1进行异或操作，结果就形成了屏蔽字。
 * @author Administrator
 *
 */
public class IdMeta {
	
	private byte machineBits;

    private byte seqBits;

    private byte timeBits;

    private byte genMethodBits;

    private byte typeBits;

    private byte versionBits;

    public IdMeta(byte machineBits, byte seqBits, byte timeBits, byte genMethodBits, byte typeBits, byte versionBits) {
        super();

        this.machineBits = machineBits;
        this.seqBits = seqBits;
        this.timeBits = timeBits;
        this.genMethodBits = genMethodBits;
        this.typeBits = typeBits;
        this.versionBits = versionBits;
    }

    public byte getMachineBits() {
        return machineBits;
    }

    public void setMachineBits(byte machineBits) {
        this.machineBits = machineBits;
    }

    public long getMachineBitsMask() {
        return -1L ^ -1L << machineBits;
    }

    public byte getSeqBits() {
        return seqBits;
    }

    public void setSeqBits(byte seqBits) {
        this.seqBits = seqBits;
    }

    public long getSeqBitsStartPos() {
        return machineBits;
    }

    public long getSeqBitsMask() {
        return -1L ^ -1L << seqBits;
    }

    public byte getTimeBits() {
        return timeBits;
    }

    public void setTimeBits(byte timeBits) {
        this.timeBits = timeBits;
    }

    public long getTimeBitsStartPos() {
        return machineBits + seqBits;
    }

    public long getTimeBitsMask() {
        return -1L ^ -1L << timeBits;
    }

    public byte getGenMethodBits() {
        return genMethodBits;
    }

    public void setGenMethodBits(byte genMethodBits) {
        this.genMethodBits = genMethodBits;
    }

    public long getGenMethodBitsStartPos() {
        return machineBits + seqBits + timeBits;
    }

    public long getGenMethodBitsMask() {
        return -1L ^ -1L << genMethodBits;
    }

    public byte getTypeBits() {
        return typeBits;
    }

    public void setTypeBits(byte typeBits) {
        this.typeBits = typeBits;
    }

    public long getTypeBitsStartPos() {
        return machineBits + seqBits + timeBits + genMethodBits;
    }

    public long getTypeBitsMask() {
        return -1L ^ -1L << typeBits;
    }

    public byte getVersionBits() {
        return versionBits;
    }

    public void setVersionBits(byte versionBits) {
        this.versionBits = versionBits;
    }

    public long getVersionBitsStartPos() {
        return machineBits + seqBits + timeBits + genMethodBits + typeBits;
    }

    public long getVersionBitsMask() {
        return -1L ^ -1L << versionBits;
    }
    
    public static void main(String[] args) {
    	Long l = -1L;
    	System.out.println(l.toBinaryString(l));
    	Long l2 = l <<10;
    	System.out.println(l2.toBinaryString(l2));
    	Long l3 = l ^ l2;
    	System.out.println(l3.toBinaryString(l3));
    	System.out.println(l3);
	}

}
