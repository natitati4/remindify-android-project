import pymongo
from hashlib import sha3_256
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]


def add_user_to_db(phone_number: str, username: str, password: str) -> str:
    """
    Add a new user to the database.

    Arguments:
        phone_number (str): The phone number of the user.
        username (str): The username of the user.
        password (str): The password of the user.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    # Check if the username already exists in the database
    if not list(users_collection.find({"username": username})):
        # Create a new user document and insert it into the database
        new_user = {"phone number": phone_number,
                    "username": username,
                    "password": sha3_256(password.encode()).hexdigest()}  # hash the password and turn it to hex
        users_collection.insert_one(new_user)
        return "user added successfully"
    # Return an error message if the username already exists
    return "username already exists"


