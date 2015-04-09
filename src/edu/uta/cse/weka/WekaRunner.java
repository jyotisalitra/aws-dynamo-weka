/**
 * Jyoti Salitra
 * UTA ID: ***********
 * Cloud Computing (CSE - 6331) - David Levine
 * Programming Assignment # 4
 * Date: 11/09/2014
 */

package edu.uta.cse.weka;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.explorer.ClustererPanel;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import edu.uta.cse.dynamo.GetDataFromDynamo;
import edu.uta.cse.util.Constants;

/**
 * Loads titanic data from AWS DynamoDB, create Weka instances, 
 * uses cluserting algorithm to process data, 
 * and then create the visualization
 * 
 * References:
 * 1. http://weka.wikispaces.com/Visualizing+cluster+assignments
 * 2. http://weka.wikispaces.com/Programmatic+Use
 * 3. http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LowLevelJavaScanning.html
 */
public class WekaRunner 
{

	public static void main(String[] args) throws Exception 
	{
		//WekaRunner params specifying the clustering algorithm
		String [] params = {"-W", "weka.clusterers.EM -I 50"};

		long start = System.currentTimeMillis();
		
		visualizeCluster(params);
		
		//timespent in WekaRunner program
    	long totalTime = System.currentTimeMillis() - start;
    	System.out.println("Total Time Taken [WekaRunner]: " + totalTime + " ms");
	}
	
	/**
	 * Create graph visualization using Weka and Java Swing
	 * @throws Exception
	 */
	private static void visualizeCluster (String[] params) throws Exception
	{
		//create Weka instances using data from DynamoDB
	    Instances titanicTrainingSet = generateInstances();
	    
	    // some data formats store the class attribute information as well
	    //hence, throw exception
	    if (titanicTrainingSet.classIndex() != -1)
	    {
	    	throw new IllegalArgumentException("Data cannot have class attribute!");
	    }

	    // instantiate clusterer
	    //get algorithm name
	    String[] options = Utils.splitOptions(Utils.getOption('W', params));
	    String classname = options[0];
	    options[0] = "";
	    Clusterer clusterer = AbstractClusterer.forName(classname, options);
	    
	    // evaluate clusterer
	    System.out.println("Building Weka Clusters");
	    clusterer.buildClusterer(titanicTrainingSet);
	    ClusterEvaluation eval = new ClusterEvaluation();
	    eval.setClusterer(clusterer);
	    eval.evaluateClusterer(titanicTrainingSet);

	    // setup visualization
	    // taken from: ClustererPanel.startClusterer()
	    System.out.println("Preparing Visualization");
	    PlotData2D predData = ClustererPanel.setUpVisualizableInstances(titanicTrainingSet, eval);
	    String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
	    String cname = clusterer.getClass().getName();
	    if (cname.startsWith("weka.clusterers."))
	    {
	    	name += cname.substring("weka.clusterers.".length());
	    }
	    else
	    {
	    	name += cname;
	    }

	    //prepare visualizationPanel
	    VisualizePanel vp = new VisualizePanel();
	    vp.setName(name + " (" + titanicTrainingSet.relationName() + ")");
	    predData.setPlotName(name + " (" + titanicTrainingSet.relationName() + ")");
	    vp.addPlot(predData);

	    // display data
	    // taken from: ClustererPanel.visualizeClusterAssignments(VisualizePanel)
	    String plotName = vp.getName();
	    JFrame jf = new JFrame("Weka Clusterer Visualize: " + plotName);
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jf.setSize(600,500);
	    jf.getContentPane().setLayout(new BorderLayout());
	    jf.getContentPane().add(vp, BorderLayout.CENTER);
	    jf.setVisible(true);
	}
	
	/**
	 * Create Weka Instances with the data from DynamoDB
	 */
	private static Instances generateInstances ()
	{
		System.out.println("Generating Weka Instances");
		System.out.println("Creating Attributes");
		
        // Declare a nominal attribute for passengerClass
        FastVector fvNominalClass = new FastVector(4);
        fvNominalClass.addElement("1st");
        fvNominalClass.addElement("2nd");
        fvNominalClass.addElement("3rd");
        fvNominalClass.addElement("crew");
        Attribute Attribute1 = new Attribute("passengerclass", fvNominalClass);
         
        // Declare a nominal attribute for age
        FastVector fvNominalAge = new FastVector(2);
        fvNominalAge.addElement("adult");
        fvNominalAge.addElement("child");
        Attribute Attribute2 = new Attribute("age", fvNominalAge);
        
        // Declare a nominal attribute for sex
        FastVector fvNominalSex = new FastVector(2);
        fvNominalSex.addElement("male");
        fvNominalSex.addElement("female");
        Attribute Attribute3 = new Attribute("sex", fvNominalSex);
        
        // Declare a nominal attribute for survived
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("yes");
        fvClassVal.addElement("no");
        Attribute ClassAttribute = new Attribute("survived", fvClassVal);
         
        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(4);
        fvWekaAttributes.addElement(Attribute1);    
        fvWekaAttributes.addElement(Attribute2);    
        fvWekaAttributes.addElement(Attribute3);  
        fvWekaAttributes.addElement(ClassAttribute);
         
        //get data from DynamoDB
        GetDataFromDynamo dynamo = new GetDataFromDynamo();
        
        //scan titanic table and scan entire table using AWS SDK
        List<Map<String, AttributeValue>> dynamoData = dynamo.scanTable(Constants.DYNAMO_TABLE_NAME);
        
        // Create an empty training set with capacity of the data coming from DynamoDB
        Instances instancesSet = new Instances("TitanicWekaRelation", fvWekaAttributes, dynamoData.size());       
         
        //iterate over all the records
        for (Map<String, AttributeValue> attributeList : dynamoData)
        {
        	//create a new instance for each record
            Instance inst = new Instance(4);
            
            //iterate over all the attributes of the record
        	for (Map.Entry<String, AttributeValue> item : attributeList.entrySet()) 
        	{
                String attributeName = item.getKey();
                AttributeValue value = item.getValue();
                
                //set attribute value in weka instance depending on the type
                if(attributeName.equals("Class"))
                {
                	inst.setValue((Attribute)fvWekaAttributes.elementAt(0), value.getS());     
                }
                else if(attributeName.equals("Age"))
                {
                	inst.setValue((Attribute)fvWekaAttributes.elementAt(1), value.getS());     
                }
                else if(attributeName.equals("Sex"))
                {
                	inst.setValue((Attribute)fvWekaAttributes.elementAt(2), value.getS());     
                }
                else if(attributeName.equals("Survived"))
                {
                	inst.setValue((Attribute)fvWekaAttributes.elementAt(3), value.getS());     
                }
            }
        	//add instance to the set
        	instancesSet.add(inst);
        }
        return instancesSet;
	}
}
