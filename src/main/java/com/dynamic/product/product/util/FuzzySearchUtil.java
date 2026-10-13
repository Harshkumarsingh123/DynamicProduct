package com.dynamic.product.product.util;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Locale;

public class FuzzySearchUtil {

    private static final LevenshteinDistance DISTANCE=LevenshteinDistance.getDefaultInstance();
    private FuzzySearchUtil(){

    }

    public static boolean isFuzzyMatch(String keyword,String text){

        if (keyword == null || text == null || keyword.isBlank() || text.isBlank()) {
            return false;
        }

        String searchWord = keyword.trim().toLowerCase(Locale.ROOT);
        if (searchWord.length() <= 3) {
            return false;
        }
        String[] words = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");

        int maxDistance;

        if (searchWord.length() <= 5) {
            maxDistance = 1;
        } else if (searchWord.length() <= 8) {
            maxDistance = 2;
        } else {
            maxDistance = 3;
        }

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            int distance = DISTANCE.apply(searchWord, word);

            if (distance <= maxDistance) {
                return true;
            }
        }

        return false;
    }
}
