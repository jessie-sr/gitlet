package gitlet;

import org.reflections.vfs.Vfs;

import java.io.File;
import java.util.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Rong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // If args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                add(args[1]);
                break;
            case "commit":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                commit(args[1]);
                break;
            case "restore":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                if (args.length == 3) {
                    restore(null, args[2]);
                } else if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                } else {
                    restore(args[1], args[3]);
                }
                break;
            case "log":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                log();
                break;
            case "global-log":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                globalLog();
                break;
            case "status":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                status();
                break;
            case "rm":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                rm(args[1]);
                break;
            case "find":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                find(args[1]);
                break;
            case "branch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                branch(args[1]);
                break;
            case "switch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                switchBranch(args[1]);
                break;
            case "rm-branch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                rmBranch(args[1]);
                break;
            case "reset":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                reset(args[1]);
                break;
            case "merge":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    public static boolean isInitialized() {
        File gitletDir = new File(System.getProperty("user.dir"), ".gitlet");
        return gitletDir.exists() && gitletDir.isDirectory();
    }

    public static void init() {
        File gitletDir = new File(Repository.CWD, ".gitlet");
        if (gitletDir.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        gitletDir.mkdir();
        Commit initCommit = new Commit("initial commit", null, "main");
        CommitTree treeSystem = initCommit.getTree();
        Utils.writeObject(new File(gitletDir, "commitTree"), treeSystem);
        treeSystem.save();
        StagingArea stagingArea = new StagingArea();
        stagingArea.save();
    }

    public static void add(String fileName) {
        CommitTree commitTree = CommitTree.load();
        StagingArea stagingArea = StagingArea.load();
        Set<String> rmFiles = stagingArea.getRmFiles().keySet();
        if (rmFiles.contains(fileName)) {
            stagingArea.rmRmFiles(fileName);
        }
        File targetFile = new File(Repository.CWD, fileName);
        if (!targetFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            if (stagingArea.contains(fileName)) {
                stagingArea.remove(fileName);
            }
            Commit currCommit = commitTree.getMain();
            Blob currentBlob = currCommit.getBlob(fileName);
            byte[] fileBytes = Utils.readContents(targetFile);
            if (currentBlob != null && currentBlob.isEqualContent(fileBytes)) {
                // The file content is identical to the current commit, no need to stage
                stagingArea.save();
                return;
            }
            byte[] copyBytes = new byte[fileBytes.length];
            System.arraycopy(fileBytes, 0, copyBytes, 0, fileBytes.length);
            Blob newBlob = new Blob(copyBytes);
            stagingArea.add(fileName, newBlob);
            stagingArea.save();
            commitTree.save();
        }
    }

    public static void commit(String message) {
        StagingArea stagingArea = StagingArea.load();
        if (stagingArea.getStagedFiles().isEmpty() && stagingArea.getRmFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        CommitTree commitTree = CommitTree.load();
        Commit parentCommit = commitTree.getMain();
        Map<String, Blob> stagedFiles = stagingArea.getStagedFiles();
        Commit newCommit = new Commit(message, parentCommit, parentCommit.getBranchName());
        Map<String, Blob> parentBlobs = parentCommit.getBlobs();
        for (Map.Entry<String, Blob> entry: parentBlobs.entrySet()) {
            String fileName = entry.getKey();
            Blob blob = entry.getValue();
            File file = new File(Repository.CWD, fileName);
            if (file.exists()) {
                newCommit.addBlob(fileName, blob);
            }
        }
        newCommit.getBlobs().putAll(parentCommit.getBlobs());
        for (Map.Entry<String, Blob> entry: stagedFiles.entrySet()) {
            String fileName = entry.getKey();
            Blob blob = entry.getValue();
            newCommit.addBlob(fileName, blob);
        }
        stagingArea.clear();
        stagingArea.save();
        commitTree.setMain(newCommit.getBranchName(), newCommit);
        commitTree.save();
    }

    public static void restore(String commitId, String fileName) {
        CommitTree commitTree = CommitTree.load();
        Commit parentCommit;

        if (commitId == null) {
            parentCommit = commitTree.getMain();
        } else {
            parentCommit = commitTree.findCommit(commitId);
            if (parentCommit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
        }

        Blob fileBlob = parentCommit.getBlob(fileName);
        if (fileBlob == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        byte[] fileContent = fileBlob.getContent();
        File restoredFile = new File(Repository.CWD, fileName);
        Utils.writeContents(restoredFile, fileContent);
    }

    public static void log() {
        CommitTree commitTree = CommitTree.load();
        Commit commit = commitTree.getMain();

        while (commit != null) {
            System.out.println("===");
            System.out.println("commit " + commit.getId());

            // Print merge commit information if applicable
//            if (commit.getParents().size() > 1) {
//                System.out.print("Merge: ");
//                System.out.print(commit.getParents().get(0).getId().substring(0, 7) + " ");
//                System.out.println(commit.getParents().get(1).getId().substring(0, 7));
//            }

            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();

            commit = commit.getParent();
        }
    }

    public static void globalLog() {
        CommitTree commitTree = CommitTree.load();
        Map<String, Commit> commits = commitTree.getCommits();

        for (Commit commit: commits.values()) {
            System.out.println("===");
            System.out.println("commit " + commit.getId());

            // Print merge commit information if applicable
//            if (commit.getParents().size() > 1) {
//                System.out.print("Merge: ");
//                System.out.print(commit.getParents().get(0).getId().substring(0, 7) + " ");
//                System.out.println(commit.getParents().get(1).getId().substring(0, 7));
//            }

            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public static void status() {
        CommitTree commitTree = CommitTree.load();
        Commit currMain = commitTree.getMain();
        Map<String, Commit> branches = commitTree.getBranches();
        System.out.println("=== Branches ===");
        for (String branchName: branches.keySet()) {
            if (branchName.equals(currMain.getBranchName())) {
                System.out.println("*"+branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        StagingArea stagingArea = StagingArea.load();
        Map<String, Blob> stagedFiles = stagingArea.getStagedFiles();
        Set<String> sortedStagedFiles = new TreeSet<>(stagedFiles.keySet());
        System.out.println("=== Staged Files ===");
        for (String fileName: sortedStagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Set<String> rmFiles = stagingArea.getRmFiles().keySet();
        for (String fileName: rmFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void rm(String fileName) {
        StagingArea stagingArea = StagingArea.load();
        Map<String, Blob> stagedFiles = stagingArea.getStagedFiles();
        CommitTree commitTree = CommitTree.load();
        Commit currCommit = commitTree.getMain();
        commitTree.addRmFile(currCommit.getBranchName(), fileName);
        Map<String, Blob> currBlobs = currCommit.getBlobs();
        // Check if the file is currently staged for addition, and unstage it if it is.
        if (stagedFiles.containsKey(fileName)) {
            stagingArea.remove(fileName);
        }
        // Check if the file is tracked in the current commit, and stage it for removal.
        else if (currBlobs.containsKey(fileName)) {
            Blob rmBlob = currBlobs.get(fileName);
            stagingArea.addRm(fileName, rmBlob);
            // Remove the file from the working directory if the user has not already done so.
            File rmFile = new File(Repository.CWD, fileName);
            if (rmFile.exists()) {
                rmFile.delete();
            }
        }
        // Print an error message if the file is neither staged nor tracked by the head commit.
        else {
            commitTree.rmRmFile(currCommit.getBranchName(), fileName);
            System.out.println("No reason to remove the file.");
        }
    }

    public static void find(String commitMessage) {
        List<String> commitList = Utils.plainFilenamesIn(".gitlet/commit");
        CommitTree commitTree = CommitTree.load();
        boolean commitExist = false;
        for (String commitId: commitList) {
            Commit commit = commitTree.findCommit(commitId);
            if (commit.getMessage().equals(commitMessage)) {
                commitExist = true;
                System.out.println(commitId);
            }
        }
        if (!commitExist) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branch(String branchName) {
        CommitTree commitTree = CommitTree.load();
        Map<String, Commit> currBranches = commitTree.getBranches();
        if (currBranches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Commit currCommit = commitTree.getMain();
        commitTree.addBranch(branchName, currCommit);
        commitTree.save();
    }

    public static void switchBranch(String branchName) {
        StagingArea stagingArea = StagingArea.load();
        CommitTree commitTree = CommitTree.load();
        Commit currMain = commitTree.getMain();
        Map<String, Commit> currBranches = commitTree.getBranches();
        // Check if the branch with the given branchName exists.
        if (!currBranches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        Commit newMain = currBranches.get(branchName);
        // Check if that branch is the current branch.
        if (branchName.equals(currMain.getBranchName())) {
            System.out.println("No need to switch to the current branch.");
            return;
        }
        // Check if a working file in the branch to be switched to is untracked in the current branch.
        Map<String, Blob> newMainBlobs = newMain.getBlobs();
        Map<String, Blob> currMainBlobs = currMain.getBlobs();
        for (String fileName: newMainBlobs.keySet()) {
            File overwrittenFile = new File(Repository.CWD, fileName);
            Blob b = newMainBlobs.get(fileName);
            if (overwrittenFile.isFile() && b != null && !b.isEqualContent(Utils.readContents(overwrittenFile)) && !currMainBlobs.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        // Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist.
        for (String fileName: newMainBlobs.keySet()) {
            Blob b = newMainBlobs.get(fileName);
            File overwrittenFile = new File(Repository.CWD, fileName);
            Utils.writeContents(overwrittenFile, (Object) b.getContent());
        }
        // Delete any files that are tracked in the current branch but are not present in the checked-out branch.
        for (String fileName: currMainBlobs.keySet()) {
            Set<String> rmFiles = commitTree.getRmFiles().get(branchName);
            if (!newMainBlobs.containsKey(fileName) || rmFiles != null && rmFiles.contains(fileName)) {
                File deletedFile = new File(Repository.CWD, fileName);
                deletedFile.delete();
            }
        }
        commitTree.setMain(branchName, newMain);
        commitTree.save();
        stagingArea.clear();
        stagingArea.save();
    }

    public static void rmBranch(String branchName) {
        CommitTree commitTree = CommitTree.load();
        Map<String, Commit> currBranches = commitTree.getBranches();
        // Check if a branch with the given name exists.
        if (!currBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        // Check if the branch to be removed is the current branch.
        Commit currMain = commitTree.getMain();
        Commit removedBranch = currBranches.get(branchName);
        if (currMain.equals(removedBranch)) {
            System.out.println("Cannot remove the current branch.");
        }
        // Delete the branch (pointer to the Commit) with the given name.
        currBranches.remove(branchName);
        commitTree.save();
    }

    public static void reset(String commitId) {
        StagingArea stagingArea = StagingArea.load();
        Map<String, Blob> stagedFiles = stagingArea.getStagedFiles();
        CommitTree commitTree = CommitTree.load();
        // Check if the commit with the given commitId exists.
        Commit targetCommit = commitTree.findCommit(commitId);
        if (targetCommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        // Check if a working file is untracked in the current branch and would be overwritten by the reset.
        Map<String, Blob> targetBlobs = targetCommit.getBlobs();
        Commit currMain = commitTree.getMain();
        Map<String, Blob> mainBlobs = currMain.getBlobs();
        for (String blobName : targetBlobs.keySet()) {
            File file = new File(Repository.CWD, blobName);
            Blob b = targetBlobs.get(blobName);
            if (file.isFile() && b != null && !b.isEqualContent(Utils.readContents(file)) && !mainBlobs.containsKey(blobName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        // Restore all the files tracked by the given commit.
        for (String blobName : targetBlobs.keySet()) {
            restore(commitId, blobName);
        }
        for (String fileName : stagedFiles.keySet()) {
            if (!targetBlobs.containsKey(fileName)) {
                rm(fileName);
            }
        }
        // Move the current branch’s head to that commit node.
        commitTree.setMain(commitTree.getMain().getBranchName(), targetCommit);
        commitTree.save();
        stagingArea.clear();
        stagingArea.save();
    }

    public static void merge(String branchName) {
        StagingArea stagingArea = StagingArea.load();
        CommitTree commitTree = CommitTree.load();
        Commit mainBranch = commitTree.getMain();
        if (!commitTree.getBranches().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!stagingArea.getStagedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        Commit givenBranch = commitTree.getBranches().get(branchName);
        Map<String, Blob> mainBlobs = mainBranch.getBlobs();
        Map<String, Blob> givenBlobs = givenBranch.getBlobs();
        for (String fileName: givenBlobs.keySet()) {
            File overwrittenFile = new File(Repository.CWD, fileName);
            Blob b = givenBlobs.get(fileName);
            if (overwrittenFile.isFile() && b != null && !b.isEqualContent(Utils.readContents(overwrittenFile)) && !mainBlobs.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        Commit splitPoint = findSplitPoint(branchName, mainBranch);
        if (mainBranch.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        // Check if the split point is the same commit as the given branch.
        if (splitPoint.equals(givenBranch)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        // Check if the split point is the current branch.
        if (splitPoint.equals(mainBranch)) {
            System.out.println("Current branch fast-forwarded.");
            switchBranch(branchName);
            return;
        }
        Map<String, Blob> spBlobs = splitPoint.getBlobs();
        for (String fileName: mainBlobs.keySet()) {
            stagingArea.add(fileName, mainBlobs.get(fileName));
            if (commitTree.getRmFiles().get(mainBranch.getBranchName()) != null && commitTree.getRmFiles().get(mainBranch.getBranchName()).contains(fileName)) {
                stagingArea.remove(fileName);
                File rmFile = new File(Repository.CWD, fileName);
                if (rmFile.exists()) {
                    rmFile.delete();
                }
            }
            if (commitTree.getRmFiles().get(branchName) != null && commitTree.getRmFiles().get(branchName).contains(fileName)) {
                File rmFile = new File(Repository.CWD, fileName);
                if (rmFile.exists()) {
                    rmFile.delete();
                }
            }
        }
        for (String fileName: givenBlobs.keySet()) {
            Blob givenBlob = givenBlobs.get(fileName);
            // Any files that were not present at the split point and are present only in the given branch should be checked out and staged.
            if (!spBlobs.containsKey(fileName) && !mainBlobs.containsKey(fileName)) {
                stagingArea.add(fileName, givenBlob);
                restore(givenBranch.getId(), fileName);
            }
            if (spBlobs.containsKey(fileName) && mainBlobs.containsKey(fileName)) {
                Blob mainBlob = mainBlobs.get(fileName);
                Blob spBlob = spBlobs.get(fileName);
                // Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch, then all be automatically staged.
                if (!givenBlob.isEqualContent(spBlob.getContent()) && mainBlob.isEqualContent(spBlob.getContent())) {
                    restore(givenBranch.getId(), fileName);
                }
                if (commitTree.getRmFiles().get(branchName) != null && commitTree.getRmFiles().get(branchName).contains(fileName)) {
                    stagingArea.remove(fileName);
                    File rmFile = new File(Repository.CWD, fileName);
                    if (rmFile.exists()) {
                        rmFile.delete();
                    }
                }
            }
        }
        for (String fileName: spBlobs.keySet()) {
            if (!givenBlobs.containsKey(fileName) && mainBlobs.containsKey(fileName)) {
                Blob mainBlob = mainBlobs.get(fileName);
                Blob spBlob = spBlobs.get(fileName);
                if (mainBlob.isEqualContent(spBlob.getContent())) {
                    rm(fileName);
                }
            }
        }
        boolean conflict = false;
        Set<String> fileNames = fileSet(givenBlobs, mainBlobs);
        for (String fileName: fileNames) {
            conflict = isInConflict(givenBlobs, mainBlobs, spBlobs, fileName);
        }
        String commitMessage = "Merged " + branchName + " into " + mainBranch.getBranchName() + ".";
        mergeCommit(commitMessage, branchName);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }

    }

    public static void mergeCommit(String message, String branchName) {
        StagingArea stagingArea = StagingArea.load();
        CommitTree commitTree = CommitTree.load();
        Commit parentCommit = commitTree.getMain();
        Commit givenCommit = commitTree.getBranches().get(branchName);
        Map<String, Blob> stagedFiles = stagingArea.getStagedFiles();
        Commit newCommit = new Commit(message, parentCommit, parentCommit.getBranchName());
        newCommit.addParent(givenCommit);
        for (Map.Entry<String, Blob> entry: stagedFiles.entrySet()) {
            String fileName = entry.getKey();
            Blob blob = entry.getValue();
            newCommit.addBlob(fileName, blob);
        }
        stagingArea.clear();
        stagingArea.save();
        commitTree.setMain(newCommit.getBranchName(), newCommit);
        commitTree.save();
    }

    public static Set<String> fileSet(Map<String, Blob> givenBlobs, Map<String, Blob> mainBlobs) {
        Set<String> fileNames = new TreeSet<>(givenBlobs.keySet());
        for (String fileName: mainBlobs.keySet()) {
            if (!fileNames.contains(fileName)) {
                fileNames.add(fileName);
            }
        }
        return fileNames;
    }

    public static boolean isInConflict(Map<String, Blob> givenBlobs, Map<String, Blob> mainBlobs, Map<String, Blob> spBlobs, String fileName) {
        boolean conflict = false;
        // Contents of both branches are changed from the split point and differ from each other.
        File file = new File(Repository.CWD, fileName);
        if (file.exists() && givenBlobs.containsKey(fileName) && mainBlobs.containsKey(fileName) && spBlobs.containsKey(fileName)) {
            Blob mainBlob = mainBlobs.get(fileName);
            Blob givenBlob = givenBlobs.get(fileName);
            Blob spBlob = spBlobs.get(fileName);
            if (!givenBlob.isEqualContent(spBlob.getContent()) && !mainBlob.isEqualContent(spBlob.getContent()) && !givenBlob.isEqualContent(mainBlob.getContent())) {
                conflict = true;
                treatConflict(mainBlob, givenBlob, fileName);
            }
        }
        // File was absent at the split point and has different contents in the given and current branches.
        else if (file.exists() && !spBlobs.containsKey(fileName) && givenBlobs.containsKey(fileName) && mainBlobs.containsKey(fileName)) {
            Blob mainBlob = mainBlobs.get(fileName);
            Blob givenBlob = givenBlobs.get(fileName);
            if (!givenBlob.isEqualContent(mainBlob.getContent())) {
                conflict = true;
                treatConflict(mainBlob, givenBlob, fileName);
            }
        }
        // The contents of file in the given branch are changed and the one in main branch is deleted.
        else if (!file.exists() && !mainBlobs.containsKey(fileName) && givenBlobs.containsKey(fileName) && spBlobs.containsKey(fileName) ) {
            Blob spBlob = spBlobs.get(fileName);
            Blob givenBlob = givenBlobs.get(fileName);
            if (!givenBlob.isEqualContent(spBlob.getContent())) {
                conflict = true;
                treatConflict(null, givenBlob, fileName);
            }
        }
        // The contents of file in main branch are changed and the one in the given branch is deleted,
        else if (!file.exists() && spBlobs.containsKey(fileName)) {
            Blob spBlob = spBlobs.get(fileName);
            Blob mainBlob = mainBlobs.get(fileName);
            if (!mainBlob.isEqualContent(spBlob.getContent())) {
                conflict = true;
                treatConflict(mainBlob, null, fileName);
            }
        }
        return conflict;
    }

    public static boolean mainChanged(File file, String fileName, Map<String, Blob> mainBlobs, Map<String, Blob> spBlobs) {
        if (file.exists() && mainBlobs.containsKey(fileName)) {
            if (spBlobs.containsKey(fileName)) {
                Blob spBlob = spBlobs.get(fileName);
                Blob mainBlob = mainBlobs.get(fileName);
                if (!spBlob.isEqualContent(mainBlob.getContent())) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static void treatConflict(Blob blob1, Blob blob2, String fileName) {
        StagingArea stagingArea = StagingArea.load();
        String content1 = "";
        String content2 = "";
        if (blob1 != null) {
            content1 += new String(blob1.getContent());
        }
        if (blob2 != null) {
            content2 = new String(blob2.getContent());
        }
        String newContent = "<<<<<<< HEAD\n" + content1 + "=======\n" + content2 + ">>>>>>>\n";
        File newFile = new File(Repository.CWD, fileName);
        byte[] newBytes = newContent.getBytes();
        Utils.writeContents(newFile, newBytes);
        Blob newblob = new Blob(newBytes);
        stagingArea.add(fileName, newblob);
        System.out.println("Encountered a merge conflict.");
    }

    public static Commit findSplitPoint(String branchName, Commit mainBranch) {
        CommitTree commitTree = CommitTree.load();
        Commit givenBranch = commitTree.getBranches().get(branchName);
        while ((givenBranch.getParent() != null)) {
            if (givenBranch.getParent().getId().equals(mainBranch.getId())) {
                return mainBranch;
            }
            givenBranch = givenBranch.getParent();
        }
        givenBranch = commitTree.getBranches().get(branchName);
        if ((mainBranch.getParent() != null) && (mainBranch.getParent().getParent() == null) && givenBranch.getParent() != null && mainBranch.getParent().getId().equals(givenBranch.getParent().getId())) {
            return mainBranch.getParent();
        }
        while ((mainBranch.getParent() != null) && (!mainBranch.getParent().getBlobs().isEmpty()) && (!mainBranch.getId().equals(givenBranch.getId()) || mainBranch.numberOfChildren() < 2 || !mainBranch.getChildren().containsKey(givenBranch))) {
            mainBranch = mainBranch.getParent();
        }
        return mainBranch;
    }
}
