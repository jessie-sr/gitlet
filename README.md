# Gitlet
Gitlet is a minimalistic version control system inspired by Git, designed to replicate some of the core functionalities of Git with a focus on object-oriented programming in Java, efficient data structures, and test-driven development (TDD) methodologies.

## Features

- **Commit Mechanism:** Save snapshots of directories of files, which can be restored at a later time.
- **Branch Management:** Maintain sequences of commits in separate branches.
- **Checkout System:** Restore individual files or entire branches to their state at the point of a specific commit.
- **Merge Functionality:** Combine changes from different branches.
- **Log History:** View the history of commits.
- **Data Persistence:** Utilizes Java serialization to persist data, emulating a flat directory structure for repositories.
- **Error Handling:** Implements a robust system using Java's exception mechanisms to ensure stability and reliability.

## Getting Started

Before running Gitlet, make sure to compile the Java files using the following command:
```bash
javac gitlet/*.java
```

To start using Gitlet, run the following command to initialize a new Gitlet repository:
```bash
java gitlet.Main init
```

### Basic Commands

- **Add a file to the staging area:**
```bash
java gitlet.Main add [file name]
```

- **Commit changes:**
```bash
java gitlet.Main commit "Commit message"
```

- **Remove a file:**
```bash
java gitlet.Main rm [file name]
```

- **View the commit log:**
```bash
java gitlet.Main log
```

- **View the global log (all commits):**
```bash
java gitlet.Main global-log
```

- **Find a commit by message:**
```bash
java gitlet.Main find [commit message]
```

- **Display the status of the repository:**
```bash
java gitlet.Main status
```


### Advanced Commands

- **Restore a file to a previous state:**
```bash
java gitlet.Main restore -- [file name]
java gitlet.Main restore [commit id] -- [file name]
```

- **Create a new branch:**
```bash
java gitlet.Main branch [branch name]
```

- **Switch to a different branch:**
```bash
java gitlet.Main switch [branch name]
```

- **Remove a branch:**
```bash
java gitlet.Main rm-branch [branch name]
```

- **Reset to a specific commit:**
```bash
java gitlet.Main reset [commit id]
```

- **Merge two branches:**
```bash
java gitlet.Main merge [branch name]
```

## Testing

Gitlet has been developed using TDD principles, and a comprehensive suite of JUnit tests can be found in the `tests` directory. To run the tests, use:
```bash
java org.junit.runner.JUnitCore gitlet.TestingSuite
```

## Acknowledgments

- This project is based on Project 2 of UC Berkeley's Data Structures and Programming Methodology Course.
- Thanks to the contributors who have helped shape this project.
