package ru.bloof.nist;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;
import ru.bloof.prng.BBSRandom;

import java.io.PrintWriter;
import java.util.Random;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class NistTests {
    public static final int BITS_FOR_TEST = 1024;
    public static final int BITS_IN_BYTE = 8;

    public static void main(String[] args) {
        Random rnd = new BBSRandom(128);
        byte[] data = new byte[BITS_FOR_TEST / BITS_IN_BYTE];
        rnd.nextBytes(data);
        try (PrintWriter pw = new PrintWriter(System.out)) {
            frequencyTest(data, pw);
            frequencyBlockTest(data, 32, pw);
            runsTest(data, pw);
            onesLongestRun(data, pw);
        }
    }

    public static void frequencyTest(byte[] bytes, PrintWriter out) {
        int sum = 0;
        for (byte b : bytes) {
            for (int j = 0; j < 8; j++) {
                sum += testBit(b, j) ? 1 : -1;
            }
        }
        double p = Erf.erfc(FastMath.abs(sum) / FastMath.sqrt(bytes.length * BITS_IN_BYTE * 2));
        out.println(p > 0.01 ? "Frequency test passed" : "Frequency test failed");
    }

    public static void runsTest(byte[] bytes, PrintWriter out) {
        int ones = 0;
        for (byte b : bytes) {
            for (int j = 0; j < BITS_IN_BYTE; j++) {
                if (testBit(b, j)) {
                    ones++;
                }
            }
        }
        int bits = bytes.length * BITS_IN_BYTE;
        double pi = 1. * ones / bits;
        if (pi - 0.5 >= 2 / FastMath.sqrt(bits)) {
            out.println("Runs test failed");
            return;
        }
        int sign = -1, v_n = 1;
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < BITS_IN_BYTE; j++) {
                int curSign = testBit(bytes[i], j) ? 1 : -1;
                if (i != 0 && j != 0 && curSign != sign) {
                    v_n++;
                }
                sign = curSign;
            }
        }
        double p = Erf.erfc(FastMath.abs(v_n - 2 * bits * pi * (1 - pi)) / 2 * FastMath.sqrt(2 * bits) * pi * (1 - pi));
        out.println(p >= 0.1 ? "Runs test passed" : "Runs test failed");
    }

    // В блоке 8 бит.
    public static void onesLongestRun(byte[] bytes, PrintWriter out) {
        int bitsInBlock = 8;
        int bits = bytes.length * BITS_IN_BYTE;
        int blocksCount = bits / bitsInBlock;
        if (bits % bitsInBlock != 0) {
            blocksCount++;
        }
        int[] maxRun = new int[blocksCount];
        int block = -1, run = 0;
        for (int i = 0; i < bits; i++) {
            int blockNumber = i / bitsInBlock;
            if (blockNumber != block) {
                run = 0;
            }
            block = blockNumber;
            if (testBit(bytes, i)) {
                run++;
            }
            if (maxRun[blockNumber] < run) {
                maxRun[blockNumber] = run;
            }
        }
        int[] v = new int[4];
        for (int aMaxRun : maxRun) {
            int index;
            switch (aMaxRun) {
                case 0:
                case 1:
                    index = 0;
                    break;
                case 2:
                    index = 1;
                    break;
                case 3:
                    index = 2;
                    break;
                default:
                    index = 3;
                    break;
            }
            v[index]++;
        }
        double chi_sq = 0;
        double r = 16;
        double[] pi = {0.2148, 0.3672, 0.2305, 0.1875};
        for (int i = 0; i < v.length; i++) {
            chi_sq += (v[i] - r * pi[i]) / (r * pi[i]);
        }
        double p = Gamma.regularizedGammaQ(v.length / 2., chi_sq / 2);
        out.println(p > 0.01 ? "OnesLongestRun test passed" : "OnesLongestRun test failed");
    }

    public static boolean testBit(byte[] bytes, int num) {
        int byteNumber = num / BITS_IN_BYTE;
        int bitInByte = num % BITS_IN_BYTE;
        return testBit(bytes[byteNumber], bitInByte);
    }

    public static void frequencyBlockTest(byte[] bytes, int bitsInBlock, PrintWriter out) {
        int blocksCount = bytes.length * BITS_IN_BYTE / bitsInBlock;
        int bitsInBlocks = bitsInBlock * blocksCount;
        int[] ones = new int[blocksCount];
        for (int i = 0; i < bitsInBlocks; i++) {
            int byteNumber = i / BITS_IN_BYTE;
            int bitInByte = i % BITS_IN_BYTE;
            int blockNumber = i / bitsInBlock;
            if (testBit(bytes[byteNumber], bitInByte)) {
                ones[blockNumber]++;
            }
        }
        double chi_sq = 0;
        for (int i = 0; i < blocksCount; i++) {
            chi_sq += FastMath.pow(1. * ones[i] / bitsInBlock - 0.5, 2);
        }
        chi_sq *= 4 * bitsInBlock;
        double p = Gamma.regularizedGammaQ(blocksCount / 2., chi_sq / 2);
        out.println(p > 0.01 ? "Frequency block test passed" : "Frequency block test failed");
    }

    public static boolean testBit(byte value, int bit) {
        return ((value >> bit) & 1) == 1;
    }
}
