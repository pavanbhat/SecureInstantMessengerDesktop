/**
 * Foundations of Computer Networks
 * Term Project 
 * Submitted by - Swapnil Kamat(snk6855@rit.edu), Pavan Bhat(pxb8715@rit.edu)
 */

package client;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Random;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Secure {
    
    /**
     * Generates the RSA private and public keys on registration
     */
    protected static void generateRSAKeys(){
		BigInteger p = BigInteger.probablePrime(256, new Random(System.currentTimeMillis()));
		BigInteger q = BigInteger.probablePrime(256, new Random(System.currentTimeMillis() * 10));
		BigInteger n = p.multiply(q);
		BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		BigInteger e = new BigInteger("" + (new Random().nextInt(200)));
		while (true) {
                    if (phi.gcd(e).equals(BigInteger.ONE)) {
                        break;
                    }
                    e = e.add(BigInteger.ONE);
		}
		BigInteger d = e.modInverse(phi);

		// public key (n, e)
                Client.pub_e = e.toString();
                Client.pub_n = n.toString();

		// private key (n, d)
                Client.pri_d = d.toString();
    }
    
    /**
     * Encrypts the plain text
     * @param text text to be encrypted
     * @param ns part of public key
     * @param es part of the public key
     * @return
     * @throws UnsupportedEncodingException 
     */
    protected static String encrypt(String text, String ns, String es) throws UnsupportedEncodingException {
		String encrypted = "";
		char[] chars = text.toCharArray();
		String s = ""+convert(text);
                BigInteger n = new BigInteger(ns);
                BigInteger e = new BigInteger(es);
                
		BigInteger cypherText = new BigInteger(""+s).modPow(e, n);
		
		encrypted = cypherText.toString();
                
                String padded = performPadding(encrypted);
                
		return padded;
	}

    /**
     * Decrypts the ciphertext
     * @param text ciphertext to be deciphered
     * @param ns part of the private key
     * @param ds part of the private key
     * @return
     * @throws UnsupportedEncodingException 
     */
    protected static String decrypt(String text, String ns, String ds) throws UnsupportedEncodingException {

            String unPadded = removePadding(text);
            String result = "";

            BigInteger n = new BigInteger(ns);
            BigInteger d = new BigInteger(ds);

            BigInteger decypherText = new BigInteger(unPadded).modPow(d, n);

            result = deconvert(decypherText.toString());

            return result;
    }

    /**
     * Convert the text string to integer format before feeding to the RSA algorithm
     * @param str string to be converted
     * @return 
     */
    private static String convert(String str) {

            int len = str.length();

            int divide = 8;

            String converted = "1";

            for (int i = 0; i < str.length(); i++) {
                    String ascii = "" + (int) str.charAt(i);

                    if (ascii.length() == 2) {
                            converted += "0" + ascii;
                    } else if (ascii.length() == 3) {
                            converted += ascii;
                    }
            }

            return converted+"1";
    }

    /**
     * Convert the integer string received after decrypting by RSA to text format
     * @param str string to be converted
     * @return 
     */
    private static String deconvert(String str) {
            String deconverted = "";
            for (int i = 1; i < str.length()-3; i++) {
                    String asciival = ""+str.charAt(i)+str.charAt(i+1)+str.charAt(i+2);
                    deconverted += ""+(char)Integer.parseInt(asciival);
                    i += 2;
            }
            return deconverted;
    }


    /**
     * used for OAEP algorithm
     * @param src
     * @param srcPos
     * @param dest
     * @param destPos
     * @param len
     * @return 
     */
    public static byte[] copyArray(byte[] src, int srcPos, byte[] dest, int destPos, int len){
            if((destPos+ len -1) < dest.length && (srcPos + len - 1) < src.length){
                       for(int j = 0; j < len; j++){
                               dest[destPos + j] =  src[srcPos + j];
                       }
       }
            return dest;
    }

    private static byte[] maskGenerationFunc(byte[] fixedInp,int inpLen, int len, int offset) {
            byte[] maskedOutput = new byte[len];
            try{
                    byte[] tempData = new byte[inpLen + 4]; 

                    copyArray(fixedInp, offset, tempData, 4, inpLen);
                    int innerOffset = 0;
                    for(int i = 0; (innerOffset < len); i++){
                            tempData[0] = (byte) (i >>> 24);
                            tempData[1] = (byte) (i >>> 16);
                            tempData[2] = (byte) (i >>> 8);
                            tempData[3] = (byte) (i);
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] sha = digest.digest(tempData);
                            if((len - innerOffset) < 32){								       
                                    copyArray(sha, 0, maskedOutput, innerOffset, (len - innerOffset));
                            }else{
                                    copyArray(sha, 0, maskedOutput, innerOffset, 32);
                            }
                            innerOffset += 32;
                    }

            }catch(Exception e){
                    System.err.println(e.getMessage());
            }

            return maskedOutput;
    }

    public static final SecureRandom r = new SecureRandom();

    /**
     * Pad the text using OAEP algorithm
     * @param padMsg message to pad
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static String performPadding(String padMsg) throws UnsupportedEncodingException{

        byte[] msg = padMsg.getBytes("UTF-8");
        int len = msg.length + 65;
        byte[] output = new byte[len];
        try{
                byte[] tempData = new byte[len - 32];
                MessageDigest di = MessageDigest.getInstance("SHA-256");
        byte[] sha = di.digest("MySecret".getBytes("UTF-8"));
                tempData = copyArray(sha, 0, tempData, 0, 32);
                tempData = copyArray(msg, 0, tempData, (len - msg.length - (32 << 1) + 32), msg.length);
                tempData[31+ len - msg.length - (32 << 1)] = 1;

                byte[] fixedInp = new byte[32];
                r.nextBytes(fixedInp);

                byte[] tempDataMask = maskGenerationFunc(fixedInp, 32, len - 32, 0);
                for(int i = 0; i < len - 32; i++){
                        tempData[i] ^= tempDataMask[i];
                }

                byte[] fixedInpMask = maskGenerationFunc(tempData, len - 32, 32, 0);
                for(int j = 0; j < 32; j++){
                        fixedInp[j] ^= fixedInpMask[j];
                }
                output = copyArray(fixedInp, 0, output, 0, 32);
                output = copyArray(tempData, 0, output, 32, len - 32);
        }catch(Exception e){
                System.err.println("Padding error: " +e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : output) {
            sb.append(String.format("%02X", b));
        }


        return sb.toString();
    }

    /**
     * Remove the pad using OAEP algorithm
     * @param padded padded message
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static String removePadding(String padded) throws UnsupportedEncodingException{

        HexBinaryAdapter hba = new HexBinaryAdapter();
        byte[] msg = hba.unmarshal(padded);

        int count = -1;
        byte[] tempData = new byte[msg.length];
        try{
                tempData = copyArray(msg, 0, tempData, 0, msg.length);
                byte[] fixedInpMask = maskGenerationFunc(tempData, msg.length - 32, 32, 32);
                for (int i = 0; i < 32; i++) {
                        tempData[i] ^= fixedInpMask[i];
                }
        byte[] tempDatapMask = maskGenerationFunc(tempData, 32, msg.length - 32, 0);
        for (int i = 32; i < msg.length; i++) {
                        tempData[i] ^= tempDatapMask[i - 32];
                        if(count == -1 && tempData[i] == 1){
                                count = i+1;
                        }
                }

        }catch (Exception e) {
                System.err.println("Remove Padding error: " + e.getMessage());
        }
        byte[] output = new byte[msg.length - count];			

        byte[] unpadded = copyArray(tempData, count, output, 0, msg.length - count);	

        return new String(unpadded, "UTF-8");
    }
    
}
