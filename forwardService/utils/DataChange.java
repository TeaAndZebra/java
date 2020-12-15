package forwardService.utils;

public class DataChange {
    public static  byte[] IntToBytes(int data){
        byte[] bytes = new byte[4];
        for(int i =0;i<4;i++){
            bytes[i] = (byte)(data>>(3-i)*8);
           // System.out.println(bytes[i]);
        }

        return bytes;
    }
    public static  byte[] IntsToBytes(int [] data){
        byte[] bytes = new byte[data.length];
        for(int i =0;i<data.length;i++){
            bytes[i] = (byte)data[i];
        }
        return bytes;
    }
    public static int bytes2Int(byte[] bytes )
    {
        int a =0;
        for(int i =0;i<bytes.length;i++){
            a  <<=8;
            a |= (bytes[i]&0xff);
        }
        return a;
    }
    public static int[] IntToHex(int i) {
        int lowByte;
        int highByte;
        lowByte = i&0xff;
        highByte = (i>>8)&0xff;
        int[] HexByte = new int[]{highByte,lowByte};
        //System.out.println(lowByte+"低位"+highByte);
        return HexByte;
    }
    public static byte[] longToBytes(long i){
        byte[] a = new byte[8];
        for(int x = 0;x<8;x++){
            int offset = 64-(x+1)*8;
            a[x] = (byte)((i>>offset)&0xff);
        }
        return a;
    }


}