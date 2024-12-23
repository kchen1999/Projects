package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kevin
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
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
    /**
     * Each commit (rectangle) points to some blobs (circles), which contain file contents
     * The commits contain the file names and references to these blobs, as well as a parent link.
     * These references, depicted as arrows, are represented in the .gitlet directory using their SHA-1 hash values
     * (the small hexadecimal numerals above the commits and below the blobs).
     * Your commit class will somehow store all of the information that this diagram shows:
     * a careful selection of internal data structures will make the implementation easier or harder
     * */
    /** a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference
     * every commit in our case–has a unique integer id that serves as a reference to the object
     * In the case of commits, it means the same metadata, the same mapping of names to references,
     * and the same parent reference.
     * Include all metadata and references when hashing a commit.
     * Distinguishing somehow between hashes for commits and hashes for blobs.
     * A good way of doing this involves a well-thought out directory structure within the .gitlet directory.
     * Another way to do so is to hash in an extra word for each object that has one value for blobs
     * and another for commits.
     * */

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

    public Commit(String message, String parent) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        this.message = message;
        this.parentUID = parent;
        this.parent1UID = null;
        if (parent == null) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
        this.trackedFiles = new HashMap<>();
        setCommitUID();
    }

    /*TODO:
       A commit will only update the contents of files it is tracking that have been staged for addition at the
     *  time of commit, in which case the commit will now include the version of the file that was staged
     *  instead of the version it got from its parent.
     *  A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     *  Finally, files tracked in the current commit may be untracked in the new commit as a result being
     *  staged for removal by the rm command (below).
     *  If no files have been staged, abort. Print the message No changes added to the commit.
     */

    public void updateFileContents(HashMap<String, String> trackedFiles) {
        //this.trackedFiles = trackedFiles; //this is the culprit - trackedFiles is a pointer! 2nd time mistake!
        for (String fileName : trackedFiles.keySet()) {
            this.trackedFiles.put(fileName, trackedFiles.get(fileName));
        }
        HashMap<String, String> additions = additionsFromFile();
        HashMap<String, String> removals = removalsFromFile();
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

    private static HashMap additionsFromFile() {
        File additionsFile = join(".gitlet", "stagingArea", "additions");
        return readObject(additionsFile, HashMap.class);
    }

    private static HashMap removalsFromFile() {
        File removalsFile = join(".gitlet", "stagingArea", "removals");
        return readObject(removalsFile, HashMap.class);
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
}
