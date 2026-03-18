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

    // I'm using a 0-indexed array to store the heap
    // For any node at index i:
    //   left child  = 2*i + 1
    //   right child = 2*i + 2
    //   parent      = (i-1) / 2
    String[] heap;
    int size;

    // Set up the heap with a fixed capacity
    public tryHeapsort(int capacity) {
        heap = new String[capacity];
        size = 0;
    }

    // Swap two elements in an array
    // I made this take the array as a parameter so sort() can use it on a copy
    void swap(String[] arr, int i, int j) {
        String tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    // Push an element DOWN the heap until it's in the right place
    // This is used in both bottom-up build and during sorting
    // It's a max-heap so the alphabetically largest word ends up at the top
    void pushDown(String[] arr, int i, int n) {
        int biggest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // Check if left child is bigger than current biggest
        if (left < n && arr[left].compareTo(arr[biggest]) > 0)
            biggest = left;

        // Check if right child is bigger than current biggest
        if (right < n && arr[right].compareTo(arr[biggest]) > 0)
            biggest = right;

        // If a child was bigger, swap and keep going down
        if (biggest != i) {
            swap(arr, i, biggest);
            pushDown(arr, biggest, n);
        }
    }

    // Push an element UP the heap until it's in the right place
    // This is used when inserting one word at a time (top-down build)
    void pushUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            // If this word comes after its parent alphabetically, swap them
            if (heap[i].compareTo(heap[parent]) > 0) {
                swap(heap, i, parent);
                i = parent;
            } else {
                break; // already in the right spot
            }
        }
    }

    // ---------- BUILD METHOD 1: Bottom-up (Floyd's method) O(n) ----------
    // Copy all the words in first, then fix the heap from the bottom up
    // This is faster than inserting one by one
    public void buildBottomUp(String[] words) {
        for (int i = 0; i < words.length; i++) {
            heap[i] = words[i];
        }
        size = words.length;

        // Start from the last node that has children and push everything down
        for (int i = (size / 2) - 1; i >= 0; i--) {
            pushDown(heap, i, size);
        }
    }

    // ---------- BUILD METHOD 2: Top-down (repeated insert) O(n log n) ----------
    // Add one word at a time and fix the heap after each insert
    public void addWord(String word) {
        if (size >= heap.length)
            throw new RuntimeException("Heap is full - something went wrong");
        heap[size] = word;
        pushUp(size);
        size++;
    }

    // ---------- SORT: shared by both build methods ----------
    // I work on a copy so I don't lose the heap after sorting
    // Repeatedly pulls the max (root) to the back - gives alphabetical order
    public String[] sortWords() {
        // Make a copy to avoid messing up the original heap
        String[] copy = new String[size];
        for (int i = 0; i < size; i++) {
            copy[i] = heap[i];
        }

        // Pull the biggest word to the back each time, shrink the heap
        for (int end = copy.length - 1; end > 0; end--) {
            swap(copy, 0, end);       // move max to back
            pushDown(copy, 0, end);   // fix the smaller heap
        }

        // After this loop copy goes from smallest to largest = alphabetical
        return copy;
    }

    // ---------- Read ulysses.text and clean the words ----------
    // The file is raw novel text so I need to:
    // 1. Split each line into individual words
    // 2. Strip punctuation (commas, full stops, etc.)
    // 3. Make everything lowercase so "The" and "the" count as the same word
    static String[] loadWords(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                for (String token : line.trim().split("\\s+")) {
                    String clean = token.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!clean.isEmpty()) {
                        words.add(clean);
                    }
                }
            }
            System.out.println("Loaded " + words.size() + " words from " + filename);
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
        }
        return words.toArray(new String[0]);
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {

        // ── Quick test on a small array first ────────────────────────
        // I want to make sure the logic works before throwing 260 000 words at it
        System.out.println("=== SMALL TEST (20 words) ===");

        String[] small = {
            "zebra",  "mango",  "apple",  "heap",   "data",
            "sort",   "java",   "queue",  "node",   "tree",
            "binary", "insert", "delete", "index",  "array",
            "stack",  "list",   "graph",  "search", "path"
        };

        // Test bottom-up
        tryHeapsort test1 = new tryHeapsort(small.length);
        test1.buildBottomUp(small.clone());
        String[] result1 = test1.sortWords();

        System.out.println("Bottom-up result:");
        for (int i = 0; i < result1.length; i++)
            System.out.println("  " + (i + 1) + ". " + result1[i]);

        // Test top-down
        tryHeapsort test2 = new tryHeapsort(small.length);
        for (String w : small) test2.addWord(w);
        String[] result2 = test2.sortWords();

        System.out.println("\nTop-down result:");
        for (int i = 0; i < result2.length; i++)
            System.out.println("  " + (i + 1) + ". " + result2[i]);

        // Check they match
        boolean same = true;
        for (int i = 0; i < result1.length; i++) {
            if (!result1[i].equals(result2[i])) { same = false; break; }
        }
        System.out.println("\nDo both methods give the same order? " + same);

        // ── Now run on the full Ulysses text ──────────────────────────
        System.out.println("\n=== ULYSSES FULL RUN ===");

        String[] words = loadWords("ulysses.text");
        if (words.length == 0) {
            System.err.println("No words loaded - check the file is in the right folder");
            return;
        }

        // Warm-up pass so JIT doesn't skew the first real timing
        new tryHeapsort(words.length).buildBottomUp(words.clone());

        // ── Time bottom-up ────────────────────────────────────────────
        long start = System.nanoTime();
        tryHeapsort buHeap = new tryHeapsort(words.length);
        buHeap.buildBottomUp(words.clone());
        String[] buSorted = buHeap.sortWords();
        long buTime = System.nanoTime() - start;

        // ── Time top-down ─────────────────────────────────────────────
        start = System.nanoTime();
        tryHeapsort tdHeap = new tryHeapsort(words.length);
        for (String w : words) tdHeap.addWord(w);
        String[] tdSorted = tdHeap.sortWords();
        long tdTime = System.nanoTime() - start;

        // ── Print timing comparison ───────────────────────────────────
        System.out.println("\n=== TIMING RESULTS ===");
        System.out.printf("Bottom-up build + sort : %.3f ms%n", buTime / 1_000_000.0);
        System.out.printf("Top-down  build + sort : %.3f ms%n", tdTime / 1_000_000.0);

        if (buTime < tdTime)
            System.out.printf("Bottom-up was faster by %.2fx%n", (double) tdTime / buTime);
        else
            System.out.printf("Top-down was faster by %.2fx%n", (double) buTime / tdTime);

        // ── Show first 20 sorted words ────────────────────────────────
        System.out.println("\n=== FIRST 20 WORDS IN ALPHABETICAL ORDER ===");
        for (int i = 0; i < Math.min(20, buSorted.length); i++)
            System.out.println("  " + (i + 1) + ". " + buSorted[i]);

        // ── Confirm both methods give the same output ─────────────────
        boolean match = true;
        for (int i = 0; i < buSorted.length; i++) {
            if (!buSorted[i].equals(tdSorted[i])) {
                match = false;
                System.out.println("First difference at position " + i +
                    ": " + buSorted[i] + " vs " + tdSorted[i]);
                break;
            }
        }
        System.out.println("\nBoth methods produced " +
            (match ? "IDENTICAL" : "DIFFERENT") + " results.");
    }
}

//run to test
