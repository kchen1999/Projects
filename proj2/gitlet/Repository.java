package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kevin
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commit tree - mapping of commit references to their commit. */
    private static HashMap<String, Commit> commits = new HashMap<>();
    /** The abbreviated commit tree - mapping of 6 digit commit abbreviations to their commit reference*/
    private static HashMap<String, ArrayList<String>> commitPrefixes= new HashMap<>();
    /** Mapping of branch names to references to commits */
    private static TreeMap<String, Stack<String>> branches = new TreeMap<>();
    /** Staging area (files staged for addition): Mapping of file names to blob references */
    private static TreeMap<String, String> additions = new TreeMap<>();
    /** Staging area (files staged for removal): Mapping of file names to blob references */
    private static TreeMap<String, String> removals = new TreeMap<>();
    /** Mapping of blob references to file contents **/
    private static HashMap<String, File> blobMap = new HashMap<>();
    /** The current branch */
    private static String currentBranch;

    private static HashMap<String, Commit> commitsFromFile() {
        File inFile = join(GITLET_DIR, "commits");
        return readObject(inFile, HashMap.class);
    }

    private static HashMap<String, ArrayList<String>> commitPrefixesFromFile() {
        File inFile = join(GITLET_DIR, "commitPrefixes");
        return readObject(inFile, HashMap.class);
    }

    private static TreeMap<String, Stack<String>> branchesFromFile() {
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

    private static String getHeadCommitId() {
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        return branches.get(currentBranch).peek();
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

    private static void checkIfFileExistsInCommit(HashMap<String, String> trackedFiles, String fileName) {
        if (trackedFiles == null || !trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    private static HashMap<String, String> getBranchCommitFiles(String branchName) {
        branches = branchesFromFile();
        commits = commitsFromFile();
        String commitReference = branches.get(branchName).peek();
        return commits.get(commitReference).getTrackedFiles();
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

    private static boolean isStagedForAddition(String fileName, TreeMap<String, String> additions) {
        return additions.containsKey(fileName);
    }

    private static boolean isStagedForRemoval(String fileName, TreeMap<String, String> removals) {
        return removals.containsKey(fileName);
    }

    private static boolean noFilesStagedForAddition() {
        additions = additionsFromFile();
        return additions.isEmpty();
    }

    private static boolean noFilesStagedForRemoval() {
        removals = removalsFromFile();
        return removals.isEmpty();
    }

    private static HashMap<String, String> getCurrentCommitTrackedFiles() {
        return getHeadCommit().getTrackedFiles();
    }

    private static boolean isTrackedByCurrentCommit(String fileName) {
        return getCurrentCommitTrackedFiles().containsKey(fileName);
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

    public static void init() {
        setUpPersistence();
        File commitsFile = join(GITLET_DIR, "commits");
        File branchesFile = join(GITLET_DIR, "branches");
        File currentBranchFile = join(GITLET_DIR, "currentBranch");

        Commit initialCommit = new Commit("initial commit", null);
        Stack<String> parentPointers = new Stack();
        parentPointers.push(initialCommit.getCommitUID());
        commits.put(initialCommit.getCommitUID(), initialCommit);
        branches.put("master", parentPointers);
        currentBranch = "master";

        writeObject(commitsFile, commits);
        writeObject(branchesFile, branches);
        writeObject(currentBranchFile, currentBranch);
    }

    public static void commit(String message) {
        checkGitletDirIsInitialized();
        File commitsFile = join(GITLET_DIR, "commits");
        File branchesFile = join(GITLET_DIR, "branches");
        commits = commitsFromFile();
        currentBranch = currentBranchFromFile();

        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit.getCommitUID());
        if (message.contains("Merged ") && message.contains("into " + currentBranch)) {
            newCommit.updateFileContents(parentCommit.getTrackedFiles(), true);
        } else {
            newCommit.updateFileContents(parentCommit.getTrackedFiles(), false);
        }
        commits.put(newCommit.getCommitUID(), newCommit);
        Stack<String> parentPointers = branches.get(currentBranch);
        if (!parentCommit.isSplitPoint()) {
            parentPointers.pop();
        }
        parentPointers.push(newCommit.getCommitUID());
        branches.put(currentBranch, parentPointers);

        addCommitPrefix(newCommit.getCommitUID());
        writeObject(commitsFile, commits);
        writeObject(branchesFile, branches);
        clearStagingArea();
    }

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
            if (isStagedForAddition(fileName, additions)) {
                removeFileStagedForAddition(fileName);
            }
            if (isStagedForRemoval(fileName, removals)) {
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

    public static void rm(String fileName) {
        checkGitletDirIsInitialized();
        additions = additionsFromFile();
        removals = removalsFromFile();
        String currentVersionBlobUID = getCurrentVersionBlobUID(fileName);
        if (isStagedForAddition(fileName, additions)) {
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

    public static void globalLog() {
        checkGitletDirIsInitialized();
        commits = commitsFromFile();
        for (String commitUID : commits.keySet()) {
            Commit commit = commits.get(commitUID);
            printIndividualCommit(commitUID, commit);
        }
    }

    public static void find(String message) {
        checkGitletDirIsInitialized();
        commits = commitsFromFile();
        Boolean found = false;
        for (String commitUID : commits.keySet()) {
            Commit commit = commits.get(commitUID);
            if (message.equals(commit.getMessage())) {
                System.out.println(commitUID);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    private static TreeMap<String, String> getModificationsNotStagedForCommit () {
        TreeMap<String, String> map = new TreeMap<>();
        additions = additionsFromFile();
        removals = removalsFromFile();
        List<String> wkDirFiles = plainFilenamesIn(CWD);
        for (String fileName : wkDirFiles) {
            File f = new File(fileName);
            String blobUID = sha1(readContents(f));
            if (isTrackedByCurrentCommit(fileName) && !isIdenticalFile(getCurrentVersionBlobUID(fileName), blobUID)
                    && !isStagedForAddition(fileName, additions)) {
                map.put(fileName, "modified");
            } else if (isStagedForAddition(fileName, additions) && !isIdenticalFile(additions.get(fileName), blobUID)) {
                map.put(fileName, "modified");
            }
        }
        for (String fileName : additions.keySet()) {
            if (!wkDirFiles.contains(fileName)) {
                map.put(fileName, "deleted");
            }
        }
        for (String fileName : getCurrentCommitTrackedFiles().keySet()) {
            if (!wkDirFiles.contains(fileName) && !isStagedForRemoval(fileName, removals)) {
                map.put(fileName, "deleted");
            }
        }
        return map;
    }

    private static ArrayList<String> getUntrackedFiles() {
        ArrayList<String> list = new ArrayList<>();
        List<String> wkDirFiles = plainFilenamesIn(CWD);
        additions = additionsFromFile();
        removals = removalsFromFile();
        for (String fileName : wkDirFiles) {
            if (!isTrackedByCurrentCommit(fileName) && !isStagedForAddition(fileName, additions)) {
                list.add(fileName);
            }
        }
        return list;
    }

    public static void status() {
        checkGitletDirIsInitialized();
        branches = branchesFromFile();
        additions = additionsFromFile();
        removals = removalsFromFile();
        currentBranch = currentBranchFromFile();
        TreeMap<String, String> unstagedModifications = getModificationsNotStagedForCommit();
        ArrayList<String> untrackedFiles = getUntrackedFiles();
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
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : unstagedModifications.keySet()) {
            System.out.println(fileName + " (" + unstagedModifications.get(fileName) + ")");
        }
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println("");
    }

    private static void overwriteCurrentFileVersion(HashMap<String, String> trackedFiles, String fileName) {
        File currentFile = new File(fileName);
        blobMap = blobMapFromFile();
        checkIfFileExistsInCommit(trackedFiles, fileName);
        String commitVersionBlobUID = trackedFiles.get(fileName);
        File commitVersion = blobMap.get(commitVersionBlobUID);
        writeContents(currentFile, readContents(commitVersion));
    }

    private static void overwriteCurrentWorkingDirectory(HashMap<String, String> branchCommitTrackedFiles) {
        for (String fileName : branchCommitTrackedFiles.keySet()) {
            File currentFile = new File(fileName);
            blobMap = blobMapFromFile();
            String commitVersionBlobUID = branchCommitTrackedFiles.get(fileName);
            File commitVersion = blobMap.get(commitVersionBlobUID);
            writeContents(currentFile, readContents(commitVersion));
        }
    }

    private static void removeTrackedFilesNotInCommit(HashMap<String, String> currentCommitTrackedFiles,
                                                      HashMap<String, String> givenCommitTrackedFiles) {
        for (String fileName : currentCommitTrackedFiles.keySet()) {
            if (!givenCommitTrackedFiles.containsKey(fileName)) {
                File f = new File(fileName);
                f.delete();
            }
        }
    }

    public static void checkout(String fileName) {
        checkGitletDirIsInitialized();
        Commit headCommit = getHeadCommit();
        overwriteCurrentFileVersion(headCommit.getTrackedFiles(), fileName);
    }

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

    public static void checkoutBranch(String branchName) {
        checkGitletDirIsInitialized();
        branches = branchesFromFile();
        File currentBranchFile = join(GITLET_DIR, "currentBranch");
        currentBranch = currentBranchFromFile();
        ArrayList<String> untrackedFiles = getUntrackedFiles();
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else if (untrackedFiles.size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        HashMap<String, String> branchCommitTrackedFiles = getBranchCommitFiles(branchName);
        overwriteCurrentWorkingDirectory(branchCommitTrackedFiles);
        removeTrackedFilesNotInCommit(getCurrentCommitTrackedFiles(), branchCommitTrackedFiles);
        currentBranch = branchName;
        writeObject(currentBranchFile, currentBranch);
        clearStagingArea();
    }

    public static void branch(String branchName) {
        checkGitletDirIsInitialized();
        File branchesFile = join(GITLET_DIR, "branches");
        File commitsFile = join(GITLET_DIR, "commits");
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        commits = commitsFromFile();
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit currentCommit = getHeadCommit();
        currentCommit.setSplitPoint();
        commits.put(getHeadCommitId(), currentCommit);
        Stack<String> newBranchParentPointers = new Stack<>();
        Stack<String> currentBranchParentPointers = branches.get(currentBranch);
        for (String commitUID : currentBranchParentPointers) {
            newBranchParentPointers.push(commitUID);
        }
        branches.put(branchName, newBranchParentPointers);
        writeObject(branchesFile, branches);
        writeObject(commitsFile, commits);
    }

    public static void rmBranch(String branchName) {
        checkGitletDirIsInitialized();
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        File branchesFile = join(GITLET_DIR, "branches");
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(branchName);
        writeObject(branchesFile, branches);
    }

    public static void reset(String commitUID) {
        checkGitletDirIsInitialized();
        if (commitUID.length() < 40) {
            commitUID = getFullCommitUID(commitUID);
        }
        Commit commit = getCommit(commitUID);
        if (commit == null) {
            printCommitIDErrorMessage();
        }
        File branchesFile = join(GITLET_DIR, "branches");
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        ArrayList<String> untrackedFiles = getUntrackedFiles();
        if (untrackedFiles.size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        HashMap<String, String> givenCommitTrackedFiles = commit.getTrackedFiles();
        overwriteCurrentWorkingDirectory(givenCommitTrackedFiles);
        removeTrackedFilesNotInCommit(getCurrentCommitTrackedFiles(), givenCommitTrackedFiles);
        Stack<String> parentPointers = branches.get(currentBranch);
        parentPointers.pop();
        parentPointers.push(commitUID);
        branches.put(currentBranch, parentPointers);
        writeObject(branchesFile, branches);
        clearStagingArea();
    }

    private static Commit findSplitPoint(String currentBranch, String givenBranch) {
        branches = branchesFromFile();
        commits = commitsFromFile();
        Stack<String> currentBranchParents = branches.get(currentBranch);
        Stack<String> givenBranchParents = branches.get(givenBranch);
        String splitPointUID = null;
        for (String s1 : currentBranchParents) {
            if (splitPointUID != null) {
                break;
            }
            for (String s2: givenBranchParents) {
                if (s1.equals(s2)) {
                    splitPointUID = s1;
                    break;
                }
            }
        }
        String currentBranchCommitUID= branches.get(currentBranch).peek();
        String givenBranchCommitUID= branches.get(givenBranch).peek();
        if(splitPointUID.equals(givenBranchCommitUID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPointUID.equals(currentBranchCommitUID)) {
            checkoutBranch(givenBranch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        return getCommit(splitPointUID);
    }

    private static void mergeFileContents(String fileName, String currentBranchFileUID, String givenBranchFileUID) {
        File file = join(CWD, fileName);
        file.delete();
        blobMap = blobMapFromFile();
        if (currentBranchFileUID == null) {
            writeContents(file, "<<<<<<< HEAD\n", "=======\n", readContents(blobMap.get(givenBranchFileUID)),
                    ">>>>>>>\n");
        } else if (givenBranchFileUID == null) {
            writeContents(file, "<<<<<<< HEAD\n", readContents(blobMap.get(currentBranchFileUID)), "=======\n",
                    ">>>>>>>\n");
        } else {
            writeContents(file, "<<<<<<< HEAD\n", readContents(blobMap.get(currentBranchFileUID)),
                    "=======\n", readContents(blobMap.get(givenBranchFileUID)), ">>>>>>>\n");
        }
        String blobUID = sha1(readContents(file));
        stageFileForAddition(fileName, blobUID);
    }

    public static void merge(String branchName) {
        Boolean mergeConflict = false;
        checkGitletDirIsInitialized();
        branches = branchesFromFile();
        currentBranch = currentBranchFromFile();
        ArrayList<String> untrackedFiles = getUntrackedFiles();

        if(!noFilesStagedForAddition() || !noFilesStagedForRemoval()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchName.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (untrackedFiles.size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        Commit splitPoint = findSplitPoint(currentBranch, branchName);
        HashMap<String, String> splitPointCommitFiles = splitPoint.getTrackedFiles();
        HashMap<String, String> currentBranchCommitFiles = getCurrentCommitTrackedFiles();
        HashMap<String, String> givenBranchCommitFiles = getBranchCommitFiles(branchName);
        for (String fileName : splitPointCommitFiles.keySet()) {
            String splitPointFileUID = splitPointCommitFiles.get(fileName);
            String currentBranchFileUID = currentBranchCommitFiles.get(fileName);
            String givenBranchFileUID = givenBranchCommitFiles.get(fileName);
            if (currentBranchFileUID == null && givenBranchFileUID == null) {
                continue;
            } else if (currentBranchFileUID == null && isIdenticalFile(splitPointFileUID, givenBranchFileUID)) {
                continue;
            } else if (currentBranchFileUID == null && !isIdenticalFile(splitPointFileUID, givenBranchFileUID)) {
                mergeFileContents(fileName, null, givenBranchFileUID);
                mergeConflict = true;
            } else if (givenBranchFileUID == null && isIdenticalFile(splitPointFileUID, currentBranchFileUID)) {
                rm(fileName);
            } else if (givenBranchFileUID == null && !isIdenticalFile(splitPointFileUID, currentBranchFileUID)) {
                mergeFileContents(fileName, currentBranchFileUID, null);
                mergeConflict = true;
            } else if (!isIdenticalFile(splitPointFileUID, currentBranchFileUID) && isIdenticalFile(currentBranchFileUID,
                    givenBranchFileUID)) {
                continue;
            } else if (!isIdenticalFile(splitPointFileUID, givenBranchFileUID) && isIdenticalFile(splitPointFileUID,
                    currentBranchFileUID)) {
                checkout(branches.get(branchName).peek(), fileName);
                stageFileForAddition(fileName, givenBranchFileUID);
            } else if (!isIdenticalFile(splitPointFileUID, currentBranchFileUID) && isIdenticalFile(splitPointFileUID,
                    givenBranchFileUID)) {
                continue;
            } else if (!isIdenticalFile(splitPointFileUID, currentBranchFileUID) && !isIdenticalFile(currentBranchFileUID,
                    givenBranchFileUID)) {
                mergeFileContents(fileName, currentBranchFileUID, givenBranchFileUID);
                mergeConflict = true;
            }
        }
        ArrayList<String> newFilesInGivenBranch = new ArrayList<>();
        for (String fileName : givenBranchCommitFiles.keySet()) {
            if (!splitPointCommitFiles.containsKey(fileName)) {
                String currentBranchFileUID = currentBranchCommitFiles.get(fileName);
                String givenBranchFileUID = givenBranchCommitFiles.get(fileName);
                if (currentBranchFileUID == null) {
                    stageFileForAddition(fileName, givenBranchFileUID);
                    newFilesInGivenBranch.add(fileName);
                } else if (!isIdenticalFile(givenBranchFileUID, currentBranchFileUID)) {
                    mergeFileContents(fileName, currentBranchFileUID, givenBranchFileUID);
                    mergeConflict = true;
                }
            }
        }
        commit("Merged " + branchName + " into " + currentBranch + ".");
        if (!newFilesInGivenBranch.isEmpty()) {
            for (String fileName : newFilesInGivenBranch) {
                overwriteCurrentFileVersion(getCurrentCommitTrackedFiles(), fileName);
            }
        }
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

}