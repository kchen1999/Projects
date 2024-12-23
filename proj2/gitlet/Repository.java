package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
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
    /** The abbreviated commit tree - mapping of 6 digit commit abbreviations to their commit reference*/
    public static HashMap<String, ArrayList<String>> commitPrefixes= new HashMap<>();
    /** Mapping of branch names to references to commits */
    public static TreeMap<String, String> branches = new TreeMap<>();
    /** Staging area (files staged for addition): Mapping of file names to references to files */
    public static TreeMap<String, String> additions = new TreeMap<>();
    /** Staging area (files staged for removal): Mapping of file names to references to files */
    public static TreeMap<String, String> removals = new TreeMap<>();
    /** Mapping of blob references to file contents **/
    public static HashMap<String, File> blobMap = new HashMap<>();
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

    private static HashMap<String, ArrayList<String>> commitPrefixesFromFile() {
        File inFile = join(GITLET_DIR, "commitPrefixes");
        return readObject(inFile, HashMap.class);
    }

    private static TreeMap<String, String> branchesFromFile() {
        File inFile = join(GITLET_DIR, "branches");
        return readObject(inFile, TreeMap.class);
    }

    private static String currentBranchFromFile() {
        File inFile = join(GITLET_DIR, "currentBranch");
        return readObject(inFile, String.class);
    }

    private static TreeMap<String, String> additionsFromFile() {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        return readObject(additionsFile, TreeMap.class);
    }

    private static TreeMap<String, String> removalsFromFile() {
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        return readObject(removalsFile, TreeMap.class);
    }

    private static HashMap<String, File> blobMapFromFile() {
        File inFile = join(GITLET_DIR, "blobMap");
        return readObject(inFile, HashMap.class);
    }

    private static void checkGitletDirIsInitialized() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void setUpPersistence () {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        File stagingAreaDir = join(GITLET_DIR, "stagingArea");
        stagingAreaDir.mkdir();
        File blobs = join(GITLET_DIR, "blobs");
        blobs.mkdir();

        File commitPrefixesFile = join(GITLET_DIR, "commitPrefixes");
        File blobMapFile = join(GITLET_DIR, "blobMap");
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        writeObject(commitPrefixesFile, commitPrefixes);
        writeObject(blobMapFile, blobMap);
        writeObject(additionsFile, additions);
        writeObject(removalsFile, removals);
    }

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

    private static void addCommitPrefix(String commitUID) {
        File commitPrefixesFile = join(GITLET_DIR, "commitPrefixes");
        commitPrefixes = commitPrefixesFromFile();
        String commitUIDAbbreviated = commitUID.substring(0, 6);
        String commitUIDRest = commitUID.substring(6, commitUID.length());
        ArrayList<String> commitUIDRemainder = commitPrefixes.get(commitUIDAbbreviated);
        if (commitUIDRemainder == null) {
            commitUIDRemainder = new ArrayList<>();
        }
        commitUIDRemainder.add(commitUIDRest);
        commitPrefixes.put(commitUIDAbbreviated, commitUIDRemainder);
        writeObject(commitPrefixesFile, commitPrefixes);
    }

    private static void clearStagingArea() {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        additions = additionsFromFile();
        removals = removalsFromFile();
        additions.clear();
        removals.clear();
        writeObject(additionsFile, additions);
        writeObject(removalsFile, removals);
    }

    private static boolean isCurrentlyTrackedByCurrentCommit(String headCommitBlobUID) {
        return headCommitBlobUID != null;
    }

    private static boolean isIdenticalFile(String f1, String f2) {
        return f1.equals(f2);
    }

    private static boolean isIdenticalToCurrentCommitVersion(String headCommitBlobUID, String blobUID) {
        return isCurrentlyTrackedByCurrentCommit(headCommitBlobUID) && isIdenticalFile(headCommitBlobUID, blobUID);
    }

    private static void removeFileStagedForAddition(String fileName) {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        additions.remove(fileName);
        writeObject(additionsFile, additions);
    }

    private static void removeFileStagedForRemoval(String fileName) {
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        removals.remove(fileName);
        writeObject(removalsFile, removals);
    }

    private static void stageFileForAddition(String fileName, String blobUID) {
        File additionsFile = join(GITLET_DIR, "stagingArea", "additions");
        additions.put(fileName, blobUID);
        writeObject(additionsFile, additions);
    }

    private static void stageFileForRemoval(String fileName, String blobUID) {
        File removalsFile = join(GITLET_DIR, "stagingArea", "removals");
        removals.put(fileName, blobUID);
        writeObject(removalsFile, removals);
    }

    private static String getCurrentVersionBlobUID(String fileName) {
        Commit headCommit = getHeadCommit();
        HashMap<String, String> headCommitTrackedFiles = headCommit.getTrackedFiles();
        return headCommitTrackedFiles.get(fileName);
    }

    private static void printIndividualCommit(String commitHash, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (commit.getParent1UID() != null) {
            System.out.println("Merge: " + commit.getParentUID().substring(0, 7) + commit.getParent1UID().substring(0, 7));
        }
        String date = String.format("%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", commit.getTimestamp());
        System.out.println("Date: " + date);
        System.out.println(commit.getMessage());
        System.out.println("");
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
        blobMap = blobMapFromFile();
        File commitVersion = blobMap.get(commitVersionBlobUID);
        writeContents(currentFile, readContents(commitVersion));
    }

    private static void printCommitIDErrorMessage() {
        System.out.println("No commit with that id exists.");
        System.exit(0);
    }


    private static String getFullCommitUID (String commitUID) {
        if (commitUID.length() < 6) {
            printCommitIDErrorMessage();
        }
        commitPrefixes = commitPrefixesFromFile();
        ArrayList<String> commitUIDRemainder = commitPrefixes.get(commitUID.substring(0, 6));
        if (commitUIDRemainder == null) {
            printCommitIDErrorMessage();
        }
        if (commitUID.length() == 6) {
            return commitUID.concat(commitUIDRemainder.get(0));
        }
        for (String s : commitUIDRemainder) {
            if (commitUID.substring(6, commitUID.length()).equals(s.substring(0, commitUID.length() - 6))) {
                return commitUID.substring(0, 6).concat(s);
            }
        }
        printCommitIDErrorMessage();
        return null;
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

    public static void commit(String message) {
        checkGitletDirIsInitialized();
        File commitsFile = join(GITLET_DIR, "commits");
        File branchesFile = join(GITLET_DIR, "branches");
        commits = commitsFromFile();
        currentBranch = currentBranchFromFile();

        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit.getCommitUID());
        newCommit.updateFileContents(parentCommit.getTrackedFiles());
        commits.put(newCommit.getCommitUID(), newCommit);
        branches.put(currentBranch, newCommit.getCommitUID());

        addCommitPrefix(newCommit.getCommitUID());
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

    public static void add(String fileName) {
        checkGitletDirIsInitialized();
        File file = new File(fileName);
        File blobMapFile = join(GITLET_DIR, "blobMap");
        if (!file.exists()) {
            System.out.println("File does not exist");
            System.exit(0);
        }
        additions = additionsFromFile();
        removals = removalsFromFile();
        blobMap = blobMapFromFile();

        String currentVersionBlobUID = getCurrentVersionBlobUID(fileName);
        String blobUID = sha1(readContents(file));

        if (isIdenticalToCurrentCommitVersion(currentVersionBlobUID, blobUID)) {
            if (additions.containsKey(fileName)) {
                removeFileStagedForAddition(fileName);
            }
            if (removals.containsKey(fileName)) {
                removeFileStagedForRemoval(fileName);
            }
            return;
        }
        stageFileForAddition(fileName, blobUID);
        File fileCopy = join(GITLET_DIR, "blobs", blobUID); //need to make of copy file contents instead of adding file pointer directly!
        writeContents(fileCopy, readContents(file));
        blobMap.put(blobUID, fileCopy);
        writeObject(blobMapFile, blobMap);
    }

    /*
     * TODO: Unstage the file if it is currently staged for addition
     *  If the file is tracked in the current commit, stage it for removal and remove the file from the working
     *  directory if the user has not already done so (do not remove it unless it is tracked in the current commit).
     *  If the file is neither staged nor tracked by the head commit, print the error message No reason to remove
     *  the file.
     * */
    public static void rm(String fileName) {
        checkGitletDirIsInitialized();
        additions = additionsFromFile();
        removals = removalsFromFile();
        String currentVersionBlobUID = getCurrentVersionBlobUID(fileName);
        if (additions.containsKey(fileName)) {
            removeFileStagedForAddition(fileName);
        } else if (currentVersionBlobUID != null) {
            File f = new File(fileName);
            if (f.exists()) {
                f.delete();
            }
            stageFileForRemoval(fileName, currentVersionBlobUID);
        } else {
            System.out.println("No reasons to remove the file.");
            System.exit(0);
        }
    }

    /**
     * TODO: Starting at the current head commit, display information about each commit backwards along the commit tree until the
     *  initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
     *  For every node in this history, the information it should display is the commit id, the time the commit was made,
     *  and the commit message.
     *  For merge commits (those that have two parent commits), add a line just below the first
     * */

    public static void log() {
        checkGitletDirIsInitialized();
        commits = commitsFromFile();
        Commit headCommit = getHeadCommit();
        while (headCommit != null) {
            printIndividualCommit(headCommit.getCommitUID(), headCommit);
            String parentCommitUID = headCommit.getParentUID();
            headCommit = commits.get(parentCommitUID);
        }
    }

    /**
     * TODO: Displays what branches currently exist, and marks the current branch with a *.
     *  Also displays what files have been staged for addition or removal.
     */

    public static void status() {
        branches = branchesFromFile();
        additions = additionsFromFile();
        removals = removalsFromFile();
        currentBranch = currentBranchFromFile();
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        for (String addition : additions.keySet()) {
            System.out.println(addition);
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        for (String removal : removals.keySet()) {
            System.out.println(removal);
        }
        System.out.println("");
    }

    /**
     * TODO: Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     *  TODO: If the file does not exist in the previous commit, abort, printing the error message
     *   File does not exist in that commit. Do not change the CWD.
     * **/
    public static void checkout(String fileName) {
        checkGitletDirIsInitialized();
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
        checkGitletDirIsInitialized();
        if (commitUID.length() < 40) {
            commitUID = getFullCommitUID(commitUID);
        }
        Commit commit = getCommit(commitUID);
        if (commit == null) {
            printCommitIDErrorMessage();
        }
        overwriteCurrentFileVersion(commit.getTrackedFiles(), fileName);
    }

    /*
    * TODO: Creates a new branch with the given name, and points it at the current head commit.
    *  A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
    *  Before you ever call branch, your code should be running with a default branch called “master”.
    *  If a branch with the given name already exists, print the error message A branch with that name already exists.
     */
    public static void branch(String branchName) {

    }

}
