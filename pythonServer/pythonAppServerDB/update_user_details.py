import pymongo.errors
from mongo_client import client

app_db = client["Finalyearproject"]
users_collection = app_db["Users"]
location_collection = app_db["LocationsByUsername"]


# this function can be used both for editing a location, and adding/editing a task. Since both actions "update" the
# location Json string.
def update_user_details(old_user_username, new_user_phone_number, new_user_username):
    """
    Updates the details of a user in the users collection and updates the username attribute of all the locations
    with the old username to the new username.

    Arguments:
        old_user_username (str): The current username of the user.
        new_user_phone_number (str): The new phone number of the user.
        new_user_username (str): The new username of the user.
        new_user_password (str): The new password of the user.

    Returns:
        str: A string indicating whether the update was successful or not.
    """
    try:
        matching_user = list(users_collection.find({"username": old_user_username}))
        matching_locations = list(location_collection.find({"username": old_user_username}))

        # update the user details, and the username attribute of all the locations of the username.
        if list(users_collection.find({"username": new_user_username})) and not old_user_username == new_user_username:
            return "username already exists"
    except pymongo.errors.PyMongoError:
        return "Problem with connecting to MongoDB"

    if matching_user:

        old_user = matching_user[0]  # the app already checks earlier if the user exists.
        new_user = {"$set": {"phone number": new_user_phone_number,
                             "username": new_user_username}}  # prepare new item

        try:
            users_collection.update_one(old_user, new_user)
        except pymongo.errors.PyMongoError:
            return "Problem with connecting to MongoDB"

        for storedLocation in matching_locations:
            old_location = storedLocation
            new_location = {"$set": {"username": new_user_username}}
            location_collection.update_one(old_location, new_location)

        return "user updated successfully"
    return "user updating failed"
