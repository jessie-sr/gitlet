package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class Blob implements Serializable {
    private byte[] content;

    private String id;

    private String fileName;


    public Blob(byte[] content) {
        this.content = content;
        this.id = Utils.sha1(content);
        this.save();
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isEqualContent(byte[] otherContent) {
        return Arrays.equals(content, otherContent);
    }

    public String getId() {
        return this.id;
    }

    public void setName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void save() {
        File commitDir = new File(".gitlet/blob");
        if (!commitDir.exists()) {
            commitDir.mkdir();
        }
        File fileOut = new File(".gitlet/blob/" + this.getId());
        Utils.writeObject(fileOut, this);
    }
}
