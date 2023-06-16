public class File {
    String type, id, parentID;
    LinkedList<String> childIDs;

    public File(String type, String id, String parentID){
        this.type = type;
        this.id = id;
        this.parentID = parentID;
        childIDs = new LinkedList<>();
    }

    public void addChild(String childID){
        childIDs.append(childID);
    }
}
