package me.blablubbabc.BlaDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BlaDB {
	private LinkedHashMap<String,Object> data;
	private File file;
	private boolean autosave = true;
	
	public BlaDB(String name, String path) {
		this.data = new LinkedHashMap<String,Object>();
		File f = new File(path);
		f.mkdirs();
		this.file = new File(path+"/"+name+".data");
		if(file.exists()) this.load();
		this.save();
    }
	
	@SuppressWarnings("unchecked")
	private void load(){

		try{
			this.data = (LinkedHashMap<String, Object>) load(this.file.getPath());
		}catch(Exception e){
			System.out.println("Could not load existing Data! File: "+this.file.getPath());
			e.printStackTrace();
			this.data = null;
		}
	}
	
	private void save(){

		try{
			save(this.data, this.file.getPath());
		}catch(Exception e){
			System.out.println("Could not save Data! File: "+this.file.getPath());
			e.printStackTrace();
		}
	}
	
	private void save(Object obj,String path) throws Exception
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}
	private Object load(String path) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		Object result = ois.readObject();
		ois.close();
		return result;
	}
	
    //////////////////////////////////////////////////
    //
    // METHODS
    //
    //////////////////////////////////////////////////

    public void setValue(String key, Object value) {
        this.data.put(key, value);
        if(autosave) this.save();
    }

    public void removeValue(String key) {
        this.data.remove(key);
        if(autosave) this.save();
    }
    
    public void clear() {
        this.data.clear();
        if(autosave) this.save();
    }
    
    public void saveFile() {
    	this.save();
    }
    
    public void autosave(boolean auto) {
    	this.autosave = auto;
    }
    //////////GETTERS//////////////////////////////////
    public Set<Map.Entry<String, Object>> getEntrys() {
    	return this.data.entrySet();
    }
    
    public LinkedHashMap<String, Object> getData() {
    	return this.data;
    }
    
    public Object getValue(String key) {
    	return this.data.get(key);
    }
    public String getString(String key) {
        return (String) this.data.get(key);
    }
    
    public int getInt(String key) {
        return (Integer) this.data.get(key);
    }
    
    public boolean getBoolean(String key) {
        return (Boolean) this.data.get(key);
    }
    
    public long getLong(String key) {
        return (Long) this.data.get(key);
    }
    
    public double getDouble(String key) {
        return (Double) this.data.get(key);
    }
    
    public float getFloat(String key) {
        return (Float) this.data.get(key);
    }
    
    //////////////////////////////////////////////////////
    //ENTRIES
    //////////////////////////////////////////////////////
    
    public void addRegister(String key, BlaDBRegister value) {
    	this.data.put(key, value.getData());
    	if(autosave) this.save();
    }
    
    @SuppressWarnings("unchecked")
    public BlaDBRegister getRegister(String key) {
    	BlaDBRegister r = new BlaDBRegister();
		LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) this.data.get(key);
		if(map == null) return null;
    	for( Entry<String, Object> e : map.entrySet()) {
    		r.setValue(e.getKey(), e.getValue());
    	}
    	return r;
    }
    
}
