# Gitlet Design Document

**Name**: Rong Sun

## Classes and Data Structures

### Class 1: Commit

#### Fields

1. private String message: The message of this Commit.
2. private String author: The name of the user who created this commit.
3. private Object timeStamp: The timestamp when the commit was created. It's stored as an Object but should represent a date and time. 
4. private String id: A unique identifier for this Commit, usually a SHA1 hash. 
5. private String branchName: The name of the branch to which this Commit belongs. 
6. private Commit parent: A reference to the parent commit, the previous commit in the history. 
7. private Commit parent2: A secondary parent reference used for merge commits which have two parents. 
8. private HashMap<String, Commit> children: A mapping from branch names to child commits, representing the children of this commit in the commit tree.
9. private CommitTree currTree: The current state of the CommitTree.
10. private HashMap<String, Blob> Blobs: A collection of Blobs (files) that are included in this commit.

#### Methods
1. Commit(String message, Commit parent, String branchName): Constructor that initializes the commit with a message, parent, and branch name.
2. String Time(): Returns a formatted string representation of the current date and time.
3. String Setid(): Generates a unique SHA1 identifier for the commit.
4. CommitTree getTree(): Getter for the current commit tree.
5. void setTree(CommitTree currTree): Setter for the current commit tree.
6. Map<String, Blob> getBlobs(): Getter for the blobs included in this commit.
7. void setBlobs(HashMap<String, Blob> blobs): Setter for the blobs.
8. Blob getBlob(String fileName): Retrieves a blob with the specified file name from this commit.
9. void addBlob(String fileName, Blob copyFile): Adds a blob to the commit.
10. String getMessage(): Getter for the commit message.
11. String getTimestamp(): Getter for the commit timestamp.
12. String getId(): Getter for the commit identifier.
13. Commit getParent(): Getter for the commit's parent.
14. HashMap<String, Commit> getChildren(): Getter for the commit's children.
15. int numberOfChildren(): Returns the number of children commits.
16. String getBranchName(): Getter for the branch name.
17. void setBranchName(String newBranchName): Setter for the branch name.
18. void addChild(Commit childCommit): Adds a child commit to the current commit.
19. void addParent(Commit parentCommit): Adds a secondary parent to the commit, used in merges.
20. boolean hasFile(String fileName): Checks if a file is present in the commit.
21. void save(): Saves the commit object to a file in the .gitlet/commit directory.

### Class 2: Blob

#### Fields

1. private byte[] content: The binary content of the blob.
2. private String id: The unique SHA1 identifier for the blob content.
3. private String fileName: The name of the file that this blob represents.

#### Methods
1. public Blob(byte[] content): Constructor that takes the content of the file, computes its SHA1 id, and saves the blob. 
2. public byte[] getContent(): Getter for the blob's content.
3. public boolean isEqualContent(byte[] otherContent): Compares the content of this blob to another array of bytes. 
4. public String getId(): Getter for the blob's SHA1 id. 
5. public void setName(String fileName): Setter for the file name associated with this blob. 
6. public String getFileName(): Getter for the file name associated with this blob. 
7. public void save(): Saves the blob to a .gitlet/blob directory, creating the directory if it doesn't exist.

### Class 3: CommitTree

#### Fields

1. private Map<String, Commit> branches: A mapping of branch names to their respective head Commit objects.
2. private Map<String, Commit> commits: A mapping of commit IDs to their respective Commit objects.
3. private Map<String, Set<String>> rmFiles: A mapping of branch names to a set of file names that are marked for removal.
4. private Commit main: The main or current Commit that the tree points to.

