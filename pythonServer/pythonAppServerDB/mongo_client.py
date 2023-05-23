import pymongo

# credentials for the mongoDB client
USERNAME = "natitati"
PASSWORD = "nati090105"

client = \
    pymongo.MongoClient(f"mongodb+srv://{USERNAME}:{PASSWORD}@database1.3hb3aie.mongodb.net/"
                        f"?retryWrites=true&w=majority",
                        serverSelectionTimeoutMS=2000)
