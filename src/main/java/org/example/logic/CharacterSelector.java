package org.example.logic;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

public class CharacterSelector {
    public static final int[] LETTER_VALUES = {
            4, // A
            2, // B
            2, // C
            2, // D
            3, // E
            2, // F
            2, // G
            2, // H
            3, // I
            2, // J
            2, // K
            2, // L
            2, // M
            2, // N
            3, // O
            2, // P
            2, // Q
            2, // R
            2, // S
            2, // T
            3, // U
            2, // V
            1, // W
            2, // X
            2, // Y
            1  // Z
    };

    public static char getRandomChar() {
        Random random = new Random();
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;

        int i = 0;

        for (i = 0; i < 26; i++) {
            cumulativeProbability += (double) LETTER_VALUES[i] / 32;
            if (randomValue <= cumulativeProbability)
                break;
        }

        return (char) ('A'+i);
    }


    public static Hashtable<Character, Integer> gen() {
        char chararacter;
        Hashtable<Character,Integer> characters=new Hashtable<>();
        for (int i = 0; i < 8; i++) {
            chararacter = CharacterSelector.getRandomChar();
            if (characters.get(chararacter) == null) characters.put(chararacter, 1);
            else characters.put(chararacter, (characters.get(chararacter)) + 1);
        }
        return characters;
    }


    public static float getScore(String word){
        float totalScore = 0;

        for (char letter : word.toCharArray()) totalScore += Math.abs(5-LETTER_VALUES[letter-97]); // 97 is ascii for a

        return totalScore/26;
    }
}
