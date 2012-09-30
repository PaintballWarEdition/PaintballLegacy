package me.blablubbabc.BlaDB;

import java.util.LinkedHashMap;

public class BlaDBRegister {
	private LinkedHashMap<String,Object> data;
	
	public BlaDBRegister() {
		this.data = new LinkedHashMap<String,Object>();
    }
	
    //////////////////////////////////////////////////
    //
    // METHODS
    //
    //////////////////////////////////////////////////

    public synchronized void setValue(String key, Object value) {
        this.data.put(key, value);
    }

    public synchronized void removeValue(String key) {
        this.data.remove(key);
    }
    
    public synchronized void clear() {
        this.data = new LinkedHashMap<String, Object>();
    }
    
    //////////GETTERS//////////////////////////////////
    
    public synchronized Object getValue(String key) {
    	return this.data.get(key);
    }
    
    public synchronized LinkedHashMap<String, Object> getData() {
    	return new LinkedHashMap<String, Object>(this.data);
    }

}
