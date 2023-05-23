import pymongo.errors
from bson import Int64
from mongo_client import client

app_db = client["Finalyearproject"]
location_collection = app_db["currentIDs"]

# these functions can be used for both editing a location, and adding/editing a task. Since both actions "update" the
# location Json string.


def save_id(className: str, idNum: str) -> str:
    """
    Save the current ID number for a given class LocationsClass or TaskClass.

    Arguments:
        className (str): The name of the class.
        idNum (int): The ID number to be saved.

    Returns:
        str: A string indicating the success or failure of the operation.
    """
    try:
        matching_id_objects = list(location_collection.find({"className": className}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_id_objects:
        old_id_object = matching_id_objects[0]
        new_id_object = {"$set": {"currentID": Int64(idNum)}}

        try:
            location_collection.update_one(old_id_object, new_id_object)
        except pymongo.errors.PyMongoError:
            return "Problem with connecting to MongoDB"

        return "id saved successfully"
    return "id saving failed"


def get_id(className: str) -> str:
    """
    Get the current ID number for a given class.

    Arguments:
        className (str): The name of the class.

    Returns:
        str: A string indicating the success or failure of the operation, along with the current ID number if
        successful.
    """
    try:
        matching_id_objects = list(location_collection.find({"className": className}))
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_id_objects:
        currentID = matching_id_objects[0]["currentID"]
        return "got current id" + str(len(str(currentID))).zfill(10) + str(currentID)
    return "id getting failed"
