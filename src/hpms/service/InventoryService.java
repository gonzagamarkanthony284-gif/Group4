package hpms.service;

import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    public static List<String> setStock(String name, String qty) {
        List<String> out = new ArrayList<>(); 
        if (name==null || name.trim().isEmpty() || qty==null || qty.trim().isEmpty()) { 
            out.add("Error: Missing parameters"); 
            return out; 
        }
        int q; 
        try { q = Integer.parseInt(qty.trim()); } catch (Exception e) { 
            out.add("Error: Invalid quantity"); 
            return out; 
        }
        if (q < 0) { 
            out.add("Error: Invalid quantity"); 
            return out; 
        }
        // Medicine functionality removed - commenting out medicineStock
        // DataStore.medicineStock.put(name.trim(), q); 
        out.add("Stock updated - medicine functionality disabled"); 
        return out;
    }
    
    public static List<String> summary() { 
        List<String> out = new ArrayList<>(); 
        // Medicine functionality removed - commenting out medicineStock
        /*
        for (java.util.Map.Entry<String, Integer> e : DataStore.medicineStock.entrySet()) 
            out.add(e.getKey()+": "+e.getValue()); 
        if (out.isEmpty()) 
        */
        out.add("No medicine records - functionality disabled"); 
        return out; 
    }
}

