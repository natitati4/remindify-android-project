from base64 import b64encode, b64decode
import rail_fence_from_bottom

RAILS = 3
KEY = "THE_ULTIMATE_KEY"


def encrypt(st: str) -> str:
    """
    Encrypt a string using base64, Rail Fence cipher, and XOR cipher.

    Arguments:
        st (str): The string to be encrypted.

    Returns:
        str: The encrypted string.
    """
    # encrypting base64
    encrypted_base64 = b64encode(st.encode()).decode()

    # encrypting Rail Fence
    encrypted_railFence = rail_fence_from_bottom.encrypt(encrypted_base64, RAILS)

    # encrypting with xors
    final_encrypted = ""
    for i in range(len(encrypted_railFence)):
        final_encrypted += chr(ord(encrypted_railFence[i]) ^ ord(KEY[i % len(KEY)]))

    # returning the final encrypted
    return final_encrypted


def decrypt(st: str) -> str:
    """
    Decrypt an encrypted string that was encrypted using base64, Rail Fence cipher, and XOR cipher.

    Arguments:
        st (str): The encrypted string to be decrypted.

    Returns:
        str: The decrypted string.
    """
    # decrypting xor encryption
    decrypted_xors = ""
    for i in range(len(st)):
        decrypted_xors += chr(ord(st[i]) ^ ord(KEY[i % len(KEY)]))

    # decrypting Rail Fence
    decrypted_railFence = rail_fence_from_bottom.decrypt(decrypted_xors, RAILS)

    # decrypting base64 and returning the final decrypted
    return b64decode(decrypted_railFence.encode()).decode()

