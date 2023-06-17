package src;

public class Main {
    public static void main(String[] args){
        /*
         * Argument structure:
         * args[0]: local backup schema file (path can be relative or absolute)
         * args[1]: action
         *  s/sync registered files
         *      downloads files from the tablet, as long as they are newer than the local copies
         *  r/register new file
         *      args[2] - absolute file path on remarkable
         *      args[3] - absolute file path on local machine
         *  dr/deregister file
         *      args[2] - file path on remarkable
         */
        FileSystem fs = new FileSystem(args[0]);
        String action = args[1];
        
        if(action.equals("s") || action.equals("sync")){
            fs.backupAllRegisteredEntries();
        }
        else if(action.equals("r") || action.equals("register")){
            String remotePath = args[2];
            String localPath = args[3];
            fs.addNewBackupEntry(localPath, remotePath);
        }
        else if(action.equals("dr") || action.equals("deregister")){
            String remotePath = args[2];
            fs.removeBackupEntry(remotePath);
        }

        fs.saveLocalMapping(args[0]);
    }
}
