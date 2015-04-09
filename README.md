# aws-dynamo-weka
Upload data to Amazon AWS DynamoDB instance to measure the time taken. Then use Weka to create clusters of data.

###Steps:
1. Created AWS DynamoDB instance on AWS account.
2. Created a table on DynamoDB on the above instance and set PersonID as the primary
key.
3. Installed AWS SDK in Eclipse to access AWS services from Java code.
4. Downloaded titanic survival data from
`http://www.cs.toronto.edu/~delve/data/titanic/desc.html`
5. Uploaded data from `titanic.data` file to DynamoDB using `LoadDataToDynamo.java`. 
6. Downloaded `Weka API (3.6.11)` from `http://sourceforge.net/projects/weka/` and configured Eclipse project to use `weka.jar`.
7. To create clusters and visualize titanic data run `WekaRunner.java`. This class does following things:
    * Create Weka Instances after download data from DynamoDB using AWS API.
    * These weka instances are then passed to a clusterer. `weka.clusterers.EM` class is used for clustering.
    * Using `VisualizePanel` class in weka, created a cluster visualization of the titanic data.
    * Timings:
        Total Time Taken [DynamoDB Scan]: 1718 ms
        Total Time Taken [WekaRunner]: 24356 ms
