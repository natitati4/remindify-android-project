import pymongo
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]


def get_phone_number_by_username(username: str) -> str:
    """
    Find the phone number of a user by their username.

    Arguments:
        username (str): The username of the user.

    Returns:
        str: The phone number of the user, or a failure message if the user does not exist.
    """
    matching_users_list = list(users_collection.find({"username": username}))
    if matching_users_list:
        phone_number = matching_users_list[0]["phone number"]
        return phone_number
    return "user does not exist"


