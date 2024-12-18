package gitlet;

import java.io.File;
import java.util.HashMap;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kevin
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
    /** The commit tree - mapping of commit references to their commit. */
    public static HashMap<String, Commit> commitTree = new HashMap<>();
    /** Mapping of branch names to references to commits */
    public static HashMap<String, String> branchMap = new HashMap<>();
    /** Staging area: Mapping of file names to references to files */
    public static HashMap<String, String> stagedForAddition;
    /** Staging area: Mapping of file names to references to files */
    public static HashMap<String, String> stagedForRemoval;
    /** Mapping of blob references to file contents **/
    public static HashMap<String, File> blobs;
    /** The current branch */
    public static String currentBranch;

    /**
     * Does required file system operations to set up for persistence
     * (creates any necessary folders or files)
     *   .gitlet/
     *   - stagingArea/
     *      - addition
     *      - removal
     *   - commits/
     *   - blobs/?
     * */

    public static void setUpPersistence () {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();

        File stagingAreaDir = join(GITLET_DIR, "stagingArea");
        File blobsDir = join(GITLET_DIR, "blobs");
        stagingAreaDir.mkdir();
        blobsDir.mkdir();

        File additionsDir = join(GITLET_DIR, "stagingArea", "additions");
        File removalsDir = join(GITLET_DIR, "stagingArea", "removals");
        additionsDir.mkdir();
        removalsDir.mkdir();

    }

    private static HashMap commitsFromFile() {
        File inFile = join(GITLET_DIR, "commits");
        return readObject(inFile, HashMap.class);
    }

    private static HashMap branchesFromFile() {
        File inFile = join(GITLET_DIR, "branches");
        return readObject(inFile, HashMap.class);
    }

    private static String currentBranchFromFile() {
        File inFile = join(GITLET_DIR, "currentBranch");
        return readObject(inFile, String.class);
    }

    private static HashMap additionsFromFile() {
        File additionsDir = join(GITLET_DIR, "stagingArea", "additions");
        return readObject(additionsDir, HashMap.class);
    }

    private static HashMap removalsFromFile() {
        File removalsDir = join(GITLET_DIR, "stagingArea", "removal");
        return readObject(removalsDir, HashMap.class);
    }

    private static HashMap blobsFromFile() {
        File inFile = join(GITLET_DIR, "blobs");
        return readObject(inFile, HashMap.class);
    }

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
        File commits = join(GITLET_DIR, "commits");
        File branches = join(GITLET_DIR, "branches");
        File currentBranchFile = join(GITLET_DIR, "currentBranch");

        Commit initialCommit = new Commit("initial commit", null);
        String initialCommitHash = initialCommit.getCommitHashId();
        commitTree.put(initialCommitHash, initialCommit);
        branchMap.put("master", initialCommitHash);
        currentBranch = "master";

        writeObject(commits, commitTree);
        writeObject(branches, branchMap);
        writeObject(currentBranchFile, currentBranch);
    }

    /*
     * Each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files
     * TODO: clone parent commit
     * TODO: After the commit command, the new commit is added as a new node in the commit tree - commit tree????
     * TODO: staging area is cleared after a commit.
     * TODO: The commit just made becomes the “current commit”, and the head pointer now points to it.
     *  The previous head commit is this commit’s parent commit.
     * TODO: Each commit is identified by its SHA-1 id, which must include the file (blob) references of its files,
     *  parent reference, log message, and commit time
     * TODO: A commit will only update the contents of files it is tracking that have been staged for addition at the
     *  time of commit, in which case the commit will now include the version of the file that was staged
     *  instead of the version it got from its parent.
     *  A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     *  Finally, files tracked in the current commit may be untracked in the new commit as a result being
     *  staged for removal by the rm command (below).
     * The commit command never adds, changes, or removes files in the working directory (other than those in the .gitlet directory).
     * The rm command will remove such files, as well as staging them for removal, so that they will be untracked
     * after a commit.
     * Any changes made to files after staging for addition or removal are ignored by the commit command,
     * which only modifies the contents of the .gitlet directory.
     * TODO: Each commit should contain the date and time it was made.
     *  If no files have been staged, abort. Print the message No changes added to the commit.
     *  Every commit must have a non-blank message.
     *  If it doesn’t, print the error message Please enter a commit message.
     */

    private static String getHeadCommitId() {
        branchMap = branchesFromFile();
        currentBranch = currentBranchFromFile();
        return branchMap.get(currentBranch);
    }

    private static Commit getHeadCommit() {
        commitTree = commitsFromFile();
        String headCommitId = getHeadCommitId();
        return commitTree.get(headCommitId);
    }

    public static void clearStagingArea() {
        File additionsDir = join(GITLET_DIR, "stagingArea", "additions");
        File removalsDir = join(GITLET_DIR, "stagingArea", "removal");
        HashMap<String, String> stagedForAddition = additionsFromFile();
        HashMap<String, String> stagedForRemoval = removalsFromFile();
        stagedForAddition.clear();
        stagedForRemoval.clear();
        writeObject(additionsDir, stagedForAddition);
        writeObject(removalsDir, stagedForRemoval);
    }

    public static void commit(String message) {
        File commits = join(GITLET_DIR, "commits");
        File branches = join(GITLET_DIR, "branches");
        commitTree = commitsFromFile();
        String headCommitId = getHeadCommitId();
        Commit parentCommit = commitTree.get(headCommitId);
        Commit newCommit = parentCommit;
        newCommit.updateCommit(message, headCommitId);
        String newCommitHash = newCommit.getCommitHashId();
        commitTree.put(newCommitHash, newCommit);
        branchMap.put(currentBranch, newCommitHash);
        writeObject(commits, commitTree);
        writeObject(branches, branchMap);
        clearStagingArea();
    }


    /*
    * TODO: Adds a copy of the file as it currently exists to the staging area
    * TODO: Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
    * TODO: If the current working version of the file is identical to the version in the current commit,
    *  do not stage it to be added, and remove it from the staging area if it is already there
    *  (as can happen when a file is changed, added, and then changed back to it’s original version).
     * The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
     *  If the file does not exist, print the error message File does not exist. and exit without changing anything.
     */

    public static void add(String fileName) {
        File f = new File(fileName);
        File additionsDir = join(GITLET_DIR, "stagingArea", "additions");
        File removalsDir = join(GITLET_DIR, "stagingArea", "removal");
        File blobsDir = join(GITLET_DIR, "blobs");
        if (!f.exists()) {
            System.out.println("File does not exist");
            System.exit(0);
        }
        stagedForAddition = additionsFromFile();
        stagedForRemoval = removalsFromFile();
        blobs = blobsFromFile();

        Commit headCommit = getHeadCommit();
        HashMap<String, String> headCommitTrackedFiles = headCommit.getTrackedFiles();
        String headCommitFileUID = headCommitTrackedFiles.get(fileName);
        String fileUID = sha1(readContents(f));

        if (headCommitFileUID.equals(fileUID)) {
            if (stagedForAddition.containsKey(fileName)) {
                stagedForAddition.remove(fileName);
                writeObject(additionsDir, stagedForAddition);
            }
            if (stagedForRemoval.containsKey(fileName)) {
                stagedForRemoval.remove(fileName);
                writeObject(removalsDir, stagedForRemoval);
            }
            return;
        }
        stagedForAddition.put(fileName, fileUID);
        blobs.put(fileUID, f);
        writeObject(additionsDir, stagedForAddition);
        writeObject(blobsDir, blobs);
    }
}
