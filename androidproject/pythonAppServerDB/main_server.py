# modules
import socket
import json

# files
from add_user import add_user_to_db
from check_login import check_login
from get_phone_number_by_username import get_phone_number_by_username
from change_password_by_user import change_password
from add_location import add_location_to_db
from get_locations_by_username import get_locations_by_username
from update_location import update_location
from delete_location import delete_location
from current_ids import get_id, save_id
from get_user_details_by_username import get_user_details_by_username
from update_user_details import update_user_details
from encryption_decryption import encrypt, decrypt

IP, PORT = "0.0.0.0", 50001
HEADER_LENGTH = 5


def main() -> None:
    """
    Main function. Listens for a connection from the client, and once a connection is made, it gets the message,
    decrypts it, parses it, executes function to perform the necessary database operations according to the activity
    name and data sent with it. Finally, it sends the result to the user and closes the connection.

    Returns:
        None
    """
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((IP, PORT))
    server_socket.listen(1)

    print("Waiting for connections...")

    while True:

        client_socket, client_address_port = server_socket.accept()
        print(f"{client_address_port} connected.\n")

        # length is in plain text
        length_of_data = client_socket.recv(HEADER_LENGTH).decode('utf-8')
        print(length_of_data)
        actual_data = client_socket.recv(int(length_of_data)).decode('utf-8')
        print(f"Got raw data:\n {actual_data}")

        plain_text_data = decrypt(actual_data)
        print(f"Decrypted: {plain_text_data}\n")

        # to keep track of where we are at the string
        chars_read_so_far = 0

        activity_length_to_read = plain_text_data[:HEADER_LENGTH]
        chars_read_so_far += HEADER_LENGTH

        activity_name = plain_text_data[chars_read_so_far:chars_read_so_far + int(activity_length_to_read)]
        chars_read_so_far += int(activity_length_to_read)

        print(f"Got activity: {str(activity_length_to_read)}{activity_name}")

        if activity_name:

            data_length_to_read = plain_text_data[chars_read_so_far:chars_read_so_far + HEADER_LENGTH]
            chars_read_so_far += HEADER_LENGTH

            data_string = plain_text_data[chars_read_so_far:chars_read_so_far + int(data_length_to_read)]
            chars_read_so_far += int(data_length_to_read)

            print(f"Got data: {str(data_length_to_read)}{data_string}\n")

            # the variable which will store the result to send
            result = None

            if activity_name == "SignUpActivity":

                signup_activity_details = data_string.split("|")

                # 0 is phone number, 1 is username, 2 is password
                result = add_user_to_db(signup_activity_details[0], signup_activity_details[1],
                                        signup_activity_details[2])

            elif activity_name == "LoginActivity":

                login_activity_details = data_string.split("|")

                # 0 is username, 1 is password
                result = check_login(login_activity_details[0], login_activity_details[1])

            elif activity_name == "ResetPasswordActivity":

                reset_password_activity_details = data_string.split("|")  # just username basically

                # 0 is username
                result = get_phone_number_by_username(reset_password_activity_details[0])

            elif activity_name == "ResetPasswordCodeActivity":

                code_received_activity_details = data_string.split("|")

                # 0 is user, 1 is new password
                result = change_password(code_received_activity_details[0], code_received_activity_details[1])

            elif activity_name == "AddLocationActivity":

                add_location_activity_details = data_string.split("|")

                # 0 is username, 1 is location json string
                result = add_location_to_db(add_location_activity_details[0], add_location_activity_details[1])

            elif activity_name == "MainLocationsActivity" or activity_name == "GeofenceBroadcastReceiver":

                main_locations_activity_details = data_string.split("|")

                # 0 is request
                if main_locations_activity_details[0] == "requesting location list":

                    # 1 is username
                    result_list_of_locations = get_locations_by_username(
                        main_locations_activity_details[1])

                    # make the list a json string
                    list_as_json_str = json.dumps(result_list_of_locations)
                    result = list_as_json_str

                elif main_locations_activity_details[0] == "requesting location deletion":

                    result = delete_location(main_locations_activity_details[1])

            # this is for updating the location. Location updating is done by Editing the location details, adding a
            # task, editing a task, or deleting a task (the last 3 are modifying the location's task list, basically
            # editing the location).

            elif activity_name == "EditLocationActivity" \
                    or activity_name == "AddTaskActivity" \
                    or activity_name == "EditTaskActivity" \
                    or activity_name == "MainTasksByLocationActivity":

                edit_location_activity_details = data_string.split("|")

                # 0 is old location json string, 1 is new location json string
                result = update_location(edit_location_activity_details[0], edit_location_activity_details[1])

            elif activity_name == "LocationClass" or activity_name == "TaskClass":

                # info about whether to get or save id
                class_id_details = data_string.split("|")

                # 0 is request (get or save)
                if class_id_details[0] == "saving current id":

                    # activity_name is the class saving id, [1] is the id.
                    result = save_id(activity_name, class_id_details[1])

                elif class_id_details[0] == "getting current id":

                    result = get_id(activity_name)

            elif activity_name == "AccountActivity":

                account_activity_details = data_string.split("|")

                # separated by pipes
                if account_activity_details[0] == "requesting user details":

                    result_user_details = get_user_details_by_username(
                        account_activity_details[1])  # 1 is connected username
                    result = result_user_details

                elif account_activity_details[0] == "requesting user update":

                    # 1 is connected (old) username, 2 is new phone number, 3 is new username
                    result_user_update = update_user_details(account_activity_details[1],
                                                             account_activity_details[2],
                                                             account_activity_details[3])
                    result = result_user_update

            encrypted = encrypt(result)
            print(f"Encrypted result:\n{encrypted}")

            client_socket.send(encrypted.encode())
            print(f"sent result (encrypted) - {result}\n")

        # close connection
        client_socket.close()
        print(f"closing socket: f{str(client_address_port)}\n")


if __name__ == "__main__":
    main()
