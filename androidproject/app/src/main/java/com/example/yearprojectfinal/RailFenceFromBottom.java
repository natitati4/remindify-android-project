package com.example.yearprojectfinal;

import java.util.Arrays;

// This class contains the functions for encrypting in decrypting with the rail fence from bottom
// encryption method
public class RailFenceFromBottom
{
    // Encrypt with rail fence from bottom
    public static String encrypt(String plaintext, int rails)
    {
        // Create a 2D array to store the encrypted text
        char[][] fence = new char[rails][plaintext.length()];

        // Initialize the 2D array with null characters
        for (char[] chars : fence)
        {
            Arrays.fill(chars, '\0');
        }

        // Variables to keep track of the current row and column, and the direction
        int row = rails - 1, col = 0;
        boolean going_up = false;

        // Loop through each character in the plaintext
        for (int i = 0; i < plaintext.length(); i++)
        {
            // Add the current character to the 2D array
            fence[row][col] = plaintext.charAt(i);
            col++;

            // Check if we're moving down or up the fence
            if (going_up) {
                // Check if we're on the first row
                if (row == 0) {
                    going_up = false;
                    row++;
                } else {
                    row--;
                }
            } else {
                // Check if we're on the last row
                if (row == rails - 1) {
                    going_up = true;
                    row--;
                } else {
                    row++;
                }
            }
        }

        // Build the encrypted string from the 2D array
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < fence.length; i++)
        {
            for (int j = 0; j < fence[i].length; j++) {
                // Only append characters that are not null
                if (fence[i][j] != '\0') {
                    encrypted.append(fence[i][j]);
                }
            }
        }

        // Return the encrypted string
        return encrypted.toString();
    }

    // Decrypt with rail fence from bottom
    public static String decrypt(String encrypted, int rails)
    {
        // Store the indices of each character in the encrypted string
        int[] indexes = new int[encrypted.length()];

        // Variables to keep track of the current row and column, and the direction
        int row = rails - 1, col = 0;
        boolean going_up = true;

        // Calculate the index of each character in the encrypted string
        for (int i = 0; i < encrypted.length(); i++)
        {
            indexes[col] = row;
            col++;

            // Change the direction of index calculation if the current row is at the top or bottom of the rails
            if (going_up)
            {
                if (row == 0)
                {
                    going_up = false;
                    row++;
                } else
                {
                    row--;
                }
            }
            else
            {
                if (row == rails - 1)
                {
                    going_up = true;
                    row--;
                } else {
                    row++;
                }
            }
        }

        // Store the decrypted message as an array of characters
        char[] decrypted = new char[encrypted.length()];
        int pos = 0;

        // Fill the decrypted message array with the characters from the encrypted string based on the indices
        for (int i = 0; i < rails; i++)
        {
            for (int j = 0; j < indexes.length; j++)
            {
                if (indexes[j] == i) {
                    decrypted[j] = encrypted.charAt(pos);
                    pos++;
                }
            }
        }

        return new String(decrypted);
    }
}
