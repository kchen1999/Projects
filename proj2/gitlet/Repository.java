package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Date;
import java.util.Formatter;
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
    public static HashMap<String, Commit> commits = new HashMap<>();
    /** Mapping of branch names to references to commits */
    public static HashMap<String, String> branches = new HashMap<>();
    /** Staging area (files staged for addition): Mapping of file names to references to files */
    public static HashMap<String, String> additions = new HashMap<>();
    /** Staging area (files staged for removal): Mapping of file names to references to files */
    public static HashMap<String, String> removals = new HashMap<>();
    /** Mapping of blob references to file contents **/
    public static HashMap<String, File> blobs = new HashMap<>();
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

    private static HashMap<String, Commit> commitsFromFile() {
        File inFile = join(GITLET_DIR, "commits");
        return readObject(inFile, HashMap.class);
    }

    private static HashMap<String, String> branchesFromFile() {
        File inFile = join(GITLET_DIR, "branches");
        return readObject(inFile, HashMap.class);
    }

    private static String currentBranchFromFile() {
        File inFile = join(GITLET_DIR, "currentBranch");
        return readObject(inFile, String.class);
    }

    private static HashMap<String, String> additionsFromFile() {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        return readObject(additionsFile, HashMap.class);
    }

    private static HashMap<String, String> removalsFromFile() {
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        return readObject(removalsFile, HashMap.class);
    }

    private static HashMap<String, File> blobsFromFile() {
        File inFile = join(GITLET_DIR, "blobs");
        return readObject(inFile, HashMap.class);
    }

    public static void setUpPersistence () {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        File stagingAreaDir = join(GITLET_DIR, "stagingArea");
        stagingAreaDir.mkdir();

        File blobsFile = join(GITLET_DIR, "blobs");
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        writeObject(blobsFile, blobs);
        writeObject(additionsFile, additions);
        writeObject(removalsFile, removals);
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
        setUpPersistence();
        File commitsFile = join(GITLET_DIR, "commits");
        File branchesFile = join(GITLET_DIR, "branches");
        File currentBranchFile = join(GITLET_DIR, "currentBranch");

        Commit initialCommit = new Commit("initial commit", null);
        commits.put(initialCommit.getCommitUID(), initialCommit);
        branches.put("master", initialCommit.getCommitUID());
        currentBranch = "master";

        writeObject(commitsFile, commits);
        writeObject(branchesFile, branches);
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
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        return branches.get(currentBranch);
    }

    private static Commit getHeadCommit() {
        return getCommit(getHeadCommitId());
    }

    private static Commit getCommit(String commitUID) {
        commits = commitsFromFile();
        return commits.get(commitUID);
    }

    public static void clearStagingArea() {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        additions = additionsFromFile();
        removals = removalsFromFile();
        additions.clear();
        removals.clear();
        writeObject(additionsFile, additions);
        writeObject(removalsFile, removals);
    }

    public static void commit(String message) {
        File commitsFile = join(GITLET_DIR, "commits");
        File branchesFile = join(GITLET_DIR, "branches");
        commits = commitsFromFile();
        currentBranch = currentBranchFromFile();
        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit.getCommitUID());
        newCommit.updateFileContents(parentCommit.getTrackedFiles());
        commits.put(newCommit.getCommitUID(), newCommit);
        branches.put(currentBranch, newCommit.getCommitUID());
        writeObject(commitsFile, commits);
        writeObject(branchesFile, branches);
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

    private static boolean isCurrentlyTrackedByCurrentCommit(String headCommitBlobUID) {
        return headCommitBlobUID != null;
    }

    private static boolean isIdenticalFile(String f1, String f2) {
        return f1.equals(f2);
    }

    private static boolean isIdenticalToCurrentCommitVersion(String headCommitBlobUID, String blobUID) {
        return isCurrentlyTrackedByCurrentCommit(headCommitBlobUID) && isIdenticalFile(headCommitBlobUID, blobUID);
    }

    public static void add(String fileName) {
        File file = new File(fileName);
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        File blobsFile = join(GITLET_DIR, "blobs");
        if (!file.exists()) {
            System.out.println("File does not exist");
            System.exit(0);
        }
        additions = additionsFromFile();
        removals = removalsFromFile();
        blobs = blobsFromFile();

        Commit headCommit = getHeadCommit();
        HashMap<String, String> headCommitTrackedFiles = headCommit.getTrackedFiles();
        String headCommitVersionBlobUID = headCommitTrackedFiles.get(fileName);
        String blobUID = sha1(readContents(file));

        if (isIdenticalToCurrentCommitVersion(headCommitVersionBlobUID, blobUID)) {
            if (additions.containsKey(fileName)) {
                additions.remove(fileName);
                writeObject(additionsFile, additions);
            }
            if (removals.containsKey(fileName)) {
                removals.remove(fileName);
                writeObject(removalsFile, removals);
            }
            return;
        }
        File fileContents = join(GITLET_DIR, blobUID + ".txt"); //need to make of copy file contents instead of adding file pointer directly!
        writeContents(fileContents, readContents(file));

        additions.put(fileName, blobUID);
        blobs.put(blobUID, fileContents);
        writeObject(additionsFile, additions);
        writeObject(blobsFile, blobs);
    }

    /**
     * TODO: Starting at the current head commit, display information about each commit backwards along the commit tree until the
     *  initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
     *  For every node in this history, the information it should display is the commit id, the time the commit was made,
     *  and the commit message.
     *  For merge commits (those that have two parent commits), add a line just below the first
     * */

    private static void printIndividualCommit(String commitHash, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (commit.getParent1UID() != null) {
            System.out.println("Merge: " + commit.getParentUID().substring(0, 7) + commit.getParent1UID().substring(0, 7));
        }
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println("");
    }

    public static void log() {
        commits = commitsFromFile();
        Commit headCommit = getHeadCommit();
        while (headCommit != null) {
            printIndividualCommit(headCommit.getCommitUID(), headCommit);
            String parentCommitUID = headCommit.getParentUID();
            headCommit = commits.get(parentCommitUID);
        }
    }

    private static void checkIfFileExistsInCommit(HashMap<String, String> trackedFiles, String fileName) {
        if (trackedFiles == null || !trackedFiles.containsKey(fileName)) {
          System.out.println("File does not exist in that commit.");
          System.exit(0);
      }
    }

    private static void overwriteCurrentFileVersion(HashMap<String, String> trackedFiles, String fileName) {
        File currentFile = new File(fileName);
        checkIfFileExistsInCommit(trackedFiles, fileName);
        String commitVersionBlobUID = trackedFiles.get(fileName);
        blobs = blobsFromFile();
        File commitVersion = blobs.get(commitVersionBlobUID);
        writeContents(currentFile, readContents(commitVersion));
    }

    /**
     * TODO: Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     *  TODO: If the file does not exist in the previous commit, abort, printing the error message
     *   File does not exist in that commit. Do not change the CWD.
     * **/
    public static void checkout(String fileName) {
        Commit headCommit = getHeadCommit();
        overwriteCurrentFileVersion(headCommit.getTrackedFiles(), fileName);
    }

    /**
     * TODO: Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     *  TODO: If no commit with the given id exists, print No commit with that id exists. Otherwise, if the file does not
     *   exist in the given commit, print the same message as for failure case 1. Do not change the CWD.
     * **/

    public static void checkout(String commitUID, String fileName) {
        Commit commit = getCommit(commitUID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        overwriteCurrentFileVersion(commit.getTrackedFiles(), fileName);
    }

}
