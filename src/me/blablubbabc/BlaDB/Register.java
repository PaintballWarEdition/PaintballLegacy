package me.blablubbabc.BlaDB;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Register {
	private LinkedHashMap<String,Object> data;
	
	public Register() {
		this.data = new LinkedHashMap<String,Object>();
    }
	
    //////////////////////////////////////////////////
    //
    // METHODS
    //
    //////////////////////////////////////////////////

    public void setValue(String key, Object value) {
        this.data.put(key, value);
    }

    public void removeValue(String key) {
        this.data.remove(key);
    }
    
    public void clear() {
        this.data.clear();
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

}
