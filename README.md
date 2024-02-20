# Distributed File System (Java RMI)

This project implements a Distributed File System using Java RMI (Remote Method Invocation). It consists of several classes that together enable the distributed storage and retrieval of files across multiple nodes.

## Classes Overview

### App
- Main class to initialize the distributed file system.
- Sets up server and client functionalities.
- Handles file operations such as search, download, upload, and delete.

### LogEntry
- Represents a log entry with a specific term.
- Provides methods to get and set the term.

### Node
- Represents a node in the distributed system.
- Implements functionalities for file operations, leader election, heartbeat, and message passing among nodes.
- Manages local storage and log entries.

### RaftNode
- Extends NodeInterface and defines additional methods specific to Raft consensus algorithm.
- Includes methods for starting an election and sending heartbeats.

### NodeInterface
- Interface defining remote methods accessible by nodes.
- Includes methods for file operations, leader management, status communication, and heartbeat handling.

### Request
- Represents a request for file operation.
- Includes information such as request ID, sender, operation type, file name, file content, and logical clock.


## Get Started

  1. Compile the classes.
  2. Run the App class with appropriate arguments to initialize nodes.
  3. Perform file operations using the provided interface.


## Note
- This system has semi-implementation of the Raft consensus algorithm for leader election and coordination among nodes.
- Ensure proper configuration of IP addresses, ports, and services for seamless communication between nodes.


## Contributing

Contributions are welcome! If you find any bugs or have suggestions for improvements, please open an issue or create a pull request. For major changes, please open an issue first to discuss the proposed changes.
