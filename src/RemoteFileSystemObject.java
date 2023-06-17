package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class representing an object on the remarkable's file system.
 * Objects on the system are JSON blobs.
 * File objects can be compared by the date they were last modified.
 */
public class RemoteFileSystemObject implements Comparable<Long>{
    public static final String TYPE_FOLDER = "CollectionType"; // type names on the tablet
    public static final String TYPE_DOCUMENT = "DocumentType";

    String type; // this object's type
    String id; // this object's id
    String parentId; // this object's parent
    String name; // this object's name
    ISODateTime lastModified; // date this object was last modified on the remarkable
    boolean isRoot = false; // is this object the root?
    String url; // url to this object
    private ArrayList<RemoteFileSystemObject> children; // this object's children
    private JSONArray childrenJson;

    /**
     * Constructor for a root filesystem object.
     * Root objects have type=TYPE_FOLDER, id="", and no parent.
     */
    public RemoteFileSystemObject(){
        url = "http://10.11.99.1/documents/"; // url to this object
        children = new ArrayList<>(); // init children json array
        isRoot = true; // this is the root
        type = TYPE_FOLDER; // root is always a folder
        id = ""; // root id is empty
        parentId = null; // root has no parent
        name = null; // root has no name
        lastModified = null; // root is never modified
        childrenJson = this.getJSON(); // store the root's json
        children = new ArrayList<>();
    }

    /**
     * Constructor for a generic filesystem object.
     * @param id            this object's id on the tablet
     * @param parentId      the ids of this object's parent on the tablet
     * @param name          this object's name on the tablet
     * @param type          this object's type, either folder or document
     * @param lastModified  when this object was last edited on the tablet (null for folders)
     */
    public RemoteFileSystemObject(String id, String parentId, String name, String type, ISODateTime lastModified){
        url = "http://10.11.99.1/documents/" + id;
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.lastModified = lastModified;
        if(type.equals(TYPE_DOCUMENT)){ // if this is a document
            url = url + "/pdf"; // documents can be downloaded
            children = null; // documents have no children, so do nothing else
        }
        else childrenJson = this.getJSON(); // if not a document, can get json for it
        children = new ArrayList<>();
    }

    /**
     * Builds the child objects of this object. Should be called immediately after initialization.
     */
    public void constructChildren(){
        if(!type.equals(TYPE_DOCUMENT)){ // can only build children if not a document
            for(int i = 0; i < childrenJson.length(); i++){
                JSONObject childJson = childrenJson.getJSONObject(i);
                String childId = childJson.getString("ID");
                String childParentId = childJson.getString("Parent");
                String childName = childJson.getString("VissibleName");
                String childType = childJson.getString("Type");
                ISODateTime childLastModified = new ISODateTime(childJson.getString("ModifiedClient"));
                RemoteFileSystemObject child = new RemoteFileSystemObject(childId, childParentId, childName, childType, childLastModified);
                children.add(child);
            }
        }
    }

    /**
     * Get the JSON for this object from the tablet
     * @return
     */
    public JSONArray getJSON(){
        if(type.equals(TYPE_DOCUMENT)) return null; // can't get json for documents
        InputStream in = null;
        BufferedReader dIn;
        String s;
        StringBuilder out = new StringBuilder();

        try{
            in = new URL(url).openStream();
            dIn = new BufferedReader(new InputStreamReader(in));
            while((s = dIn.readLine()) != null){
                out.append(s);
            }
        } catch (MalformedURLException e){
            System.err.println("Bad URL " + url);
            e.printStackTrace();
        } catch (IOException e){
            System.err.println("IOException! Check your permissions, and make sure you've been connected to the tablet for at least a minute or two.");
            e.printStackTrace();
        }
        finally{
            try{
                in.close();
            } catch(IOException e){}
        }
        return new JSONArray(out.toString());
    }

    public int saveToLocalSystem(File localFile){
        if(type.equals(TYPE_FOLDER)) return -1; // can't save folders
        URL u;
        InputStream in = null;

        try{
            u = new URL("http://10.11.99.1/download/" + id + "/pdf");
            in = u.openStream();
            Files.copy(in, Paths.get(localFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
            
        } catch (MalformedURLException e){
            System.err.println("Bad URL http://10.11.99.1/download/" + id + "/pdf");
            e.printStackTrace();
            return -1;
        } catch (IOException e){
            System.err.println("IOException! Check your permissions.");
            e.printStackTrace();
            return -1;
        } finally{
            try{
                in.close();
            } catch(IOException e){}
        }
        return 0;
    }

    public boolean hasChildren(){
        if(children == null) return false;
        return !children.isEmpty();
    }

    public ArrayList<RemoteFileSystemObject> getChildren(){
        return children;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("File " + id + "\n\tname: " + name + "\n\tparent: " + parentId + "\n\ttype: " + type + "\n\t" + children.size() + " children: ");
        for(RemoteFileSystemObject child : children){
            sb.append(child.id + " ");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Long o) {
        long res = this.lastModified.toLong() - o; // positive if this file is newer than the other file, negative if this file is older than the other file
        if(res < 0) return -1;
        if(res > 0) return 1;
        return 0;
    }
}
