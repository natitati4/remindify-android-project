def encrypt(plaintext: str, rails: int) -> str:
    """
    Encrypts a plaintext string using the rail fence cipher (from bottom) with the specified number of rails.

    Arguments:
        plaintext (str): The plaintext to be encrypted.
        rails (int): The number of rails to use in the rail fence cipher.

    Returns:
        str: The encrypted ciphertext.
    """
    # Create a 2D array to store the encrypted text
    fence = [[None for _ in range(len(plaintext))] for _ in range(rails)]

    # Variables to keep track of the current row and column, and the direction
    row = rails - 1
    col = 0
    going_up = False

    # Loop through each character in the plaintext
    for i in range(len(plaintext)):
        # Add the current character to the 2D array
        fence[row][col] = plaintext[i]
        col += 1

        # Check if we're moving down or up the fence
        if going_up:
            # Check if we're on the first row
            if row == 0:
                going_up = False
                row += 1
            else:
                row -= 1
        else:
            # Check if we're on the last row
            if row == rails - 1:
                going_up = True
                row -= 1
            else:
                row += 1

    # Build the encrypted string from the 2D array
    encrypted = ""
    for i in range(rails):
        for j in range(len(plaintext)):
            # Only append characters that are not None
            if fence[i][j]:
                encrypted += fence[i][j]

    # Return the encrypted string
    return encrypted


def decrypt(encrypted: str, rails: int) -> str:
    """
    Decrypts an encrypted string that was encrypted using the rail fence cipher (from bottom) with the specified number
    of rails.

    Arguments:
        encrypted (str): The encrypted string to be decrypted.
        rails (int): The number of rails used in the original encryption.

    Returns:
        str: The decrypted plaintext.
    """
    # Store the indices of each character in the encrypted string
    indices = [None for _ in range(len(encrypted))]

    # Variables to keep track of the current row and column, and the direction
    row = rails - 1
    col = 0
    going_up = True

    # Calculate the index of each character in the encrypted string
    for i in range(len(encrypted)):
        indices[col] = row
        col += 1

        # Change the direction of index calculation if the current row is at the top or bottom of the rails
        if going_up:
            if row == 0:
                going_up = False
                row += 1
            else:
                row -= 1
        else:
            if row == rails - 1:
                going_up = True
                row -= 1
            else:
                row += 1

    # Store the decrypted message as an array of characters
    decrypted = ["" for _ in range(len(encrypted))]
    pos = 0

    # Fill the decrypted message array with the characters from the encrypted string based on the indices
    for i in range(rails):
        for j in range(len(encrypted)):
            if indices[j] == i:
                decrypted[j] = encrypted[pos]
                pos += 1

    return "".join(decrypted)

