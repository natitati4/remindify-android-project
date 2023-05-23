import pymongo.errors
from mongo_client import client

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
    try:
        matching_user = list(users_collection.find({"username": username}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_user:
        matching_user_details = matching_user[0]
        return matching_user_details["phone number"] + "|" + matching_user_details["username"]

    return "failed to get user details"




