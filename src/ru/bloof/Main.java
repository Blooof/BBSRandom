package ru.bloof;

import ru.bloof.prng.BBSRandom;

public class Main {
    public static void main(String[] args) {
        BBSRandom rnd = new BBSRandom(32);
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
    }
}
