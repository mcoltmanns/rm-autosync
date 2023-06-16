public class Main {
    public static void main(String[] args){
        /*
         * Argument structure:
         * args[0]: action
         *  s/sync registered files
         *      args[1] - n(ew)/f(orce)
         *          new syncs only files which are newer than those stored on the machine
         *          force syncs all files, regardless of age
         *  r/register new file
         *      args[1] - file path on remarkable
         *      args[2] - file path on local machine
         *  dr/deregister file
         *      args[1] - file path on remarkable
         *  c/config
         *      args[1] - path to new storage schema
         */
    }
}
