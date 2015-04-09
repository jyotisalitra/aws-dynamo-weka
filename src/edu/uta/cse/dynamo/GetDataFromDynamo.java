/**
 * Jyoti Salitra
 * UTA ID: ***********
 * Cloud Computing (CSE - 6331) - David Levine
 * Programming Assignment # 4
 * Date: 11/09/2014
 */

package edu.uta.cse.dynamo;

import java.util.List;
import java.util.Map;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import edu.uta.cse.util.Constants;

/**
 * Scans data from AWS DynamoDB table using AWS SDK
 * 
 * References:
 * 1. http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LowLevelJavaScanning.html
 */
public class GetDataFromDynamo 
{
	
	private AmazonDynamoDBClient client = null;
	private BasicAWSCredentials awsCreds = null;
	
	public GetDataFromDynamo() 
	{
		//initialize awsCredentials instance using AWS account access keys
		awsCreds = new BasicAWSCredentials(Constants.ACCESS_KEY_ID, Constants.SECRET_ACCESS_KEY);
		
		//use awsCredential to create DynamoDB client
		client = new AmazonDynamoDBClient(awsCreds);
		//set the region of AWS to US-Oregon
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
	}
	
	
	/**
	 * Scans the given table and returns all rows in the list format
	 * @param tableName
	 */
	public List<Map<String, AttributeValue>> scanTable (String tableName) 
	{
		System.out.println("Creating DynamoDB ScanRequest for " + tableName);
		
		long start = System.currentTimeMillis();
		//create a scan request for entire table
		ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
		
		//execute scan requesnt using dynamoDB client
		ScanResult result = client.scan(scanRequest);
		
		//timespent in scanning table from DynamoDB
    	long totalTime = System.currentTimeMillis() - start;
    	System.out.println("Total Time Taken [DynamoDB Scan]: " + totalTime + " ms");
    	
		System.out.println("DynamoDB Scan Complete. Return " + result.getScannedCount() + " records");
		return result.getItems();
	}
}
