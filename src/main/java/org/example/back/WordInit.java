package org.example.back;
//https://github.com/lorenbrichter/Words/tree/master

import java.io.*;
import java.sql.SQLException;

public class WordInit {
    public static void init() throws IOException {
        String filePath = "src/main/java/org/example/fr";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int i = 0;
            // Boucle pour lire chaque ligne du fichier
            while ((line = br.readLine()) != null) {
                // Ajout du mot à la base de données
                DB.addWord(replaceAccents(line));
                System.out.println(((double)100/336527)*i++ + "%");
            }
        } catch (IOException | SQLException _) {}
    }

    public static String replaceAccents(String str) {
        return str.replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùû]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[îï]", "i")
                .replaceAll("[ÉÈÊË]", "E")
                .replaceAll("[ÀÂÄ]", "A")
                .replaceAll("[ÔÖ]", "O")
                .replaceAll("[ÙÛ]", "U")
                .replaceAll("[ÎÏ]", "I");
    }
}
