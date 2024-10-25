package org.Bairagi.dump;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class JsonToPojoUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates POJO classes from all JSON files in a folder.
     *
     * @param folderPath   The path to the folder containing the JSON files.
     * @param packageName  The desired package name for the generated classes.
     * @throws IOException if there's an issue reading the JSON files or writing the class files.
     */
    public static void generatePojoFromFolder(String folderPath, String packageName) throws IOException {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        // Process all JSON files in the folder
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            throw new IllegalArgumentException("No JSON files found in folder: " + folderPath);
        }

        // Generate POJOs for each JSON file
        for (File jsonFile : jsonFiles) {
            String className = toCamelCase(jsonFile.getName().replace(".json", ""));
            generatePojoFromJsonFile(jsonFile, packageName, className);
        }
    }

    /**
     * Generates a POJO class from a JSON file.
     *
     * @param jsonFile    The JSON file.
     * @param packageName The desired package name for the generated class.
     * @param className   The name of the class to be generated.
     * @throws IOException if there's an issue reading the JSON file or writing the class file.
     */
    public static void generatePojoFromJsonFile(File jsonFile, String packageName, String className) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonFile);

        // Build the class definition
        StringBuilder classBuilder = new StringBuilder();

        // Add the package name
        classBuilder.append("package ").append(packageName).append(";\n\n");
        classBuilder.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        classBuilder.append("import lombok.*;\n");
        classBuilder.append("import java.util.List;\n\n");

        // Add Lombok annotations to the main class
        classBuilder.append("@Data\n");
        classBuilder.append("@Builder\n");
        classBuilder.append("@NoArgsConstructor\n");
        classBuilder.append("@AllArgsConstructor\n");
        classBuilder.append("public class ").append(className).append(" {\n");

        // Recursively generate fields and inner classes for the JSON structure
        generateFieldsAndInnerClasses(jsonNode, classBuilder, className);

        classBuilder.append("}\n");

        // Create directories if they don't exist
        String directoryPath = "src/main/java/" + packageName.replace(".", "/");
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Write the generated class to a .java file
        File javaFile = new File(directory, className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(classBuilder.toString());
        }

        System.out.println("Class generated and compiled successfully: " + javaFile.getPath());
    }

    // Method to recursively generate fields and inner classes for nested JSON objects/arrays
    private static void generateFieldsAndInnerClasses(JsonNode jsonNode, StringBuilder classBuilder, String parentClassName) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            String fieldType = determineFieldType(fieldValue, parentClassName, classBuilder);

            // Annotate fields with @JsonProperty for JSON mapping
            classBuilder.append("    @JsonProperty(\"").append(fieldName).append("\")\n");
            classBuilder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n");
        }
    }

    // Method to determine the type of a field based on its JSON value
    private static String determineFieldType(JsonNode fieldValue, String parentClassName, StringBuilder classBuilder) {
        if (fieldValue.isTextual()) {
            return "String";
        } else if (fieldValue.isInt() || fieldValue.isLong()) {
            return "int";
        } else if (fieldValue.isBoolean()) {
            return "boolean";
        } else if (fieldValue.isDouble()) {
            return "double";
        } else if (fieldValue.isArray()) {
            // Handle arrays by determining the type of elements in the array
            JsonNode firstElement = fieldValue.elements().next();
            String arrayType = determineFieldType(firstElement, parentClassName, classBuilder);
            return "List<" + arrayType + ">"; // Handle list of elements
        } else if (fieldValue.isObject()) {
            // For nested objects, generate an inner class
            String nestedClassName = parentClassName + capitalizeFirstLetter(fieldValue.fieldNames().next());
            classBuilder.append("    @Data\n");
            classBuilder.append("    @Builder\n");
            classBuilder.append("    @NoArgsConstructor\n");
            classBuilder.append("    @AllArgsConstructor\n");
            classBuilder.append("    public static class ").append(nestedClassName).append(" {\n");
            generateFieldsAndInnerClasses(fieldValue, classBuilder, nestedClassName);
            classBuilder.append("    }\n\n");
            return nestedClassName;
        } else {
            return "String"; // Default to String if unknown type
        }
    }

    // Utility method to capitalize the first letter of a string
    private static String capitalizeFirstLetter(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    // Utility method to convert file names to CamelCase for class names
    private static String toCamelCase(String fileName) {
        StringBuilder camelCaseName = new StringBuilder();
        boolean nextUpperCase = true;

        for (char c : fileName.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                camelCaseName.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                camelCaseName.append(c);
            }
        }

        return camelCaseName.toString();
    }
}
