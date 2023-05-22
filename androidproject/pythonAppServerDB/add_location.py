import pymongo
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(
        f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

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

    location_collection.insert_one(new_location)
    return "location added successfully"
