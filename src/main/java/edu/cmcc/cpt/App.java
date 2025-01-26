package edu.cmcc.cpt;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



/**
 * Student class for returning data
 */

class Student {
    String cpt_username;
    String class_code;
    String passkey;


    /**
     * Class Initializer used to create empty Student object
     *
    public Student() {
        this.cpt_username = "";
        this.class_code = "";
        this.passkey = "";
    }
    */

    /**
     * Class Initializer overload with all parameters!
     * @param cpt_username - CPT username
     * @param class_code - Class code
     * @param passkey - Passkey for assignment
     */
    public Student(String cpt_username, String class_code, String passkey) {
        this.cpt_username = cpt_username;
        this.class_code = class_code;
        this.passkey = passkey;
    }

    public String get_username() {
        return this.cpt_username;
    }

    public void set_username(String username) {
        this.cpt_username = username;
    }

    public String get_class_code() {
        return this.class_code;
    }

    public void set_class_code(String class_code) {
        this.class_code = class_code;
    }

    public String get_passkey() {
        return this.passkey;
    }

    public void set_passkey(String passkey) {
        this.passkey = passkey;
    }
};

public class App {
    // Load environment variables from .env file
    private static final Dotenv dotenv = Dotenv.load();


    private static final int add_two_numbers(int a,int b) {
        return a + b;
    }

    public static void main(String[] args) {
        // Example usage

        Student student = new Student();
        student = queryDatabase();

        double c = add_two_numbers(4,5);

        if(c == 10) {
            if(student.get_username() != "") {
                sendPostRequest(student);
            }
        } else {
            System.out.println("Your code should not get here!");
        }
        
    }

    /**
     * Function to query a PostgreSQL database and print results.
     */
    public static Student queryDatabase() {
        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");

        String query = "SELECT cpt_username, 'CPT245' as class_code, passkey from cpt_program.week_two_keys where class_code = 'CPT245'"; // Replace with your actual table and fields


        Student student = new Student();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("Connected to the database!");

            

            while (resultSet.next()) {

                student.set_username(resultSet.getString("cpt_username"));
                student.set_class_code(resultSet.getString("class_code"));
                student.set_passkey(resultSet.getString("passkey"));
            }

            System.out.println("Collected Student " + student.get_username());

            return student;

        } catch (Exception e) {
            System.err.println("Error querying database: " + e.getMessage());
            e.printStackTrace();
            return student;
        }
    }

    /**
     * HINT = there is an issue here!  The below is a function body, but what happened to the definition?!
     */

        String apiUrl = dotenv.get("API_URL");
        String jsonPayload = "{\"cpt_username\":\"" + student.get_username() + "\",\"class_code\":\"" + student.get_class_code() + "\",\"passkey\":\"" + student.get_passkey() + "\"}";

        // System.out.println(jsonPayload);

        System.out.println("Calling API Endpoint");
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write JSON payload to the request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes("UTF-8"));
                os.flush();
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }


                    System.out.println("POST request successful.");
                    System.out.println("Result: " + response.toString());



                }
            } else {
                System.out.println("POST request failed.");
                System.out.println("Result: " + connection.toString());
            }

        } catch (Exception e) {
            System.err.println("Error sending POST request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}