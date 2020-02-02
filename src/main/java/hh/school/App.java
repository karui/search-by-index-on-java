package hh.school;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App
{
    private static HashMap<String, HashMap<Integer, ArrayList<Integer>>> index = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
    private static HashMap<Integer, ArrayList<Integer>> inner = new HashMap<Integer, ArrayList<Integer>>();

    public static List<String> splitStringToWords(String string) {
        List<String> words = new ArrayList<String>();
        StringBuilder word = new StringBuilder();
        for (char symbol : string.toLowerCase().toCharArray()) {
            if (Character.isLetter(symbol)) {
                word.append(symbol);
            } else if (symbol == '2' && word.toString().endsWith("b")) {
                word.append(symbol);
            } else if (symbol == '+' && (word.toString().endsWith("c") || word.toString().endsWith("+"))) {
                word.append(symbol);
            } else {
                if (word.length() > 0) {
                    words.add(word.toString());
                    word = new StringBuilder();
                }
            }
        }

        if (word.length() > 0) words.add(word.toString());

        return words;
    }


    public static void addWordsIntoIndex(Integer doc_id, List<String> words) {
        ListIterator<String> words_it = words.listIterator();
        while (words_it.hasNext()) {
//            System.out.println(doc_id + " " + words_it.nextIndex() + " " + words_it.next());
            int word_position = words_it.nextIndex();
            String word = words_it.next();
            if (index.containsKey(word)) {
                if (index.get(word).containsKey(doc_id)) {
                    index.get(word).get(doc_id).add(word_position);
                } else {
                    index.get(word).put(doc_id, new ArrayList<Integer>(Arrays.asList(word_position)));
                }

            } else {
                index.put(word, new HashMap<Integer, ArrayList<Integer>>());
                index.get(word).put(doc_id, new ArrayList<Integer>(Arrays.asList(word_position)));
            }
        }
    }

    public static void doIndexing(String docs_filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(docs_filename))) {

            String line;
            Integer doc_id = 1;
            while ((line = br.readLine()) != null) {
                List<String> words = splitStringToWords(line);
                addWordsIntoIndex(doc_id, words);
                doc_id++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveIndexIntoFile(String index_filename) {
        try {
            FileWriter myWriter = new FileWriter(index_filename);

            for (String word: index.keySet()) {
                String line = word + ":" +
                        index.get(word).keySet().stream()
                                .map(i -> i.toString())
                                .collect(Collectors.joining(",")) + ":" +
                        index.get(word).values().stream()
                                .map(x -> x.stream()
                                        .map(i -> i.toString())
                                        .collect(Collectors.joining(",")))
                                .collect(Collectors.joining(";"));

                myWriter.write(line+"\r\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static void loadIndexFromFile(String index_filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(index_filename))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] line_parts = line.split(":");
                String word = line_parts[0];

                ArrayList<Integer> id_list = Arrays.stream(line_parts[1].split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(ArrayList::new));

                ArrayList<ArrayList<Integer>> position_list = Arrays.stream(line_parts[2].split(";"))
                        .map(l -> Arrays.stream(l.split(","))
                                .map(Integer::parseInt).collect(Collectors.toCollection(ArrayList::new)))
                        .collect(Collectors.toCollection(ArrayList::new));

                index.put(word, new HashMap<Integer, ArrayList<Integer>>());
                IntStream.range(0, id_list.size())
                        .forEach(i -> index.get(word).put(id_list.get(i), position_list.get(i)));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]) {
        System.setProperty("file.encoding" , "UTF-8");
        if (args.length<3) {
            System.out.println("Usage:");
            System.out.println("Indexing: main.py -i index_file docs_file");
            System.out.println("Searching: main.py -s index_file request");

            System.exit(0);
        }

        if (args[0].equals("-i")) {
            System.out.print("Индексация... ");
            String index_filename = args[1];
            String docs_filename = args[2];
            doIndexing(docs_filename);
            saveIndexIntoFile(index_filename);
            System.out.println("ок.");
        }

        if (args[0].equals("-s")) {
            System.out.println("Поиск... ");
            String index_filename = args[1];
            String request = args[2];
            loadIndexFromFile(index_filename);
            System.out.print(request+":");
            System.out.println(index.getOrDefault(request, inner).keySet());
        }

    }
}
