import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat;

public class Main {

    // ObjectMapper is used to read JSON files into Java Map objects
    static ObjectMapper mapper = new ObjectMapper();

    // ---------------------------------------------------------
    // Convert JSON data from Format 1 → Unified Format
    // ---------------------------------------------------------
    public static Map<String, Object> convertFromFormat1(Map<String, Object> jsonObject) {

        // ASSUMPTION:
        // Format 1 stores location as a single string separated by "/"
        // Example: "India/Bangalore/Area1/Factory1/SectionA"
        // We split this string to extract structured location fields
        String location = (String) jsonObject.get("location");
        String[] locationParts = location.split("/");

        // ASSUMPTION:
        // Location string always contains exactly 5 parts in order:
        // country/city/area/factory/section
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("country", locationParts[0]);
        locationMap.put("city", locationParts[1]);
        locationMap.put("area", locationParts[2]);
        locationMap.put("factory", locationParts[3]);
        locationMap.put("section", locationParts[4]);

        // DECISION:
        // Rename fields to match unified schema
        // operationStatus → status
        // temp → temperature
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("status", jsonObject.get("operationStatus"));
        dataMap.put("temperature", jsonObject.get("temp"));

        // NOTE:
        // Timestamp is already assumed to be in milliseconds in Format 1,
        // so no conversion is needed
        Map<String, Object> result = new HashMap<>();
        result.put("deviceID", jsonObject.get("deviceID"));
        result.put("deviceType", jsonObject.get("deviceType"));
        result.put("timestamp", jsonObject.get("timestamp"));
        result.put("location", locationMap);
        result.put("data", dataMap);

        return result;
    }

    // ---------------------------------------------------------
    // Convert JSON data from Format 2 → Unified Format
    // ---------------------------------------------------------
    public static Map<String, Object> convertFromFormat2(Map<String, Object> jsonObject) throws Exception {

        // ASSUMPTION:
        // Format 2 uses ISO 8601 timestamp (e.g., "2023-10-01T10:20:30.000Z")
        // The unified format requires timestamp in milliseconds since epoch
        String timestampStr = (String) jsonObject.get("timestamp");

        // DECISION:
        // Use SimpleDateFormat to parse ISO string and convert to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Date date = sdf.parse(timestampStr);

        // Convert to milliseconds since epoch (standard format)
        long timestamp = date.getTime();

        // ASSUMPTION:
        // Device details are nested inside "device" object
        Map<String, Object> device = (Map<String, Object>) jsonObject.get("device");

        // Location fields are already structured in Format 2
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("country", jsonObject.get("country"));
        locationMap.put("city", jsonObject.get("city"));
        locationMap.put("area", jsonObject.get("area"));
        locationMap.put("factory", jsonObject.get("factory"));
        locationMap.put("section", jsonObject.get("section"));

        // NOTE:
        // Data field is already in correct structure, so no transformation needed
        Map<String, Object> result = new HashMap<>();
        result.put("deviceID", device.get("id"));
        result.put("deviceType", device.get("type"));
        result.put("timestamp", timestamp);
        result.put("location", locationMap);
        result.put("data", jsonObject.get("data"));

        return result;
    }

    // ---------------------------------------------------------
    // Main Logic: Detect format and convert accordingly
    // ---------------------------------------------------------
    public static Map<String, Object> mainLogic(Map<String, Object> jsonObject) throws Exception {

        // DECISION:
        // If "device" field exists → Format 2
        // Otherwise → Format 1
        if (jsonObject.get("device") == null) {
            return convertFromFormat1(jsonObject);
        } else {
            return convertFromFormat2(jsonObject);
        }
    }

    // ---------------------------------------------------------
    // Entry point of the program
    // ---------------------------------------------------------
    public static void main(String[] args) {
        try {
            // Read input JSON files
            Map<String, Object> jsonData1 = mapper.readValue(new File("data-1.json"), Map.class);
            Map<String, Object> jsonData2 = mapper.readValue(new File("data-2.json"), Map.class);

            // Expected output for validation
            Map<String, Object> expected = mapper.readValue(new File("data-result.json"), Map.class);

            // Convert both formats into unified format
            Map<String, Object> result1 = mainLogic(jsonData1);
            Map<String, Object> result2 = mainLogic(jsonData2);

            // VALIDATION:
            // Compare results with expected output to ensure correctness
            if (result1.equals(expected)) {
                System.out.println("Type 1 conversion PASSED");
            } else {
                System.out.println("Type 1 conversion FAILED");
            }

            if (result2.equals(expected)) {
                System.out.println("Type 2 conversion PASSED");
            } else {
                System.out.println("Type 2 conversion FAILED");
            }

        } catch (Exception e) {
            // ERROR HANDLING:
            // Handles file errors, parsing issues, or invalid formats
            e.printStackTrace();
        }
    }
}
