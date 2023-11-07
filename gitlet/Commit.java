package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Rong
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;


    private String author;

    private Object timeStamp;

    private String id;

    private String branchName;

    private Commit parent;

    private Commit parent2;

    private HashMap<String, Commit> children;

    private CommitTree currTree;

    private HashMap<String, Blob> Blobs;

    public Commit(String message, Commit parent, String branchName) {
        this.message = message;
        this.parent = parent;
        this.children = new HashMap<>();
        this.branchName = branchName;
        this.author = System.getProperty("user.name");
        this.id = Setid();
        this.Blobs = new HashMap<>();
        if (!Main.isInitialized()) {
            this.timeStamp = "00:00:00 UTC, Thursday, 1 January 1970";
        } else {
            this.timeStamp = Time();
        }
        if (parent != null) {
            this.setTree(parent.getTree());
            parent.addChild(this);
            parent.save();
        } else {
            CommitTree tree = new CommitTree(this);
            this.currTree = tree;
        }
        this.save();
        this.currTree.addCommit(this.id, this);
    }

    private String Time() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    private String Setid() {
        double vals = Math.random();
        return Utils.sha1(Double.toString(vals));
    }

    public CommitTree getTree() {
        return currTree;
    }
    public void setTree(CommitTree currTree) {
        this.currTree = currTree;
    }

    public Map<String, Blob> getBlobs() {
        return Blobs;
    }

    public void setBlobs(HashMap<String, Blob> blobs) {
        this.Blobs = blobs;
    }

    public Blob getBlob(String fileName) {
        return Blobs.getOrDefault(fileName, null);
    }

    public void addBlob(String fileName, Blob copyFile) {
        Blobs.put(fileName, copyFile);
        copyFile.setName(fileName);
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timeStamp.toString();
    }

    public String getId() {
        return id;
    }

    public Commit getParent() {
        return parent;
    }

    public HashMap<String, Commit> getChildren() {
        return children;
    }

    public int numberOfChildren() {
        return children.size();
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String newBranchName) {
        this.branchName = newBranchName;
    }

    public void addChild(Commit childCommit) {
        this.children.put(childCommit.getBranchName(), childCommit);
    }

    public void addParent(Commit parentCommit) {
        this.parent2 = parentCommit;
    }

    public boolean hasFile(String fileName) {
        if (Blobs.containsKey(fileName)) {
            return true;
        } else {
            return false;
        }
    }

    public void save() {
        File commitDir = new File(".gitlet/commit");
        if (!commitDir.exists()) {
            commitDir.mkdir();
        }
        File fileOut = new File(".gitlet/commit/" + this.getId());
        Utils.writeObject(fileOut, this);
    }
}
