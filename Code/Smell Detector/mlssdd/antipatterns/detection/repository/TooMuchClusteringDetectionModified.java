

package mlssdd.antipatterns.detection.repository;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.antipatterns.detection.AbstractAntiPatternDetection;
import mlssdd.antipatterns.detection.IAntiPatternDetection;
import mlssdd.kernel.impl.MLSAntiPattern;
import mlssdd.utils.PropertyGetter;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.sax.SAXSource;
import java.lang.*;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class TooMuchClusteringDetectionModified extends AbstractAntiPatternDetection
		implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {

		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		final int minNbOfMethodsPerClass = PropertyGetter
			.getIntProp("TooMuchClustering.MinNbOfMethodsPerClass", 6);

		final Set<MLSAntiPattern> antiPatternSet = new HashSet<>();

		try {
			final XPathExpression PACKAGE_EXP = this.xPath
				.compile(
					"ancestor::unit/" + IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression FILEPATH_EXP = this.xPath
				.compile(
					"ancestor::unit/" + IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);
			final XPathExpression NAME_EXP = this.xPath.compile("name");

			// Java classes
			final NodeList classList = (NodeList) this.xPath
				.evaluate("descendant::class", xml, XPathConstants.NODESET);
			final int nbClasses = classList.getLength();

			classloop:
			for (int j = 0; j < nbClasses; j++) {
				final Node thisClassNode = classList.item(j);
				// Native method declaration
				final NodeList nativeDeclList = (NodeList) NATIVE_EXP
					.evaluate(thisClassNode, XPathConstants.NODESET);
					System.out.println("number of native methods: "+nativeDeclList.getLength());
				if (nativeDeclList.getLength() > minNbOfMethodsPerClass) {
					//if number of native methods in the class > 6, check any of the method called outside of this class
					

					for(int a=0; a<nativeDeclList.getLength(); a++)
					{
						XPathExpression name = xpath.compile("name");
        				final String thisNativeMethod = name.evaluate(nativeDeclList.item(a));

        				//class name
        				final String thisClass = NAME_EXP.evaluate(thisClassNode);

        				//interface name which this class implements.
				        XPathExpression in = xpath.compile("descendant::super_list/implements/super/name");
				        String interfaceName = in.evaluate(thisClassNode);


				        //child classes.
				        final String cc1 = String.format("descendant::class[super_list/extends/super/name='%s']",thisClass);
				        final NodeList childClasses = (NodeList) xpath.evaluate(cc1, xml, XPathConstants.NODESET);
				        String[] children = new String[childClasses.getLength()];
				        for(int child = 0; child<childClasses.getLength(); child++)
				        {
				          //child class name
				            XPathExpression childName = xpath.compile("name/name | name");
				            String name1 = childName.evaluate(childClasses.item(child));
				            System.out.println("child class: "+name1);
				            children[child] = name1;
				        }
        				//java files which imports this method
				        //import name className.methodName
				        String importName = thisClass+"."+thisNativeMethod;



				        //extracting files which import this method.
				        final String imp = String.format("//unit[import[contains(., '%s')]]", importName);
				        final NodeList importFiles = (NodeList) xpath.evaluate(imp, xml, XPathConstants.NODESET);
						
						//list of parameter types for a particular native function
				        final String params = String.format("descendant::parameter_list/parameter/decl/type/name", thisNativeMethod);
				        final NodeList parameter_list = (NodeList) xpath.evaluate(params, nativeDeclList.item(a), XPathConstants.NODESET);					        

				        //calls in the other class be me direct where the class was imported
				        //or class.method or object.method

				        //calls in the importing files.
						  for(int iF = 0; iF<importFiles.getLength(); iF++)
						  {
						      //list of calls to the native method in the file which import this native method
						    final String callQuery3 = String.format("descendant::call[name='%s']", thisNativeMethod);
						    final NodeList importingCalls= (NodeList) xpath.evaluate(callQuery3, importFiles.item(iF), XPathConstants.NODESET);
						    
						    for(int ic=0; ic<importingCalls.getLength(); ic++)
						    {
						      System.out.println("call: "+importingCalls.item(ic).getTextContent());


						          //ancestor functions.
						          XPathExpression xp = xpath.compile("ancestor::function");
						          NodeList AncestorFunctions = (NodeList) xp.evaluate(importingCalls.item(ic), XPathConstants.NODESET);
						        
						        //class in which this call happend.
				        XPathExpression cn2 = xpath.compile("(ancestor::class)");
				        NodeList thisClass1 = (NodeList) cn2.evaluate(importingCalls.item(ic), XPathConstants.NODESET);

						        //arguments in the call
						         // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list[1]/argument)",thisNativeMethod, i+1);
						         //    final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET); 
						         
						          final String callQuery2 = String.format("descendant::argument_list[1]/argument");
						            final NodeList callList = (NodeList) xpath.evaluate(callQuery2, importingCalls.item(ic), XPathConstants.NODESET); 
						         

						         //  //System.out.println("Number of arguments: "+callList.getLength());

						          if(parameter_list.getLength()==callList.getLength())
						          {
						            //list of argument in a particular call.
						            // final String callQuery2 = String.format("(descendant::call[name='%s'][%s]/argument_list/argument)",thisNativeMethod, i+1);
						            // final NodeList callList = (NodeList) xpath.evaluate(callQuery2, thisClass.item(0), XPathConstants.NODESET);
						            //System.out.println(callList.getLength());
						            int equal=1;
						            for (int j1=0; j1<callList.getLength();j1++)
						            { 
						              //System.out.println(callList.item(j).getTextContent());
						          

						              //type of the argument passed.
						              //final String arg = String.format("(descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::decl_stmt/decl[name='%s']/type/name | descendant::call[name='%s']/argument_list/argument/expr[name='%s']//preceding::parameter_list/parameter/decl[name='%s']/type/name)[last()] | descendant::call[name='%s']/argument_list/argument/expr[literal='%s']/literal/@type", thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(),thisNativeMethod, callList.item(j).getTextContent(), callList.item(j).getTextContent(), thisNativeMethod, callList.item(j).getTextContent());
						              // final String arg = String.format("(descendant::call[name='%s'][%s]//"+
						              //     "preceding::decl_stmt/decl[name='%s']/type/name)[1] | (descendant::call[name='%s'][%s]//"+
						              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
						              //     "descendant::call[name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type | "
						              //     +"(descendant::call[name/name='this' and name/name='%s'][%s]//"+
						              //     "preceding::decl_stmt/decl[name='%s']/type/name | "
						              //     +"descendant::call[name/name='this' and name/name='%s'][%s]//"+
						              //     "preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
						              //     "descendant::call[name/name='this' and name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type", 
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent(),
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent(), 
						              //     thisNativeMethod, i+1, callList.item(j).getTextContent());
						              
						              String argType="";

						              //check whether this argument is passed dirctly as literal.
						              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList.item(j1).getTextContent());
						              argType = xpath.evaluate(xp2, importingCalls.item(ic));
						              
						              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
						              if(argType.equals(""))
						              {
						                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
						                {
						                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
						                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList.item(j1).getTextContent());
						                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
						                  //System.out.println("argType= "+argType);
						                  if(!argType.equals(""))
						                    break;
						                }

						                //if it is still null it means it is not passed as a parameter to ancestor function
						                //now check whether this variable is declared in the class but that should not be inside any function

						                if(argType.equals(""))
						                {
						                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList.item(j1).getTextContent());
						                  argType = xpath.evaluate(xp3, thisClass1.item(0));
						                }
						              }


						              //String test = String.format("(descendant::decl_stmt/decl[name='%s']/type/name)", callList.item(j).getTextContent());

						              //final String argType =  xpath.evaluate(test, thisClass.item(0));
						  
						                  argType = argType.replace("const","");
						                argType = argType.replace("final","");
						                argType = argType.replace("static","");
						                //argType = argType.replace("short","");
						                //argType = argType.replace("long","");
						                argType = argType.replace("unsigned","");
						                argType = argType.replace("signed","");
						                argType = argType.replace(" ","");

						                 System.out.println(argType);

						                if(argType.equals("number") && (parameter_list.item(j1).getTextContent().equals("int") || parameter_list.item(j1).getTextContent().equals("float") || parameter_list.item(j1).getTextContent().equals("double") || parameter_list.item(j1).getTextContent().equals("long")))
						                  continue;
						                if(argType.equals(""))
						                  continue;
						                if((argType.equals("int") && (parameter_list.item(j1).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(j1).getTextContent().equals("int"))))
						                  continue;

						                 if(!argType.equalsIgnoreCase(parameter_list.item(j1).getTextContent()))
						                 equal=0;
						              // }
						        
						          }
						      if(equal==1)
						        { 
						        	System.out.println("called in importing file"+thisNativeMethod);
						    
						          final String thisPackage =
										PACKAGE_EXP.evaluate(thisClassNode);
									final String thisFilePath =
										FILEPATH_EXP.evaluate(thisClassNode);
									antiPatternSet
										.add(
											new MLSAntiPattern(
												this.getAntiPatternName(),
												"",
												"",
												thisClass,
												thisPackage,
												thisFilePath));
										continue classloop;
						          
						        }
						      }
						    }
						  }


				        //extracting the calls of the type class.method() or object.method()
				        final String callQuery1 = String.format("descendant::call[name/name='%s']",thisNativeMethod);
        				final NodeList calls1= (NodeList) xpath.evaluate(callQuery1, xml, XPathConstants.NODESET);
						
						for (int x=0; x<calls1.getLength(); x++)
				        {


				          System.out.println("call: "+calls1.item(x).getTextContent());
				          String callName = calls1.item(x).getTextContent();
				          String[] tokens = callName.split("\\.");
				          String callerName;
				          if(!tokens[0].equals("this"))
				            callerName=tokens[0];
				          else callerName = tokens[1];
				          

				        //class in which this call happend.
				        XPathExpression cn2 = xpath.compile("(ancestor::class)");
				        NodeList thisClass1 = (NodeList) cn2.evaluate(calls1.item(x), XPathConstants.NODESET);
				        //System.out.println("class: "+thisClass1.item(0).getTextContent());


				        
				        XPathExpression cn5 = xpath.compile("name/name | name");
				        String className2="";
				        if(thisClass1.item(0)!=null) 
				        {       
				        className2 = cn5.evaluate(thisClass1.item(0));
				         System.out.println("calling class: "+className2);

				         //otherClasses.add(filename+"_"+className2);

				        //ancestor functions.
				          XPathExpression xp = xpath.compile("ancestor::function");
				          NodeList AncestorFunctions = (NodeList) xp.evaluate(calls1.item(x), XPathConstants.NODESET);
				        
				          String callerType="";

				              
				              //check whether this callerName is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callerName);
				                  callerType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!callerType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(callerType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callerName);
				                  if(thisClass1.item(0)!=null)
				                  callerType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				          
				        System.out.println(callerType);

				          //System.out.println(!(callerType=="") && callerType.equals(interfaceName));
				          //if callerName is classname or callertype is either class type or interface type then proceed.

				        

				        
				          if(callerName.equals(thisClass) || callerType.equals(thisClass) || (callerType!="" && callerType.equals(interfaceName)))
				          {  
				            
				         

				         //final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				         final String callQuery3 = String.format("descendant:: argument_list[1]/argument");
				            final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, calls1.item(x), XPathConstants.NODESET); 
				          //System.out.println("Number of arguments: "+NumberOfArguments1.intValue());
				           


				            //System.out.println(NumberOfParameters.intValue()+" "+callList1.getLength());
				          if(parameter_list.getLength()==callList1.getLength())
				          {
				            //list of argument in a particular call.
				            // final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				            // final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, doc, XPathConstants.NODESET);
				            //System.out.println(callList1.getLength());
				            int equal=1;
				            for (int c1=0; c1<callList1.getLength();c1++)
				            { 
				              //System.out.println(callList1.item(c1).getTextContent());
				          

				               String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList1.item(c1).getTextContent());
				              argType = xpath.evaluate(xp2, calls1.item(x));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				              }






				              
				               argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				                argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];


				               System.out.println(argType);

				                if(argType.equals("number") && (parameter_list.item(c1).getTextContent().equals("int") || parameter_list.item(c1).getTextContent().equals("float") || parameter_list.item(c1).getTextContent().equals("double") || parameter_list.item(c1).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(c1).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(c1).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(c1).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          	System.out.println("called in other class");

				          
							final String thisPackage =
								PACKAGE_EXP.evaluate(thisClassNode);
							final String thisFilePath =
								FILEPATH_EXP.evaluate(thisClassNode);
							antiPatternSet
								.add(
									new MLSAntiPattern(
										this.getAntiPatternName(),
										"",
										"",
										thisClass,
										thisPackage,
										thisFilePath));
								continue classloop;
				          
				        }
				      }
				  }

				else
				{
				    for(int cn = 0; cn< children.length ; cn++)
				    {
				        if(callerType.equals(children[cn]))
				        {
				            System.out.println("matched child");

				            //count the number of argement passed.
				         

				         //final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				         final String callQuery3 = String.format("descendant:: argument_list[1]/argument");
				            final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, calls1.item(x), XPathConstants.NODESET); 
				          //System.out.println("Number of arguments: "+NumberOfArguments1.intValue());
				           


				            System.out.println(parameter_list.getLength()+" "+callList1.getLength());
				          if(parameter_list.getLength()==callList1.getLength())
				          {
				            //int temp=0;
				            //list of argument in a particular call.
				            // final String callQuery3 = String.format("(descendant::call[name/name='%s' and name/name='%s'][%s]/argument_list/argument)",className, thisNativeMethod, x+1);
				            // final NodeList callList1 = (NodeList) xpath.evaluate(callQuery3, doc, XPathConstants.NODESET);
				            //System.out.println(callList1.getLength());
				            int equal=1;
				            for (int c1=0; c1<callList1.getLength();c1++)
				            { 
				              //System.out.println(callList1.item(c1).getTextContent());
				          

				              //type of the argument passed.
				              String argType="";

				              //check whether this argument is passed dirctly as literal.
				              String xp2 = String.format("descendant::argument_list/argument/expr[literal='%s']/literal/@type", callList1.item(c1).getTextContent());
				              argType = xpath.evaluate(xp2, calls1.item(x));
				              
				              //check whether this argument is passed as a parameter in its ancestor functions or declared in the ancestor functions..
				              if(argType.equals(""))
				              {
				                for(int ff = 0; ff<AncestorFunctions.getLength(); ff++)
				                {
				                 //System.out.println(AncestorFunctions.item(ff).getTextContent());
				                  String xp1 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp1, AncestorFunctions.item(ff));
				                  //System.out.println("argType= "+argType);
				                  if(!argType.equals(""))
				                    break;
				                }

				                //if it is still null it means it is not passed as a parameter to ancestor function
				                //now check whether this variable is declared in the class but that should not be inside any function

				                if(argType.equals(""))
				                {
				                  String xp3 = String.format("descendant::decl[name='%s']/type/name", callList1.item(c1).getTextContent());
				                  argType = xpath.evaluate(xp3, thisClass1.item(0));
				                }
				              }







				              // final String arg1 = String.format("(descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[name='%s']"+
				              //   "//preceding::decl_stmt/decl[name='%s']/type/name | "+
				              //   "descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[name='%s']"+
				              //   "//preceding::parameter_list[1]/parameter/decl[name='%s']/type/name)[last()] | "+
				              //   "descendant::call[name/name='%s'][%s]/argument_list[1]/argument/expr[literal='%s']/literal/@type",thisNativeMethod, x+1, callList1.item(c1).getTextContent(), callList1.item(c1).getTextContent(), thisNativeMethod, x+1, callList1.item(c1).getTextContent(), callList1.item(c1).getTextContent(), thisNativeMethod, x+1, callList1.item(c1).getTextContent());

				              //String argType1 =  xpath.evaluate(arg1, thisClass1.item(0));
				  
				                argType = argType.replace("const","");
				                argType = argType.replace("final","");
				                argType = argType.replace("static","");
				                //argType = argType.replace("short","");
				                //argType = argType.replace("long","");
				                argType = argType.replace("unsigned","");
				                argType = argType.replace("signed","");
				                argType = argType.replace(" ","");
				               System.out.println(argType);
				               argType = argType.split("<")[0];
				                String[] parts = argType.split("::");
				                if(parts.length>1)
				                {
				                	argType=parts[parts.length-1];
				                }
				                else
				                	argType=parts[0];

				                if(argType.equals("number") && (parameter_list.item(c1).getTextContent().equals("int") || parameter_list.item(c1).getTextContent().equals("float") || parameter_list.item(c1).getTextContent().equals("double") || parameter_list.item(c1).getTextContent().equals("long")))
				                  continue;
				                if(argType.equals(""))
				                  continue;
				                if((argType.equals("int") && (parameter_list.item(c1).getTextContent().equals("Integer"))) || (argType.equals("Integer") && (parameter_list.item(c1).getTextContent().equals("int"))))
				                  continue;

				                 if(!argType.equalsIgnoreCase(parameter_list.item(c1).getTextContent()))
				                 equal=0;
				              // }
				        
				          }
				      if(equal==1)
				        { 
				    
				          System.out.println("called in other class");

				          
					final String thisPackage =
						PACKAGE_EXP.evaluate(thisClassNode);
					final String thisFilePath =
						FILEPATH_EXP.evaluate(thisClassNode);
					antiPatternSet
						.add(
							new MLSAntiPattern(
								this.getAntiPatternName(),
								"",
								"",
								thisClass,
								thisPackage,
								thisFilePath));
						continue classloop;
				          
				        }
				      }
				  }
				        }
				    }

				}

				}

					}



					// final String thisClass = NAME_EXP.evaluate(thisClassNode);
					// final String thisPackage =
					// 	PACKAGE_EXP.evaluate(thisClassNode);
					// final String thisFilePath =
					// 	FILEPATH_EXP.evaluate(thisClassNode);
					// antiPatternSet
					// 	.add(
					// 		new MLSAntiPattern(
					// 			this.getAntiPatternName(),
					// 			"",
					// 			"",
					// 			thisClass,
					// 			thisPackage,
					// 			thisFilePath));
				}
			}
			//			}
			this.setSetOfAntiPatterns(antiPatternSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
