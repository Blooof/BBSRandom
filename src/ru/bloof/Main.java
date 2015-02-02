package ru.bloof;

import ru.bloof.prng.BBSRandom;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        BBSRandom rnd = new BBSRandom(32, new Random());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
        System.out.println(rnd.nextInt());
    }
}