#### Methods
1. public CommitTree(Commit currCommit): Constructor that initializes a new CommitTree with the provided current commit, setting it as the main commit and adding it to the branches and commits maps.
Methods
2. public Map<String, Commit> getBranches(): Getter for the branches map.
3. public Map<String, Commit> getCommits(): Getter for the commits map.
4. public Map<String, Set<String>> getRmFiles(): Getter for the rmFiles map.
5. public void addRmFile(String branchName, String fileName): Adds a file to the set of files to be removed for a specific branch.
6. public void rmRmFile(String branchName, String fileName): Removes a file from the set of files to be removed for a specific branch.
7. public void addCommit(String commitId, Commit commit): Adds a commit to the commits map.
8. public void addBranch(String branchName, Commit newBranch): Adds a new branch to the branches map.
9. public Commit getMain(): Getter for the main commit.
10. public void setMain(String branchName, Commit mainCommit): Sets the main commit and updates the branch it points to.
11. public static CommitTree load(): Static method to load the commit tree from persistence.
12. public void save(): Saves the commit tree to persistence.
13. public Commit findCommit(String commitId): Finds and returns a commit by its ID. Also includes functionality to find a commit with a prefix of the full commit ID.

### Class 4: StagingArea

#### Fields

1. private Map<String, Blob> stagedFiles: A map where keys are file names and values are Blob objects for files that are staged to be committed.
2. private Map<String, Blob> stagedRmFiles: A map where keys are file names and values are Blob objects for files that are staged to be removed from the next commit.

#### Methods

1. public StagingArea(): Constructor that initializes a new StagingArea with empty maps for staged files and staged removals.
Methods
2. public void add(String fileName, Blob blob): Adds a file to the staging area for addition.
3. public void addRm(String fileName, Blob blob): Adds a file to the staging area for removal.
4. public void remove(String fileName): Removes a file from the staging area for addition.
5. public boolean contains(String fileName): Checks if a file is currently staged for addition.
6. public static StagingArea load(): Static method to load the staging area from persistence.
7. public void save(): Saves the current state of the staging area to persistence.
8. public void clear(): Clears all staged files for addition and removal.
9. public Map<String, Blob> getStagedFiles(): Getter for the map of staged files for addition.
10. public Map<String, Blob> getRmFiles(): Getter for the map of staged files for removal.
11. public void rmRmFiles(String fileName): Removes a file from the staging area for removal.

## Algorithms

### Commit Operation
1. Initialization: Create a new commit object and store the current timestamp, the commit message, and a reference to the parent commit(s).
2. Staging to Commit: Iterate over the stagedFiles map in the StagingArea. Each entry is added to the new commit's snapshot of the repository by creating a corresponding Blob object.
3. File Removal: Check the stagedRmFiles map for any files scheduled for removal and update the commit's snapshot to exclude these files.
4. Commit Tree Update: Insert the new commit into the CommitTree, which maintains a reference to each commit by its unique SHA-1 id, also updating the corresponding branch pointer.
5. Persistence: Serialize the updated CommitTree and StagingArea to their respective files, ensuring the new state is saved.

### Branch Operation
1. Branch Creation: Create a new branch by associating the name of the branch with the current commit in the CommitTree branches map.
2. Branch Deletion: Remove a branch by deleting its entry in the branches map. Additional checks ensure that you do not delete the currently checked-out branch.

### Checkout Operation
1. File Checkout: Replace the working directory file with the version in the commit if the file is different from the current commit version.
2. Branch Checkout: Update the HEAD to point to the new branch's latest commit, and update the working directory to match the snapshot of the new commit. 

### Merge Operation
1. Common Ancestor: Find the most recent common ancestor of the two branches to merge.
2. Merge Conflict Resolution: Determine changes between the common ancestor and the two branches. Apply non-conflicting changes directly, and for conflicting changes, prompt the user for resolution.
3. New Commit: Create a new commit with the merged content as its snapshot, referencing both parent commits to maintain the commit history.

### Data Persistence
Utilize Java serialization to convert the CommitTree, StagingArea, and other necessary objects into a byte stream to be stored in the file system, imitating a flat directory structure.
Deserialize objects on startup or when needed to restore the state of the repository.

### Error Handling
Use try-catch blocks to identify and respond to exceptional conditions during execution.
Define custom exceptions for specific error cases (e.g., BranchNotFoundException, MergeConflictException) to provide clear feedback to the user.
Ensure that operations are atomic where possible, so that an error in the middle of an operation does not corrupt the repository state.


