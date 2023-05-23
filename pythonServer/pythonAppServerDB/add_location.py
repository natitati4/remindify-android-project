import pymongo.errors
from mongo_client import client

app_db = client["Finalyearproject"]
location_collection = app_db["LocationsByUsername"]


def add_location_to_db(username: str, locationJsonString: str) -> str:
    """
    Add a location for a user to the database.

    Arguments:
        username (str): The username of the user.
        locationJsonString (str): A JSON string representing the location data.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    new_location = {"username": username,
                    "locationJsonString": locationJsonString}

    try:
        location_collection.insert_one(new_location)
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    return "location added successfully"
