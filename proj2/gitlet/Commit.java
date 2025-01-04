package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kevin
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    /** The hash ID of this commit. */
    private String commitUID;
    /** The message of this Commit. */
    private String message;
    /** The timestamp of this Commit. */
    private Date timestamp;
    /** Link to Parent Commit. */
    private String parentUID;
    /** Link to Second Parent Commit for merges. */
    private String parent1UID;
    /** Mapping of file names to blob references*/
    private HashMap<String, String> trackedFiles;
    /** Marks a commit that is a split point of two branches*/
    private boolean isSplitPoint;

    public void setCommitUID() {
        if (parentUID == null) {
            this.commitUID = sha1(message, serialize(timestamp), serialize(trackedFiles));
        }
        else if (parent1UID == null) {
            this.commitUID = sha1(message, serialize(timestamp), serialize(trackedFiles), parentUID);
        } else {
            this.commitUID = sha1(message, serialize(timestamp), serialize(trackedFiles), parentUID, parent1UID);
        }
    }

    public void setSplitPoint() {
        this.isSplitPoint = true;
    }

    public Commit(String message, String parent) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        this.message = message;
        this.parentUID = parent;
        this.parent1UID = null;
        this.isSplitPoint = false;
        if (parent == null) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
        this.trackedFiles = new HashMap<>();
        setCommitUID();
    }

    public Commit(String message, String parent, String parent1) {
        this.message = message;
        this.parentUID = parent;
        this.parent1UID = parent1;
        this.isSplitPoint = false;
        this.timestamp = new Date();
        this.trackedFiles = new HashMap<>();
        setCommitUID();
    }

    public void updateFileContents(HashMap<String, String> trackedFiles, Boolean isMerge) {
        for (String fileName : trackedFiles.keySet()) {
            this.trackedFiles.put(fileName, trackedFiles.get(fileName));
        }
        TreeMap<String, String> additions = additionsFromFile();
        TreeMap<String, String> removals = removalsFromFile();
        if (additions.isEmpty() && removals.isEmpty() && isMerge) {
            return;
        }
        if (additions.isEmpty() && removals.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        for (String fileName : additions.keySet()) {
            String blobUID = additions.get(fileName);
            this.trackedFiles.put(fileName, blobUID);
        }
        for (String fileName: removals.keySet()) {
            this.trackedFiles.remove(fileName);
        }
        setCommitUID();
    }

    private static TreeMap additionsFromFile() {
        File additionsFile = join(".gitlet", "stagingArea", "additions");
        return readObject(additionsFile, TreeMap.class);
    }

    private static TreeMap removalsFromFile() {
        File removalsFile = join(".gitlet", "stagingArea", "removals");
        return readObject(removalsFile, TreeMap.class);
    }

    public HashMap<String, String> getTrackedFiles() {
        return this.trackedFiles;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getParentUID() {
        return parentUID;
    }

    public String getParent1UID() {
        return parent1UID;
    }

    public String getCommitUID() {
        return this.commitUID;
    }

    public Boolean isSplitPoint() {
        return this.isSplitPoint;
    }
}
