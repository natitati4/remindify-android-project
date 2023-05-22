import pymongo
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
location_collection = app_db["LocationsByUsername"]


def get_locations_by_username(username: str) -> list[str]:
    """
    Retrieve a list of location JSON strings for a given username.

    Arguments:
        username (str): The username of the user.

    Returns:
        list[str]: A list of location JSON strings for the given username.
    """
    matching_locations_list = list(location_collection.find({"username": username}))

    locations_list = []

    if matching_locations_list:
        for storedLocation in matching_locations_list:
            locations_list.append(storedLocation["locationJsonString"])

    return locations_list



