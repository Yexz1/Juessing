package org.example.draw;

import org.example.back.DB;
import org.example.logic.*;


import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.*;


public class Frame extends JFrame {
    Hashtable<Integer, ArrayList<String>> context = new Hashtable<>();
    // les key `Integer` sont les diffirent niveau
    // la primier valuer  dans les valuer(`ArrayList<String>`) est la la chaine de caractere aleatoire
    // la derniere valuer  dans les valuer(`ArrayList<String>`) est le score

    int current_context = 0; // Indice du contexte actuel

    int fontSize = 50;
    JPanel keys;
    JButton j;
    JPanel words;
    JPanel word;
    JButton addBtn;
    JButton next;
    JButton previous;
    JPanel panel;
    float score = 0;
    JLabel scoreLabel;
    JTextArea contextNumbers;

    Hashtable<Character, Integer> characters;


    public Frame() {
        setLayout(new BorderLayout(20, 15));

        // next button
        next = new JButton("next");
        next.addActionListener(new MoveDiffCharacters());
        add(next, BorderLayout.EAST);


        // previous button
        previous = new JButton("previous");
        previous.addActionListener(new MoveDiffCharacters());
        add(previous, BorderLayout.WEST);


        // Panneau pour les touches
        keys = new JPanel();
        keys.setLayout(new FlowLayout());
        add(keys, BorderLayout.NORTH);


        // Panneau pour les mots
        words = new JPanel();
        words.setLayout(new GridLayout(10, 1));


        // Panneau pour une chaque seul mot
        word = new JPanel(new GridLayout(1, 10));
        words.add(word);


        // Bouton pour ajouter un mot
        ImageIcon imageIcon = new ImageIcon("src/main/java/org/example/assets/add32.png");
        addBtn = new JButton(imageIcon);
        addBtn.setBorderPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(false);
        addBtn.addActionListener(new CheckWord());
        words.add(addBtn);
        add(words, BorderLayout.CENTER);


        // Étiquette pour afficher le score
        Font f = new Font("serif", Font.PLAIN, fontSize);
        scoreLabel = new JLabel("score = " + score);
        scoreLabel.setFont(f);
        panel = new JPanel(new BorderLayout());
        panel.add(scoreLabel, BorderLayout.NORTH);
        contextNumbers = new JTextArea();
        panel.add(contextNumbers, BorderLayout.WEST);
        add(panel, BorderLayout.SOUTH);


        prepareContext(); // Préparation du contexte,
        load_keys(); // Chargement des touches d'apres le contexte


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // je sauvgarde le context lorseque tu quitter l application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                unloadContext();
            }
        });
        setResizable(false);
        pack();
        show();
    }


    private void prepareContext() {
        String filePath = "hashtable.ser"; // Chemin du fichier de sauvegarde
        File file = new File(filePath);
        if (file.exists()) {
            loadContext(); // Chargement du contexte si le fichier existe
        } else { // Création d'un nouveau contexte
            context.put(current_context, new ArrayList<>());
            characters = CharacterSelector.gen(); // Génération de caractères aléatoires
            context.get(current_context).add(charactersToString()); // ajoute les caracteres dans le context
            context.get(current_context).add(String.valueOf(score)); // ajouter le score, mise en 0
        }
        contextNumbers.setText(current_context + 1 + "/" + context.size()); // afficher le niveau courant
    }

    // retourne une chaine de caractere des caractere aleatoire d'apres un hashtable
    private String charactersToString() {
        StringBuilder ret = new StringBuilder();
        for (Character c : characters.keySet()) {
            int repeats = characters.get(c);
            for (int i = 0; i < repeats; i++)
                ret.append(c.toString());
        }
        return ret.toString();
    }

    // afficker les caractere aleatoire
    private void load_keys() {
        for (Character c : characters.keySet()) {
            int repeats = characters.get(c);
            for (int i = 0; i < repeats; i++) {
                j = new JButton(c.toString());
                j.addActionListener(new AddChar());
                keys.add(j);
            }
        }

        keys.repaint();
        keys.revalidate();
    }


    // s'il existe une execution precedent, et sauvgarde d;etat, lire d'apres le ficheir
    private void loadContext() {
        // Deserialize
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("hashtable.ser"))) {
            context = (Hashtable<Integer, ArrayList<String>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // comme dans (ligne 21), dernier valeur est le score
        score = Float.parseFloat(context.get(current_context).getLast());
        scoreLabel.setText(String.valueOf(score));

        // cree un hash table d'apres la chaine de caractere des caractere aleatoire sauvgarder dans le fichier
        recreateCharacters();
        recreateGuessed();


    }

    // afficher les mot deviner et le score lors de passation des contexte (niveau)
    private void recreateGuessed() {
        // suprimer tout les mot afficher
        word.removeAll();
        ArrayList<String> geussed = context.get(current_context);
        for (int i = 1; i < geussed.size() - 1; i++) {
            for (int j = 0; j < geussed.get(i).length(); j++) {
                // afficher les  nouveaux mot pour cette contexte
                JButton button = new JButton(String.valueOf(geussed.get(i).charAt(j)));
                button.setEnabled(false);
                word.add(button);

            }
            word.revalidate();
            word.repaint();
            words.add(word);
        }
        word = new JPanel(new GridLayout(1, 10));
        words.add(word);
        // ajouter la boutton d'ajoute
        words.add(addBtn);
        words.revalidate();
        words.repaint();
    }

    // cree un hash table d'apres les caracteres aleatoire du contexte courant
    private void recreateCharacters() {
        characters = new Hashtable<>();
        // comme dans (ligne 22) le premier valeur est la chaine des caracteres aleatoire
        String charactersString = context.get(current_context).getFirst();
        for (int i = 0; i < charactersString.length(); i++) {
            if (characters.get(charactersString.charAt(i)) == null) {
                characters.put(charactersString.charAt(i), 1);
            } else {
                characters.put(charactersString.charAt(i), characters.get(charactersString.charAt(i)) + 1);
            }

        }

    }

    // suvgarder le hashtable du context dans un ficheier
    private void unloadContext() {
        // Serialize
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("hashtable.ser"))) {
            oos.writeObject(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // Classe interne pour gérer l'ajout ou le retrait de caractères
    private class AddChar implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            // Récupération du bouton qui a déclenché l'événement
            JButton button = (JButton) e.getSource();

            // Vérification si le bouton est actuellement dans le panneau des mots
            // d'ou il faut le supprimer de panneau du mot et le retourne dans les touches `keys`
            if (button.getParent() == word) {
                word.remove(button);
                keys.add(button);
            } else {
                // Sinon, on l'ajoute au panneau des mots et on le retire du panneau des touches `keys`
                word.add(button);
                keys.remove(button);
            }

            // Mise à jour de l'affichage des panneaux
            keys.revalidate();
            keys.repaint();
            word.revalidate();
            word.repaint();

        }
    }

    // Classe interne pour vérifier si le mot deviné est correct
    private class CheckWord implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder wordString = new StringBuilder();

            // Parcours des composants du panneau des mots pour récupérer les caractères
            for (Component comp : word.getComponents()) {
                JButton button = (JButton) comp;
                wordString.append(button.getText().toLowerCase());
            }
            try {
                // Vérification si le mot existe déjà dans le contexte
                boolean isExist = checkExist(wordString.toString());

                // Vérification si le mot est dans la base de données et n'a pas encore été deviné
                if (DB.searchWord(wordString.toString()) && !isExist) {

                    // Désactivation des boutons du mot deviné
                    // pour ne les click pas et les retourne dans la panneau des touches
                    for (Component comp : word.getComponents()) {
                        JButton button = (JButton) comp;
                        button.setEnabled(false);
                    }
                    // Ajout du mot deviné au contexte courant
                    // dans la première position car la `0` occupee par les characters aleatoir du context
                    context.get(current_context).add(1, wordString.toString());

                    // ajouter un panneau pour le mot suivant à deviner
                    word = new JPanel(new GridLayout(1, 10));
                    // repositioner le bouton d'ajout
                    words.remove(addBtn);
                    words.add(word);
                    words.add(addBtn);

                    // Retrait de tous les boutons du panneau des touches
                    keys.removeAll();
                    // Chargement des toutes les touches
                    load_keys();


                    float oldscore = score; // Sauvegarde de l'ancien score, just pour l'affichage
                    score = CharacterSelector.getScore(wordString.toString()); // calcuter le score du mot
                    scoreLabel.setText("score = " + oldscore + " + " + score);
                    score += oldscore;
                    context.get(current_context).set(context.get(current_context).size() - 1, String.valueOf(score)); // modifier le score dans le context
                } else {
                    if (wordString.toString().isEmpty()) scoreLabel.setText("mot vide");
                    else if (isExist) scoreLabel.setText("mot deja deviner");
                    else scoreLabel.setText("pas une mot");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        }

        private boolean checkExist(String string) {
            ArrayList<String> guessed = context.get(current_context);
            for (int i = 1; i < guessed.size() - 1; i++) //remove the first and the last
                if (string.equals(guessed.get(i)))
                    return true;
            return false;
        }
    }



    // Classe interne pour gérer le changement de contexte
    private class MoveDiffCharacters implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            // Vérification si le bouton "next" a été cliqué
            if ( e.getSource() == next ) current_context++; // Incrémentation du contexte actuel
            else if (current_context==0) return; // Si le contexte actuel est déjà à 0, ne rien faire
            else current_context--; // Décrémentation du contexte actuel

            // Suppression de tous les composants du panneau des mots et le redessiner
            words.removeAll();
            words.revalidate();
            words.repaint();


            // Vérification si le contexte actuel est nul
            // ca veut dire i faut genere un nouveau context (generer des caracteres aleatoire)
            if (context.get(current_context) == null) {
                System.out.println("here");
                context.put(current_context, new ArrayList<>());
                characters = null;
                characters = CharacterSelector.gen();
                System.out.println(characters);
                context.get(current_context).add(charactersToString());
                context.get(current_context).add(String.valueOf(0));

                word = new JPanel(new GridLayout(1, 10));

                words.add(word);
                words.add(addBtn);
                words.revalidate();
                words.repaint();



            } else {
                // Si le contexte existe, recréer les caractères et les mots devinés
                recreateCharacters();
                recreateGuessed();

            }

            // Mise à jour de l'affichage du score et du numéro de contexte
            scoreLabel.setText(context.get(current_context).getLast());
            score = Float.parseFloat(scoreLabel.getText());
            contextNumbers.setText(current_context + 1 + "/" + context.size());


            keys.removeAll(); // Retrait de tous les boutons du panneau des touches
            load_keys(); // Chargement des touches a nouveau


        }
    }
}