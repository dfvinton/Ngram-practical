import java.io.*;
import java.util.*;
import java.lang.*;



public class WordAnalyser {

static final int defaultGramSize = 3;

    static int gramSize = defaultGramSize; //Size of ngram, input by user later; default is 3

    public static void main(String[] args) {

        if (args.length < 2) { //Incorrect command line arguments: print warning
            System.out.println("Usage: java WordAnalyser input.txt output.txt");
            return;
        }

        if (args.length == 3) { //User has specified ngram size to use
            try {
                gramSize = Integer.parseInt(args[2]);
                if (gramSize < 2) { //ngram size too small
                    System.out.println("Error: input Ngram size larger than 1");
                    System.exit(0);
                }
            } catch (NumberFormatException e) { //User typed something wrong: print error message
                System.out.println("Error: input valid Ngram size");
                System.exit(1);
            }

        }
        String input = args[0];
        String output = args[1];

        HashMap<String, NgramCount> hmap = new HashMap<String, NgramCount>(); //Hashmap, indexed by ngram string, values are ngramCount objects
        List<NgramCount> nGrams = new ArrayList<NgramCount>(); //List of ngramCount objects, to be sorted once all are counted from file

        readFile(input, hmap, nGrams); //Process input file and add ngrams to hashmap
        nGrams = sortByCount(hmap, nGrams); //Put ngrams in list and sort in descending size order
        writeCsv(output, nGrams); //Write sorted ngrams to CSV output file
    }

    public static void readFile(String input, HashMap<String, NgramCount> hmap, List<NgramCount> nGrams) { //Read input file and add ngrams and their counts to hashmap
        String[] buffer = new String[gramSize]; //Buffer used to store words and add them to hashmap when buffer is full (i.e.complete ngram has been read)
        String line = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            while ((line = reader.readLine()) != null) {
                if (!(line.equals("") || line.equals(" ") || line.equals("  ") || line.equals("\n"))) { // If line is not empty, space, tab, newline
                    List<String> words = cleanLine(line); //Strip punctuation, convert to lowercase
                    for (int i = 0; i < words.size(); i++) { //For all words in this line
                        buffer[gramSize - 1] = words.get(i); // Go through words and add to end every time
                        shuntBuffer(buffer, hmap);  // "Shunt" buffer along ready for a new word, and if buffer is full, add ngram to hashmap
                    }
                }
            }
        } catch (IOException e) { //Something went wrong
            System.out.println("IO Exception: file in unexpected format or not found");
            System.exit(1);
        }
    }

    public static void writeCsv(String output, List<NgramCount> nGrams) { //Write sorted list of ngrams to specified output file
        try {
            PrintWriter w = new PrintWriter(new FileWriter(output));

            for (NgramCount n : nGrams) { //For all ngrams in list

                    w.println("\"" + n.getNgram() + "\"" + "," + n.getCount()); //Write ngram string, then comma, then its count


            }

            w.close();

        } catch (IOException e) { //Something went wrong with output file
            System.out.println("Write error: make sure file exists");
            System.exit(1);
        }

    }


    public static void shuntBuffer(String[] buffer, HashMap<String, NgramCount> hmap) { //Shunt buffer along and , if buffer full, create new ngram

        if (buffer[0] != null) { //Buffer full
            String gram = concatenateGram(buffer); //Add the words in the buffer together into a string (ngram)
            storeGram(hmap, gram); //Put ngram in the hashmap
        }

        for (int i = 0; i < gramSize - 1; i++) { //"Shunt" along ready for a new word to be placed in end of buffer: i.e [a, b, c] ----> [b, c, _]
            buffer[i] = buffer[i + 1];
        }
    }

    public static String concatenateGram(String[] buffer) { //Concatenates words in buffer together to form ngram string
        String gram = "";
        for (int i = 0; i < gramSize; i++) { // Concatenates string
            gram = gram + (buffer[i] + " ");
        }

        char[] charArray = gram.toCharArray();
        char[] newCharArray = new char[charArray.length - 1]; // Deletes space at end
        for (int i = 0; i < newCharArray.length; i++) {
                newCharArray[i] = charArray[i];
        }
       // String newGram = gram.trim();

        gram = new String(newCharArray); //Convert to string

        return gram;
    }

    public static void storeGram(HashMap<String, NgramCount> hmap, String gram) { //Add ngram to hashmap
        NgramCount temp = hmap.get(gram); //See if ngram already exists in hashmap (i.e. we've seen it before)
        if (temp == null) { //New ngram: make new ngramCount for it (with count 1) and add to hashmap
            NgramCount gramObject = new NgramCount(gram, 1);
            hmap.put(gram, gramObject);
        } else { //Already seen ngram: just update its count value
            hmap.get(gram).incrementCount();
        }
    }

    public static List<String> cleanLine(String line) { //Strips punctuation, converts all caps to lowercase
        line = line.toLowerCase();
        line = line.trim();

        line = line.replaceAll("[^a-zA-Z\\s]", ""); // syntax from http://stackoverflow.com/questions/23332146/remove-punctuation-preserve-letters-and-white-space-java-regex (removes numbers and all special characters)
        List<String> words = new ArrayList<String>(Arrays.asList(line.split("\\s+")));   // Gets rid of extra whitespace

        List<Integer> toRemove = new ArrayList<Integer>(); // Empty words due to problem with above space-stripping regex
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).equals("")) { //Empty word
                toRemove.add(i);
            }
        }

        for (int x : toRemove) { //Remove all empty words
            words.remove(x);
        }

        return words;
    }

    public static List<NgramCount> sortByCount(HashMap<String, NgramCount> hmap, List<NgramCount> nGrams) {
        for (NgramCount ngram : hmap.values()) { //Add all ngram objects to list
            nGrams.add(ngram);
        }

        //Normal comparison function sorts in ascending order: we want descending, so use reverse order
        Collections.sort(nGrams, Collections.reverseOrder()); //http://stackoverflow.com/questions/5789503/java-comparator-using-reverseorder-but-with-an-inner-class
        return nGrams;
    }
}


