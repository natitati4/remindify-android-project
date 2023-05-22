import pymongo
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]


def get_user_details_by_username(username: str) -> str:
    """
    Retrieves the phone number, username, and password of a user by their username.

    Arguments:
        username (str): The username of the user.

    Returns:
        str: A string containing the user's phone number, username, and password separated by '|',
            or a string indicating the failure of the operation.
    """
    matching_user = list(users_collection.find({"username": username}))

    if matching_user:

        matching_user_details = matching_user[0]

        return matching_user_details["phone number"] + "|" + matching_user_details["username"]

    return "failed to get user details"




