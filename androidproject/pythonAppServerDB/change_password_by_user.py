import pymongo
from hashlib import sha3_256

from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]


def change_password(username: str, new_password: str) -> str:
    """
    Change the password for a user.

    Arguments:
        username (str): The username of the user.
        new_password (str): The new password to be set.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    matching_users_list = list(users_collection.find({"username": username}))
    if matching_users_list:
        old_user_item = matching_users_list[0]  # the app already checks earlier if the user exists.
        new_user_item = {"$set": {"password": sha3_256(new_password.encode()).hexdigest()}}  # prepare new item
        users_collection.update_one(old_user_item, new_user_item)
        return "password updated successfully"
    return "password updating failed"


