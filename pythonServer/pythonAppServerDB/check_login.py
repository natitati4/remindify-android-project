import pymongo.errors
from hashlib import sha3_256
from mongo_client import client

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]


def check_login(username: str, password: str) -> str:
    """
    Check if the user credentials match and send a message accordingly.

    Arguments:
        username (str): The username of the user.
        password (str): The password entered by the user.

    Returns:
        str: A string indicating the success or failure (and what went wrong) of the login.
    """
    try:
        matching_users_list = list(users_collection.find({"username": username}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_users_list:
        if sha3_256(password.encode()).hexdigest() == matching_users_list[0]["password"]:  # compare hashes
            return "user successful login"
        return "wrong password"
    return "user does not exist"


