import pymongo.errors
from mongo_client import client

app_db = client["Finalyearproject"]
location_collection = app_db["LocationsByUsername"]


# this function can be used both for editing a location, and adding/editing a task. Since both actions "update" the
# location Json string.
def update_location(oldLocationJsonString, newLocationJsonString):
    """
    Update the location details for a given JSON string of the old location.

    Arguments:
        oldLocationJsonString (str): The JSON string of the old location.
        newLocationJsonString (str): The JSON string of the new location.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    # Find the matching location with the old JSON string in the database
    try:
        matching_locations_list = list(location_collection.find({"locationJsonString": oldLocationJsonString}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_locations_list:
        # Get the old location object and prepare the new location object
        old_location = matching_locations_list[0]
        new_location = {"$set": {"locationJsonString": newLocationJsonString}}

        # Update the old location with the new location details
        try:
            location_collection.update_one(old_location, new_location)
        except pymongo.errors.PyMongoError:
            return "Problem with connecting to MongoDB"

        return "location updated successfully"

    # Return an error message if the old location is not found in the database
    return "location updating failed"

