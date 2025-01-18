package io.hahnsoftware.emp;

import com.google.gson.Gson;

public class GsonTest {
    public static void main(String[] args) {
        // Create a new Gson instance
        Gson gson = new Gson();
        
        // Create a test object
        TestObject obj = new TestObject("test", 123);
        
        // Convert to JSON
        String json = gson.toJson(obj);
        System.out.println("Converted to JSON: " + json);
        
        // Parse from JSON
        TestObject parsed = gson.fromJson(json, TestObject.class);
        System.out.println("Parsed from JSON: " + parsed.name + ", " + parsed.value);
    }
    
    static class TestObject {
        String name;
        int value;
        
        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}