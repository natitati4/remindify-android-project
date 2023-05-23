import pymongo.errors
from mongo_client import client

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
    try:
        matching_locations_list = list(locations_collection.find({"locationJsonString": deletedLocationJsonString}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_locations_list:
        location_to_remove = matching_locations_list[0]

        try:
            locations_collection.delete_one(location_to_remove)  # delete location
        except pymongo.errors.PyMongoError:
            return "Problem with connecting to MongoDB"

        return "location deleted successfully"

    return "location deletion failed"


