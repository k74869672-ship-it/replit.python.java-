import json
from datetime import datetime

# -------------------------------
# Convert Format 1 → Unified Format
# -------------------------------
def convert_from_format1(data):
    location_parts = data["location"].split("/")

    return {
        "deviceID": data["deviceID"],
        "deviceType": data["deviceType"],
        "timestamp": data["timestamp"],
        "location": {
            "country": location_parts[0],
            "city": location_parts[1],
            "area": location_parts[2],
            "factory": location_parts[3],
            "section": location_parts[4],
        },
        "data": {
            "status": data["operationStatus"],
            "temperature": data["temp"]
        }
    }


# -------------------------------
# Convert Format 2 → Unified Format
# -------------------------------
def convert_from_format2(data):
    # Convert ISO timestamp → milliseconds
    dt = datetime.strptime(data["timestamp"], "%Y-%m-%dT%H:%M:%S.%fZ")
    timestamp = int(dt.timestamp() * 1000)

    return {
        "deviceID": data["device"]["id"],
        "deviceType": data["device"]["type"],
        "timestamp": timestamp,
        "location": {
            "country": data["country"],
            "city": data["city"],
            "area": data["area"],
            "factory": data["factory"],
            "section": data["section"],
        },
        "data": data["data"]
    }


# -------------------------------
# Main Conversion Function
# -------------------------------
def unify_data(data):
    # Detect format automatically
    if "device" in data:
        return convert_from_format2(data)
    else:
        return convert_from_format1(data)


# -------------------------------
# Example Usage
# -------------------------------
if __name__ == "__main__":
    # Load JSON files
    with open("data-1.json") as f1, open("data-2.json") as f2:
        jsonData1 = json.load(f1)
        jsonData2 = json.load(f2)

    # Convert both formats
    result1 = unify_data(jsonData1)
    result2 = unify_data(jsonData2)

    # Print results
    print("Unified Data from Format 1:\n", json.dumps(result1, indent=2))
    print("\nUnified Data from Format 2:\n", json.dumps(result2, indent=2))

    # Optional: Compare both results
    if result1 == result2:
        print("\n✅ Data matched (Reconciled successfully)")
    else:
        print("\n❌ Data mismatch")
