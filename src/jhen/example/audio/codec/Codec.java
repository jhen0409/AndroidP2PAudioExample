package jhen.example.audio.codec;

public class Codec {
	static final private String TAG = "Codec";
    static final private Codec INSTANCE = new Codec();
    
    public native int encode(byte[] data, int dataOffset, int dataLength,
            byte[] samples, int samplesOffset);
    
    public native int decode(byte[] samples, int samplesOffset,
            int samplesLength, byte[] data, int dataOffset);
    
    private native int init(int mode);
    
    private Codec() {
        System.loadLibrary("ilbc-codec");
        init(30);
    }
    
    static public Codec instance() {
        return INSTANCE;
    }
}
