package gitlet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StagingArea implements Serializable {
    private Map<String, Blob> stagedFiles;

    private Map<String, Blob> stagedRmFiles;

    public StagingArea() {
        stagedFiles = new HashMap<>();
        stagedRmFiles = new HashMap<>();
    }

    public void add(String fileName, Blob blob) {
        stagedFiles.put(fileName, blob);
        this.save();
    }

    public void addRm(String fileName, Blob blob) {
        stagedRmFiles.put(fileName, blob);
        this.save();
    }

    public void remove(String fileName) {
        stagedFiles.remove(fileName);
        this.save();
    }

    public boolean contains(String fileName) {
        return stagedFiles.containsKey(fileName);
    }

    public static StagingArea load() {
        File fileIn = new File(".gitlet/stagingArea.ser");
        return Utils.readObject(fileIn, StagingArea.class);
    }

    public void save() {
        File fileOut = new File(".gitlet/stagingArea.ser");
        Utils.writeObject(fileOut, this);
    }

    public void clear() {
        stagedFiles = new HashMap<>();
        stagedRmFiles = new HashMap<>();
    }

    public Map<String, Blob> getStagedFiles() {
        return stagedFiles;
    }

    public Map<String, Blob> getRmFiles() {
        return stagedRmFiles;
    }

    public void rmRmFiles(String fileName) {
        stagedRmFiles.remove(fileName);
        this.save();
    }
}
