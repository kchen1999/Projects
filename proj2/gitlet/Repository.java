package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /*
    * TODO: create initial commit
    *  Initial message: initial commit
    *  Timestamp for initial commit: 00:00:00 UTC, Thursday, 1 January 1970
    * TODO: single branch -> intialize master branch which initially points to this initial commit
    *  and master will be the current branch - current branch?
    *  What is UID?
    *   If there is already a Gitlet version-control system in the current directory, it should abort.
    *   It should NOT overwrite the existing system with a new one. Should print the error message
    *   A Gitlet version-control system already exists in the current directory.
    * */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        File commits = join(GITLET_DIR, "commits");
        Commit initialCommit = new Commit("initial commit", null);
        Utils.writeObject(commits, initialCommit);



    }


}
