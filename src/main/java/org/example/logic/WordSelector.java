package org.example.logic;

import org.example.back.DB;

import java.sql.SQLException;
import java.util.Random;

public class WordSelector {
    public static String select(){
        //WordInit.init();
        Random random = new Random();
        int min = 1;
        int max = 336528;

        // Generate random number between min (inclusive) and max (inclusive)
        int randomNumber = random.nextInt(max - min + 1) + min;
        //System.out.println(randomNumber);
        try {
            return DB.getWord(randomNumber);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
