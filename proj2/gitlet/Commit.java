package gitlet;

// TODO: any imports you need here

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

    /** The message of this Commit. */
    private String message;
    /** The timestamp of this Commit. */
    private Date timestamp;
    /** Link to Parent Commit. */
    private String parent;
    /** Link to Second Parent Commit for merges. */
    private String parent1;
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
    }

    public String getCommitHashId() {
        if (parent1 == null) {
            return sha1(message, timestamp, trackedFiles, parent);
        }
        return sha1(message, timestamp, trackedFiles, parent, parent1);
    }
}
