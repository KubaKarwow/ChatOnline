# ChatOnline

This application is a chat server with functionalities for client login, message handling, and client logout. Below are the implementation details:

Functionalities:

Client Login:

-Clients can log in to the server using only an identifier (ID).
Message Handling:

-The server accepts messages from logged-in clients and broadcasts them to all other logged-in clients.
Client Logout:

-Clients can log out from the server, ending their session.
Server Log:

-The server keeps a log of all client requests and responses stored in memory.
Classes:

-ChatServer:

*Manages client connections and message broadcasting.
*Uses a selector to handle multiple client requests concurrently in a single thread.
*Provides methods to start and stop the server, and to retrieve the server log.

-ChatClient:

*Represents a client that can log in to the server, send messages, and log out.
*Provides methods to login, logout, send messages, and get the chat view from the client's perspective.

-ChatClientTask:

*Allows running clients in separate threads using ExecutorService.
*Creates tasks that log in the client, send a list of messages with optional delays between them, and log out the client.

-Main Class:

*The Main class reads server and client configurations from a test file.
*It starts the server, creates client tasks, executes them, and prints the server log and each client's chat view.

-Execution Flow:

The server starts and listens for client connections.
Clients connect to the server, log in, send messages, and log out.
The server logs all activities and broadcasts messages to all logged-in clients.
The server log and client chat views are printed at the end of the execution.

Example Output:

The server and client activities are logged with timestamps.
Each client's chat view shows the sequence of messages received from the server.
Errors during client-server interactions are logged in the client's chat view with a specific format.
This application demonstrates concurrent handling of multiple clients in a chat server environment using non-blocking I/O and multithreading.
