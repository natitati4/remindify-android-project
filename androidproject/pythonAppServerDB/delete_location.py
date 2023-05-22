import pymongo
from credentials import USERNAME, PASSWORD

client = \
    pymongo.MongoClient(
        f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/?retryWrites=true&w=majority")

app_db = client["Finalyearproject"]
locations_collection = app_db["LocationsByUsername"]


# this function can be used for both editing a location, and adding/editing a task. Since both actions "update" the
# location Json string.
def delete_location(deletedLocationJsonString: str) -> str:
    """
    Deletes a location from the database.

    Arguments:
        deletedLocationJsonString (str): A JSON string representing the location to be deleted.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    matching_locations_list = list(locations_collection.find({"locationJsonString": deletedLocationJsonString}))

    if matching_locations_list:
        location_to_remove = matching_locations_list[0]
        locations_collection.delete_one(location_to_remove)  # delete location

        return "location deleted successfully"

    return "location deletion failed"


