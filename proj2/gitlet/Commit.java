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
    private String parent;
    /** Link to Second Parent Commit for merges. */
    private String parent1;
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
        if (parent1 == null) {
            this.commitUID = sha1(message, serialize(timestamp), serialize(trackedFiles), parent);
        }
        this.commitUID = sha1(message, serialize(timestamp), serialize(trackedFiles), parent, parent1);
    }


    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.parent1 = null;
        if (parent == null) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
        this.trackedFiles = new HashMap<String, String>();
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

    private static HashMap additionsFromFile() {
        File additionsDir = join(".gitlet", "stagingArea", "additions");
        return readObject(additionsDir, HashMap.class);
    }

    private static HashMap removalsFromFile() {
        File removalsDir = join(".gitlet", "stagingArea", "removal");
        return readObject(removalsDir, HashMap.class);
    }

    public HashMap getTrackedFiles() {
        return this.trackedFiles;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }

    public String getParent1() {
        return parent1;
    }

    public void updateFileContents() {
        HashMap<String, String> stagedForAddition = additionsFromFile();
        HashMap<String, String> stagedForRemoval = removalsFromFile();
        if (stagedForAddition.isEmpty()) {
            System.out.println("No changes added to the commit");
            System.exit(0);
        }
        for (String fileName : stagedForAddition.keySet()) {
            String blobReference = stagedForAddition.get(fileName);
            this.trackedFiles.put(fileName, blobReference);
        }
        for (String fileName: stagedForRemoval.keySet()) {
            this.trackedFiles.remove(fileName);
        }
    }

    public void updateCommit(String message, String parent) {
        updateFileContents();
        if (message == "") {
            System.out.println("Please enter a commit message");
            System.exit(0);
        }
        this.message = message;
        this.parent = parent;
        this.timestamp = new Date();
    }

    public String getCommitHashId() {
        return this.commitUID;
    }
}
