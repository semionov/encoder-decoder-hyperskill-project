package correcter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        byte[] text;
        byte[] encodedText;
        int[] perBiteArray;
        int[][] perBite2dArray;

        System.out.println("Write a mode:");
        switch (scanner.nextLine()) {

            case "encode":
                text = readFromFile("send.txt");
                writeToFile(text, "forme.txt");
                perBite2dArray = toPerBite2dArray(text);
                //perBiteArray = encodeBites(perBite2dArray);
                perBiteArray = encodeBitesHammingCode(perBite2dArray);
                encodedText = fromPerBiteArray(perBiteArray, false);
                writeToFile(encodedText, "encoded.txt");
                break;

            case "send":
                encodedText = readFromFile("encoded.txt");
                switchOneBite(encodedText);
                writeToFile(encodedText, "received.txt");
                break;

            case "decode":
                encodedText = readFromFile("received.txt");
                perBite2dArray = toPerBite2dArray(encodedText);
                //perBiteArray = decodeBites(perBite2dArray);
                perBiteArray = decodeBitesHammingCode(perBite2dArray);
                text = fromPerBiteArray(perBiteArray, true);
                writeToFile(text, "decoded.txt");
                break;

            default:
                break;
        }
        scanner.close();
    }

    public static byte[] readFromFile(String path) throws IOException {
        File file = new File(path);
        byte[] allBytes = Files.readAllBytes(file.toPath());

        return allBytes;
    }

    public static void writeToFile(byte[] text, String path) throws IOException {
        OutputStream outputStream = new FileOutputStream(path, false);
        outputStream.write(text);
        outputStream.close();
    }

    public static void switchOneBite(byte[] text) {
        Random random = new Random();

        for (int i = 0; i < text.length; i++) {
            text[i] ^= 1 << random.nextInt(7);
        }
    }

    public static int[][] toPerBite2dArray(byte[] text) {

        String[] toBinaryString = new String[text.length];
        int[][] perBite2dArray = new int[text.length][8];

        for (int i = 0; i < text.length; i++) {
            toBinaryString[i] = Integer.toBinaryString(text[i] & 255 | 256).substring(1);
        }

        for (int i = 0; i < perBite2dArray.length; i++) {
            for (int j = 0; j < perBite2dArray[i].length; j++) {
                perBite2dArray[i][j] = toBinaryString[i].charAt(j) - '0';
            }
        }
        return perBite2dArray;
    }

    public static byte[] fromPerBiteArray(int[] perBiteArray, boolean decoding) {

        int count = 0;
        int secondCount = 0;
        String character = "";
        int lengthOfCharArray;
        int remainder = perBiteArray.length % 8;
        int bitWisorOfLastByte;

        if (remainder == 0) {
            lengthOfCharArray = perBiteArray.length / 8;
        } else {
            lengthOfCharArray = perBiteArray.length / 8 + 1;
        }
        byte[] perBiteByteArray = new byte[lengthOfCharArray];

        for (int bite : perBiteArray) {
            if (count == 8) {
                perBiteByteArray[secondCount] = (byte) Integer.parseInt(character, 2);
                character = "";
                secondCount++;
                count = 0;
            }
            character += Integer.toString(bite);
            count++;
        }

        int d = perBiteArray.length - remainder;
        if (remainder == 6) {
            bitWisorOfLastByte = perBiteArray[d] ^ perBiteArray[d + 2] ^ perBiteArray[d + 4];
            character = character + bitWisorOfLastByte + bitWisorOfLastByte;
        } else if (remainder == 4) {
            bitWisorOfLastByte = perBiteArray[d] ^ perBiteArray[d + 2];
            character = character + "00" + bitWisorOfLastByte;
        } else if (remainder == 2) {
            bitWisorOfLastByte = perBiteArray[d];
            character = character + "0000" + bitWisorOfLastByte;
        }

        perBiteByteArray[secondCount] = (byte) Integer.parseInt(character, 2);
        return perBiteByteArray;
    }

    public static int[] encodeBites(int[][] perBite2dArray) {

        int lengthForArray;
        if ((perBite2dArray.length * perBite2dArray[0].length * 8) % 3 == 0) {
            lengthForArray = (perBite2dArray.length * perBite2dArray[0].length * 8) / 3;
        } else {
            lengthForArray = (perBite2dArray.length * perBite2dArray[0].length * 8) / 3 + 1;
        }

        int[] encodedPerBiteArray = new int[lengthForArray];
        int count = 0;
        int secondCount = 0;
        int bitWiseOR;

        for (int i = 0; i < perBite2dArray.length; i++) {
            for (int j = 0; j < perBite2dArray[i].length; j++) {
                encodedPerBiteArray[count] = perBite2dArray[i][j];
                encodedPerBiteArray[count + 1] = perBite2dArray[i][j];
                count += 2;
                secondCount++;
                if (secondCount == 3) {
                    bitWiseOR = encodedPerBiteArray[count - 1] ^
                        encodedPerBiteArray[count - 3] ^
                        encodedPerBiteArray[count - 5];
                    encodedPerBiteArray[count] = bitWiseOR;
                    encodedPerBiteArray[count + 1] = bitWiseOR;
                    count += 2;
                    secondCount = 0;
                }
            }
        }
        return encodedPerBiteArray;
    }

    public static int[] decodeBites(int[][] perBite2dArray) {

        int oneByteCounter = 0;
        int[] oneByte = new int[perBite2dArray[0].length / 2];
        int damagedBite = 0;
        int sum = 0;

        int lengthForDecodedPerBiteArray;
        if ((perBite2dArray.length * 3) % 8 != 0) {
            lengthForDecodedPerBiteArray = -1;
        } else {
            lengthForDecodedPerBiteArray = 0;
        }
        lengthForDecodedPerBiteArray += (perBite2dArray.length * perBite2dArray[0].length) / 2
            - (perBite2dArray.length * perBite2dArray[0].length) / 8;

        int[] decodedPerBiteArray = new int[lengthForDecodedPerBiteArray];
        int resultCounter = 0;

        for (int i = 0; i < perBite2dArray.length; i++) {
            for (int j = 1; j < perBite2dArray[i].length; j += 2) {
                if (perBite2dArray[i][j] == perBite2dArray[i][j - 1]) {
                    oneByte[oneByteCounter] = perBite2dArray[i][j];
                } else {
                    oneByte[oneByteCounter] = 0;
                    damagedBite = oneByteCounter;
                }
                oneByteCounter++;
            }

            for (int m = 0; m < oneByte.length; m++) {
                sum += oneByte[m];
            }

            if (sum == 0 || sum == 2) {
                oneByte[damagedBite] = 0;
            } else {
                oneByte[damagedBite] = 1;
            }

            oneByteCounter = 0;
            damagedBite = 0;
            sum = 0;

            for (int n = 0; n < oneByte.length - 1; n++) {
                if (resultCounter < decodedPerBiteArray.length) {
                    decodedPerBiteArray[resultCounter] = oneByte[n];
                    resultCounter++;
                }
            }
        }
        return decodedPerBiteArray;
    }

    public static int[] encodeBitesHammingCode(int[][] perBite2dArray) {

        int[] encodedPerBiteArray = new int[perBite2dArray.length * perBite2dArray[0].length * 2];
        int count = 0;
        int secondCount = 0;

        for (int i = 0; i < perBite2dArray.length; i++) {
            for (int j = 0; j < 2; j++) {
                encodedPerBiteArray[count] = perBite2dArray[i][secondCount] ^ perBite2dArray[i][secondCount + 1] ^ perBite2dArray[i][secondCount + 3];
                encodedPerBiteArray[count + 1] = perBite2dArray[i][secondCount] ^ perBite2dArray[i][secondCount + 2] ^ perBite2dArray[i][secondCount + 3];
                encodedPerBiteArray[count + 2] = perBite2dArray[i][secondCount];
                encodedPerBiteArray[count + 3] = perBite2dArray[i][secondCount + 1] ^ perBite2dArray[i][secondCount + 2] ^ perBite2dArray[i][secondCount + 3];
                encodedPerBiteArray[count + 4] = perBite2dArray[i][secondCount + 1];
                encodedPerBiteArray[count + 5] = perBite2dArray[i][secondCount + 2];
                encodedPerBiteArray[count + 6] = perBite2dArray[i][secondCount + 3];
                encodedPerBiteArray[count + 7] = 0;
                secondCount += perBite2dArray[0].length / 2;
                count += perBite2dArray[0].length;
            }
            secondCount = 0;
        }
        return encodedPerBiteArray;
    }

    public static int[] decodeBitesHammingCode(int[][] perBite2dArray) {

        int[] decodedPerBiteArray = new int[perBite2dArray.length * perBite2dArray[0].length / 2];
        int count = 0;
        int damagedBytecount = 0;

        for (int i = 0; i < perBite2dArray.length; i++) {
            if (perBite2dArray[i][0] != (perBite2dArray[i][2] ^ perBite2dArray[i][4] ^ perBite2dArray[i][6])) {
                damagedBytecount += 1;
            }
            if (perBite2dArray[i][1] != (perBite2dArray[i][2] ^ perBite2dArray[i][5] ^ perBite2dArray[i][6])) {
                damagedBytecount += 2;
            }
            if (perBite2dArray[i][3] != (perBite2dArray[i][4] ^ perBite2dArray[i][5] ^ perBite2dArray[i][6])) {
                damagedBytecount += 4;
            }

            damagedBytecount -= 1;
            if (damagedBytecount > 1) {
                perBite2dArray[i][damagedBytecount] ^= 1;
            }
            damagedBytecount = 0;

            decodedPerBiteArray[count] = perBite2dArray[i][2];
            decodedPerBiteArray[count + 1] = perBite2dArray[i][4];
            decodedPerBiteArray[count + 2] = perBite2dArray[i][5];
            decodedPerBiteArray[count + 3] = perBite2dArray[i][6];
            count += perBite2dArray[0].length / 2;
        }
        return decodedPerBiteArray;
    }
}