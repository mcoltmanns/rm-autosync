package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Structure for mapping files between the host system and the tablet
 */
public class FileSystem {
    private RemoteFileSystemObject remoteRoot;
    private HashMap<String, RemoteFileSystemObject> remoteFiles; // hash table of file ids to remote objects (only downloadable files)
    private HashMap<String, String> remotePathsToIds; // hash table of file ids to their remote paths
    private HashMap<String, File> localFiles; // hash table of remote file ids to local files (only downloadable files)

    public FileSystem(String absolutePathToLocalMapping){
        remoteFiles = new HashMap<>();
        localFiles = new HashMap<>();
        remotePathsToIds = new HashMap<>();
        remoteRoot = new RemoteFileSystemObject();

        // set up the file tree and remote hash map
        remoteRoot.constructChildren(); // construct the root node's children
        // start recursive part: for each non-folder child in the root's children, construct the children
        constructChildrenRecursive(remoteRoot, "");

        loadLocalMapping(absolutePathToLocalMapping); // load the local file locations
    }

    /**
     * Loads the hashmap for the local locations of files.
     * Hash map stores file ids and local locations line-per-line, like this: <id> : <location>
     * @param absolutePathToLocalMapping    relative path to the hash map
     */
    public void loadLocalMapping(String absolutePathToLocalMapping){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(absolutePathToLocalMapping));
            String line;
            while((line = reader.readLine()) != null){
                // local mappings are stored as <remote id>:<local path>, with 1 mapping per line
                int separatorIndex = line.indexOf(":");
                String remoteId = line.substring(0, separatorIndex);
                File localFile = new File(line.substring(separatorIndex + 1));
                if(!localFile.exists()){
                    System.out.println("Can't access file at " + localFile.getAbsolutePath());
                }
                else{
                    localFile.createNewFile();
                }
                // can't have 2 mappings for the same id
                if(!localFiles.containsKey(remoteId)) localFiles.put(remoteId, localFile);
            }
            reader.close();
        } catch(IOException e){
            System.err.println("Couldn't load local mapping. Check your permissions and file path!");
            e.printStackTrace();
        }
    }

    /**
     * Saves the current local mapping to disk. Should be called on program exit.
     * @param pathToLocalMapping    relative path to the hash map
     */
    public void saveLocalMapping(String pathToLocalMapping){
        BufferedWriter writer;
        try{
            writer  = new BufferedWriter(new FileWriter(pathToLocalMapping));
            for(Entry<String, File> fileMapping : localFiles.entrySet()){
                String id = fileMapping.getKey();
                String absolutePath = fileMapping.getValue().getAbsolutePath();
                writer.write(id + ":" + absolutePath + "\n");
            }
            writer.close();
        } catch(IOException e){
            System.err.println("Couldn't save local mapping. Check your permissions and file path!");
            e.printStackTrace();
        }
    }

    /**
     * Recursively populate the filesystem, adding non-folder files to the download hashmap as you go.
     * @param current   the current node to load children of
     */
    private void constructChildrenRecursive(RemoteFileSystemObject current, String remotePath){
        // base case: this child has no children
        if(!current.hasChildren()){
            remoteFiles.put(current.id, current);
            remotePathsToIds.put(remotePath, current.id);
            return;
        }
        // otherwise:
        for(RemoteFileSystemObject child : current.getChildren()){
            child.constructChildren();
            constructChildrenRecursive(child, remotePath + "/" + child.name);
        }
    }

    /**
     * Download an object from the tablet to its mapped file on the system
     * @param remotePath    id of the file on the tablet
     * @return              0 on success, -1 if the file doesn't exist on the tablet or hasn't been registered for backup.
     */
    private int downloadObjectById(String targetId){
        if(!this.remoteFiles.containsKey(targetId) || !this.localFiles.containsKey(targetId)){// can't download objects not in the system
            System.err.println("Key " + targetId + " has not been registered to the local system, or is not present on the device.");
            return -1;
        }
        RemoteFileSystemObject target = remoteFiles.get(targetId);
        File localFile = localFiles.get(targetId);
        if(target.type.equals(RemoteFileSystemObject.TYPE_FOLDER)) return -2; // can't download folders
        //TODO: newness feature is buggy
        //if(target.compareTo(localFile.lastModified()) >= 0){ // if remote is newer than local
            System.out.println("saving " + targetId + " to " + localFile.getAbsolutePath());
            target.saveToLocalSystem(localFiles.get(targetId)); // save this object to the file it maps to locally
        //}
        //else{
            //System.out.println("local is newer, skipping " + targetId);
        //}
        return 0; // success
    }

    public int backupAllRegisteredEntries(){
        for(Entry<String, File> localFile : localFiles.entrySet()){ // for all registered files
            downloadObjectById(localFile.getKey());
        }
        return 0;
    }

    /**
     * Register a new file for backup
     * @param localPathAbsolute     the absolute path to save the file on the local system, for example "C:user\bill\important-stuff\taxes.pdf"
     * @param remotePathAbsolute    the absolute path to the file to be saved on the tablet
     * @return                      0 on success
     */
    public int addNewBackupEntry(String localPathAbsolute, String remotePathAbsolute){
        String remoteId = remotePathsToIds.get(remotePathAbsolute);
        localFiles.putIfAbsent(remoteId, new File(localPathAbsolute));
        return 0;
    }

    /**
     * Deregister a file for backup
     * @param remotePathAbsolute    the path on the tablet of the file to deregister
     * @return                      0 on success
     */
    public int removeBackupEntry(String remotePathAbsolute){
        String targetId = remotePathsToIds.get(remotePathAbsolute);
        localFiles.remove(targetId);
        return 0;
    }

    public String toString(){
        return remoteFiles.toString();
    }
}   
