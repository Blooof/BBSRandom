package ru.bloof.prng;

import java.math.BigInteger;
import java.util.Random;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class BBSRandom extends Random {
    private static final BigInteger BIG_THREE = BigInteger.valueOf(3);
    private static final BigInteger BIG_FOUR = BigInteger.valueOf(4);
    private static final BigInteger BIG_TWO = BigInteger.valueOf(2);
    private final BigInteger n;
    private BigInteger state;

    public BBSRandom(int bits, Random initRnd) {
        try {
            n = generateN(bits, initRnd);
            byte[] seed = new byte[bits / 8];
            initRnd.nextBytes(seed);
            setSeed(seed);
        } finally {
            if (initRnd instanceof DeviceRandom) {
                ((DeviceRandom) initRnd).close();
            }
        }
    }

    private static BigInteger generateN(int bits, Random rand) {
        BigInteger p = getPrime(bits / 2, rand);
        BigInteger q = getPrime(bits / 2, rand);
        while (p.equals(q)) {
            q = getPrime(bits, rand);
        }
        return p.multiply(q);
    }

    private static BigInteger getPrime(int bits, Random rand) {
        BigInteger p;
        do {
            p = new BigInteger(bits, 10, rand);
        } while (!p.mod(BIG_FOUR).equals(BIG_THREE));
        return p;
    }

    private void setSeed(byte[] seed) {
        BigInteger s = new BigInteger(1, seed);
        state = s.mod(n);
    }

    @Override
    protected int next(int bits) {
        int result = 0;
        for (int i = 0; i < bits; i++) {
            state = state.modPow(BIG_TWO, n);
            result = (result << 1) | (state.testBit(0) ? 1 : 0);
        }
        return result;
    }
}
