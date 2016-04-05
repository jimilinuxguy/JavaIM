# Sanchez GUI Chat
Java Instant Messaging Client, Graphical Client, and Server implementations, roughly based on the idea of IRC.
The client can be ran by evecuting the java Client, with the parameters username (String), and port (Integer). If you attempt to use a port < 1024, you may need to run your application with additional permissions. If only a port is specified, the client defaults to the username ChatUser\<RandomInteger\>.

## List of Commands
* /who - Returns a list of all users presently connected to the server
* /join <String> - Creates/joins a topic
* /part <String> - Parts a topic
* /quit - Disconnects client from server
