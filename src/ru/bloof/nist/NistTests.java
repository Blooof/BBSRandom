package ru.bloof.nist;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;
import ru.bloof.prng.BBSRandom;
import ru.bloof.prng.DeviceRandom;

import java.io.PrintWriter;
import java.util.Random;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class NistTests {
    public static final int BITS_FOR_TEST = 6272;
    public static final int BITS_FOR_MATRIX_TEST = 38912;
    public static final int BITS_IN_BYTE = 8;

    public static void main(String[] args) {
        Random rnd = createBBSRandom();
        byte[] data = new byte[BITS_FOR_TEST / BITS_IN_BYTE];
        rnd.nextBytes(data);
        try (PrintWriter pw = new PrintWriter(System.out)) {
            frequencyTest(data, pw);
            frequencyBlockTest(data, 32, pw);
            runsTest(data, pw);
            onesLongestRun(data, pw);

            data = new byte[BITS_FOR_MATRIX_TEST / BITS_IN_BYTE];
            rnd.nextBytes(data);
            binaryMatrixRankTest(data, pw);
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
                if (curSign != sign) {
                    if (!(i == 0 && j == 0)) {
                        v_n++;
                    }
                }
                sign = curSign;
            }
        }
        double p = Erf.erfc(FastMath.abs(v_n - 2 * bits * pi * (1 - pi)) / (2 * FastMath.sqrt(2 * bits) * pi * (1 - pi)));
        out.println(p >= 0.1 ? "Runs test passed" : "Runs test failed");
    }

    // В блоке 128 бит.
    public static void onesLongestRun(byte[] bytes, PrintWriter out) {
        int bitsInBlock = 128;
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
            } else {
                run = 0;
            }
            if (maxRun[blockNumber] < run) {
                maxRun[blockNumber] = run;
            }
        }

        int k = 5;
        double chi_sq = 0;
        int[] v = new int[k + 1];
        for (int aMaxRun : maxRun) {
            int index;
            switch (aMaxRun) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    index = 0;
                    break;
                case 5:
                    index = 1;
                    break;
                case 6:
                    index = 2;
                    break;
                case 7:
                    index = 3;
                    break;
                case 8:
                    index = 4;
                    break;
                default:
                    index = 5;
                    break;
            }
            v[index]++;
        }
        double[] pi = {0.1174, 0.2430, 0.2493, 0.1752, 0.1027, 0.1124};
        for (int i = 0; i <= k; i++) {
            chi_sq += FastMath.pow(v[i] - blocksCount * pi[i], 2) / (blocksCount * pi[i]);
        }
        double p = Gamma.regularizedGammaQ(k / 2., chi_sq / 2);
        out.println(p > 0.01 ? "OnesLongestRun test passed" : "OnesLongestRun test failed");
    }

    // M = Q = 32
    public static void binaryMatrixRankTest(byte[] bytes, PrintWriter out) {
        int M, Q = M = 32;
        int matrixSize = M * Q;
        int bits = bytes.length * BITS_IN_BYTE;
        int matricesCount = bits / matrixSize;
        int F_M = 0, F_M1 = 0;
        for (int i = 0; i < matricesCount; i++) {
            int[][] data = new int[M][Q];
            for (int j = 0; j < M; j++) {
                for (int k = 0; k < Q; k++) {
                    int bit = i * matrixSize + j * Q + k;
                    data[j][k] = testBit(bytes, bit) ? 1 : 0;
                }
            }
            int rank = computeRank(data);
            if (rank == M) {
                F_M++;
            } else if (rank == M - 1) {
                F_M1++;
            }
        }

        double[] pi = new double[]{0.2888, 0.5776, 0.1336};
        double chi_sq = 0;
        chi_sq += FastMath.pow(F_M - pi[0] * matricesCount, 2) / (pi[0] * matricesCount);
        chi_sq += FastMath.pow(F_M1 - pi[1] * matricesCount, 2) / (pi[1] * matricesCount);
        chi_sq += FastMath.pow(matricesCount - F_M - F_M1 - pi[2] * matricesCount, 2) / (pi[2] * matricesCount);
        double p = Gamma.regularizedGammaQ(1, chi_sq / 2);
        out.println(p > 0.01 ? "BinaryMatrixRank test passed" : "BinaryMatrixRank test failed");
    }

    /**
     * Считает ранг квадратной двоичной матрицы.
     */
    private static int computeRank(int[][] data) {
        int M = data.length;
        for (int i = 0; i < M - 1; i++) {
            if (data[i][i] == 1) {
                rowOperations(true, i, M, data);
            } else if (findUnitAndSwap(false, i, M, data)) {
                rowOperations(false, i, M, data);
            }
        }
        for (int i = M - 1; i > 0; i--) {
            if (data[i][i] == 1) {
                rowOperations(false, i, M, data);
            } else if (findUnitAndSwap(false, i, M, data)) {
                rowOperations(false, i, M, data);
            }
        }
        return rank(M, data);
    }

    private static int rank(int q, int[][] data) {
        int rank = q;
        for (int i = 0; i < q; i++) {
            boolean allZeroes = true;
            for (int j = 0; j < q; j++) {
                if (data[i][j] == 1) {
                    allZeroes = false;
                    break;
                }
            }
            if (allZeroes) {
                rank--;
            }
        }
        return rank;
    }

    private static boolean findUnitAndSwap(boolean forward, int i, int m, int[][] data) {
        if (forward) {
            int index = i + 1;
            while (index < m && data[index][i] == 0) {
                index++;
            }
            if (index < m) {
                swapRows(i, index, data);
                return true;
            }
        } else {
            int index = i - 1;
            while (index >= 0 && data[index][i] == 0) {
                index--;
            }
            if (index >= 0) {
                swapRows(i, index, data);
                return true;
            }
        }
        return false;
    }

    private static void swapRows(int a, int b, int[][] data) {
        int[] value = data[a];
        data[a] = data[b];
        data[b] = value;
    }

    private static void rowOperations(boolean forward, int i, int m, int[][] data) {
        if (forward) {
            for (int j = i + 1; j < m; j++) {
                if (data[j][i] == 1) {
                    for (int k = i; k < m; k++) {
                        data[j][k] = (data[j][k] + data[i][k]) % 2;
                    }
                }
            }
        } else {
            for (int j = i - 1; j >= 0; j--) {
                if (data[j][i] == 1) {
                    for (int k = 0; k < m; k++) {
                        data[j][k] = (data[j][k] + data[i][k]) % 2;
                    }
                }
            }
        }
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
        int intValue = value < 0 ? 256 + value : value;
        return ((intValue >> bit) & 1) == 1;
    }

    @SuppressWarnings("unused")
    private static Random createMersenneTwisterRandom() {
        return new Random() {
            MersenneTwister twister = new MersenneTwister();

            @Override
            public void nextBytes(byte[] data) {
                twister.nextBytes(data);
            }
        };
    }

    @SuppressWarnings("unused")
    private static BBSRandom createBBSRandom() {
        return new BBSRandom(64, new DeviceRandom());
    }
}
