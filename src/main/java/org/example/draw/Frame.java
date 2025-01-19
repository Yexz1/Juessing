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
    // the key is the current random chars
    // vals - 0 is the random char gen - then the geussed word - last is the score
    Hashtable<Integer, ArrayList<String>> context = new Hashtable<>();
    int current_context = 0;

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

        // keys panel inside it button on random chars
        keys = new JPanel();
        keys.setLayout(new FlowLayout());


        add(keys, BorderLayout.NORTH);

        // words panel
        words = new JPanel();
        words.setLayout(new GridLayout(10, 1));


        // // each word is its own panel, and each char is a button inside the panel
        word = new JPanel(new GridLayout(1, 10));

        words.add(word);

        // // add button : to add new word space is the word above is correct
        ImageIcon imageIcon = new ImageIcon("src/main/java/org/example/assets/add32.png");

        addBtn = new JButton(imageIcon);
        addBtn.setBorderPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(false);
        addBtn.addActionListener(new CheckWord());

        words.add(addBtn);


        add(words, BorderLayout.CENTER);


        Font f = new Font("serif", Font.PLAIN, fontSize);
        scoreLabel = new JLabel("score = " + score);
        scoreLabel.setFont(f);

        panel = new JPanel(new BorderLayout());

        panel.add(scoreLabel, BorderLayout.NORTH);

        contextNumbers = new JTextArea();
        panel.add(contextNumbers, BorderLayout.WEST);


        add(panel, BorderLayout.SOUTH);

        prepareContext();
        load_keys();


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        String filePath = "hashtable.ser";
        File file = new File(filePath);
        if (file.exists()) {
            loadContext();
        } else {
            context.put(current_context, new ArrayList<>());
            characters = CharacterSelector.gen();
            context.get(current_context).add(charactersToString());
            context.get(current_context).add(String.valueOf(score));
        }
        contextNumbers.setText(current_context + 1 + "/" + context.size());
    }

    private String charactersToString() {
        StringBuilder ret = new StringBuilder();
        for (Character c : characters.keySet()) {
            int repeats = characters.get(c);
            for (int i = 0; i < repeats; i++)
                ret.append(c.toString());
        }
        return ret.toString();
    }


    private void load_keys() {

        // add all chars to keys
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

    private void loadContext() {
        // Deserialize
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("hashtable.ser"))) {
            context = (Hashtable<Integer, ArrayList<String>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        score = Float.parseFloat(context.get(current_context).getLast());
        scoreLabel.setText(String.valueOf(score));


        recreateCharacters();
        recreateGuessed();


    }

    private void recreateGuessed() {
        word.removeAll(); // bech ki t7ot chars w matvalidich maya93douch
        ArrayList<String> geussed = context.get(current_context);
        for (int i = 1; i < geussed.size() - 1; i++) {
            for (int j = 0; j < geussed.get(i).length(); j++) {
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
        words.add(addBtn);
        words.revalidate();
        words.repaint();
    }

    private void recreateCharacters() {
        characters = new Hashtable<>();
        String charactersString = context.get(current_context).getFirst();
        for (int i = 0; i < charactersString.length(); i++) {
            if (characters.get(charactersString.charAt(i)) == null) {
                characters.put(charactersString.charAt(i), 1);
            } else {
                characters.put(charactersString.charAt(i), characters.get(charactersString.charAt(i)) + 1);
            }

        }

    }


    private void unloadContext() {
        // Serialize
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("hashtable.ser"))) {
            oos.writeObject(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class AddChar implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            JButton button = (JButton) e.getSource();

            if (button.getParent() == word) {
                word.remove(button);
                keys.add(button);
            } else {
                word.add(button);
                keys.remove(button);
            }

            keys.revalidate();
            keys.repaint();
            word.revalidate();
            word.repaint();

        }
    }


    private class CheckWord implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder wordString = new StringBuilder();
            for (Component comp : word.getComponents()) {
                JButton button = (JButton) comp;
                wordString.append(button.getText().toLowerCase());
            }
            try {
                boolean isExist = checkExist(wordString.toString());
                if (DB.searchWord(wordString.toString()) && !isExist) {

                    for (Component comp : word.getComponents()) {
                        JButton button = (JButton) comp;
                        button.setEnabled(false);
                    }
                    context.get(current_context).add(1, wordString.toString());
                    word = new JPanel(new GridLayout(1, 10));
                    words.remove(addBtn);
                    words.add(word);
                    words.add(addBtn);

                    keys.removeAll();
                    load_keys();


                    float oldscore = score;
                    score = CharacterSelector.getScore(wordString.toString());
                    scoreLabel.setText("score = " + oldscore + " + " + score);
                    score += oldscore;
                    context.get(current_context).set(context.get(current_context).size() - 1, String.valueOf(score));
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


    private class MoveDiffCharacters implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            if ( e.getSource() == next ) current_context++;
            else if (current_context==0) return;
            else current_context--;

            words.removeAll();
            words.revalidate();
            words.repaint();

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
                recreateCharacters();


                recreateGuessed();

            }

            scoreLabel.setText(context.get(current_context).getLast());
            score = Float.parseFloat(scoreLabel.getText());
            contextNumbers.setText(current_context + 1 + "/" + context.size());

            keys.removeAll();
            //keys.revalidate();
            load_keys();


        }
    }
}