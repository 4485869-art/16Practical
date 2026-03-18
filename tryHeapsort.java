// Themba Sithole
// 4485869
// Practical 6 - Heap Sort
// Consulted: Claude by Anthropic (free version) - claude.ai

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class tryHeapsort {

    // Array to hold the heap and track how many elements are in it
    String[] heap;
    int size;

    // Set up the heap with enough space for all the words
    public tryHeapsort(int capacity) {
        heap = new String[capacity];
        size = 0;
    }

    // ---------- Swap two elements in an array ----------
    // I pass the array in so I can reuse this in sort() on a copy
    private void swap(String[] arr, int i, int j) {
        String temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ---------- Push element DOWN to its correct position ----------
    // Used when building bottom-up and during sorting
    // Checks both children and swaps with the biggest one
    private void pushDown(String[] arr, int i, int n) {
        int biggest = i;
        int left = 2 * i + 1;   // left child index
        int right = 2 * i + 2;  // right child index

        // Check if left child is bigger than current node
        if (left < n && arr[left].compareTo(arr[biggest]) > 0)
            biggest = left;

        // Check if right child is bigger than current biggest
        if (right < n && arr[right].compareTo(arr[biggest]) > 0)
            biggest = right;

        // If a child was bigger, swap and keep pushing down
        if (biggest != i) {
            swap(arr, i, biggest);
            pushDown(arr, biggest, n);
        }
    }

    // ---------- Push element UP to its correct position ----------
    // Used when inserting one word at a time in top-down build
    // Keeps swapping with parent until the word is in the right place
    private void pushUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            // If this word comes after its parent alphabetically, swap them
            if (heap[i].compareTo(heap[parent]) > 0) {
                swap(heap, i, parent);
                i = parent;
            } else {
                // Already in the right spot, stop here
                break;
            }
        }
    }

    // ---------- Bottom-up heap construction ----------
    // Copy all words in first, then fix the heap from the bottom up
    // This is O(n) - faster than inserting one by one
    public void buildBottomUp(String[] words) {
        // Copy words into the heap array
        for (int i = 0; i < words.length; i++) {
            heap[i] = words[i];
        }
        size = words.length;

        // Start from the last node that has children and push down
        // Nodes below size/2 are leaves so no need to push them down
        for (int i = (size / 2) - 1; i >= 0; i--) {
            pushDown(heap, i, size);
        }
    }

    // ---------- Top-down heap construction ----------
    // Add one word at a time and fix the heap after each insert
    // This is O(n log n) - slower than bottom-up
    public void insert(String word) {
        if (size >= heap.length) {
            throw new IllegalStateException("Heap is full");
        }
        // Add to the end then bubble it up to the right spot
        heap[size] = word;
        pushUp(size);
        size++;
    }

    // ---------- Sort the heap into alphabetical order ----------
    // Works on a copy so the original heap is not destroyed
    // Repeatedly moves the biggest word to the back
    public String[] sort() {
        // Make a copy to avoid messing up the original heap
        String[] copy = new String[size];
        for (int i = 0; i < size; i++) {
            copy[i] = heap[i];
        }

        // Each loop: move the max (root) to the back, shrink the heap
        // After all iterations, array goes from smallest to largest
        for (int i = copy.length - 1; i > 0; i--) {
            swap(copy, 0, i);       // move max to back
            pushDown(copy, 0, i);   // fix the smaller heap
        }

        return copy;
    }

    // ---------- Read and clean words from the file ----------
    // The file is raw novel text so each line needs to be split into words
    // Punctuation is stripped and everything is lowercased
    // so "The" and "the" are treated as the same word
    private static String[] readWordsFromFile(String filename) {
        List<String> wordList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Separate line into words using whitespace
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    // Remove punctuation, keep only letters
                    String cleaned = token.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    // Skip empty tokens
                    if (!cleaned.isEmpty()) {
                        wordList.add(cleaned);
                    }
                }
            }
            System.out.println("Successfully read " + wordList.size() + " words from " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return wordList.toArray(new String[0]);
    }

    public static void main(String[] args) {

        // ---------- Small test first ----------
        // I want to confirm the logic works before running on 260 000 words
        System.out.println("=== SMALL TEST (20 words) ===");

        String[] small = {
            "zebra",  "mango",  "apple",  "heap",   "data",
            "sort",   "java",   "queue",  "node",   "tree",
            "binary", "insert", "delete", "index",  "array",
            "stack",  "list",   "graph",  "search", "path"
        };

        // Test bottom-up on the small array
        tryHeapsort h1 = new tryHeapsort(small.length);
        h1.buildBottomUp(small.clone());
        String[] sorted1 = h1.sort();

        // Test top-down on the small array
        tryHeapsort h2 = new tryHeapsort(small.length);
        for (String w : small) h2.insert(w);
        String[] sorted2 = h2.sort();

        System.out.println("Bottom-up sorted:");
        for (int i = 0; i < sorted1.length; i++)
            System.out.println("  " + (i + 1) + ": " + sorted1[i]);

        System.out.println("\nTop-down sorted:");
        for (int i = 0; i < sorted2.length; i++)
            System.out.println("  " + (i + 1) + ": " + sorted2[i]);

        // Check both methods give the same result
        boolean smallMatch = true;
        for (int i = 0; i < sorted1.length; i++) {
            if (!sorted1[i].equals(sorted2[i])) { smallMatch = false; break; }
        }
        System.out.println("\nBoth methods match: " + smallMatch);

        // ---------- Full Ulysses run ----------
        System.out.println("\n=== FULL ULYSSES RUN ===");

        String filename = "ulysses.text";
        String[] words = readWordsFromFile(filename);

        if (words.length == 0) {
            System.err.println("No words found in file. Exiting.");
            return;
        }

        System.out.println("Total words to sort: " + words.length);

        // Warm up the JVM first so it does not skew the timing results
        for (int w = 0; w < 3; w++) {
            tryHeapsort tmp1 = new tryHeapsort(words.length);
            tmp1.buildBottomUp(words.clone());
            tmp1.sort();
            tryHeapsort tmp2 = new tryHeapsort(words.length);
            for (String word : words) tmp2.insert(word);
            tmp2.sort();
        }

        // ---------- Bottom-up timing ----------
        // Average over 3 runs to get a stable result
        int RUNS = 3;
        long totalBottom = 0;
        long totalTop = 0;

        for (int r = 0; r < RUNS; r++) {
            // Time the bottom-up build and sort
            long startTime = System.nanoTime();
            tryHeapsort heapBU = new tryHeapsort(words.length);
            heapBU.buildBottomUp(words.clone());
            heapBU.sort();
            totalBottom += System.nanoTime() - startTime;

            // ---------- Top-down timing ----------
            startTime = System.nanoTime();
            tryHeapsort heapTD = new tryHeapsort(words.length);
            for (String word : words) heapTD.insert(word);
            heapTD.sort();
            totalTop += System.nanoTime() - startTime;
        }

        long avgBottom = totalBottom / RUNS;
        long avgTop = totalTop / RUNS;

        // ---------- Display results ----------
        System.out.println("\n=== TIMING RESULTS ===");
        System.out.printf("Bottom-up build + sort: %.3f ms%n", avgBottom / 1_000_000.0);
        System.out.printf("Top-down  build + sort: %.3f ms%n", avgTop / 1_000_000.0);

        // Bottom-up should be faster since it is O(n) vs O(n log n)
        if (avgBottom < avgTop) {
            System.out.printf("Bottom-up is faster by %.2fx%n", (double) avgTop / avgBottom);
        } else {
            System.out.printf("Top-down is faster by %.2fx%n", (double) avgBottom / avgTop);
        }

        // Run once more to get the sorted arrays to display
        tryHeapsort finalBU = new tryHeapsort(words.length);
        finalBU.buildBottomUp(words.clone());
        String[] sortedBU = finalBU.sort();

        tryHeapsort finalTD = new tryHeapsort(words.length);
        for (String word : words) finalTD.insert(word);
        String[] sortedTD = finalTD.sort();

        // ---------- Verify first 20 words ----------
        System.out.println("\n=== VERIFICATION (first 20 sorted words) ===");
        System.out.println("Bottom-up sort result:");
        for (int i = 0; i < Math.min(20, sortedBU.length); i++) {
            System.out.println((i + 1) + ": " + sortedBU[i]);
        }

        // Check if both sorts give the same output
        boolean match = true;
        for (int i = 0; i < sortedBU.length; i++) {
            if (!sortedBU[i].equals(sortedTD[i])) {
                match = false;
                System.out.println("Mismatch at index " + i +
                    ": " + sortedBU[i] + " vs " + sortedTD[i]);
                break;
            }
        }
        System.out.println("\nBoth sorts produced " +
            (match ? "IDENTICAL" : "DIFFERENT") + " results.");
    }
}

//Last commit 