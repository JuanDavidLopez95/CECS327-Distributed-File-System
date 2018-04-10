import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
// import a json package
import com.google.gson.Gson;


/* JSON Format

 { //use method put in chord to send to the cloud 
    // use md5 of localfile for GUID for page 
    // delete pages then delete the file as well
    //page is an actually physical file in the cloud
    //use put() from Chord to upload to the network
    "metadata" :
    {
        file :
        {
            name  : "File1"
            numberOfPages : "3"
            pageSize : "1024" //dont worry about this
            size : "2291" 
            page :
            {
                number : "1" //dont worry about number, since JSON keeps track this.
                guid   : "22412"
                size   : "1024"
            }
            page :
            {
                number : "2"
                guid   : "46312"
                size   : "1024"
            }
            page :
            {
                number : "3"
                guid   : "93719"
                size   : "243"
            }
        }
    }
}
 */

public class DFS {
    int port;
    Chord chord;
    
    private Gson gson;
    private String json;
    private FileStream filestream;
    private Metadata metadata;
    
    private long md5(String objectName)
    {
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(objectName.getBytes());
            BigInteger bigInt = new BigInteger(1,m.digest());
            return Math.abs(bigInt.longValue());
        }
        catch(NoSuchAlgorithmException e)
        {
                e.printStackTrace();
                
        }
        return 0;
    }
    
    public DFS(int port) throws Exception
    {
        gson = new Gson();
        filestream = new FileStream();
        
        //ArrayList<Page> pages = new ArrayList<Page>();
        //MetaFile file = new MetaFile("New-name", 0, 0, 0, pages);
        ArrayList<MetaFile> files = new ArrayList<MetaFile>();
        metadata = new Metadata("New-name", files);
        
        json = gson.toJson(metadata);
        
        System.out.println(json);
        
        this.port = port;
        long guid = md5("" + port);
        chord = new Chord(port, guid);
        Files.createDirectories(Paths.get(guid+"/repository"));
    }

    public void join(String Ip, int port) throws Exception {
        chord.joinRing(Ip, port);
        chord.Print();
    }
    
  /*  public JSonParser readMetaData() throws Exception
    {
        JsonParser jsonParser _ null;
        long guid = md5("Metadata");
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        InputStream metadataraw = peer.get(guid);
        // jsonParser = Json.createParser(metadataraw);
        return jsonParser;
    }
    
    public void writeMetaData(InputStream stream) throws Exception
    {
        JsonParser jsonParser _ null;
        long guid = md5("Metadata");
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        peer.put(guid, stream);
    }
   */
    public void mv(String oldName, String newName) throws Exception {
        // TODO:  Change the name in Metadata
        // Write Metadata
        
    	Metadata metadata = gson.fromJson(json, Metadata.class);
    	metadata.changeName(oldName, newName);
    	json = gson.toJson(metadata);
    }

    public String ls() throws Exception {
        String listOfFiles = "";
       // TODO: returns all the files in the Metadata
       // JsonParser jp = readMetaData();
        
        Metadata metadata = gson.fromJson(json, Metadata.class);
    	listOfFiles = metadata.getFileNames();
        
        return listOfFiles;
    }
    
    public void touch(String fileName) throws Exception {
        // TODO: Create the file fileName by adding a new entry to the Metadata
        // Write Metadata
        
    	Metadata metadata = gson.fromJson(json, Metadata.class);
    	metadata.createFile(fileName);
    	json = gson.toJson(metadata);
    }
    
    public void delete(String fileName) throws Exception {
        // TODO: remove all the pages in the entry fileName in the Metadata and then the entry
        // for each page in Metadata.filename
        //     peer = chord.locateSuccessor(page.guid);
        //     peer.delete(page.guid)
        // delete Metadata.filename
           Metadata metadata = gson.fromJson(json, Metadata.class);
           metadata.delete(fileName);
           json = gson.toJson(metadata);
        // Write Metadata    	
    }
    
    public FileStream read(String fileName, int pageNumber) throws Exception {
        // TODO: read pageNumber from fileName
    	Metadata metadata = gson.fromJson(json, Metadata.class);
    	Page page = metadata.getFile(fileName).getPage(pageNumber);
    	
    	String filepath = fileName + ".txt";
    	PrintWriter writer = new PrintWriter(filepath, "UTF-8");
    	writer.println(page);
    	writer.close();
    	
    	FileStream inputstream = new FileStream(filepath);
    	return inputstream;
    }
    
    
    public FileStream tail(String fileName) throws Exception
    {
        // TODO: return the last page of the fileName
    	Metadata metadata = gson.fromJson(json, Metadata.class);
    	Page page = metadata.getFile(fileName).getLastPage();
    	
    	String filepath = fileName + ".txt";
    	PrintWriter writer = new PrintWriter(filepath, "UTF-8");
    	writer.println(page);
    	writer.close();
    	
    	FileStream inputstream = new FileStream(filepath);
    	return inputstream;
    }
    public FileStream head(String fileName) throws Exception
    {
        // TODO: return the first page of the fileName
    	Metadata metadata = gson.fromJson(json, Metadata.class);
    	Page page = metadata.getFile(fileName).getFirstPage();
    	
    	String filepath = fileName + ".txt";
    	PrintWriter writer = new PrintWriter(filepath, "UTF-8");
    	writer.println(page);
    	writer.close();
    	
    	FileStream inputstream = new FileStream(filepath);
    	return inputstream;
    }
    public void append(String filename, String filepath) throws Exception
    {
        // TODO: append data to fileName. If it is needed, add a new page.
        // Let guid be the last page in Metadata.filename
        //ChordMessageInterface peer = chord.locateSuccessor(guid);
        //peer.put(guid, data);
        // Write Metadata
    	
    	// Get the file's size
    	File file = new File(filepath);
    	long fileSpace = file.getTotalSpace();
    	
    	// md5 the file
    	long guid = md5(filename);
    	
    	// store the data into a page
    	Page page = new Page(metadata.getFile(filename).getLastPage().getNumber() + 1, guid, fileSpace);
    	
    	// put the page into the metadata
    	metadata.getFile(filename).addPage(page);
    	
    	// Write the file to disk
    	String newFilepath = filename + ".txt";
    	PrintWriter writer = new PrintWriter(newFilepath, "UTF-8");
    	writer.println(page);
    	writer.close();
    	
    	FileStream inputstream = new FileStream(newFilepath);
        
    	// TODO: update the file
    	ChordMessageInterface peer = chord.locateSuccessor(guid);
        peer.put(guid, inputstream);
    }
}
