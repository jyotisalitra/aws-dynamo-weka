/**
 * Jyoti Salitra
 * UTA ID: ***********
 * Cloud Computing (CSE - 6331) - David Levine
 * Programming Assignment # 4
 * Date: 11/09/2014
 */

package edu.uta.cse.dynamo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import edu.uta.cse.util.Constants;

/**
 * Loads data to AWS DynamoDB table using AWS SDK
 * 
 * References:
 * 1. http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LoadData_Java.html
 */
public class LoadDataToDynamo 
{
	private static AmazonDynamoDBClient client = null;
	
	public static void main(String[] args) throws Exception 
	{
		//initialize awsCredentials instance using AWS account access keys
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(Constants.ACCESS_KEY_ID, Constants.SECRET_ACCESS_KEY);
		
		//use awsCredential to create DynamoDB client
		client = new AmazonDynamoDBClient(awsCreds);
		//set the region of AWS to US-Oregon
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
		
		try {
			//start the timer
			System.out.println("Starting uploading data to DynamoDB");
	        long start = System.currentTimeMillis();
	        
			uploadTitanicData(Constants.DYNAMO_TABLE_NAME);
			
			//calculate the time spent in uploading two csv files to AWS DynamoDB
        	long totalTime = System.currentTimeMillis() - start;
        	System.out.println("Total Time Taken: " + totalTime + " ms");

		} catch (AmazonServiceException ase) {
			System.err.println("Data load script failed.");
		}
	}

	/**
	 * Upload titanic data to DynamoDB table
	 * @param tableName
	 */
	private static void uploadTitanicData(String tableName) 
	{
		BufferedReader br = null;
		String line = "";
		try {
			//read from the local data file
			br = new BufferedReader(new FileReader(Constants.CSV_FILE_PATH));
			int id = 0;

			while ((line = br.readLine()) != null) {

				// use comma as separator to split the values
				String[] data = line.split(",");
				
				String passengerClass = data[0];
				String age = data[1];
				String sex = data[2];
				String survived = data[3];
				
				//Create a map of all attributes and values for a record
				Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
				
				//set a personID by incrementing id (required as a primary key)
				item.put("PersonID", new AttributeValue().withN(String.valueOf(++id)));
				item.put("Class", new AttributeValue().withS(passengerClass));
				item.put("Age", new AttributeValue().withS(age));
				item.put("Sex", new AttributeValue().withS(sex));
				item.put("Survived", new AttributeValue().withS(survived));

				//create the putItemRequest
				PutItemRequest itemRequest = new PutItemRequest().withTableName(tableName).withItem(item);
				//execute putItemRequest using AWS DynamoDB client
				client.putItem(itemRequest);
				item.clear();
			}
			
		} catch (AmazonServiceException ase) {
			System.err.println("Failed to create item in " + tableName + " " + ase);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
